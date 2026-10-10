<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Login | Urban Traffic Monitoring</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="login-wrap">
  <div class="card login-card">
    <div class="eyebrow">Java Project &middot; Team 4</div>
    <h1>Urban Traffic Monitoring and Analytics Engine</h1>
    <p style="color:var(--muted)">Sign in to open the monitoring dashboard.</p>

    <c:if test="${not empty error}">
      <div class="flash error"><c:out value="${error}"/></div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/login">
      <div class="field">
        <label for="username">Username</label>
        <input id="username" name="username" required autofocus autocomplete="username">
      </div>
      <div class="field">
        <label for="password">Password</label>
        <input id="password" name="password" type="password" required autocomplete="current-password">
      </div>
      <button class="btn" type="submit" style="width:100%">Sign in</button>
    </form>
    <p class="hint"><b>Any new username works</b>: type a username and password and an account is created for you automatically (monitor and report access).
      Admin login: <b>admin / admin123</b></p>
  </div>
</div>
</body>
</html>
