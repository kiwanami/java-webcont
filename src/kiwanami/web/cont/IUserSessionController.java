package kiwanami.web.cont;

/**
 * 継続の実装に必要なインタフェース定義
 * （まだ作成中）
 */
public interface IUserSessionController {

    public void init(Object sessionID,IUserSession userSession);
    
    public void continueSession();
    
    public void terminateSession();
    
}
