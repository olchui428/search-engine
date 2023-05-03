<%@ page import="project.*"%>
<html><head><meta http-equiv="Content-Type" content="text/html; charset=windows-1252"></head><body>
<%
String s = request.getParameter("txtname");
if(s != null) {
	String[] words = s.split(" ");
	for (String w : words) {
//		out.print(w + "<br>");

	}
recman_webpages = new DatabaseManager(DatabaseFiles.WEBPAGE_DB);
recman_inverted_title = new DatabaseManager(DatabaseFiles.INVERTED_INDEX_TITLE);
recman_inverted_content = new DatabaseManager(DatabaseFiles.INVERTED_INDEX_CONTENT);
Retriever retriever = new Retriever(s, recman_webpages, recman_inverted_title, recman_inverted_content);
ArrayList<Webpage> result = retriever.start();
for (Webpage w : result){
//	out.print(w.getScore() + "	");
//	out.print(w.getTitle() + "<br>");
//	out.print(w.getUrl() + "<br>");
//	out.print(w.getDate() + "<br>");
//	for (URL u : w.getChildLinks()){
//		out.print(u + "<br>");
w.toString();
}
%>


</body></html>
