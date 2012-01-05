<%@ page contentType="text/html; charset=UTF-8" %>
<html>
<head>
<title>メッセージ表示</title>
</head>
<body>

<span style="color:red;font-size:250%;"> ${message} </span>

<a href="${module_uri}?value=ok">終了</a>

<hr />
<span style="color:blue"> ${script_lang} </span>
</body>
</html>