<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="kiwanami.web.cont.*,java.util.regex.*" %>
<html>
<head>
<title>Global/Branchサンプル</title>
</head>

<body>

<h1>Global/Branchサンプル</h1>

message : <span style="color:red"> ${message} </span><br>
global : <span style="color:red"><%= CSUtils.go(application,"go_message") %></span><br>

<form name="message" method="post">
<input type="text" name="name" value="${name}" />
<input type="submit" value="設定" />
</form>

<a href="<%=request.getContextPath()%>/branchsub.jsp?key=${_session_key}" target="_blank">[open branch]</a>
<hr />

</body>
</html>
