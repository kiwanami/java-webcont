package kiwanami.web.cont;

/**
   現在のURLの接続情報を格納するクラス。
   セッション開始のための情報と、セッション継続中の情報がある。
   
   ■共通
   path : (context path)/(servlet path)
   options : (module or session parameters)
   
   ■開始
   module : (module name)
   sessionId : (generated session id)
   
   ■継続中
   sessionId : (session id)

*/
public class RoutingInfo {

	//(context path)/(servlet path)
	public final String path;
	public final String[] options;

	//継続中のセッションは session id を持っている
	public final String sessionId;

	//開始モジュール名
	public final String module;

	RoutingInfo(String path,String m,String[] os,boolean isStart) {
		this.path = path;
		if (isStart) {
			this.module = m;
			this.sessionId = createSessionId();
		} else {
			this.module = null;
			this.sessionId = m;
		}
		this.options = os;
	}

	private static long uid = ((long)(Math.random()*100000000));

	private static synchronized String createSessionId() {
		long a = System.currentTimeMillis();
		return ""+a+""+(uid++);
	}

	public boolean isContinuedSession() {
		return module != null;
	}

	public String getOption(int index) {
		if (options == null || options.length == 0 || options.length <= index) {
			return null;
		}
		return options[index];
	}

	public int indexOf(String key) {
		if (options == null || key == null) {
			return -1;
		}
		for(int i=0;i<options.length;i++) {
			if (key.equals(options[i])) {
				return i;
			}
		}
		return -1;
	}

	public String getJoinedOptions() {
		if (options == null || options.length == 0) return "";
		StringBuffer sb = new StringBuffer();
		for(String a : options) {
			sb.append(a).append("/");
		}
		return sb.substring(0,sb.length()-1);
	}

	public String getSessionPath(String cpath) {
		return cpath+"/"+sessionId;
	}

	public String getSessionPath() {
		return getSessionPath(path);
	}

}
