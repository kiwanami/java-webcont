package kiwanami.web.cont;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.bsf.BSFManager;
import org.apache.log4j.Logger;

public class ContinuationServlet extends HttpServlet {

	private static Logger logger = Logger.getLogger(ContinuationServlet.class);

	private String welcome = "/";
	private String error = null;
	private String wait = null;

	private String sessionPath = "session";

	private HashMap<String,IUserSessionFactory> moduleFactoryMap = 
        new HashMap<String,IUserSessionFactory>();//name -> factory

	private UserSessionManager sessionManager = new UserSessionManager();

	//=============================================================
	// Initialization
	//=============================================================

	public void init(ServletConfig config) {
		logger.info("### Initializing... ");
		try {
			String w = config.getInitParameter("welcome");
			if (w != null) {
				welcome = w;
				logger.info("Config: welcome="+w);
			}
			String e = config.getInitParameter("error");
			if (e != null) {
				error = e;
				logger.info("Config: error="+e);
			}
			String s = config.getInitParameter("wait");
			if (s != null) {
				wait = s;
				logger.info("Config: wait="+s);
			}
			String sp = config.getInitParameter("session_path");
			if (sp != null) {
				sessionPath = sp;
				logger.info("Config: session_path="+sp);
			}
			String sst = config.getInitParameter("session_time");
			if (sst != null) {
				try {
					sessionManager.setSessionTime(Integer.parseInt(sst));
					logger.info("Config: session time="+sst);
				} catch (NumberFormatException ex) {
					logger.error("Config: Session_Time: wrong number format: "+sst);
				}
			}
			String ms = config.getInitParameter("max");
			if (ms != null) {
				try {
					sessionManager.setMaxSessionNum(Integer.parseInt(ms));
					logger.info("Config: session max="+ms);
				} catch (NumberFormatException ex) {
					logger.error("Config: MaximumSessionNum: wrong number format: "+ms);
				}
			}

			String params = config.getInitParameter("modules");
			if (params == null) {
				logger.error("No module.");
			} else {
				String[] modules = params.split("\\|");
				for(int i=0;i<modules.length;i++) {
					String[] els = modules[i].trim().split(",");
					if (els.length >= 2) {
						String name = els[0];
						String cls = els[1];
						String opt = (els.length >= 3) ? els[2] : null;
						addModule(config,name,cls,opt);
					} else {
						logger.warn("Wrong session parameter: "+modules[i]);
					}
				}
			}
			addModule(config,"script","kiwanami.web.cont.ScriptSessionFactory",null);

			BSFManager.registerScriptingEngine
				("groovy", "org.codehaus.groovy.bsf.GroovyEngine", 
				 new String[] { "groovy", "gy" }
				 );

			config.getServletContext().setAttribute("session_manager",sessionManager);
			config.getServletContext().setAttribute("global_service",sessionManager.getGlobalService());
		} finally {
			logger.info("### Finished initializing.");
		}
	}

	private void addModule(ServletConfig config,String name,String className,String option) {
		try {
			Class klass = Class.forName(className);
			IUserSessionFactory factory = (IUserSessionFactory)klass.newInstance();
			factory.init(config,option);
			moduleFactoryMap.put(name,factory);
		} catch (Exception e) {
			logger.warn("Class ["+className+"] was not found.",e);
		}
	}

	//=============================================================
	// Servlet framework
	//=============================================================

	public void destroy() {
		sessionManager.shutdown();
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		execute(new ContServletRequest(req),resp);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		execute(new ContServletRequest(req),resp);
	}

	//=============================================================
	// Continuation framework
	//=============================================================

	private void execute(ContServletRequest req,HttpServletResponse resp) throws ServletException, IOException {
		try {
			RoutingInfo routingInfo = parseRoutingInfo(req);

			String bsid = req.getSession().getId();
			String ssid = routingInfo.sessionId;
			UserSessionController session = sessionManager.getUserSession(routingInfo.sessionId);

			if (session != null && !session.getBrowserSessionID().equals(bsid)) {
				logger.warn("Different browser ID : "+session.getBrowserSessionID()+" --> "+bsid);
				session = null;
			}

			if (session == null) {
				//creating user session
				IUserSessionFactory factory = getFactory(routingInfo.module);
				if (factory == null) {
					logger.debug("Module not found: "+req.getRequestURI());
					resp.sendRedirect(welcome);
					return;
				}
				session = new UserSessionController(bsid,ssid,factory.getInstance(routingInfo.options),sessionManager.getGlobalService());
				sessionManager.registerSession(session);
				String path = routingInfo.getSessionPath(req.getContextPath()+"/"+sessionPath)+"/"+routingInfo.getJoinedOptions();
                logger.debug("==== store parameters : "+req.getMethod());
                if ("post".equals(req.getMethod().toLowerCase())) {
                    session.setLastParameters(req.getParameterMap());
                } else {
                    if (req.getQueryString() != null) {
                        path += "?"+req.getQueryString();
                    }
                }
				resp.sendRedirect(path);
			} else {
                logger.debug("==== continue session");
				//continue user session
				try {
					boolean finished = session.continueSession(routingInfo,req,resp);
					if (finished) {
						sessionManager.removeSession(session.getSessionID());
					}
				} catch (Throwable e) {
					sessionManager.removeSession(session.getSessionID());
					logger.info("Removed session because of an error: "+e.toString());
					throw e;
				}
			}
		} catch (MaximumSessionException e) {
			logger.warn("MaximumSessionException: "+e.getMessage());
			if (wait != null) {
				dispatch(req,resp,wait);
			} else {
				output(generateWaitPage(req,resp),resp);
			}
		} catch (Throwable e) {
			String code = generateErrorCode();
			logger.warn("Error was occurred: "+e.getMessage()+"  CODE:"+code,e);
			if (error != null) {
				req.setAttribute("error_code",code);
				req.setAttribute("error_message",e.getClass().getName()+" | "+e.getMessage());
				req.setAttribute("error_stacktrace",trace2str(e));
				dispatch(req,resp,error);
			} else {
				output(generateErrorPage(req,resp,code,e),resp);
			}
		}
	}

