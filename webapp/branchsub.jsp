<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="kiwanami.web.cont.*,java.util.regex.*" %>
<html>
<head>
<title>Global/Branchサンプル : sub</title>
<%
	String sessionKey = request.getParameter("key");
%>
</head>
<body>

<h1>Global/Branchサンプル : sub</h1>

branch : <span style="color:red"><%= CSUtils.bo(application,sessionKey,"bo_message") %></span><br>
global : <span style="color:red"><%= CSUtils.go(application,"go_message") %></span><br>

<hr />

</body>
</html>
