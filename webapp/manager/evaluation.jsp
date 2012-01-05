<%
String sid = request.getParameter("sid");
String titleId = sid;
if (titleId != null) {
	titleId = titleId.substring(0,Math.min(10,titleId.length()))+"...";
} else {
	titleId = "No session id.";
}
%>
<html>
<head>
<title>Evaluation session : <%= titleId %></title>
<link href="article-blue.css" type="text/css" rel="stylesheet">
<script src="prototype.js"></script>
<script src="shortcuts.js"></script>
<script>
Event.observe(window,"load",function() {
	$('code').focus();
	Event.observe($('eval_button'),"click",function() {
		execCode();
	});
	shortcut("Ctrl+Enter",function() {
		execCode();
	});
});
function execCode() {
	new Ajax.Request(
		"exec.jsp", 
		{
			method: 'post', 
			parameters: 'sid=<%= sid%>&code='+escape($('code').value),
			onComplete: function(req,obj) {
                var lines = req.responseText.split("\n");
                if (lines.length > 4) {
                    lines = lines.slice(4);
                }
                $('result').innerHTML = lines.join("\n");
			},
			onFailure: function() {
                $('result').innerHTML = "failed to evaluation...";
			}
		});
}
</script>
</head>
<body>

<table border="1px" width="100%" class="data">
<tr>
<td>Code:</td>
<td>
<textarea id="code" cols="80" rows="8"></textarea>
<button id="eval_button">Eval</button>
</td>
</tr>

<tr>
<td>Result:</td>
<td>
<pre class="program" id="result">
</pre>
</td>

</tr>
</table>
<hr>
</body>
</html>
