<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.regex.*" %>
<html>
<head>
<title>AJAXサンプル</title>
<script src="<%=request.getContextPath()%>/javascripts/prototype.js" type="text/javascript"></script>
<script>
var counter = 0;
var BASE = "${_session_path}";
function countup() {
	counter += 1;
	$('counter').innerHTML = ""+counter;
	new Ajax.Request(BASE+'/ajax/countup', { method:'get' })
}
Event.observe(window,'load',function() {
	Event.observe($('countup'),'click',countup);
});
</script>

</head>

<body>

<h1>AJAXサンプル</h1>

<span style="color:red"> ${message} </span><br>

<span id="counter" style="color:blue">0</span><br>

<input id="countup" type="button" value="カウント+1"/>

<form name="refresh" method="post">
<input type="submit" value="再表示" />
</form>

<hr />

</body>
</html>