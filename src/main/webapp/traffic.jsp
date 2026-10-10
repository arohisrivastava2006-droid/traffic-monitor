<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Traffic Data"/>
<c:set var="active" value="traffic"/>
<%@ include file="includes/header.jsp" %>

<div class="page-head">
  <div class="eyebrow">Module 3 &middot; Traffic data management</div>
  <h1>Traffic Records</h1>
  <p>Add a reading by hand, or generate sample data to try out the analytics.</p>
</div>

<div class="card" style="margin-bottom:1.2rem">
  <h2>Add traffic record</h2>
  <form method="post" action="${pageContext.request.contextPath}/traffic">
    <input type="hidden" name="action" value="add">
    <div class="form-row">
      <div>
        <label for="junctionId">Junction</label>
        <select id="junctionId" name="junctionId" required>
          <option value="">Choose...</option>
          <c:forEach var="j" items="${junctions}">
            <option value="${j.junctionId}"><c:out value="${j.name}"/></option>
          </c:forEach>
        </select>
      </div>
      <div><label for="vehicleCount">Vehicle count</label>
        <input id="vehicleCount" name="vehicleCount" type="number" min="0" required></div>
      <div><label for="density">Traffic density (0-100 %)</label>
        <input id="density" name="density" type="number" min="0" max="100" step="0.1" required></div>
      <div><label for="speed">Average speed (km/h)</label>
        <input id="speed" name="speed" type="number" min="0" max="200" step="0.1" required></div>
      <div><button class="btn" type="submit">Save record</button></div>
    </div>
  </form>
</div>

<div class="card" style="margin-bottom:1.2rem">
  <h2>Sample data generator (multithreaded)</h2>
  <p style="color:var(--muted);margin-top:0">Each junction is processed by a worker thread. Live batches also raise alerts when traffic is heavy.</p>
  <form class="inline" method="post" action="${pageContext.request.contextPath}/traffic">
    <input type="hidden" name="action" value="simulate">
    <input type="hidden" name="mode" value="live">
    <button class="btn" type="submit">Generate live batch</button>
  </form>
  <form class="inline" method="post" action="${pageContext.request.contextPath}/traffic">
    <input type="hidden" name="action" value="simulate">
    <input type="hidden" name="mode" value="history">
    <button class="btn secondary" type="submit">Generate 7-day history</button>
  </form>
  <form class="inline" method="post" action="${pageContext.request.contextPath}/traffic">
    <input type="hidden" name="action" value="simulate">
    <input type="hidden" name="mode" value="history30">
    <button class="btn secondary" type="submit">Generate 30-day history (big data set)</button>
  </form>
  <c:if test="${sessionScope.user.role == 'ADMIN'}">
    <form class="inline" method="post" action="${pageContext.request.contextPath}/traffic"
          onsubmit="return confirm('Delete ALL traffic records?')">
      <input type="hidden" name="action" value="clearAll">
      <button class="btn danger" type="submit">Delete all records</button>
    </form>
  </c:if>
</div>

<div class="card">
  <div class="toolbar">
    <h2 class="grow" style="margin:0">Latest 100 records</h2>
    <form method="get" action="${pageContext.request.contextPath}/traffic" class="toolbar" style="margin:0">
      <select name="junctionId" onchange="this.form.submit()" style="width:auto">
        <option value="">All junctions</option>
        <c:forEach var="j" items="${junctions}">
          <option value="${j.junctionId}" ${selectedJunction == j.junctionId ? 'selected' : ''}><c:out value="${j.name}"/></option>
        </c:forEach>
      </select>
    </form>
  </div>
  <c:choose>
    <c:when test="${empty records}"><p class="empty">No records yet. Generate sample data above.</p></c:when>
    <c:otherwise>
      <div class="table-wrap"><table>
        <tr><th>#</th><th>Junction</th><th>Vehicles</th><th>Density</th><th>Speed</th><th>Level</th><th>Time</th><c:if test="${sessionScope.user.role == 'ADMIN'}"><th></th></c:if></tr>
        <c:forEach var="r" items="${records}">
          <tr>
            <td>${r.recordId}</td>
            <td><c:out value="${r.junctionName}"/></td>
            <td>${r.vehicleCount}</td>
            <td>${r.trafficDensity}%</td>
            <td>${r.averageSpeed} km/h</td>
            <td><span class="badge ${fn:toLowerCase(r.level)}">${r.level}</span></td>
            <td>${r.timestampText}</td>
            <c:if test="${sessionScope.user.role == 'ADMIN'}">
              <td><form class="inline" method="post" action="${pageContext.request.contextPath}/traffic">
                <input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${r.recordId}">
                <button class="btn danger small" type="submit">Delete</button></form></td>
            </c:if>
          </tr>
        </c:forEach>
      </table></div>
    </c:otherwise>
  </c:choose>
</div>

<%@ include file="includes/footer.jsp" %>
