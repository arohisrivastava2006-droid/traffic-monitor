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
  <c:choose>
    <c:when test="${param.refresh == 'on'}"><a class="btn secondary small" href="${pageContext.request.contextPath}/dashboard">Auto-refresh is ON (click to turn off)</a></c:when>
    <c:otherwise><a class="btn secondary small" href="${pageContext.request.contextPath}/dashboard?refresh=on">Turn on auto-refresh (15 s)</a></c:otherwise>
  </c:choose>
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
    <c:choose>
      <c:when test="${empty junctions}"><p class="empty">No junctions.</p></c:when>
      <c:otherwise>
        <div class="bars">
          <c:forEach var="j" items="${junctions}">
            <c:set var="d" value="${empty j.latest ? 0 : j.latest.trafficDensity}"/>
            <div class="bar-row">
              <span class="lbl" title="<c:out value='${j.name}'/>"><c:out value="${j.name}"/></span>
              <div class="bar-track"><div class="bar-fill ${empty j.latest ? '' : fn:toLowerCase(j.latest.level)}" style="width:${d}%"></div></div>
              <span class="val">${d}%</span>
            </div>
          </c:forEach>
        </div>
      </c:otherwise>
    </c:choose>
  </div>
  <div class="card">
    <h2>Average vehicles by hour of day</h2>
    <c:choose>
      <c:when test="${empty hourly}"><p class="empty">No data yet.</p></c:when>
      <c:otherwise>
        <c:set var="mx" value="1"/>
        <c:forEach var="h" items="${hourly}"><c:if test="${h.avgVehicles > mx}"><c:set var="mx" value="${h.avgVehicles}"/></c:if></c:forEach>
        <div class="bars">
          <c:forEach var="h" items="${hourly}">
            <div class="bar-row">
              <span class="lbl">${h.hr}:00</span>
              <div class="bar-track"><div class="bar-fill cyan" style="width:${h.avgVehicles * 100 / mx}%"></div></div>
              <span class="val">${h.avgVehicles}</span>
            </div>
          </c:forEach>
        </div>
      </c:otherwise>
    </c:choose>
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

<%@ include file="includes/footer.jsp" %>
