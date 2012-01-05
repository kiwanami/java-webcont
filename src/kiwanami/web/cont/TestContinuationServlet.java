package kiwanami.web.cont;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.bsf.BSFManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


public class TestContinuationServlet {

    private static Logger logger = Logger.getLogger(TestContinuationServlet.class);

	private HashMap<String,IUserSessionFactory> moduleFactoryMap = 
        new HashMap<String,IUserSessionFactory>();//name -> factory

    //new File("/home/sakurai/nwork/libs/cont/WEB-INF/web.xml")
    public TestContinuationServlet(File webXml) {
        if (webXml == null) {
            String curPath = System.getProperty("user.dir");
            int ps = curPath.indexOf("WEB-INF");
            if (ps == -1) {
                webXml = new File(curPath+"/WEB-INF/web.xml");
            } else {
                webXml = new File(curPath.substring(0,ps) + "/WEB-INF/web.xml");
            }
        }
        if (!webXml.exists()) {
            throw new RuntimeException("web.xml not found. : "+webXml.getPath());
        }
        logger.info("TestRunner init.");
        initModules(webXml);
    }

    public ISessionTestContext getTestSession() {
        return new ASessionTestContext();
    }

    public void initModules(File webXml) {
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(webXml);

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            xpath.setNamespaceContext(new NamespaceContext(){
                    public String getNamespaceURI(String prefix) {
                        if (prefix == null) throw new IllegalArgumentException();
                        else if ("j".equals(prefix)) return "http://java.sun.com/xml/ns/j2ee";
                        return null;
                    }
                    public String getPrefix(String namespaceURI) {
                        if (namespaceURI == null) throw new IllegalArgumentException();
                        else if ("http://java.sun.com/xml/ns/j2ee".equals(namespaceURI)) return "j";
                        return null;
                    }
                    public Iterator<String> getPrefixes(String namespaceURI) {
                        if (namespaceURI == null) throw new IllegalArgumentException();
                        else if ("http://java.sun.com/xml/ns/j2ee".equals(namespaceURI)) return Arrays.asList("j").iterator();
                        return null;
                    }
                });

            String scriptPath = xpath.evaluate("/j:web-app/j:servlet/j:init-param[child::j:param-name/text()=\"script_path\"]/j:param-value/text()",doc);
            logger.debug("script_path ["+scriptPath+"]");
            initParam.put("script_path",scriptPath);

            String moduleParams = xpath.evaluate("/j:web-app/j:servlet/j:init-param[child::j:param-name/text()=\"modules\"]/j:param-value/text()",doc);
            logger.debug("module param ["+moduleParams+"]");
            initParam.put("modules",moduleParams);
        
            if (moduleParams == null) {
                logger.error("No module.");
            } else {
                String[] modules = moduleParams.split("\\|");
                for(int i=0;i<modules.length;i++) {
                    String[] els = modules[i].trim().split(",");
                    if (els.length >= 2) {
                        String name = els[0];
                        String cls = els[1];
                        String opt = (els.length >= 3) ? els[2] : null;
                        addModule(name,cls,opt);
                    } else {
                        logger.warn("Wrong session parameter: "+modules[i]);
                    }
                }
            }
            addModule("script","kiwanami.web.cont.ScriptSessionFactory",null);

            BSFManager.registerScriptingEngine
                ("groovy", "org.codehaus.groovy.bsf.GroovyEngine", 
                 new String[] { "groovy", "gy" }
                );
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            throw new RuntimeException("Can not initialize modules.");
        }
    }

    private void addModule(String name,String className,String option) {
		try {
			Class klass = Class.forName(className);
			IUserSessionFactory factory = (IUserSessionFactory)klass.newInstance();
			factory.init(servletConfig,option);
			moduleFactoryMap.put(name,factory);
            logger.debug("AddModule: "+name+" / "+className);
		} catch (Exception e) {
			logger.warn("Class ["+className+"] was not found.",e);
		}
    }

	/**
	   request uri から、(module名)と(option path)を取ってくる
	   "modele" => UserSession担当モジュール名
	   "options" => モジュール名以降のパスの配列。スラッシュは含まない。
	*/
	private RoutingInfo parseRoutingInfo(String uri,boolean isStart) {
        //  uri -> /(module)/(option1)/(option2)/(option3)/(option4)/(option5)/(option6)
        Matcher m = Pattern.compile("^\\/([^\\/]+)(\\/[^\\/]+)?(\\/[^\\/]+)?(\\/[^\\/]+)?(\\/[^\\/]+)?(\\/[^\\/]+)?(\\/[^\\/]+)?").matcher(uri);
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
            return new RoutingInfo("",module,opts.toArray(new String[0]),isStart);
        } else {
            logger.warn("Can not get a module name. : "+uri);
        }
		throw new RuntimeException("Invalid path structure: "+uri);
	}

    
    private ServletContext servletContext = new TestServletContext(null);
    private ServletConfig servletConfig = new TestServletConfig(servletContext);
    private HashMap<String,String> initParam = new HashMap<String,String>();

    private IGlobalService globalService = new IGlobalService() {
            private HashMap<String,Object> objects = new HashMap<String,Object>();
            public void set(String key,Object obj) {
                objects.put(key,obj);
            }
            public Object get(String key) {
                return objects.get(key);
            }
            public String[] keys() {
                return objects.keySet().toArray(new String[objects.size()]);
            }
            public void addShutdownTask(String key,Runnable task) {
                //
            }
        };

    class ASessionTestContext implements ISessionTestContext {

        private UserSessionController sessionController = null;
        private TestRequest request = null;
        private TestResponse response = null;

        public UserSessionController access(String path) throws Throwable {
            RoutingInfo routingInfo = parseRoutingInfo(path,true);
            IUserSessionFactory factory = moduleFactoryMap.get(routingInfo.module);
            if (factory == null) {
                throw new RuntimeException("Module not found: "+path);
            }
            sessionController = new UserSessionController("browser-session-id","test-session-id",factory.getInstance(routingInfo.options), globalService);

            request = new TestRequest();
            response = new TestResponse();

            doAction(path);

            return sessionController;
        }
            
        public HttpServletRequest getRequest() {
            return request;
        }
        public HttpServletResponse getResponse() {
            return response;
        }
            
        public void doAction(String path) throws Throwable {
            response.clear();
            request.clearAttrs();
            RoutingInfo routingInfo = parseRoutingInfo(path,false);
            sessionController.continueSession(routingInfo,new ContServletRequest(request),response);
            request.clearParams();
        }
            
        public String getForwardPath() {
            return request.getForwardPath();
        }
        public String getRedirectPath() {
            return response.getRedirectUrl();
        }
    }
    

    //==================================================

    class TestServletContext implements ServletContext {

        private HashMap attrs = new HashMap();
        private TestRequest request;

        TestServletContext(TestRequest r) {
            request = r;
        }

		public Object getAttribute(String arg0) {
			return attrs.get(arg0);
		}

		public Enumeration getAttributeNames() {
			return Collections.enumeration(attrs.keySet());
		}

		public ServletContext getContext(String arg0) {
			return null;
		}

		public String getInitParameter(String arg0) {
			return initParam.get(arg0);
		}

		public Enumeration getInitParameterNames() {
			return Collections.enumeration(initParam.keySet());
		}

		public int getMajorVersion() {
			return 0;
		}

		public String getMimeType(String arg0) {
			return null;
		}

		public int getMinorVersion() {
			return 0;
		}

		public RequestDispatcher getNamedDispatcher(String arg0) {
			return null;
		}

		public String getRealPath(String arg0) {
            String path = System.getProperty("user.dir");
            int ps = path.indexOf("WEB-INF");
            if (ps == -1) {
                return path+"/"+arg0;
            } else {
                return path.substring(0,ps) + "/" + arg0;
            }
		}

		public RequestDispatcher getRequestDispatcher(String arg0) {
            return request.getRequestDispatcher(arg0);
		}

		public URL getResource(String arg0) throws MalformedURLException {
			return this.getClass().getResource(arg0);
		}

		public InputStream getResourceAsStream(String arg0) {
			return this.getClass().getResourceAsStream(arg0);
		}

		public Set getResourcePaths(String arg0) {
			return null;
		}

		public String getServerInfo() {
			return "";
		}

		public Servlet getServlet(String arg0) throws ServletException {
			return null;
		}

		public String getServletContextName() {
			return "test-servlet";
		}

		public Enumeration getServletNames() {
			return null;
		}

		public Enumeration getServlets() {
			return null;
		}

		public void log(String arg0) {
            logger.info(arg0);
		}

		public void log(Exception arg0, String arg1) {
            logger.info(arg1,arg0);
		}

		public void log(String arg0, Throwable arg1) {
            logger.info(arg0,arg1);
		}

		public void removeAttribute(String arg0) {
            attrs.remove(arg0);
		}

		public void setAttribute(String arg0, Object arg1) {
            attrs.put(arg0,arg1);
		}
    }

    class TestServletConfig implements ServletConfig {

        private ServletContext servletContext;

        TestServletConfig(ServletContext c) {
            servletContext = c;
        }

		public String getInitParameter(String arg0) {
			return initParam.get(arg0);
		}

		public Enumeration getInitParameterNames() {
			return Collections.enumeration(initParam.keySet());
		}

		public ServletContext getServletContext() {
			return servletContext;
		}

		public String getServletName() {
			return null;
		}
    }

    class TestRequest implements HttpServletRequest,IVariableParameter {

        private HashMap<String,String> param = new HashMap<String,String>();
        private HashMap<String,String> attrs = new HashMap<String,String>();
        private String forwardPath = null;
        private String includePath = null;

        public String getForwardPath() {
            return forwardPath;
        }

        public void clearParams() {
            param.clear();
        }

        public void clearAttrs() {
            forwardPath = null;
            attrs.clear();
        }

        public void setParameter(String k,Object v) {
            param.put(k,String.valueOf(v));
        }
        
		public String getAuthType() {
			return null;
		}

		public String getContextPath() {
			return "/";
		}

		public Cookie[] getCookies() {
			return null;
		}

		public long getDateHeader(String arg0) {
			return System.currentTimeMillis();
		}

		public String getHeader(String arg0) {
			return null;
		}

		public Enumeration getHeaderNames() {
			return null;
		}

		public Enumeration getHeaders(String arg0) {
			return null;
		}

		public int getIntHeader(String arg0) {
			return 0;
		}

		public String getMethod() {
			return "GET";
		}

		public String getPathInfo() {
			return "TestRequest.getPathInfo";
		}

		public String getPathTranslated() {
			return "TestRequest.getPathTranslated";
		}

		public String getQueryString() {
			return "TestRequest.getQueryString";
		}

		public String getRemoteUser() {
			return "TestRequest.getRemoteUser";
		}

		public String getRequestURI() {
			return "Test.getRequestURI";
		}

		public StringBuffer getRequestURL() {
			return new StringBuffer("Test.getRequestURL");
		}

		public String getRequestedSessionId() {
			return "";
		}

		public String getServletPath() {
			return null;
		}

		public HttpSession getSession() {
			return null;
		}

		public HttpSession getSession(boolean arg0) {
			return null;
		}

		public Principal getUserPrincipal() {
			return null;
		}

		public boolean isRequestedSessionIdFromCookie() {
			return false;
		}

		public boolean isRequestedSessionIdFromURL() {
			return false;
		}

		public boolean isRequestedSessionIdFromUrl() {
			return false;
		}

		public boolean isRequestedSessionIdValid() {
			return false;
		}

		public boolean isUserInRole(String arg0) {
			return false;
		}

		public Object getAttribute(String key) {
			return attrs.get(key);
		}

		public Enumeration getAttributeNames() {
			return Collections.enumeration(attrs.keySet());
		}

		public String getCharacterEncoding() {
			return "UTF-8";
		}

		public int getContentLength() {
			return 0;
		}

		public String getContentType() {
			return "text/html";
		}

		public ServletInputStream getInputStream() throws IOException {
			return null;
		}

		public String getLocalAddr() {
			return "127.0.0.0";
		}

		public String getLocalName() {
			return "localhost";
		}

		public int getLocalPort() {
			return 0;
		}

		public Locale getLocale() {
			return Locale.getDefault();
		}

		public Enumeration getLocales() {
			return null;
		}

		public String getParameter(String key) {
            return param.get(key);
		}

		public Map getParameterMap() {
			return param;
		}

		public Enumeration getParameterNames() {
			return Collections.enumeration(param.keySet());
		}

		public String[] getParameterValues(String arg0) {
			return null;
		}

		public String getProtocol() {
			return "http";
		}

		public BufferedReader getReader() throws IOException {
			return null;
		}

		public String getRealPath(String arg0) {
			return null;
		}

		public String getRemoteAddr() {
			return "127.0.0.1";
		}

		public String getRemoteHost() {
			return "localhost";
		}

		public int getRemotePort() {
			return 80;
		}

		public RequestDispatcher getRequestDispatcher(final String arg0) {
			RequestDispatcher disp = new RequestDispatcher() {
                    public void forward(ServletRequest request, ServletResponse response) throws ServletException, java.io.IOException {
                        forwardPath = arg0;
                    }
                    public void include(ServletRequest request, ServletResponse response) throws ServletException, java.io.IOException {
                        includePath = arg0;
                    }
                };
			return disp;
		}

		public String getScheme() {
			return "http";
		}

		public String getServerName() {
			return "localhost";
		}

		public int getServerPort() {
			return 80;
		}

		public boolean isSecure() {
			return false;
		}

		public void removeAttribute(String arg0) {
			attrs.remove(arg0);
		}

		public void setAttribute(String arg0, Object arg1) {
            attrs.put(arg0,String.valueOf(arg1));
		}

		public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {}
        
    }

    class TestResponse implements HttpServletResponse {

        private String redirectUrl;

        public String getRedirectUrl() {
            return redirectUrl;
        }

        public void clear() {
            redirectUrl = null;
        }

		public void addCookie(Cookie arg0) {
		}

		public void addDateHeader(String arg0, long arg1) {
		}

		public void addHeader(String arg0, String arg1) {
		}

		public void addIntHeader(String arg0, int arg1) {
		}

		public boolean containsHeader(String arg0) {
			return false;
		}

		public String encodeRedirectURL(String arg0) {
			return arg0;
		}

		public String encodeRedirectUrl(String arg0) {
			return arg0;
		}

		public String encodeURL(String arg0) {
			return arg0;
		}

		public String encodeUrl(String arg0) {
			return arg0;
		}

		public void sendError(int arg0) throws IOException {
		}

		public void sendError(int arg0, String arg1) throws IOException {
		}

		public void sendRedirect(String arg0) throws IOException {
            redirectUrl = arg0;
		}

		public void setDateHeader(String arg0, long arg1) {
		}

		public void setHeader(String arg0, String arg1) {
		}

		public void setIntHeader(String arg0, int arg1) {
		}

		public void setStatus(int arg0) {
		}

		public void setStatus(int arg0, String arg1) {
		}

		public void flushBuffer() throws IOException {
		}

		public int getBufferSize() {
			return 0;
		}

		public String getCharacterEncoding() {
			return null;
		}

		public String getContentType() {
			return null;
		}

		public Locale getLocale() {
			return null;
		}

		public ServletOutputStream getOutputStream() throws IOException {
			return null;
		}

		public PrintWriter getWriter() throws IOException {
			return null;
		}

		public boolean isCommitted() {
			return false;
		}

		public void reset() {
		}

		public void resetBuffer() {
		}

		public void setBufferSize(int arg0) {
		}

		public void setCharacterEncoding(String arg0) {
		}

		public void setContentLength(int arg0) {
		}

		public void setContentType(String arg0) {
		}

		public void setLocale(Locale arg0) {
		}
        
    }
}
