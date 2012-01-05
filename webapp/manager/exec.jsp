<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*,java.text.*" %>
<%@ page import="kiwanami.web.cont.*" %>
<%
String sid = request.getParameter("sid");
String titleId = "";
String result = "";
if (titleId != null) {
	titleId = titleId.substring(0,Math.min(10,titleId.length()))+"...";
	String code = request.getParameter("code");
	if (code != null) {
		UserSessionManager sm = (UserSessionManager)(config.getServletContext().getAttribute("session_manager"));
		UserSessionController us = sm.getUserSession(sid);
		if (us != null && us.getContextEvaluator() != null) {
			IContextEvaluator ce = us.getContextEvaluator();
			try {
				result = ce.eval(code);
			} catch (Exception e) {
				result = CSUtils.trace2str(e);
			}
		} else {
			result = "\"Can not find UserSession nor ContextEvaluator.\"";
		}
	} else {
		result = "\"No code...\"";
	}
} else {
	result = "\"No session id.\"";
}
%>
<%= result %>
