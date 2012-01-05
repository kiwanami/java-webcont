package kiwanami.web.cont;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public interface ISessionTestContext {
    
    public UserSessionController access(String path) throws Throwable;
    
    public HttpServletRequest getRequest();
    public HttpServletResponse getResponse();

    public void doAction(String path) throws Throwable;

    public String getForwardPath();
    public String getRedirectPath();

}
