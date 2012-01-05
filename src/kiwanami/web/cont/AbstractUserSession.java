package kiwanami.web.cont;

import java.io.IOException;
import javax.servlet.ServletException;

public abstract class AbstractUserSession implements IUserSession {

	//==================================================
	// 空実装
	//==================================================

    public void finishSession() {
    }

    public void initSession(ISessionContext context) {
    }

    public void timeoutSession() {
    }

    public void uncaughtException(Throwable e) throws Throwable {
        throw e;
    }

	public IContextEvaluator getContextEvaluator() {
		return null;
	}

	//==================================================
	// 便利メソッド
	//==================================================

	private String lastVisitPage;

	public String param(ISessionContext c,String name) {
		return c.getRequest().getParameter(name);
	}

	public void attr(ISessionContext c,String key,String val) {
        c.getRequest().setAttribute(key,val);
	}

	/**
	   @exception ServletException, IOException が遷移先のページで発生
	   したときはRuntimeExceptionに丸められる。
	*/
	public void show(ISessionContext c,String url) {
		attr(c,"script_lang","java");
		lastVisitPage = url;
		try {
			CSUtils.dispatchView(c,url);
		} catch (ServletException e) {
			throw new RuntimeException("View error : "+e.getMessage()+" ["+e.getClass().getName()+"]",e);
		} catch (IOException e) {
			throw new RuntimeException("View error : "+e.getMessage()+" ["+e.getClass().getName()+"]",e);
		}
	}

	public String getLastVisitPage() {
		return lastVisitPage;
	}

	public String getSessionTitle() {
		return this.getClass().getName() + " -> "+lastVisitPage;
	}

}
