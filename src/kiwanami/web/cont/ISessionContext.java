package kiwanami.web.cont;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ISessionContext {

	//アクセス中のHTTPオブジェクト（接続のたびに変わる）
    public HttpServletRequest getRequest();
    public HttpServletResponse getResponse();

	// 継続セッション識別用ID
    public Object getSessionID();

	// 接続のURL情報
    public RoutingInfo getRoutingInfo();

	// 継続セッションの情報
    public String getLastRemoteHost();
    public Date getStartedTime();
    public Date getLastLeftTime();

    // Ajaxリクエストの受け口
    public AjaxController getAjaxController();

	// セッションを一時停止してサーブレットに制御を返す
    public void sleepThread();

	// 一つの継続セッションの情報を外部プログラムから利用するためのサービス
	public IBranchService getBranchService();

	// 継続セッション間で共通で使うグローバルなサービス
	public IGlobalService getGlobalService();
}
