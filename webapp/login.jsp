<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.regex.*" %>
<html>
<head>
<title>サンプル</title>
</head>
<body>

<span style="color:red"> ${login_error} </span>

 <form name="login" method="post">
  <table cellpadding="0" cellspacing="0" class="data">
   <tr>
    <th class="data">ログインID：</th>
    <td class="data">
     <input type="text" name="id" size="40" maxlength="10" />
    </td>
   </tr>
   <tr>
    <th class="data">パスワード：</th>
    <td class="data">
     <input type="password" name="passwd" size="40" maxlength="10" />
    </td>
   </tr>
   <tr>
    <td colspan="2" style="text-align: center;">
     <input type="submit" value="ログイン" />
    </td>
   </tr>
  </table>
 </form>

<hr />
<span style="color:blue"> ${script_lang} </span>

</body>
</html>