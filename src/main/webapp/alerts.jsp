<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Alerts"/>
<c:set var="active" value="alerts"/>
<%@ include file="includes/header.jsp" %>

<div class="page-head">
  <div class="eyebrow">Module 5 &middot; Alert management</div>
  <h1>Traffic Alerts</h1>
  <p>${openAlerts} open alert(s). Alerts are raised when density crosses a threshold, speed becomes unusually low, or traffic is abnormal.</p>
</div>

<div class="card">
  <div class="toolbar">
    <span class="grow"></span>
    <form class="inline" method="post" action="${pageContext.request.contextPath}/alerts">
      <input type="hidden" name="action" value="resolveAll">
      <button class="btn secondary small" type="submit">Resolve all</button></form>
    <c:if test="${sessionScope.user.role == 'ADMIN'}">
      <form class="inline" method="post" action="${pageContext.request.contextPath}/alerts"
            onsubmit="return confirm('Delete every alert?')">
        <input type="hidden" name="action" value="clear">
        <button class="btn danger small" type="submit">Clear all</button></form>
    </c:if>
  </div>
  <c:choose>
    <c:when test="${empty alerts}"><p class="empty">No alerts yet.</p></c:when>
    <c:otherwise>
      <div class="table-wrap"><table>
        <tr><th>Junction</th><th>Type</th><th>Severity</th><th>Message</th><th>Time</th><th>Status</th><th></th></tr>
        <c:forEach var="a" items="${alerts}">
          <tr>
            <td><c:out value="${a.junctionName}"/></td>
            <td><c:out value="${a.alertType}"/></td>
            <td><span class="badge ${fn:toLowerCase(a.severity)}">${a.severity}</span></td>
            <td><c:out value="${a.message}"/></td>
            <td>${a.timestampText}</td>
            <td><span class="badge ${a.resolved ? 'resolved' : 'open'}">${a.resolved ? 'RESOLVED' : 'OPEN'}</span></td>
            <td>
              <c:if test="${not a.resolved}">
                <form class="inline" method="post" action="${pageContext.request.contextPath}/alerts">
                  <input type="hidden" name="id" value="${a.alertId}">
                  <button class="btn small secondary" type="submit">Resolve</button>
                </form>
              </c:if>
            </td>
          </tr>
        </c:forEach>
      </table></div>
    </c:otherwise>
  </c:choose>
</div>

<%@ include file="includes/footer.jsp" %>
