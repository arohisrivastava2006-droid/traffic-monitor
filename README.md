# Urban Traffic Monitoring and Analytics Engine

Java web project (Team 4): Servlets, JSP, JDBC, MySQL, with OOP, collections, multithreading and exception handling.

## What you need installed
1. **JDK 17 or newer**
2. **Apache Maven 3.8+**
3. **MySQL 8** (running on port 3306)
4. **VS Code** with the *Extension Pack for Java* (Microsoft). Optional: *Community Server Connectors* if you would rather use your own Tomcat.

## Setup (5 steps)
1. Open the `traffic-monitor` folder in VS Code (File > Open Folder).
2. Create the database: run `sql/schema.sql` in MySQL.
   - Command line: `mysql -u root -p < sql/schema.sql`
   - or paste it into MySQL Workbench and run it.
3. Open `src/main/resources/db.properties` and set your MySQL username and password.
4. In the VS Code terminal run:
   ```
   mvn clean package cargo:run
   ```
   The first run downloads Tomcat 10.1 and the libraries, so it takes a few minutes.
5. Open <http://localhost:8080/traffic-monitor/> in your browser.

Press `Ctrl+C` in the terminal to stop the server.

## Logins
| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin123` |
| Operator | `operator` | `operator123` |

Admins can add, edit and delete junctions. Operators can monitor, add traffic records and view reports.

## First steps once logged in
1. Go to **Traffic Data** and click **Generate 7-day history** (fills the charts and reports).
2. Click **Generate live batch** (creates current readings and raises alerts).
3. Check the **Dashboard**, **Alerts** and **Reports** pages.

## Project layout
```
sql/schema.sql                       MySQL tables + sample users and junctions
src/main/java/com/traffic/
  model/       User (abstract), Admin, TrafficOperator, Junction, TrafficData, Alert
  dao/         JDBC data access: UserDAO, JunctionDAO, TrafficDataDAO, AlertDAO
  service/     TrafficAnalyzer (interface), CongestionAnalyzer, TrafficProcessor (threads)
  servlet/     Login, Logout, Dashboard, Junction, TrafficData, Alert, Report servlets
  filter/      AuthFilter (login required)
  exception/   InvalidTrafficDataException (custom exception)
  util/        DBConnection
src/main/webapp/                     JSP pages, includes/, css/
```

## Where each slide topic lives in the code
| Slide topic | Where |
|-------------|-------|
| OOP: encapsulation, inheritance, polymorphism, abstraction | `model/User` + `Admin`/`TrafficOperator`, `service/TrafficAnalyzer` |
| Exception handling (try/catch/finally, custom) | `InvalidTrafficDataException`, try-with-resources in the DAOs, catch blocks in servlets |
| Multithreading (ExecutorService, Callable) | `TrafficProcessor.simulate()` |
| Collections (ArrayList, HashMap, HashSet, Queue) | DAOs (`ArrayList`, `HashMap`), `TrafficProcessor` (`ConcurrentLinkedQueue`, concurrent `Set`) |
| JDBC | `util/DBConnection` and the `dao` package |
| Servlets and JSP | `servlet` package and `webapp/*.jsp` |
| Congestion levels (Low, Moderate, High, Severe) | `service/CongestionAnalyzer` (edit the threshold constants) |

## Notes
- **JavaFX dashboard:** the slides also list a desktop JavaFX dashboard. This project is the web version of the system; the JavaFX part can be added later as a separate module using the same DAO and model classes.
- Passwords are stored as SHA-256 hashes (MySQL `SHA2`). That is fine for a college project; use BCrypt for anything real.
- If login shows a "Database error", re-check `db.properties` and that `schema.sql` ran successfully.
- Using your own Tomcat instead: run `mvn clean package`, then deploy `target/traffic-monitor.war` to **Tomcat 10.1** (not 9, because the code uses `jakarta.*` packages).

## JavaFX desktop dashboard
A desktop version of the dashboard lives in the `desktop/` folder. It reuses the same model, DAO and
service classes and the same MySQL database as the website (so run `schema.sql` first and keep MySQL running).

```
cd desktop
mvn javafx:run
```

It has tabs for Dashboard (junction tiles + charts), Traffic Data (add records, generate sample data),
Alerts, Junctions and Reports (with CSV export). Database work runs on background threads (JavaFX `Task`)
so the window never freezes. You can stay logged in to the website and the desktop app at the same time.

## No JavaScript
The website pages use only HTML, CSS and JSP/JSTL. The charts are drawn as CSS bars on the server,
auto-refresh uses an HTML meta-refresh (`/dashboard?refresh=on`), and delete confirmations use a required
"I'm sure" checkbox instead of a script.
