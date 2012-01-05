package kiwanami.web.cont;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

public class CSUtils {

	private static Logger logger = Logger.getLogger(CSUtils.class);

	/**
	   指定したファイルをUTF8のテキストと仮定して読んで、文字列にして
	   返す。何かエラーが起きたら IOException か、RuntimeException に
	   して返す。
	*/
	public static String file2str(File path) throws IOException {
		InputStream is = new FileInputStream(path);
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(is,"UTF-8"));
			StringBuffer codeBuffer = new StringBuffer();
			while(true) {
				String line = in.readLine();
				if (line == null) break;
				codeBuffer.append(line).append("\n");
			}
			return codeBuffer.toString();
		} catch (UnsupportedEncodingException e) {
			String title = "Can not open the file: "+path.getPath();
			logger.warn(title);
			logger.warn(e);
			throw new RuntimeException(title);
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	/**
	   指定したクラスパス内のリソースをUTF8のテキストと仮定して読んで、
	   文字列にして返す。何かエラーが起きたら IOException か、
	   RuntimeException にして返す。
	*/
	public static String res2str(String uri) throws IOException {
		InputStream is = CSUtils.class.getResourceAsStream(uri);
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(is,"UTF-8"));
			StringBuffer codeBuffer = new StringBuffer();
			while(true) {
				String line = in.readLine();
				if (line == null) break;
				codeBuffer.append(line).append("\n");
			}
			return codeBuffer.toString();
		} catch (UnsupportedEncodingException e) {
			String title = "Can not open the resource file: "+uri;
			logger.warn(title);
			logger.warn(e);
			throw new RuntimeException(title);
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	public static void dispatchView(ISessionContext c,String url) throws ServletException,IOException {
        c.getRequest().setAttribute("module_uri",c.getRequest().getRequestURI());
        c.getRequest().setAttribute("_session_path",c.getRoutingInfo().getSessionPath());
        c.getRequest().setAttribute("_session_key",c.getSessionID());
		RequestDispatcher rd = c.getRequest().getRequestDispatcher(url);
		HttpServletRequest req = c.getRequest();
		if (req instanceof ContServletRequest) {
			ContServletRequest cr = (ContServletRequest)req;
			cr.sync2org();
			req = (HttpServletRequest)cr.getRequest();
		}
		rd.forward(req, c.getResponse());
		c.sleepThread();
	}
	
	public static String trace2str(Throwable e) {
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

   
	public static Object go(ServletContext app,String objectKey) {
		return ((IGlobalService)app.getAttribute("global_service")).get(objectKey);
	}

	public static Object bo(ServletContext app,String sessionKey,String objectkey) {
		UserSessionManager usm = (UserSessionManager)app.getAttribute("session_manager");
		UserSessionController usc = usm.getUserSession(sessionKey);
		if (usc == null) {
			return null;
		}
		return usc.getBranchService().get(objectkey);
	}

}
