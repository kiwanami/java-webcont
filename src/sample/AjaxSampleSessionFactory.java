package sample;

import javax.servlet.ServletConfig;

import kiwanami.web.cont.IUserSession;
import kiwanami.web.cont.IUserSessionFactory;

public class AjaxSampleSessionFactory implements IUserSessionFactory {

	public IUserSession getInstance(String[] options) {
		return new AjaxSampleSession();
	}

	public void init(ServletConfig c,String a) {}
}
