package kiwanami.web.cont;

/**
 * Scriptのコンテキスト内の情報を取得するためのインタフェース。
 * 基本的には eval することを許可してデバッグに使う。
 */
public interface IContextEvaluator {

	/**
	 * ブラウザの専用画面から呼ばれる特殊な処理。
	 * 思いっきりセキュリティーホールになるため、デバッグのみ許可すること。
	 * 
	 * @param code 評価できるコードとか、情報取得のヒントになるような情報。
	 * @return その結果を文字列で返す。JSONやXMLなどでエンコードすると便利。
	 */
	public String eval(String code);

}
