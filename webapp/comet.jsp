<%@ page contentType="text/html; charset=UTF-8" %>
<html> <head>
<title>Comet</title>
</head>
<script src="<%=request.getContextPath()%>/javascripts/prototype.js" type="text/javascript"></script>
<script>
var rev = 0;
var BASE = "${_session_path}";
function sendComment() {
	try {
		ajaxObject.abort();
	} catch (e) {
	}
	var name = escape($('name').value);
	var comment = escape($('comment').value);
	var params = 'name='+name+'&comment='+comment;
	try {
	new Ajax.Request(BASE+'/ajax/send', 
					 { method:'get',
					   parameters: {name: name,comment: comment},
					   onComplete: function(req,obj) {
					   }
					 });
	} catch (e) {
		$('message').innerHTML = e.toString();
	}
}

var ajaxObject = null;

function getMessage() {
	new Ajax.Request(BASE+'/ajax/get',
					 { name:'get', 
					   parameters: 'rev='+rev,
					   initHttpObj: function(obj) {
						   ajaxObject = obj;
					   },
					   onComplete: function(req,obj) {
						   eval("var json = "+req.responseText+";");
						   if (json) {
							   var str = $('area').innerHTML;
							   rev = json.cur_rev;
							   var lines = json.data;
							   for(var i=0;i<lines.length;i++) {
								   str += "<div>"+unescape(lines[i])+"</div>";
							   }
							   $('area').innerHTML = str;
						   }
						   getMessage();
					   }});
}

Event.observe(window,'load',function() {
	Event.observe($('send'),'click',function() {
		sendComment();
	},false);
	getMessage();
},false);
</script>
<body>
<h1>Comet</h1>
<div id="message">${message}</div>
<input type="text" name="name" id="name" size="8">
<input type="text" name="comment" id="comment" size=20>
<button id="send">send</button>
<hr>

<div id="area">
</div>

<hr>
</body> </html>
