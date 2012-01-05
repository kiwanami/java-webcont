package kiwanami.web.cont;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.log4j.Logger;


public class ScriptSession implements IUserSession {

	private Logger logger;
    private Object sessionID;

	//script information
	private String source;
	private File path;
	private String scriptLang;

	//current showing page
	private String lastVisitPage;

	//exit page
	private String redirectUrl;

	//for debug object
	private IContextEvaluator contextEvaluator;

	public ScriptSession(Logger l,File path,String source) {
		logger = l;
		this.path = path;
		this.source = source;
	}
    
	public String getSessionTitle() {
		return "["+scriptLang+"]: "+path.getName() + " -> "+lastVisitPage;
	}

	public void initSession(ISessionContext context) {}

	public String session(final ISessionContext c) {
		IScriptHelper helper = new IScriptHelper() {
				public Object session_id() {
					return c.getSessionID();
				}
				public HttpServletResponse response() {
					return c.getResponse();
				}
				public HttpServletRequest request() {
					return c.getRequest();
				}
				public String module_uri() {
					return request().getRequestURI();
				}
				public Logger log() {
					return logger;
				}
				public void attr(String key,Object val) {
					request().setAttribute(key,val);
				}
				public Object get_attr(String key) {
					return request().getAttribute(key);
				}
				public String param(String name) {
					return request().getParameter(name);
				}
				public String show(String url) {
					attr("script_lang",scriptLang);
					lastVisitPage = url;
					try {
						CSUtils.dispatchView(c,url);
					} catch (Exception e) {
						throw new RuntimeException("View error : "+e.getMessage()+
												   " ["+e.getClass().getName()+"]",e);
					}
					return param("tid");
				}
				public void exit(String url) {
					redirectUrl = url;
				}
				public AjaxController ajaxController() {
					return c.getAjaxController();
				}
				public RoutingInfo routingInfo() {
					return c.getRoutingInfo();
				}
				public IGlobalService globalService() {
					return c.getGlobalService();
				}
				public IBranchService branchService() {
					return c.getBranchService();
				}
				public void contextEvaluator(IContextEvaluator e) {
					contextEvaluator = e;
				}
				public String load(String abstractPath) throws IOException {
					return CSUtils.file2str(new File(path.getParent(),abstractPath));
				}
			};
		try {
			BSFManager manager = new BSFManager();
			scriptLang = BSFManager.getLangFromFilename(path.getName());
			manager.declareBean("SB",path.getParent(),String.class);//script base directory
			manager.declareBean("web",helper,IScriptHelper.class);
			manager.exec(scriptLang, path.getName(), 0, 0, source);
			return (String)redirectUrl;
		} catch (BSFException e) {
			logger.warn("Script Error",e);
			RuntimeException ee = new RuntimeException(e);
			throw ee;
		}
	}

    public void finishSession() {
        logger.warn("Finished the session: "+sessionID);
    }

    public void timeoutSession() {
        logger.warn("Time out the session: "+sessionID);
    }

    public void uncaughtException(Throwable e) throws Throwable {
        throw e;
    }
	public IContextEvaluator getContextEvaluator() {
		return contextEvaluator;
	}
}
