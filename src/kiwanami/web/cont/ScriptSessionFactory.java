package kiwanami.web.cont;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletConfig;

import org.apache.log4j.Logger;

public class ScriptSessionFactory implements IUserSessionFactory {

	private static Logger logger = Logger.getLogger(ScriptSessionFactory.class);
	private File scriptBasePath;

	public ScriptSessionFactory() {
	}

	private static String join(String[] as) {
		if (as == null || as.length == 0) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<as.length;i++) {
			if (as[i].indexOf("..") >= 0) {
				throw new RuntimeException("Wrong option lines.");
			}
			sb.append(as[i]).append("/");
		}
		return sb.substring(0,sb.length()-1);
	}

	public IUserSession getInstance(String[] options) {
		logger.debug("Create new session: options="+Arrays.toString(options));
		File scriptPath = new File(scriptBasePath,join(options));
		try {
			return new ScriptSession(logger,scriptPath,
									 CSUtils.file2str(scriptPath));
		} catch (IOException e) {
			logger.error("Can not initialize the script ["+scriptPath.getName()+"]",e);
			throw new RuntimeException(e);
		}
	}

	public void init(ServletConfig config,String option) {
		String base = config.getInitParameter("script_path");
		String contextPath = config.getServletContext().getRealPath("/");
		scriptBasePath = new File(contextPath+"/"+base);
		logger.debug("init script base : "+scriptBasePath.getPath());
	}

}
