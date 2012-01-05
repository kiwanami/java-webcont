package sample;

import kiwanami.web.cont.AbstractUserSession;
import kiwanami.web.cont.ISessionContext;

public class SampleSession extends AbstractUserSession {

	public String session(ISessionContext c) {
		while(true) {
			show(c,"/login.jsp");
			String id = param(c,"id");
			String passwd = param(c,"passwd");
			if ((id == null || id.length()==0) && 
				(passwd == null || passwd.length()==0)) {
				attr(c,"login_error","IDとパスワードを入力してください。");
				continue;
			}
			if (id.equals("1") && passwd.equals("a")) {
				break;
			}
			attr(c,"login_error","IDまたはパスワードが違います。");
		}
		show(c,"/ok.jsp");
		show(c,"/cont1.jsp");
		show(c,"/cont2.jsp");
		show(c,"/cont3.jsp");
		return "/cont/exit.jsp";
	}
	
}
