//test script for javascript.js

access("/script/javascript.js");
assertPage("login.jsp");

param({id:"", passwd:""});
action();
assertPage("login.jsp");
assertValue("login_error","IDとパスワードを入力してください。");

param({id:"aaa", passwd:"bbb"});
action();
assertPage("login.jsp");
assertValue("login_error","IDまたはパスワードが違います。");

param({id:"1", passwd:"a"});
action();
assertPage("ok.jsp");

action();
assertPage("cont1.jsp");

action();
assertPage("cont2.jsp");

action();
assertPage("cont3.jsp");

action();
assertRedirect("/cont/exit.jsp");


