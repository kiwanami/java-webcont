package sample;

import javax.servlet.ServletConfig;
import kiwanami.web.cont.IUserSessionFactory;
import kiwanami.web.cont.IUserSession;

public class SampleSessionFactory implements IUserSessionFactory {

	public IUserSession getInstance(String[] options) {
		return new SampleSession();
	}

	public void init(ServletConfig c,String a) {}
}
