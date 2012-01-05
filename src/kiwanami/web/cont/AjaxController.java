package kiwanami.web.cont;

import java.util.Arrays;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

public class AjaxController {

	private static Logger logger = Logger.getLogger(AjaxController.class);

	private HashMap<String,IAjaxHandler> handlers = new HashMap<String,IAjaxHandler>();

	public AjaxController() {
	}
	
	public void addHandler(String key,IAjaxHandler h) {
		handlers.put(key,h);
		logger.debug("Added the handler : "+key);
	}

	public void removeHandler(String key) {
		handlers.remove(key);
		logger.debug("Removed the handler : "+key);
	}

	public IAjaxHandler getHandler(String key) {
		IAjaxHandler h = handlers.get(key);
		return (h == null) ? handlers.get("*") : h;
	}

	public void clearHandlers() {
		handlers.clear();
		logger.debug("Cleared handlers.");
	}

	boolean tryAjaxSession(RoutingInfo rinfo,HttpServletRequest req,HttpServletResponse res) throws Throwable {
		int ps = rinfo.indexOf("ajax");
		logger.debug("Check: "+Arrays.toString(rinfo.options)+" : ajax? => "+ps);
		if (ps >= 0) {
			String key = rinfo.getOption(ps+1);
			logger.debug("Incoming AJAX request: "+key);
			IAjaxHandler h = getHandler(key);
			if (h != null) {
				h.call(key,rinfo,req,res);
			} else {
				logger.warn("No such AJAX handler: "+key);
				res.getWriter().print("No such AJAX handler ["+key+"]");
			}
			return true;
		} else {
			return false;
		}
	}	
}
