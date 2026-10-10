<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Users"/>
<c:set var="active" value="users"/>
<%@ include file="includes/header.jsp" %>

<div class="page-head">
  <div class="eyebrow">Administration</div>
  <h1>Users</h1>
  <p>Anyone who signs in with a new username gets an Operator account. Admins can promote, demote or remove users here.</p>
</div>

<div class="card">
  <c:choose>
    <c:when test="${empty users}"><p class="empty">No users.</p></c:when>
    <c:otherwise>
      <div class="table-wrap"><table>
        <tr><th>ID</th><th>Name</th><th>Username</th><th>Role</th><th>Actions</th></tr>
        <c:forEach var="u" items="${users}">
          <tr>
            <td>${u.id}</td>
            <td><c:out value="${u.name}"/></td>
            <td><c:out value="${u.username}"/></td>
            <td><span class="badge ${u.role == 'ADMIN' ? 'high' : 'low'}">${u.role}</span></td>
            <td style="white-space:nowrap">
              <c:choose>
                <c:when test="${u.id == sessionScope.user.userId}"><span style="color:var(--muted)">(you)</span></c:when>
                <c:otherwise>
                  <form class="inline" method="post" action="${pageContext.request.contextPath}/users">
                    <input type="hidden" name="id" value="${u.id}">
                    <input type="hidden" name="role" value="${u.role == 'ADMIN' ? 'OPERATOR' : 'ADMIN'}">
                    <button class="btn secondary small" type="submit">Make ${u.role == 'ADMIN' ? 'Operator' : 'Admin'}</button>
                  </form>
                  <form class="inline" method="post" action="${pageContext.request.contextPath}/users"
                        onsubmit="return confirm('Delete this user?')">
                    <input type="hidden" name="id" value="${u.id}">
                    <input type="hidden" name="action" value="delete">
                    <button class="btn danger small" type="submit">Delete</button>
                  </form>
                </c:otherwise>
              </c:choose>
            </td>
          </tr>
        </c:forEach>
      </table></div>
    </c:otherwise>
  </c:choose>
</div>

<%@ include file="includes/footer.jsp" %>
