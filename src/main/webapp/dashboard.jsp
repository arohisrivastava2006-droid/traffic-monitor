<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Dashboard"/>
<c:set var="active" value="dashboard"/>
<%@ include file="includes/header.jsp" %>

<div class="page-head">
  <div class="eyebrow">Live overview</div>
  <h1>Traffic Monitoring Dashboard</h1>
  <p>Latest reading from every junction, with congestion level and active alerts.</p>
  <label style="font-size:.85rem;color:var(--muted)"><input type="checkbox" id="autoRefresh" style="width:auto"> Auto-refresh every 15 seconds</label>
</div>

<div class="grid stats">
  <div class="card stat cyan"><div class="num">${totalJunctions}</div><div class="lbl">Junctions</div></div>
  <div class="card stat orange"><div class="num">${vehiclesToday}</div><div class="lbl">Vehicles recorded today</div></div>
  <div class="card stat red"><div class="num">${openAlerts}</div><div class="lbl">Open alerts</div></div>
  <div class="card stat green"><div class="num">${totalRecords}</div><div class="lbl">Traffic records stored</div></div>
</div>

<div class="card" style="margin-bottom:1.2rem">
  <h2>Current traffic by junction</h2>
  <c:choose>
    <c:when test="${empty junctions}">
      <p class="empty">No junctions yet. An admin can add one on the Junctions page.</p>
    </c:when>
    <c:otherwise>
      <div class="tiles">
        <c:forEach var="j" items="${junctions}">
          <c:set var="lvl" value="${empty j.latest ? 'none' : fn:toLowerCase(j.latest.level)}"/>
          <div class="tile ${lvl}">
            <h3><c:out value="${j.name}"/></h3>
            <div class="loc"><c:out value="${j.location}"/></div>
            <span class="badge ${lvl}">${empty j.latest ? 'NO DATA' : j.latest.level}</span>
            <c:if test="${not empty j.latest}">
              <dl>
                <dt>Vehicles</dt><dd>${j.latest.vehicleCount}</dd>
                <dt>Density</dt><dd>${j.latest.trafficDensity}%</dd>
                <dt>Avg speed</dt><dd>${j.latest.averageSpeed} km/h</dd>
                <dt>Updated</dt><dd>${j.latest.timestampText}</dd>
              </dl>
            </c:if>
          </div>
        </c:forEach>
      </div>
    </c:otherwise>
  </c:choose>
</div>

<div class="grid two">
  <div class="card">
    <h2>Density by junction (latest)</h2>
    <div class="chart-box"><canvas id="densityChart"></canvas></div>
  </div>
  <div class="card">
    <h2>Average vehicles by hour of day</h2>
    <div class="chart-box"><canvas id="hourlyChart"></canvas></div>
  </div>
</div>

<div class="card">
  <div class="toolbar"><h2 class="grow" style="margin:0">Recent alerts</h2>
    <a class="btn secondary small" href="${pageContext.request.contextPath}/alerts">View all</a></div>
  <c:choose>
    <c:when test="${empty recentAlerts}"><p class="empty">No alerts.</p></c:when>
    <c:otherwise>
      <div class="table-wrap"><table>
        <tr><th>Junction</th><th>Type</th><th>Severity</th><th>Message</th><th>Time</th></tr>
        <c:forEach var="a" items="${recentAlerts}">
          <tr>
            <td><c:out value="${a.junctionName}"/></td>
            <td><c:out value="${a.alertType}"/></td>
            <td><span class="badge ${fn:toLowerCase(a.severity)}">${a.severity}</span></td>
            <td><c:out value="${a.message}"/></td>
            <td>${a.timestampText}</td>
          </tr>
        </c:forEach>
      </table></div>
    </c:otherwise>
  </c:choose>
</div>

<script>
  Chart.defaults.color = '#9aa7bf';
  Chart.defaults.borderColor = '#243049';

  new Chart(document.getElementById('densityChart'), {
    type: 'bar',
    data: {
      labels: [<c:forEach var="j" items="${junctions}" varStatus="s">"<c:out value='${j.name}'/>"${s.last ? '' : ','}</c:forEach>],
      datasets: [{
        label: 'Density %',
        data: [<c:forEach var="j" items="${junctions}" varStatus="s">${empty j.latest ? 0 : j.latest.trafficDensity}${s.last ? '' : ','}</c:forEach>],
        backgroundColor: '#f7931e'
      }]
    },
    options: { maintainAspectRatio: false, scales: { y: { min: 0, max: 100 } }, plugins: { legend: { display: false } } }
  });

  new Chart(document.getElementById('hourlyChart'), {
    type: 'line',
    data: {
      labels: [<c:forEach var="h" items="${hourly}" varStatus="s">"${h.hr}:00"${s.last ? '' : ','}</c:forEach>],
      datasets: [{
        label: 'Avg vehicles',
        data: [<c:forEach var="h" items="${hourly}" varStatus="s">${h.avgVehicles}${s.last ? '' : ','}</c:forEach>],
        borderColor: '#22d3ee', backgroundColor: 'rgba(34,211,238,.15)', fill: true, tension: .3
      }]
    },
    options: { maintainAspectRatio: false, plugins: { legend: { display: false } } }
  });
</script>

<script>
  (function () {
    const box = document.getElementById('autoRefresh');
    try { box.checked = localStorage.getItem('autoRefresh') === '1'; } catch (e) {}
    box.addEventListener('change', function () {
      try { localStorage.setItem('autoRefresh', box.checked ? '1' : '0'); } catch (e) {}
    });
    setInterval(function () { if (box.checked) location.reload(); }, 15000);
  })();
</script>

<%@ include file="includes/footer.jsp" %>
