<%@page import="org.grobid.service.GrobidRestService"%>
<%@ page import="org.grobid.core.cypher.SHA1"%> 
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <title>Welcome</title>
        <script src="resources/js/jquery-1.8.1.min.js">
        </script>
    </head>
    
    <body>
        <h1>Welcome to grobid</h1>
        <h2>Access to grobid documentation</h2>
        <a href='<%=request.getContextPath()+"/grobid"%>'>grobid documentation</a>
        <h2>Access to grobid params</h2>
        <h3>Log in:</h3>
        <form method="post" action='<%=request.getContextPath()+"/admin"%>'>
            <input type="password" name="sha1"/>
            <input type="submit" value="Log" />
        </form>
        <h3>Process sha1:</h3>
        <form method="post" action='<%=request.getContextPath()+"/sha1"%>'>
            <input type="text" name="sha1"/>
            <input type="submit" value="Hash" />
        </form>
        
        <h2>Send a request</h2>
        <div>
        <form method="post" id="gbdForm">
            <table border="0">
                <tr>
                    <td>Service to call</td>
                    <td><select id="selectedService">
						<option value="processHeaderDocument" selected>Process
							Header Document</option>
						<option value="processFulltextDocument">Process Fulltext
							Document</option>
						<option value="processDate">Process Date</option>
						<option value="processHeaderNames">Process Header Names</option>
						<option value="processCitationNames">Process Citation
							Names</option>
						<option value="processAffiliations">Process Affiliations</option>
                    </select></td>
                </tr>
                <tr>
                    <td><span id="label"></span></td>
                    <td><div id="field"></div></td>
                </tr>
                <tr>
                    <td colspan="2"><input type="submit" value="Submit" /></td>
                    </table>
        </form>
        </div>
        
        <script type="text/javascript">
        
        function getBaseUrl(){
            var baseUrl='<%=request.getContextPath()%>';
            return baseUrl;
        }
        
            $(document).ready(function() {
                createInputFile();
                $('#gbdForm').attr('action', getBaseUrl()+'/processHeaderDocument');
                $('#selectedService').change(function() {
                    processChange();
                    return true;
                });
             });
            
            function processChange() {
                
                var selected = $('#selectedService').attr('value');
                
                if (selected == 'processHeaderDocument') {
                    createInputFile();
                    $('#gbdForm').attr('action', getBaseUrl()+'/processHeaderDocument');
                } else if (selected == 'processFulltextDocument') {
                    createInputFile();
                    $('#gbdForm').attr('action', getBaseUrl()+'/processFulltextDocument');
                } else if (selected == 'processDate') {
                    createInputText('date');
                    $('#gbdForm').attr('action', getBaseUrl()+'/processDate');
                } else if (selected == 'processHeaderNames') {
                    createInputText('names');
                    $('#gbdForm').attr('action', getBaseUrl()+'/processHeaderNames');
                } else if (selected == 'processCitationNames') {
                    createInputText('names');
                    $('#gbdForm').attr('action', getBaseUrl()+'/processCitationNames');
                } else if (selected == 'processAffiliations') {
                    createInputText('affiliations');
                    $('#gbdForm').attr('action', getBaseUrl()+'/processAffiliations');
                }
                // alert($("div").html());
            }
            
            function createInputFile(){
                $('#label').html('Select a pdf file');
                $('#input').remove();
                $('#field').append($('<input/>').attr('type', 'file').attr('id', 'input').attr('name', 'input'));
                $('#gbdForm').attr('enctype', 'multipart/form-data');
                $('#gbdForm').attr('method', 'post');
            }
            
            function createInputText(nameInput){
                $('#label').html('Enter '+nameInput);
                $('#input').remove();
                $('#field').append($('<input/>').attr('type', 'text').attr('id', 'input').attr('name', nameInput));
                $('#gbdForm').attr('enctype', '');
                $('#gbdForm').attr('method', 'post');
            }
            
            
        </script>
    </body>
</html>
