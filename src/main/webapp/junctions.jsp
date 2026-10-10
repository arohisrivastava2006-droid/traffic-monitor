<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Junctions"/>
<c:set var="active" value="junctions"/>
<%@ include file="includes/header.jsp" %>

<div class="page-head">
  <div class="eyebrow">Module 2 &middot; Junction management</div>
  <h1>Junctions</h1>
  <p>${sessionScope.user.role == 'ADMIN' ? 'Add, edit and remove monitored junctions.' : 'View monitored junctions. Only administrators can change them.'}</p>
</div>

<c:if test="${sessionScope.user.role == 'ADMIN'}">
  <div class="card" style="margin-bottom:1.2rem">
    <h2>Add junction</h2>
    <form method="post" action="${pageContext.request.contextPath}/junctions">
      <input type="hidden" name="action" value="add">
      <div class="form-row">
        <div><label for="name">Name</label><input id="name" name="name" required maxlength="100"></div>
        <div><label for="location">Location</label><input id="location" name="location" required maxlength="200"></div>
        <div><button class="btn" type="submit">Add junction</button></div>
      </div>
    </form>
  </div>
</c:if>

<c:if test="${sessionScope.user.role == 'ADMIN'}">
  <div class="card" style="margin-bottom:1.2rem">
    <h2>Sample junctions</h2>
    <p style="color:var(--muted);margin-top:0">Add ten more sample junctions (Silk Board, Hebbal, Marathahalli and more) in one click.</p>
    <form class="inline" method="post" action="${pageContext.request.contextPath}/junctions">
      <input type="hidden" name="action" value="seed">
      <button class="btn secondary" type="submit">Add 10 sample junctions</button>
    </form>
  </div>
</c:if>

<div class="card">
  <h2>All junctions</h2>
  <c:choose>
    <c:when test="${empty junctions}"><p class="empty">No junctions yet.</p></c:when>
    <c:otherwise>
      <div class="table-wrap"><table>
        <tr><th>ID</th><th>Name</th><th>Location</th><c:if test="${sessionScope.user.role == 'ADMIN'}"><th>Actions</th></c:if></tr>
        <c:forEach var="j" items="${junctions}">
          <tr>
            <c:choose>
              <c:when test="${sessionScope.user.role == 'ADMIN'}">
                <form method="post" action="${pageContext.request.contextPath}/junctions" id="f${j.junctionId}"></form>
                <td>${j.junctionId}</td>
                <td><input form="f${j.junctionId}" name="name" value="<c:out value='${j.name}'/>" required></td>
                <td><input form="f${j.junctionId}" name="location" value="<c:out value='${j.location}'/>" required></td>
                <td style="white-space:nowrap">
                  <input form="f${j.junctionId}" type="hidden" name="id" value="${j.junctionId}">
                  <button form="f${j.junctionId}" class="btn small" name="action" value="update" type="submit">Save</button>
                  <button form="f${j.junctionId}" class="btn danger small" name="action" value="delete" type="submit">Delete</button>
                </td>
              </c:when>
              <c:otherwise>
                <td>${j.junctionId}</td>
                <td><c:out value="${j.name}"/></td>
                <td><c:out value="${j.location}"/></td>
              </c:otherwise>
            </c:choose>
          </tr>
        </c:forEach>
      </table></div>
    </c:otherwise>
  </c:choose>
</div>

<%@ include file="includes/footer.jsp" %>
