package kiwanami.web.cont;

public interface IUserSession {

    /**
     * 管理画面で表示されるセッション情報。
     */
    public String getSessionTitle();
    
    /**
     * ここで実行したいセッションの内容を記述する
     * 
     * @return セッション終了後のリダイレクト先のURL
     */
    public String session(ISessionContext context);

    /**
     * セッション開始前に呼ばれる。
     * この時点では response, request は使用できない。
     */
    public void initSession(ISessionContext context);

    /**
     * セッションが時間切れになって回収されるときに呼ばれる
     */
    public void timeoutSession();

    /**
     * セッション実行コンテキストでキャッチされていない例外がやってきたときに呼ばれる
     */
    public void uncaughtException(Throwable e) throws Throwable;

    /**
     * Exitや例外発生後、セッション終了時に呼ばれる
     */
    public void finishSession();

	/**
	 * セッションのコンテキスト内の情報を取得するためのインタフェース。
	 * 基本的には eval することを許可してデバッグに使う。
	 */
	public IContextEvaluator getContextEvaluator();
}
