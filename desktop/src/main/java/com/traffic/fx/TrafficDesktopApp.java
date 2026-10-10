package com.traffic.fx;

import com.traffic.dao.AlertDAO;
import com.traffic.dao.JunctionDAO;
import com.traffic.dao.TrafficDataDAO;
import com.traffic.dao.UserDAO;
import com.traffic.exception.InvalidTrafficDataException;
import com.traffic.model.Alert;
import com.traffic.model.Junction;
import com.traffic.model.TrafficData;
import com.traffic.model.User;
import com.traffic.service.TrafficProcessor;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * JavaFX desktop dashboard for the Urban Traffic Monitoring and Analytics Engine.
 * It reuses the same model, DAO and service classes as the website and talks to the same
 * MySQL database. Database work runs on background threads (JavaFX Task) so the window
 * never freezes.
 */
public class TrafficDesktopApp extends Application {

    private final JunctionDAO junctionDAO = new JunctionDAO();
    private final TrafficDataDAO trafficDAO = new TrafficDataDAO();
    private final AlertDAO alertDAO = new AlertDAO();
    private final UserDAO userDAO = new UserDAO();
    private final TrafficProcessor processor = new TrafficProcessor();

    private Stage stage;
    private User user;
    private Timeline autoRefresh;

    // widgets that are refreshed from the database
    private final Label status = new Label("Ready");
    private final Label statJunctions = new Label("-");
    private final Label statVehicles = new Label("-");
    private final Label statAlerts = new Label("-");
    private final Label statRecords = new Label("-");
    private final TilePane tiles = new TilePane();
    private final XYChart.Series<String, Number> densitySeries = new XYChart.Series<>();
    private final XYChart.Series<String, Number> hourlySeries = new XYChart.Series<>();
    private final TableView<TrafficData> recordTable = new TableView<>();
    private final TableView<Alert> alertTable = new TableView<>();
    private final TableView<Junction> junctionTable = new TableView<>();
    private final TableView<Map<String, Object>> summaryTable = new TableView<>();
    private final ComboBox<Junction> junctionBox = new ComboBox<>();
    private List<Map<String, Object>> lastSummary = new ArrayList<>();

