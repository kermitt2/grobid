<div id="divAdmin">
	<form method="post" action='<%=request.getContextPath()+"/allProperties"%>' id="adminForm">
		<span>Enter administrator password</span>
		<input type="password" name="sha1" id="admPwd"/> 
		<input type="submit" value="Log in" class="btn"/>
	</form>
	<br>
	<div id="admMessage"></div>
	<br>
	<div id="TabAdminProps">
	</div>
</div>

<script type="text/javascript">

	var selectedAdmKey="", selectedAdmValue, selectedAdmType;

	$(document).ready(function() {
		$('#TabAdminProps').hide();
		$('#adminForm').ajaxForm({
	        beforeSubmit: adminShowRequest,
	        success: adminSubmitSuccesful,
	        error: adminAjaxError,
	        dataType: "text"
	        });
	});
	
	function adminShowRequest(formData, jqForm, options) {
		$('#TabAdminProps').show();
		$('#admMessage').html('<font color="grey">Requesting server...</font>');
	    return true;
	}

	function adminAjaxError() {
		$('#admMessage').html("<font color='red'>Autentication error.</font>");
	}

	function adminSubmitSuccesful(responseText, statusText) {
		$('#admMessage').html("");
		parseXml(responseText);
		rowEvent();
	}
	
	function parseXml(xml){
		var out="<table class='table-striped table-hover'><thead><tr align='left'><th>Property</th><th align='left'>value</th></tr></thead>";
		$(xml).find("property").each(function(){
			var dsipKey = $(this).find("key").text();
			var key = dsipKey.split('.').join('-');
			var value = $(this).find("value").text();
			var type = $(this).find("type").text();
			out+="<tr class='admRow' id='"+key+"'><td><input type='hidden' value='"+type+"'/>"+dsipKey+"</td><td><div>"+value+"</div></td></tr>";
		});
		out+="</table>";
		$('#TabAdminProps').html(out);
	}
	
	function rowEvent(){
		$('.admRow').click(function(){
			$("#"+selectedAdmKey).find("div").html($("#val"+selectedAdmKey).attr("value"));
			selectedAdmKey=$(this).attr("id");
			selectedAdmValue=$(this).find("div").text();
			selectedAdmType=$(this).find("input").attr("value");
			$(this).find("div").html("<input type='text' id='val"+selectedAdmKey+"' size='80' value='"+selectedAdmValue+"' class='input-xxlarge'/>");
			$("#val"+selectedAdmKey).focus();
		});
		
		$('.admRow').keypress(function(event){
			var keycode = (event.keyCode ? event.keyCode : event.which);
			// Eneter key
			if(keycode == '13'){
				$("#"+selectedAdmKey).find("div").html($("#val"+selectedAdmKey).attr("value"));
				selectedAdmKey=$(this).attr("id");
				selectedAdmValue=$(this).find("div").text();
				selectedAdmType=$(this).find("input").attr("value");
				// alert("key="+selectedAdmKey+"  value="+selectedAdmValue+"  type="+selectedAdmType);
				generateXmlRequest();
			}
			// Escape key
			if(keycode == '27'){
				$("#"+selectedAdmKey).find("div").html($("#val"+selectedAdmKey).attr("value"));
			}
		});
	}
	
	function generateXmlRequest(){
		var xmlReq= "<changeProperty><password>"+$('#admPwd').attr('value')+"</password>";
		xmlReq+="<property><key>"+selectedAdmKey.split('-').join('.')+"</key><value>"+selectedAdmValue+"</value><type>"+selectedAdmType+"</type></property></changeProperty>";
		if("org.grobid.service.admin.pw"==selectedAdmKey.split('-').join('.')){
			$('#admPwd').attr('value', selectedAdmValue);
		}
		$.ajax({
			  type: 'POST',
			  url: '<%=request.getContextPath()+"/changePropertyValue"%>',
			  data: {xml: xmlReq},
			  success: changePropertySuccesful,
			  error: changePropertyError
			});
	}
	
	function changePropertySuccesful(responseText, statusText) {
		$("#"+selectedAdmKey).find("div").html(responseText);
		$('#admMessage').html("<font color='green'>Property "+selectedAdmKey.split('-').join('.')+" updated with success</font>");
	}
	
	function changePropertyError() {
		$('#admMessage').html("<font color='red'>An error occured while updating property"+selectedAdmKey.split('-').join('.')+"</font>");
	}
	
</script>