<%@ page import="java.util.List"%>
<%@ page import="org.grobid.service.util.GrobidProperty"%>
<%@ page import="org.grobid.service.util.GrobidPropertiesUtil"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<table>
  <tr><td>property<td>value</td></tr>


<% List<GrobidProperty> props = GrobidPropertiesUtil.getAllPropertiesList(); %>

<% for(GrobidProperty currProp : props) {%>
  <tr>
  	<td><%=currProp.getKey() %></td>
    <td><%=currProp.getValue() %></td>
  </tr>
<%} %>

</table>