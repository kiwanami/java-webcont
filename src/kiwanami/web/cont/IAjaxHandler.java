package kiwanami.web.cont;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IAjaxHandler {

	/**
	   Ajax呼び出し（単に継続セッションを推進しない呼び出し）用の
	   インタフェース。出力は各自 HttpServletResponse に直接行うこと。
	*/
	public void call(String key,RoutingInfo rinfo,HttpServletRequest req,HttpServletResponse res) throws Throwable;

}
