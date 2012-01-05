<%@ page import="java.util.*,java.text.*" %>
<%@ page import="kiwanami.web.cont.*" %>

<%
UserSessionManager manager = (UserSessionManager)pageContext.getServletContext().getAttribute("session_manager");
if (manager != null) {
	String sid = request.getParameter("sid");
	if (sid != null && manager.getUserSession(sid) != null) {
		manager.shutdownSession(sid);
	}
}

response.sendRedirect("index.jsp");
%>
