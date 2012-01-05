package kiwanami.web.cont;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ITestScriptHelper {

    public IUserSession access(String path);
    
    public HttpServletRequest request();
    public HttpServletResponse response();

    public void action();

    public String forwardPath();
    public String redirectPath();

    public Throwable exception();

}
