var id = web.param("id");
var passwd = web.param("passwd");

while(1) {
    web.log().info("LOGIN----");
    web.log().info("id: "+id);
    web.log().info("pass: "+passwd);

    if (id != "1" || passwd != "a") {
        web.attr("login_error","ログイン名かパスワードが違います。");
        web.exit("/cont/outer-login.jsp");
        break;
    }

    web.attr("message","id: "+web.param("id") +" / passwd: "+ web.param("passwd"));
    web.show("/message.jsp");

    web.exit("/cont/exit.jsp");
    break;
}