<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*,java.text.*" %>
<%@ page import="kiwanami.web.cont.*" %>

<%
UserSessionManager manager = (UserSessionManager)pageContext.getServletContext().getAttribute("session_manager");
SimpleDateFormat DF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
DecimalFormat NFP = new DecimalFormat("##0.0%");
DecimalFormat NFC = new DecimalFormat("###,###,###,##0");
if (manager == null) {
%>
	<p>Not initialized continuation manager...</p>
<%
} else {
	ThreadGroup gr = Thread.currentThread().getThreadGroup();
	try {
		while(true) {
			if (gr.getParent() != null) {
				gr = gr.getParent();
			} else {
				break;
			}
		}
	} catch (SecurityException e) {
	}
%>

<table width="%98" class="data">
<tr>
	<th>最大継続セッション数:</th><td> <%= manager.getMaxSessionNum() %></td>
	<th>セッション寿命:</th><td> <%= (manager.getSessionTime()/1000/60) %> 分</td>
</tr>
<tr>
	<th>現在の継続セッション数:</th><td> <%= manager.getAllSessions().length %></td>
	<th>Java総スレッド数:</th><td> <%=  gr.activeCount() %></td>
</tr>
<tr>
<% Runtime rt = Runtime.getRuntime(); %>
	<th>メモリ: </th><td colspan="3">
use <%= NFC.format(rt.totalMemory()/1024) %> Kb / 
free <%= NFC.format(rt.freeMemory()/1024) %> Kb / 
max <%= NFC.format(rt.maxMemory()/1024) %> Kb
(<%= NFP.format((float)rt.totalMemory() / (float)rt.maxMemory()) %> use)</td>
</tr>
</table>

<table border="1" width="98%" class="data">
<tr> <th>No.</th> <th>セッションID</th> <th>IPアドレス</th> <th>実行モジュール</th> <th>開始日時</th> <th>最近の接続</th> <th>寿命(分)</th> <th>終了</th><th>Eval</th>
</tr>
<%
	UserSessionController[] ss = manager.getAllSessions();
	for(int i=0;i<ss.length;i++) {
		String id = ss[i].getSessionID();
		String ip = ss[i].getLastRemoteHost();
		String module = ss[i].getSessionTitle();
		String sd = DF.format(ss[i].getStartedTime());
		String ld = DF.format(ss[i].getLastLeftTime());
		long ttg = (manager.getSessionTime() - (System.currentTimeMillis() - ss[i].getLastLeftTime().getTime()))/1000/60;
		boolean eval = ss[i].getContextEvaluator() != null;
%>
<tr>
	<td><%= (i+1)%></td>
	<td><%= id.substring(0,Math.min(10,id.length()))+"..." %></td>
	<td><%= ip %></td>
	<td><%= module %></td>
	<td><%= sd.toString() %></td>
	<td><%= ld.toString() %></td>
	<td><%= ttg %></td>
	<td><a href="delete.jsp?sid=<%= id %>">X</a></td>
	<% if (eval) { %>
       <td><a target="_blank" href="evaluation.jsp?sid=<%= id %>">[eval]</a></td>
    <% } else { %>
       <td></td>
    <% } %>
</tr>
<%
	}
%>
</table>
<%
}
%>
