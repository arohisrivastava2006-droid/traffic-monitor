<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Reports"/>
<c:set var="active" value="reports"/>
<%@ include file="includes/header.jsp" %>

<div class="page-head">
  <div class="eyebrow">Modules 4 and 6 &middot; Analytics and reports</div>
  <h1>Traffic Analytics and Reports</h1>
  <p>Junction-wise, time-based and historical analysis. Export any table as CSV.</p>
</div>

<div class="grid stats">
  <div class="card stat orange"><div class="num">${empty kpi.avgDensity ? '-' : kpi.avgDensity}</div><div class="lbl">Average traffic density (%)</div></div>
  <div class="card stat cyan"><div class="num">${empty kpi.avgVehicles ? '-' : kpi.avgVehicles}</div><div class="lbl">Average vehicle count</div></div>
  <div class="card stat green"><div class="num">${empty kpi.avgSpeed ? '-' : kpi.avgSpeed}</div><div class="lbl">Average speed (km/h)</div></div>
  <div class="card stat red"><div class="num">${empty kpi.congestionEvents ? 0 : kpi.congestionEvents}</div><div class="lbl">Congestion events</div></div>
  <div class="card stat red"><div class="num">${kpi.alerts}</div><div class="lbl">Total alerts</div></div>
</div>

<c:if test="${not empty busiest}">
  <div class="grid two">
    <div class="card"><h2>Highest traffic junction</h2>
      <p style="font-size:1.3rem;margin:.2rem 0"><b><c:out value="${busiest.name}"/></b></p>
      <p style="color:var(--muted);margin:0">Avg ${busiest.avgVehicles} vehicles &middot; peak ${busiest.peakVehicles}</p></div>
    <div class="card"><h2>Lowest traffic junction</h2>
      <p style="font-size:1.3rem;margin:.2rem 0"><b><c:out value="${quietest.name}"/></b></p>
      <p style="color:var(--muted);margin:0">Avg ${quietest.avgVehicles} vehicles &middot; peak ${quietest.peakVehicles}</p></div>
  </div>
</c:if>

<div class="grid two">
  <div class="card">
    <h2>Time-based analysis</h2>
    <c:choose>
      <c:when test="${empty periods}"><p class="empty">No data yet.</p></c:when>
      <c:otherwise>
        <c:set var="pm" value="1"/>
        <c:forEach var="p" items="${periods}"><c:if test="${p.avgVehicles > pm}"><c:set var="pm" value="${p.avgVehicles}"/></c:if></c:forEach>
        <div class="bars">
          <c:forEach var="p" items="${periods}">
            <div class="bar-row">
              <span class="lbl">${p.period}</span>
              <div class="bar-track"><div class="bar-fill" style="width:${p.avgVehicles * 100 / pm}%"></div></div>
              <span class="val">${p.avgVehicles}</span>
            </div>
          </c:forEach>
        </div>
      </c:otherwise>
    </c:choose>
  </div>
  <div class="card">
    <h2>Peak hours</h2>
    <c:choose>
      <c:when test="${empty hourly}"><p class="empty">No data yet.</p></c:when>
      <c:otherwise>
        <c:set var="hm" value="1"/>
        <c:forEach var="h" items="${hourly}"><c:if test="${h.avgVehicles > hm}"><c:set var="hm" value="${h.avgVehicles}"/></c:if></c:forEach>
        <div class="bars">
          <c:forEach var="h" items="${hourly}">
            <div class="bar-row">
              <span class="lbl">${h.hr}:00</span>
              <div class="bar-track"><div class="bar-fill cyan" style="width:${h.avgVehicles * 100 / hm}%"></div></div>
              <span class="val">${h.avgVehicles}</span>
            </div>
          </c:forEach>
        </div>
      </c:otherwise>
    </c:choose>
  </div>
</div>

<div class="card" style="margin-bottom:1.2rem">
  <div class="toolbar"><h2 class="grow" style="margin:0">Junction report</h2>
    <a class="btn secondary small" href="${pageContext.request.contextPath}/reports?csv=junction">Download CSV</a></div>
  <c:choose>
    <c:when test="${empty summary}"><p class="empty">No data yet.</p></c:when>
    <c:otherwise>
      <div class="table-wrap"><table>
        <tr><th>Junction</th><th>Records</th><th>Avg vehicles</th><th>Avg density</th><th>Avg speed</th><th>Peak vehicles</th></tr>
        <c:forEach var="r" items="${summary}">
          <tr><td><c:out value="${r.name}"/></td><td>${r.records}</td><td>${empty r.avgVehicles ? '-' : r.avgVehicles}</td>
              <td>${empty r.avgDensity ? '-' : r.avgDensity}%</td><td>${empty r.avgSpeed ? '-' : r.avgSpeed} km/h</td>
              <td>${empty r.peakVehicles ? '-' : r.peakVehicles}</td></tr>
        </c:forEach>
      </table></div>
    </c:otherwise>
  </c:choose>
</div>

<div class="card">
  <div class="toolbar"><h2 class="grow" style="margin:0">Daily report (last 14 days)</h2>
    <a class="btn secondary small" href="${pageContext.request.contextPath}/reports?csv=daily">Download CSV</a>
    <a class="btn secondary small" href="${pageContext.request.contextPath}/reports?csv=records">All records CSV</a></div>
  <c:choose>
    <c:when test="${empty daily}"><p class="empty">No data yet.</p></c:when>
    <c:otherwise>
      <div class="table-wrap"><table>
        <tr><th>Day</th><th>Total vehicles</th><th>Avg density</th><th>Avg speed</th><th>Records</th></tr>
        <c:forEach var="r" items="${daily}">
          <tr><td>${r.day}</td><td>${r.totalVehicles}</td><td>${r.avgDensity}%</td><td>${r.avgSpeed} km/h</td><td>${r.records}</td></tr>
        </c:forEach>
      </table></div>
    </c:otherwise>
  </c:choose>
</div>

<%@ include file="includes/footer.jsp" %>
