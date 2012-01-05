package kiwanami.web.cont;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class UserSessionManager {

	private static Logger logger = Logger.getLogger(UserSessionManager.class);

	//SessionID -> セッションスレッドのマップ
	private HashMap<Object,UserSessionController> sessionMap = new HashMap<Object,UserSessionController>();

	//最大セッション数
	private int maxSessionNum = 1000;
	
	//この時間(msec)を過ぎたらセッションスレッドが回収される
	private long sessionTime = 1000*60*60;

	private boolean[] finishFlag = {false}; //trueでGCスレッド停止

	//shutdown時に実行するタスク
	private HashMap<String,Runnable> releaseTasks = new HashMap<String,Runnable>();

	private Thread gcThread;
	private Runnable gcLoop = new Runnable() {
			public void run() {
				logger.info("GC started.");
				try {
					while(true) {
						synchronized(finishFlag) {
							if (finishFlag[0]) {
								finishFlag.notifyAll();
								break;
							}
						}
						execGC();
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							logger.warn(e);
						}
					}
				} finally {
					logger.info("GC finished.");
				}
			}
		};

	UserSessionManager() {
		initGlobalService();
		gcThread = new Thread(gcLoop);
		gcThread.start();
	}

	public boolean isGCThreadAlive() {
		return gcThread.isAlive();
	}

	public void setMaxSessionNum(int max) {
		maxSessionNum = Math.abs(max);
	}

	public int getMaxSessionNum() {
		return maxSessionNum;
	}

	public void setSessionTime(int minutes) {
		sessionTime = 1000*60*Math.abs(minutes);
	}

	public long getSessionTime() {
		return sessionTime;
	}

	public UserSessionController[] getAllSessions() {
		UserSessionController[] us = new UserSessionController[sessionMap.size()];
		Iterator it=sessionMap.keySet().iterator();
		for(int i=0; it.hasNext(); i++) {
			us[i] = sessionMap.get(it.next());
		}
		return us;
	}

	private void execGC() {
		synchronized(sessionMap) {
			long current = System.currentTimeMillis();
			ArrayList<Object> list = new ArrayList<Object>();
			Iterator it = sessionMap.keySet().iterator();
			while(it.hasNext()) {
				UserSessionController s = sessionMap.get(it.next());
				if (s == null) continue;
				if ( (current - s.getLastLeftTime().getTime()) > sessionTime ) {
					logger.info("GC: "+s.getSessionID()+" : "+s.getLastLeftTime());
					list.add(s.getSessionID());
				}
			}
			for(int i=0;i<list.size();i++) {
				shutdownSession(list.get(i));
			}
		}
	}

	public void shutdownSession(Object sid) {
		synchronized(sessionMap) {
			UserSessionController s = sessionMap.get(sid);
			if (s == null) return;
			s.terminateSession();
			sessionMap.remove(sid);
		}
		logger.debug("Removed session: "+sid);
	}

	public void addShutdownTask(String key,Runnable task) {
		if (!releaseTasks.containsKey(key)) {
			releaseTasks.put(key,task);
			logger.debug("Added a release task ["+key+"]. count -> "+releaseTasks.size());
		}
	}

	public void shutdown() {
		logger.debug("Shutdown SessionManager...");
		synchronized (sessionMap) {
			Iterator it = sessionMap.keySet().iterator();
			while(it.hasNext()) {
				UserSessionController s = sessionMap.get(it.next());
				s.terminateSession();
			}
			sessionMap.clear();
		}
		try {
			synchronized(finishFlag) {
				finishFlag[0] = true;
				finishFlag.wait();
			}
		} catch (InterruptedException e) {
			logger.warn(e);
		}
		logger.debug("Shutdown all sessions.");

		//shutdown task
		for(Iterator<String> it = releaseTasks.keySet().iterator();it.hasNext();) {
			String key = it.next();
			Runnable r = releaseTasks.get(key);
			try {
				logger.debug("Executing shutdown task... : "+key);
				r.run();
				logger.info("Finished shutdown task : "+key);
			} catch (Throwable e) {
				logger.warn("An error was occurred durning executing the release task. ["+key+"]");
				logger.warn(e);
			}
		}
		logger.debug("Shutdown SessionManager ok.");
	}

	public void registerSession(UserSessionController s) throws MaximumSessionException {
		synchronized(sessionMap) {
			if (maxSessionNum <= sessionMap.size()) {
				throw new MaximumSessionException("Can not added a session because of too many sessions [max: "+sessionMap.size()+"].");
			}
			sessionMap.put(s.getSessionID(),s);
			logger.debug("Registered session["+sessionMap.size()+"]: sid="+s.getSessionID());
		}
	}

	public UserSessionController getUserSession(String ssid) {
		synchronized(sessionMap) {
			return sessionMap.get(ssid);
		}
	}

	public void removeSession(Object sid) {
		synchronized(sessionMap) {
			sessionMap.remove(sid);
			logger.debug("Removed session["+sessionMap.size()+"]: sid="+sid);
		}
	}

	//==================================================

	private IGlobalService globalService;

	private void initGlobalService() {
		final HashMap<String,Object> globalObjects = new HashMap<String,Object>();
		globalService = new IGlobalService() {
				public void set(String key,Object obj) {
					globalObjects.put(key,obj);
				}
				public Object get(String key) {
					return globalObjects.get(key);
				}
				public String[] keys() {
					return globalObjects.keySet().toArray(new String[globalObjects.size()]);
				}
				public void addShutdownTask(String key,Runnable task) {
					addShutdownTask(key,task);
				}
			};
	}

	public IGlobalService getGlobalService() {
		return globalService;
	}

}