	private static long errorNumber = 0;
	private static String generateErrorCode() {
		return ""+System.currentTimeMillis()+""+(errorNumber++);
	}

	/**
	   request uri から、(module名)と(option path)を取ってくる
	   "modele" => UserSession担当モジュール名
	   "options" => モジュール名以降のパスの配列。スラッシュは含まない。
	*/
	private RoutingInfo parseRoutingInfo(HttpServletRequest req) {
		logger.debug("Routing:"+req.getRequestURI());
		String contextPath = req.getContextPath();
		logger.debug("ContextName:"+contextPath);
		String servletPath = req.getServletPath();
		logger.debug("ServletPath:"+servletPath);
		try {
            //  uri -> /(module)/(option1)/(option2)/(option3)/(option4)/(option5)/(option6)
			Matcher m = Pattern.compile("^"+contextPath+servletPath+"\\/([^\\/]+)(\\/[^\\/]+)?(\\/[^\\/]+)?(\\/[^\\/]+)?(\\/[^\\/]+)?(\\/[^\\/]+)?(\\/[^\\/]+)?").matcher(req.getRequestURI());
			if (m.find()) {
				String module = m.group(1);
				logger.debug("Module:"+module);
				ArrayList<String> opts = new ArrayList<String>();
				for(int i=0;i<(m.groupCount()-1);i++) {
					String opt = m.group(i+2);
					if (opt == null) {
						break;
					}
					opts.add((opt.length() > 0) ? opt.substring(1) : opt);
				}
				logger.debug("OPTIONS:"+Arrays.toString(opts.toArray()));
				return new RoutingInfo(contextPath+servletPath,module,
									   opts.toArray(new String[0]),
									   !servletPath.matches("\\/"+sessionPath+"$"));
			} else {
				logger.warn("Can not get a module name. : "+req.getRequestURI());
			}
		} catch (RuntimeException e) {
			logger.warn(e);
			logger.warn(trace2str(e));
		}
		throw new RuntimeException("Invalid path structure: "+req.getRequestURI());
	}

	private void dispatch(ContServletRequest request,HttpServletResponse response,String url) {
		try {
			RequestDispatcher rd = request.getRequestDispatcher(url);
			request.sync2org();
			rd.forward((HttpServletRequest)request.getRequest(), response);
		} catch (Exception e) {
			logger.warn(e);
			logger.warn(trace2str(e));
		}
	}

	private IUserSessionFactory getFactory(String moduleName) {
		if (moduleName == null || !moduleFactoryMap.containsKey(moduleName)) {
			logger.warn("Module ["+moduleName+"] not found.");
			return null;
		}
		return moduleFactoryMap.get(moduleName);
	}

	//==============================================================
	// Waiting and Error handling page
	//==============================================================

	private void output(String out,HttpServletResponse response) throws IOException {
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(out);
	}

	private String errorPage = null;

	private static String regexEsc(String in) {
		if (in == null) return null;
		return in.replaceAll("\\$","\\\\\\$");
	}

	private String generateErrorPage(HttpServletRequest req,HttpServletResponse resp,String code,Throwable e) throws ServletException, IOException {
		if (errorPage == null) {
			errorPage = CSUtils.res2str("/kiwanami/web/cont/error.html");
		}
		String page = errorPage.replaceAll("&=error_code;",regexEsc(code));
		String message = regexEsc(e.getClass().getName()+" | "+e.getMessage());
		logger.debug("ERROR_MESSAGE:"+message);
		page = page.replaceAll("&=error_message;",message);
		
		String TAG = "&=error_stacktrace;";
		int ps = page.indexOf(TAG);
		if (ps != -1) {
			page = page.substring(0,ps) + trace2str(e) + page.substring(ps+TAG.length());
		}
		return page;
	}

	private String trace2str(Throwable e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		pw.flush();
		String ret = sw.toString();
		if (ret == null) {
			return e.getMessage();
		}
		return ret;
	}

	private String waitPage = null;

	private String generateWaitPage(HttpServletRequest req,HttpServletResponse resp) throws ServletException, IOException {
		if (waitPage == null) {
			waitPage = CSUtils.res2str("/kiwanami/web/cont/wait.html");
		}
		return waitPage;
	}

}
