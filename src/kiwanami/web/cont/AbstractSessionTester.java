package kiwanami.web.cont;

import java.util.Enumeration;
import junit.framework.TestCase;


public abstract class AbstractSessionTester extends TestCase {

    private static TestContinuationServlet testServlet = new TestContinuationServlet(null);
    
    private ISessionTestContext context;

    public void setUp() {
        context = testServlet.getTestSession();
    }

    //==================================================
    // short cut
    
    public void param(String name,String value) {
        ((IVariableParameter)context.getRequest()).setParameter(name,value);
    }

    public Object attr(String name) {
        return context.getRequest().getAttribute(name);
    }

    public UserSessionController access(String path) throws Throwable {
        return context.access(path);
    }

    public void doAction(String path) throws Throwable {
        context.doAction(path);
    }

    public String dumpAttrs() {
        StringBuffer sb = new StringBuffer("----(dump attrs)--------\n");
        for(Enumeration e = context.getRequest().getAttributeNames();e.hasMoreElements();) {
            String key = (String) e.nextElement();
            sb.append("  "+key+" : "+attr(key)).append("\n");
        }
        return sb.toString();
    }

    //==================================================
    // test utilities

    public void assertPage(String message,String path) {
        assertEquals(message,path,context.getForwardPath());
    }
    public void assertPage(String path) {
        assertEquals(path,context.getForwardPath());
    }

    public void assertRedirect(String message,String path) {
        assertEquals(message,path,context.getRedirectPath());
    }
    public void assertRedirect(String path) {
        assertEquals(path,context.getRedirectPath());
    }

    public void assertValue(String message,String name,String expectedValue) {
        assertEquals(message,(String)attr(name),expectedValue);
    }
    public void assertValue(String name,String expectedValue) {
        assertEquals((String)attr(name),expectedValue);
    }

    public void assertAjaxCall(String method,String[] args,String responseText) {
        //TODO
    }


}
