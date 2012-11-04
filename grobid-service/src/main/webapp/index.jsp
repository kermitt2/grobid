<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <title>Welcome</title>
        <script src="resources/js/jquery-1.8.1.min.js"></script>
        <script src="resources/js/jquery.form.js"></script>
         <script src="resources/bootstrap/js/bootstrap.min.js"></script>
        <link href="resources/bootstrap/css/bootstrap.min.css" rel="stylesheet" media="screen">
    </head>
    
    <body>
    	<table>
			<thead><tr><td colspan="2"><%@include file='/WEB-INF/grobid/navigation/header.html'%></td></tr></thead>
			<tbody>
				<tr>
					<td valign="top"><%@include file='/WEB-INF/grobid/navigation/side-bar.html'%></td>
					<td rowspan="3">
						<%@include file='/WEB-INF/grobid/about/about.jsp'%>
						<%@include file='/WEB-INF/grobid/admin/admin.jsp'%>
						<%@include file='/WEB-INF/grobid/test_rest/test_rest.jsp'%>
					</td>
				</tr>
			</tbody>
		</table>    
				        
        <script type="text/javascript">
            $(document).ready(function() {
            	$("#subTitle").html("About");
            	$("#divRestI").hide();
    			$("#divAdmin").hide();
             });
        </script>
    </body>
</html>
