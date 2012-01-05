package kiwanami.web.cont;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

public interface IScriptHelper {

	/**
	   Servletが管理しているセッションIDを返す
	 */
	public Object session_id();

	/**
	   モジュールのURIを返す。
	   状態遷移が終了するまで必ずここに飛ぶようにする。
	*/
	public String module_uri();

	/**
	   適当なログ出力先を返す
	*/
	public Logger log();

	/**
	   現在処理中のRequestオブジェクトを返す
	*/
	public HttpServletRequest request();

	/**
	   現在処理中のResponseオブジェクトを返す
	*/
	public HttpServletResponse response();

	/**
	   Requestパラメーターを取得する
	*/
	public String param(String key);

	/**
	   Requestオブジェクトの属性を取得する
	*/
	public Object get_attr(String key);

	/**
	   Requestオブジェクトに属性を設定する
	   （主にJSPのELで使う用）
	*/
	public void attr(String key,Object val);

	/**
	   コンテキストパス内の別処理系に委譲して一時処理を止める。
	   具体的には request.getRequestDispatcher(url).forward(rq,rs) をする。
	   @return Requestパラメーターの中に value があればそれを返す。
	   無ければ null を返す。
	*/
	public String show(String path);

	/**
	   スクリプト終了時に移動する先のURL
	*/
	public void exit(String url);

	/**
	   文字列としてスクリプトリソース読み込み
	*/
	public String load(String abstractPath) throws IOException;

	/**
	   URL情報を格納する
	   routingInfo.getSessionPath() : 継続セッションのURL
	*/
	public RoutingInfo routingInfo();

	/**
	   全継続セッションで共通のグローバルサービス
	*/
	public IGlobalService globalService();

	/**
	   一つのセッションの状態を他Servletと共有するサービス
	*/
	public IBranchService branchService();

	/**
	   IAjaxHandlerを管理するクラスを返す。
	*/
	public AjaxController ajaxController();

	/**
	   ブラウザでのデバッグ支援用オブジェクトの操作窓口提供用
	*/
	public void contextEvaluator(IContextEvaluator e);


}
