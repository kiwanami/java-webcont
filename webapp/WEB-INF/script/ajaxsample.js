importPackage(Packages.kiwanami.web.cont);

var counter = 0;

web.ajaxController().addHandler("countup",function(key,rinfo,req,res) {
	counter += 1;
	res.getWriter().println("OK");
});

var top = this;
web.contextEvaluator(function(code) {
	return eval(""+code);
});

while(true) {
	web.attr("message","Current counter is ["+counter+"] ");
	web.show("/ajaxpage.jsp");
	if (counter > 30) {
		break;
	}
}
web.exit("/cont/welcome.html");
