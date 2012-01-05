importPackage(Packages.kiwanami.web.cont);

var top = this;
web.contextEvaluator(function(code) {
	return eval(""+code);
});

function log(m) {
	web.log().debug("## "+m);
}

//register message server
var gs = web.globalService();
var server = gs.get("mserver");
if (!server) {
	log("Creating new server...aaa");
	var messages = [];
	server = {};
	server.count = 1;
	server.lock = new Packages.sample.LockObject();
	log(server.lock.toString()+" "+server.lock.getClass().getName());
	server.messages = messages;
	server.getMessages = function(rev) {
		if (messages.length <= rev) {
			server.lock._wait(1000*10);
			if (messages.length <= rev) {
				return null;
			}
		}
		return messages.slice(rev);
	};
	server.addMessage = function(m) {
		messages.push(escape(m));
		server.lock._notifyAll();
	};
	gs.set("mserver",server);
} else {
	log("Got a message server instance");
	server.count += 1;
}

web.ajaxController().addHandler("send",function(key,rinfo,req,res) {
	var msg = unescape(req.getParameter("name"))+":"+unescape(req.getParameter("comment"));
	log(">> "+msg);
	server.addMessage(msg);
	res.getWriter().println("OK");
});

web.ajaxController().addHandler("get",function(key,rinfo,req,res) {
	var rev = parseInt(req.getParameter("rev"));
	log("rev.. "+rev);
	var ms = server.getMessages(rev);
	if (ms == null) {
		log("time out");
		res.getWriter().println("null");
	} else {
		log("got messages : "+ms.length);
		res.getWriter().println("{cur_rev:"+server.messages.length+", data:"+ms.toSource()+"}");
	}
});

var top = this;
web.contextEvaluator(function(code) {
	return eval(""+code);
});

while(true) {
	web.attr("message","Connected a server >> "+server.count);
	web.show("/comet.jsp");
	var next = web.param("tid");
	if (next == "exit") {
		break;
	}
}
web.exit("/cont/welcome.html");
