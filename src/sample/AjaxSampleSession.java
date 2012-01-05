package sample;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kiwanami.web.cont.AbstractUserSession;
import kiwanami.web.cont.IAjaxHandler;
import kiwanami.web.cont.ISessionContext;
import kiwanami.web.cont.RoutingInfo;
import kiwanami.web.cont.IContextEvaluator;

public class AjaxSampleSession extends AbstractUserSession {

	private int counter = 0;

	public String session(ISessionContext c) {
        c.getAjaxController().addHandler("countup",new IAjaxHandler() {
				public void call(String key,RoutingInfo rinfo,
								 HttpServletRequest req,HttpServletResponse res) throws Throwable {
					counter += 1;
					res.getWriter().println("OK");
				}
			});
		while(true) {
			attr(c,"message","Current counter is ["+counter+"]");
			show(c,"/ajaxpage.jsp");
			if (counter > 30) {
				return "/cont/welcome.html";
			}
		}
	}

	//================================================== for debug

	private IContextEvaluator eval = new IContextEvaluator() {
			public String eval(String code) {
				if (code != null && code.matches("[0-9]+")) {
					counter = Integer.parseInt(code);
				}
				return String.valueOf(counter);
			}
		};

	public IContextEvaluator getContextEvaluator() {
		return eval;
	}
	
}
