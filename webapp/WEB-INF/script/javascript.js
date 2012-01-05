while(true) {
    web.show("/login.jsp");
    var id = web.param("id");
    var pd = web.param("passwd");
    if ((id == null || id.length() == 0) && (pd == null || pd.length() == 0)) {
	    web.attr("login_error","IDとパスワードを入力してください。");
    } else if (id == "1" && pd == "a") {
	    break;
    } else {
	    web.attr("login_error","IDまたはパスワードが違います。");
    }
}
web.show("/ok.jsp");
web.show("/cont1.jsp");
web.show("/cont2.jsp");
web.show("/cont3.jsp");

web.exit("/cont/exit.jsp");
