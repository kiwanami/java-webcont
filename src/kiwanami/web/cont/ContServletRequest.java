package kiwanami.web.cont;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.log4j.Logger;
import java.util.Arrays;

public class ContServletRequest extends HttpServletRequestWrapper {

	private static Logger logger = Logger.getLogger(ContServletRequest.class);

	private HashMap attributes = new HashMap();

    private Map parentMap = null;

	public ContServletRequest(HttpServletRequest req) {
		super(req);
		sync2cont();
	}

    public void overrideParameter(Map map) {
        parentMap = map;
    }

    public String getParameter(String n) {
        if (parentMap != null) {
            Object v = parentMap.get(n);
            if (v != null) {
                if (v instanceof String[]) {
                    logger.debug("PARAM["+n+"] => ["+Arrays.toString((String[])v)+"]");
                    return ((String[])v)[0];
                } else {
                    //logger.debug("PARAM["+n+"] => ["+v.toString()+"] / ("+v.getClass().getName()+")");
                }
            } else {
                //logger.debug("PARAM["+n+"] => null");
            }
            if (v != null) return (String)v;
        }
        return super.getParameter(n);
    }

    public Map getParameterMap() {
        HashMap ret = new HashMap();
        Map org = super.getParameterMap();
        for(Object k : org.keySet()) {
            ret.put(k,org.get(k));
        }
        return ret;
    }

    public Enumeration<String> getParameterNames() {
        Vector<String> list = new Vector<String>();
        if (parentMap != null) { 
            for(Object k : parentMap.keySet()) {
                list.add((String)k);
            }
        }
        Enumeration e = super.getParameterNames();
        while(e.hasMoreElements()) {
            Object obj = e.nextElement();
            if (!list.contains(obj)) {
                list.addElement((String)obj);
            }
        }
        return list.elements();
    }

	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	public Enumeration getAttributeNames() {
		final Iterator it = attributes.keySet().iterator();
		return new Enumeration<String>() {
				public String nextElement() {
					Object obj = it.next().toString();
					if (obj == null) return null;
					return obj.toString();
				}
				public boolean hasMoreElements() {
					return it.hasNext();
				}
			};
	}

	public void setAttribute(String key,Object obj) {
		attributes.put(key,obj);
	}

	public void sync2org() {
		Iterator<String> keys = attributes.keySet().iterator();
		while(keys.hasNext()) {
			String key = keys.next();
			Object obj = attributes.get(key);
			super.setAttribute(key,obj);
			logger.debug("SYNC2ORG : ["+key+"] -> "+obj);
		}
	}

	public void sync2cont() {
		ArrayList<String> keys = Collections.list(super.getAttributeNames());
		for(String key : keys) {
			Object obj = super.getAttribute(key);
			attributes.put(key,obj);
			logger.debug("SYNC2CONT : ["+key+"] -> "+obj);
		}
	}

}
