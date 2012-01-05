package kiwanami.web.cont;

import javax.servlet.ServletConfig;

public interface IUserSessionFactory {

	public IUserSession getInstance(String[] options);

	public void init(ServletConfig context,String option);

}
