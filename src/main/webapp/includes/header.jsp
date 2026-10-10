<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Shared header: pass the active page with <c:set var="active" value="dashboard"/> before including --%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>${pageTitle} | Urban Traffic Monitoring</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
  <c:if test="${param.refresh == 'on'}"><meta http-equiv="refresh" content="15"></c:if>
</head>
<body>
<nav class="nav">
  <span class="brand">&#9679; Traffic Engine</span>
  <a class="link ${active == 'dashboard' ? 'active' : ''}" href="${pageContext.request.contextPath}/dashboard">Dashboard</a>
  <a class="link ${active == 'traffic' ? 'active' : ''}" href="${pageContext.request.contextPath}/traffic">Traffic Data</a>
  <a class="link ${active == 'junctions' ? 'active' : ''}" href="${pageContext.request.contextPath}/junctions">Junctions</a>
  <a class="link ${active == 'alerts' ? 'active' : ''}" href="${pageContext.request.contextPath}/alerts">Alerts</a>
  <a class="link ${active == 'reports' ? 'active' : ''}" href="${pageContext.request.contextPath}/reports">Reports</a>
  <c:if test="${sessionScope.user.role == 'ADMIN'}">
    <a class="link ${active == 'users' ? 'active' : ''}" href="${pageContext.request.contextPath}/users">Users</a>
  </c:if>
  <span class="spacer"></span>
  <span class="who"><c:out value="${sessionScope.user.name}"/> &middot; <c:out value="${sessionScope.user.accessLevel}"/></span>
  <a class="btn secondary small" href="${pageContext.request.contextPath}/logout">Log out</a>
</nav>
<div class="container">
<c:if test="${not empty sessionScope.flash}">
  <div class="flash ${sessionScope.flashType}"><c:out value="${sessionScope.flash}"/></div>
  <c:remove var="flash" scope="session"/>
  <c:remove var="flashType" scope="session"/>
</c:if>
<c:if test="${not empty error}">
  <div class="flash error"><c:out value="${error}"/></div>
</c:if>
