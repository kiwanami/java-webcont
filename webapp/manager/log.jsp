<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*,java.text.*,java.io.*" %>

<%
File file = new File("c:/usr/java/apache-tomcat-5.5.12/kiwanami-debug.log");
if (!file.exists()) {
    response.getWriter().println("{pos:0,text:[\"File not found.\"]}");
    return;
}
try {
	long size = file.length();
	long pos = 0;
	String lineStr = request.getParameter("pos");
	if (lineStr == null || lineStr.length() == 0) {
		pos = size;
	} else {
		pos = Long.parseLong(lineStr);
	}
	ArrayList<String> list = new ArrayList<String>();
	if (pos < size) {
		InputStream in = new FileInputStream(file);
		in.skip(pos);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		while(true) {
			String line = reader.readLine();
			if (line == null) {
				break;
			}
			list.add(line);
		}
	}
	StringBuffer ret = new StringBuffer();
	ret.append("{pos:"+file.length()+", text:[");
	for (String a : list) {
		ret.append("\"").append(java.net.URLEncoder.encode(a,"UTF-8")).append("\",");
	}
	ret.append("]}");
	response.getWriter().println(ret.toString());
} catch (Exception e) {
	StringBuffer ret = new StringBuffer();
	ret.append("{pos:"+file.length()+", error:\""+ e.getClass().getName()+" : "+e.getMessage() +"\"}");
	response.getWriter().println(ret.toString());
}
%>
