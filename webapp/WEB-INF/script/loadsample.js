eval(""+web.load("loadsample_1.js"));
eval(""+web.load("js/loadsample_2.js"));

while(true) {
	web.attr("message","Loading sample...<br>"+sample1+" <br>"+sample2);
	web.show("/message.jsp");
	if (web.param("value") == "ok") {
		break;
	}
}
web.exit("/cont/welcome.html");
