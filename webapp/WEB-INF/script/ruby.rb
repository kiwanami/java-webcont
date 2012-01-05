loop {
  $web.show("/login.jsp")
  id = $web.param("id")
  pd = $web.param("passwd")
  if (id.nil? || id.size == 0) && (pd.nil? || pd.size == 0) then
	$web.attr("login_error","IDとパスワードを入力してください。")
  elsif id == "1" && pd == "a" then
	break
  else
	$web.attr("login_error","IDまたはパスワードが違います。")
  end
}
$web.show("/ok.jsp")
$web.show("/cont2.jsp")
$web.show("/cont3.jsp")
$web.show("/cont1.jsp")

$web.exit("/cont/exit.jsp")

