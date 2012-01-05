package kiwanami.web.cont;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import java.util.Map;


/**
 * 基本的な継続のコントロールを行う。
 */
public class UserSessionController {

	private static Logger logger = Logger.getLogger(UserSessionController.class);

	private IGlobalService globalService;
	private IBranchService branchService;
	private IUserSession userSession;
    
    private Date startedTime = new Date();
	private Date lastLeftTime = new Date();
	private final String sessionID;

	private final String browserSessionID;
	private String lastRemoteHost;
    
    private Map lastParameterMap; //postでredirectさせるときに保存

	//true: session thread 準備完了, false: session thread 準備中 or 動作中
	private boolean[] threadLock = {false};
	private Thread sessionThread;
	private boolean finishedFlag = false;//もう終了していい場合は true

    private AjaxController ajaxController = new AjaxController();
    
	//スレッド間データ受け渡し用
	protected RoutingInfo routingInfo;
	protected ContServletRequest request;
	protected HttpServletResponse response;
	protected Throwable exception;

	//リダイレクトして別のページに出る場合
	private String redirectUrl;

	UserSessionController(String bsid,String ssid,IUserSession us,IGlobalService gs) {
		browserSessionID = bsid;
		sessionID = ssid;
        userSession = us;
		globalService = gs;
		initBranchService();
		logger.debug("A new session was created : "+sessionID);
		sessionThread = new Thread(sessionController);
		sessionThread.start();
	}

    public void setLastParameters(Map m) {
        logger.debug("Set Last Parameters : "+m.size());
        lastParameterMap = m;
    }

	private void initBranchService() {
		final HashMap<String,Object> hash = new HashMap();
		branchService = new IBranchService() {
				public void set(String key,Object obj) {
					hash.put(key,obj);
				}
				public Object get(String key) {
					return hash.get(key);
				}
				public String[] keys() {
					return hash.keySet().toArray(new String[hash.size()]);
				}
			};
	}

	public IBranchService getBranchService() {
		return branchService;
	}
    
    private ISessionContext sessionContext = new ISessionContext() {
        public AjaxController getAjaxController() {
            return ajaxController;
        }
        public Date getLastLeftTime() {
            return lastLeftTime;
        }
        public String getLastRemoteHost() {
            return lastRemoteHost;
        }
        public HttpServletRequest getRequest() {
            return request;
        }
        public HttpServletResponse getResponse() {
            return response;
        }
        public RoutingInfo getRoutingInfo() {
            return routingInfo;
        }
        public String getSessionID() {
            return sessionID;
        }
        public Date getStartedTime() {
            return startedTime;
        }
		public IGlobalService getGlobalService() {
			return globalService;
		}
		public IBranchService getBranchService() {
			return branchService;
		}
        public void sleepThread() {
            switchThread();
        }
    };

	public String getBrowserSessionID() {
		return browserSessionID;
	}
	
	public String getSessionID() {
		return sessionID;
	}

	public Date getStartedTime() {
		return startedTime;
	}

	public Date getLastLeftTime() {
		return lastLeftTime;
	}

	public String getLastRemoteHost() {
		return lastRemoteHost;
	}

	public String getSessionTitle() {
		return userSession.getSessionTitle();
	}

	public void setRedirectURL(String a) {
		redirectUrl = a;
	}

	public IContextEvaluator getContextEvaluator() {
		return userSession.getContextEvaluator();
	}

	private Runnable sessionController = new Runnable() {
			public void run() {
				logger.debug("Session was created.");
				userSession.initSession(sessionContext);
				logger.debug("Session was initialized.");
				try {
					synchronized(threadLock) {
						threadLock[0] = true;
						threadLock.wait();
					}
				} catch (InterruptedException e) {
					logger.warn(e);
				}
				try {
					logger.debug("Session is started.");
					redirectUrl = userSession.session(sessionContext);
				} catch (Throwable e) {
					logger.debug("An exception was occurred.",e);
					try {
						userSession.uncaughtException(e);
					} catch (Throwable ex) {
						exception = ex;
					}
				} finally {
					finishedFlag = true;
					userSession.finishSession();
					logger.debug("Session was finished.");
					synchronized(threadLock) {
						threadLock.notifyAll();
					}
				}
			}
		};

	/**
	 * ContinuationServletから起こされる為に呼ばれる
	 */
	boolean continueSession(RoutingInfo rinfo,ContServletRequest req,HttpServletResponse res) throws Throwable {
		request = req;
		response = res;
		routingInfo = rinfo;
		lastRemoteHost = request.getRemoteAddr();

        if (lastParameterMap != null) {
            request.overrideParameter(lastParameterMap);
            logger.debug("Override Parameters : "+lastParameterMap.size());
            lastParameterMap = null;
        }

        if (ajaxController.tryAjaxSession(rinfo, req, res)) {
            return false;
        }
		try {
			while(true) {
				synchronized(threadLock) {
					if (threadLock[0] == true) {
						threadLock.notifyAll();
						logger.debug("Waiting for session work...");
						threadLock.wait();
						break;
					}
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					logger.warn(e);
				}
			}
		} catch (InterruptedException e) {
			logger.warn(e);
		}
		logger.debug("Supporter waked up.");

		if (exception != null) {
			throw exception;
		}

		if (redirectUrl != null) {
			try {
				res.sendRedirect(redirectUrl);
			} catch (IOException e) {
				logger.warn("Can not redirect to ["+redirectUrl+"]",e);
			}
			finishedFlag = true;
		}

		return finishedFlag;
	}
	
	/**
	 * セッションスレッドを停止して、Servletスレッドを起こす
	 * セッションスレッドは後で呼び出されるまで待機する
	 */
	public void sleepSession() {
		logger.debug("Session will sleep.");
		switchThread();
	}

	private void switchThread() {
		request = null;
		response = null;
		lastLeftTime = new Date();
		synchronized(threadLock) {
			threadLock.notifyAll();
		}
		if (finishedFlag) {
			//強制終了させるときは素通りさせる為に寝かせない
			logger.debug("The session switch was passed...");
			return;
		}
		try {
			synchronized(threadLock) {
				logger.debug("Session is waiting for next access...");
				threadLock.wait();
			}
		} catch (InterruptedException e) {
			logger.warn(e);
		}
		logger.debug("Session waked up.");
	}

	/**
	 * GCによって強制終了する為に呼ばれる
	 *
	 */
	void terminateSession() {
		logger.debug("Session will be terminated by GC.");
		finishedFlag = true;
		synchronized(threadLock) {
			threadLock.notifyAll();
		}
	}

	/**
	 * セッションが時間切れになって回収されるときに呼ばれる
	 */
	protected void timeoutSession() {
        userSession.timeoutSession();
	}

}
