package sample;

import kiwanami.web.cont.AbstractSessionTester;
import kiwanami.web.cont.UserSessionController;

public class SampleSessionTest extends AbstractSessionTester {

    public void testSession1() throws Throwable {
        //UserSessionController userSession = access("/sample");
        UserSessionController userSession = access("/script/javascript.js");

        assertPage("/login.jsp");

        param("id","");
        param("passwd","");
        doAction("/action");
        assertPage("/login.jsp");
        assertValue("login_error","IDとパスワードを入力してください。");

        param("id","aaa");
        param("passwd","bbb");
        doAction("/action");
        assertPage("/login.jsp");
        assertValue("login_error","IDまたはパスワードが違います。");

        param("id","1");
        param("passwd","a");
        doAction("/action");
        assertPage("/ok.jsp");

        doAction("/action");
        assertPage("/cont1.jsp");

        doAction("/action");
        assertPage("/cont2.jsp");

        doAction("/action");
        assertPage("/cont3.jsp");

        doAction("/action");
        assertRedirect("/cont/exit.jsp");
    }

    public void testAjaxSession1() throws Throwable {
        UserSessionController userSession = access("/ajax_sample");

        assertPage("/ajaxpage.jsp");
        assertValue("message","Current counter is [0]");

        assertAjaxCall("countup",new String[0],"OK");//testはResponseをチェックする感じ

        doAction("/action");
        assertPage("/ajaxpage.jsp");
        assertValue("message","Current counter is [1]");

    }

}