    /** Everything one screen refresh needs, loaded together on a worker thread. */
    private record Snapshot(List<Junction> junctions, Map<Integer, TrafficData> latest,
                            List<TrafficData> records, List<Alert> alerts, int openAlerts,
                            long vehiclesToday, long totalRecords,
                            List<Map<String, Object>> summary, List<Map<String, Object>> hourly) { }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("Urban Traffic Monitoring and Analytics Engine");
        showLogin(null);
        stage.show();
    }

    /* ------------------------------------------------------------------ login */

    private void showLogin(String message) {
        if (autoRefresh != null) autoRefresh.stop();

        Label title = new Label("Urban Traffic Monitoring");
        title.getStyleClass().add("title");
        Label sub = new Label("Sign in (a new username is registered automatically)");
        sub.getStyleClass().add("muted");
        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        Label error = new Label(message == null ? "" : message);
        error.getStyleClass().add("error");
        Button login = new Button("Sign in");
        login.setDefaultButton(true);
        login.setMaxWidth(Double.MAX_VALUE);

        login.setOnAction(e -> {
            String u = username.getText().trim();
            String p = password.getText();
            if (u.isEmpty() || p.isEmpty()) {
                error.setText("Please enter both username and password.");
                return;
            }
            login.setDisable(true);
            error.setText("Signing in...");
            background(() -> userDAO.authenticateOrRegister(u, p), found -> {
                login.setDisable(false);
                if (found == null) {
                    error.setText("Wrong password for this username.");
                } else {
                    user = found;
                    showMain();
                }
            }, ex -> {
                login.setDisable(false);
                error.setText("Database error: " + ex.getMessage());
            });
        });

        VBox box = new VBox(10, title, sub, username, password, login, error);
        box.setPadding(new Insets(28));
        box.setMaxWidth(340);
        box.getStyleClass().add("card");
        StackLike root = new StackLike(box);
        Scene scene = new Scene(root, 520, 420);
        scene.getStylesheets().add(getClass().getResource("/desktop.css").toExternalForm());
        stage.setScene(scene);
    }

    /** Tiny helper: a pane that centres its single child. */
    private static class StackLike extends BorderPane {
        StackLike(Region child) {
            setCenter(child);
            setPadding(new Insets(20));
        }
    }

    /* ------------------------------------------------------------- main window */

    private void showMain() {
        Label title = new Label("Traffic Monitoring Dashboard");
        title.getStyleClass().add("title");
        Label who = new Label(user.getName() + "  |  " + user.getAccessLevel());
        who.getStyleClass().add("muted");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> refresh());
        CheckBox auto = new CheckBox("Auto-refresh (10 s)");
        autoRefresh = new Timeline(new KeyFrame(Duration.seconds(10), e -> refresh()));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        auto.selectedProperty().addListener((o, was, now) -> {
            if (now) autoRefresh.play(); else autoRefresh.stop();
        });
        Button logout = new Button("Log out");
        logout.setOnAction(e -> {
            user = null;
            showLogin(null);
        });
        HBox top = new HBox(12, title, spacer, who, refresh, auto, logout);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(10, 14, 10, 14));

        TabPane tabs = new TabPane(dashboardTab(), trafficTab(), alertsTab(), junctionsTab(), reportsTab());
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        status.getStyleClass().add("muted");
        HBox bottom = new HBox(status);
        bottom.setPadding(new Insets(6, 14, 6, 14));

        BorderPane root = new BorderPane(tabs);
        root.setTop(top);
        root.setBottom(bottom);
        Scene scene = new Scene(root, 1180, 760);
        scene.getStylesheets().add(getClass().getResource("/desktop.css").toExternalForm());
        stage.setScene(scene);
        refresh();
    }

    private Tab dashboardTab() {
        HBox stats = new HBox(12, statCard("Junctions", statJunctions), statCard("Vehicles today", statVehicles),
                statCard("Open alerts", statAlerts), statCard("Records stored", statRecords));

        tiles.setHgap(12);
        tiles.setVgap(12);
        tiles.setPrefColumns(3);
        ScrollPane tileScroll = new ScrollPane(tiles);
        tileScroll.setFitToWidth(true);
        tileScroll.setPrefHeight(250);

        CategoryAxis dx = new CategoryAxis();
        NumberAxis dy = new NumberAxis(0, 100, 20);
        dy.setLabel("Density %");
        BarChart<String, Number> density = new BarChart<>(dx, dy);
        density.setTitle("Traffic density by junction (latest)");
        density.setLegendVisible(false);
        density.setAnimated(false);
        density.getData().add(densitySeries);

        CategoryAxis hx = new CategoryAxis();
        hx.setLabel("Hour of day");
        NumberAxis hy = new NumberAxis();
        hy.setLabel("Avg vehicles");
        LineChart<String, Number> hourly = new LineChart<>(hx, hy);
        hourly.setTitle("Average vehicles by hour");
        hourly.setLegendVisible(false);
        hourly.setAnimated(false);
        hourly.getData().add(hourlySeries);

        HBox charts = new HBox(12, density, hourly);
        HBox.setHgrow(density, Priority.ALWAYS);
        HBox.setHgrow(hourly, Priority.ALWAYS);
        charts.setPrefHeight(300);

        VBox content = new VBox(12, stats, tileScroll, charts);
        content.setPadding(new Insets(12));
        Tab t = new Tab("Dashboard", new ScrollPane(content));
        ((ScrollPane) t.getContent()).setFitToWidth(true);
        return t;
    }

    private VBox statCard(String caption, Label value) {
        value.getStyleClass().add("stat-num");
        Label c = new Label(caption);
        c.getStyleClass().add("muted");
        VBox v = new VBox(2, value, c);
        v.getStyleClass().add("card");
        HBox.setHgrow(v, Priority.ALWAYS);
        v.setMaxWidth(Double.MAX_VALUE);
        return v;
    }

    private Tab trafficTab() {
        junctionBox.setPromptText("Junction");
        junctionBox.setConverter(new StringConverter<>() {
            @Override public String toString(Junction j) { return j == null ? "" : j.getName(); }
            @Override public Junction fromString(String s) { return null; }
        });
        TextField vehicles = new TextField();
        vehicles.setPromptText("Vehicle count");
        TextField density = new TextField();
        density.setPromptText("Density 0-100 %");
        TextField speed = new TextField();
        speed.setPromptText("Speed km/h");
        Button save = new Button("Save record");
        save.setOnAction(e -> {
            Junction j = junctionBox.getValue();
            if (j == null) {
                setStatus("Please choose a junction.", true);
                return;
            }
            TrafficData d;
            try {
                d = new TrafficData(j.getJunctionId(), Integer.parseInt(vehicles.getText().trim()),
                        Double.parseDouble(density.getText().trim()),
                        Double.parseDouble(speed.getText().trim()), LocalDateTime.now());
            } catch (NumberFormatException ex) {
                setStatus("Please enter valid numbers in every field.", true);
                return;
            }
            background(() -> processor.processReading(d, true, null), level -> {
                setStatus("Record saved. Traffic level: " + level, false);
                vehicles.clear();
                density.clear();
                speed.clear();
                refresh();
            }, ex -> setStatus(ex instanceof InvalidTrafficDataException
                    ? ex.getMessage() : "Could not save: " + ex.getMessage(), true));
        });
        HBox form = new HBox(10, junctionBox, vehicles, density, speed, save);
        form.setAlignment(Pos.CENTER_LEFT);

        Button live = new Button("Generate live batch");
        live.setOnAction(e -> simulate(6, false, 7, "live batch"));
        Button h7 = new Button("Generate 7-day history");
        h7.setOnAction(e -> simulate(40, true, 7, "7-day history"));
        Button h30 = new Button("Generate 30-day history");
        h30.setOnAction(e -> simulate(150, true, 30, "30-day history"));
        Button clear = new Button("Delete all records");
        clear.setDisable(!user.canManageJunctions());
        clear.setOnAction(e -> confirm("Delete ALL traffic records?", () ->
                background(() -> { trafficDAO.deleteAll(); return 0; }, x -> {
                    setStatus("All traffic records deleted.", false);
                    refresh();
                }, ex -> setStatus("Error: " + ex.getMessage(), true))));
        HBox gen = new HBox(10, live, h7, h30, clear);

        recordTable.getColumns().setAll(List.of(
                column("#", 60, d -> String.valueOf(d.getRecordId())),
                column("Junction", 190, TrafficData::getJunctionName),
                column("Vehicles", 80, d -> String.valueOf(d.getVehicleCount())),
                column("Density %", 90, d -> String.valueOf(d.getTrafficDensity())),
                column("Speed km/h", 90, d -> String.valueOf(d.getAverageSpeed())),
                column("Level", 100, TrafficData::getLevel),
                column("Time", 170, TrafficData::getTimestampText)));
        VBox.setVgrow(recordTable, Priority.ALWAYS);

        VBox box = new VBox(12, new Label("Add a traffic record"), form,
                new Label("Sample data generator (multithreaded)"), gen,
                new Label("Latest 100 records"), recordTable);
        box.setPadding(new Insets(12));
        return new Tab("Traffic Data", box);
    }

    private Tab alertsTab() {
        TableColumn<Alert, String> action = new TableColumn<>("");
        action.setPrefWidth(100);
        action.setCellFactory(c -> new TableCell<>() {
            private final Button b = new Button("Resolve");
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                Alert a = empty || getTableRow() == null ? null : getTableRow().getItem();
                if (a == null || a.isResolved()) {
                    setGraphic(null);
                } else {
                    b.setOnAction(e -> background(() -> { alertDAO.resolve(a.getAlertId()); return 0; },
                            x -> refresh(), ex -> setStatus("Error: " + ex.getMessage(), true)));
                    setGraphic(b);
                }
            }
        });
        action.setCellValueFactory(c -> new SimpleStringProperty(""));

        alertTable.getColumns().setAll(List.of(
                column("Junction", 170, Alert::getJunctionName),
                column("Type", 160, Alert::getAlertType),
                column("Severity", 90, Alert::getSeverity),
                column("Message", 330, Alert::getMessage),
                column("Time", 160, Alert::getTimestampText),
                column("Status", 90, a -> a.isResolved() ? "RESOLVED" : "OPEN"),
                action));
        VBox.setVgrow(alertTable, Priority.ALWAYS);

        Button resolveAll = new Button("Resolve all");
        resolveAll.setOnAction(e -> background(() -> { alertDAO.resolveAll(); return 0; },
                x -> { setStatus("All open alerts resolved.", false); refresh(); },
                ex -> setStatus("Error: " + ex.getMessage(), true)));
        Button clear = new Button("Clear all");
        clear.setDisable(!user.canManageJunctions());
        clear.setOnAction(e -> confirm("Delete every alert?", () ->
                background(() -> { alertDAO.deleteAll(); return 0; },
                        x -> { setStatus("All alerts cleared.", false); refresh(); },
                        ex -> setStatus("Error: " + ex.getMessage(), true))));

        VBox box = new VBox(12, new HBox(10, resolveAll, clear), alertTable);
        box.setPadding(new Insets(12));
        return new Tab("Alerts", box);
    }

    private Tab junctionsTab() {
        TextField name = new TextField();
        name.setPromptText("Junction name");
        TextField location = new TextField();
        location.setPromptText("Location");
        Button add = new Button("Add junction");
        add.setOnAction(e -> {
            String n = name.getText().trim();
            String l = location.getText().trim();
            if (n.isEmpty() || l.isEmpty()) {
                setStatus("Name and location are required.", true);
                return;
            }
            background(() -> { junctionDAO.add(n, l); return 0; }, x -> {
                name.clear();
                location.clear();
                setStatus("Junction added.", false);
                refresh();
            }, ex -> setStatus("Error: " + ex.getMessage(), true));
        });
        Button sample = new Button("Add 10 sample junctions");
        sample.setOnAction(e -> background(junctionDAO::addDemoJunctions, n -> {
            setStatus(n + " sample junctions added.", false);
            refresh();
        }, ex -> setStatus("Error: " + ex.getMessage(), true)));
        Button delete = new Button("Delete selected");
        delete.setOnAction(e -> {
            Junction j = junctionTable.getSelectionModel().getSelectedItem();
            if (j == null) {
                setStatus("Select a junction in the table first.", true);
                return;
            }
            confirm("Delete \"" + j.getName() + "\" and all its records and alerts?", () ->
                    background(() -> { junctionDAO.delete(j.getJunctionId()); return 0; },
                            x -> { setStatus("Junction deleted.", false); refresh(); },
                            ex -> setStatus("Error: " + ex.getMessage(), true)));
        });
        boolean admin = user.canManageJunctions();
        for (javafx.scene.Node node : List.of(name, location, add, sample, delete)) node.setDisable(!admin);

        junctionTable.getColumns().setAll(List.of(
                column("ID", 60, j -> String.valueOf(j.getJunctionId())),
                column("Name", 240, Junction::getName),
                column("Location", 360, Junction::getLocation)));
        VBox.setVgrow(junctionTable, Priority.ALWAYS);

        Label note = new Label(admin ? "Add, remove or extend the monitored junctions."
                : "Only administrators can change junctions (log in as admin).");
        note.getStyleClass().add("muted");
        VBox box = new VBox(12, note, new HBox(10, name, location, add, sample, delete), junctionTable);
        box.setPadding(new Insets(12));
        return new Tab("Junctions", box);
    }

    private Tab reportsTab() {
        Button export = new Button("Export junction report (CSV)");
        export.setOnAction(e -> exportCsv());
        Label hint = new Label("Junction-wise analysis of all stored traffic data.");
        hint.getStyleClass().add("muted");
        VBox.setVgrow(summaryTable, Priority.ALWAYS);
        VBox box = new VBox(12, hint, export, summaryTable);
        box.setPadding(new Insets(12));
        return new Tab("Reports", box);
    }

    /* ------------------------------------------------------------ data loading */

    private void refresh() {
        background(() -> new Snapshot(
                junctionDAO.getAll(), trafficDAO.getLatestPerJunction(), trafficDAO.getRecent(null, 100),
                alertDAO.getAll(200), alertDAO.countOpen(), trafficDAO.totalVehiclesToday(),
                trafficDAO.totalRecords(), trafficDAO.junctionSummary(), trafficDAO.hourlyAverages()),
                this::show, ex -> setStatus("Database error: " + ex.getMessage(), true));
    }

    private void show(Snapshot s) {
        statJunctions.setText(String.valueOf(s.junctions().size()));
        statVehicles.setText(String.valueOf(s.vehiclesToday()));
        statAlerts.setText(String.valueOf(s.openAlerts()));
        statRecords.setText(String.valueOf(s.totalRecords()));

        tiles.getChildren().clear();
        List<XYChart.Data<String, Number>> bars = new ArrayList<>();
        for (Junction j : s.junctions()) {
            TrafficData d = s.latest().get(j.getJunctionId());
            String level = d == null ? "NO DATA" : d.getLevel();
            Label name = new Label(j.getName());
            name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            Label loc = new Label(j.getLocation());
            loc.getStyleClass().add("muted");
            Label badge = new Label(level);
            badge.setStyle("-fx-font-weight: bold; -fx-text-fill: " + levelColor(level) + ";");
            VBox tile = new VBox(3, name, loc, badge);
            if (d != null) {
                tile.getChildren().addAll(new Label("Vehicles: " + d.getVehicleCount()),
                        new Label("Density: " + d.getTrafficDensity() + " %"),
                        new Label("Speed: " + d.getAverageSpeed() + " km/h"),
                        new Label("Updated: " + d.getTimestampText()));
                bars.add(new XYChart.Data<>(j.getName(), d.getTrafficDensity()));
            } else {
                bars.add(new XYChart.Data<>(j.getName(), 0));
            }
            tile.getStyleClass().add("card");
            tile.setStyle("-fx-border-color: " + levelColor(level) + "; -fx-border-width: 0 0 0 4;");
            tile.setPrefWidth(250);
            tiles.getChildren().add(tile);
        }
        densitySeries.getData().setAll(bars);

        List<XYChart.Data<String, Number>> hours = new ArrayList<>();
        for (Map<String, Object> h : s.hourly()) {
            hours.add(new XYChart.Data<>(((Number) h.get("hr")).intValue() + ":00",
                    ((Number) h.get("avgVehicles")).doubleValue()));
        }
        hourlySeries.getData().setAll(hours);

        Junction chosen = junctionBox.getValue();
        junctionBox.getItems().setAll(s.junctions());
        if (chosen != null) {
            for (Junction j : s.junctions()) {
                if (j.getJunctionId() == chosen.getJunctionId()) junctionBox.setValue(j);
            }
        }
        recordTable.getItems().setAll(s.records());
        alertTable.getItems().setAll(s.alerts());
        junctionTable.getItems().setAll(s.junctions());

        lastSummary = s.summary();
        summaryTable.getColumns().clear();
        if (!s.summary().isEmpty()) {
            for (String key : s.summary().get(0).keySet()) {
                TableColumn<Map<String, Object>, String> c = new TableColumn<>(prettyKey(key));
                c.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(
                        cell.getValue().get(key) == null ? "-" : cell.getValue().get(key))));
                summaryTable.getColumns().add(c);
            }
        }
        summaryTable.getItems().setAll(s.summary());
        setStatus("Updated " + java.time.LocalTime.now().withNano(0), false);
    }

    private void simulate(int perJunction, boolean history, int days, String label) {
        setStatus("Generating " + label + "...", false);
        background(() -> processor.simulate(junctionDAO.getAll(), perJunction, history, days), count -> {
            setStatus(count + " readings generated (" + label + ").", false);
            refresh();
        }, ex -> setStatus("Error: " + ex.getMessage(), true));
    }

    private void exportCsv() {
        if (lastSummary.isEmpty()) {
            setStatus("No data to export yet.", true);
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("junction-report.csv");
        File f = fc.showSaveDialog(stage);
        if (f == null) return;
        StringBuilder sb = new StringBuilder(String.join(",", lastSummary.get(0).keySet())).append('\n');
        for (Map<String, Object> row : lastSummary) {
            List<String> cells = new ArrayList<>();
            for (Object v : row.values()) {
                cells.add("\"" + String.valueOf(v == null ? "" : v).replace("\"", "\"\"") + "\"");
            }
            sb.append(String.join(",", cells)).append('\n');
        }
        try {
            Files.writeString(f.toPath(), sb.toString());
            setStatus("Saved " + f.getName(), false);
        } catch (Exception ex) {
            setStatus("Could not save file: " + ex.getMessage(), true);
        }
    }

    /* ----------------------------------------------------------------- helpers */

    /** Runs work on a background thread and hands the result back on the JavaFX thread. */
    private <T> void background(Callable<T> work, Consumer<T> onDone, Consumer<Throwable> onError) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };
        task.setOnSucceeded(e -> onDone.accept(task.getValue()));
        task.setOnFailed(e -> onError.accept(task.getException()));
        Thread t = new Thread(task, "db-worker");
        t.setDaemon(true);
        t.start();
    }

    private void confirm(String question, Runnable onYes) {
        javafx.scene.control.Alert a = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION, question,
                javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO);
        a.setHeaderText(null);
        a.showAndWait().ifPresent(b -> {
            if (b == javafx.scene.control.ButtonType.YES) onYes.run();
        });
    }

    private <S> TableColumn<S, String> column(String title, double width, Function<S, String> value) {
        TableColumn<S, String> c = new TableColumn<>(title);
        c.setPrefWidth(width);
        c.setCellValueFactory(cell -> new SimpleStringProperty(value.apply(cell.getValue())));
        return c;
    }

    private void setStatus(String text, boolean error) {
        status.setText(text);
        status.getStyleClass().removeAll("error", "ok", "muted");
        status.getStyleClass().add(error ? "error" : "muted");
    }

    private static String levelColor(String level) {
        return switch (level) {
            case "LOW" -> "#22c55e";
            case "MODERATE" -> "#eab308";
            case "HIGH" -> "#f7931e";
            case "SEVERE" -> "#ef4444";
            default -> "#9aa7bf";
        };
    }

    private static String prettyKey(String key) {
        String spaced = key.replaceAll("([a-z])([A-Z])", "$1 $2");
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }
}
