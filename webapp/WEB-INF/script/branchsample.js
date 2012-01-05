importPackage(Packages.kiwanami.web.cont);

var name = "(no name)";

while(true) {
	web.attr("message","Current name is ["+name+"] ");
	web.show("/branchmain.jsp");
	name = web.param("name") + "";
	if (name == "end" || name == "exit") {
		break;
	}
	if (name.match(/^[A-Z]/)) {
		web.globalService().set("go_message",name);
	}
	web.branchService().set("bo_message",name);
}
web.exit("/cont/welcome.html");
