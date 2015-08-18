/**
*  Javascript functions for the front end.
*        
*  Author: Patrice Lopez
*/

jQuery.fn.prettify = function () { this.html(prettyPrintOne(this.html(),'xml')); };

var grobid = (function($) {

	function defineBaseURL(ext) {
		var baseUrl = null;
		if ( $(location).attr('href').indexOf("index.html") != -1)
			baseUrl = $(location).attr('href').replace("index.html", ext);
		else 
			baseUrl = $(location).attr('href') + ext;
		return baseUrl;
	}

	function setBaseUrl(ext) {
		var baseUrl = defineBaseURL(ext);
		$('#gbdForm').attr('action', baseUrl);
	}
	
	$(document).ready(function() {   
		
		$("#subTitle").html("About");
		$("#divAbout").show();
		$("#divAdmin").hide();
        $("#divRestI").hide();   
		$("#divDoc").hide();
		$('#consolidateBlock').show();
		
		createInputFile();
		setBaseUrl('processHeaderDocument');             

		$('#selectedService').change(function() {
			processChange();
			return true;
		}); 

		$('#gbdForm').ajaxForm({
            beforeSubmit: ShowRequest,
            success: SubmitSuccesful,
            error: AjaxError,
            dataType: "text"
        });

		$('#adminForm').attr("action",$(location).attr('href')+"allProperties");
		$('#TabAdminProps').hide();
		$('#adminForm').ajaxForm({
	        beforeSubmit: adminShowRequest,
	        success: adminSubmitSuccesful,
	        error: adminAjaxError,
	        dataType: "text"
	        });
		
		$("#about").click(function() {
			$("#about").attr('class', 'section-active');
			$("#rest").attr('class', 'section-not-active');
			$("#admin").attr('class', 'section-not-active');
			$("#doc").attr('class', 'section-not-active');
			$("#demo").attr('class', 'section-not-active');
			
			$("#subTitle").html("About"); 
			$("#subTitle").show();
			
			$("#divAbout").show();
			$("#divRestI").hide();
			$("#divAdmin").hide();
			$("#divDoc").hide();
			$("#divDemo").hide();
			return false;
		});
		$("#rest").click(function() {
			$("#rest").attr('class', 'section-active');
			$("#doc").attr('class', 'section-not-active');
			$("#about").attr('class', 'section-not-active');
			$("#admin").attr('class', 'section-not-active');
			$("#demo").attr('class', 'section-not-active');
			
			$("#subTitle").hide(); 
			//$("#subTitle").show();
			processChange();
			
			$("#divRestI").show();
			$("#divAbout").hide();
			$("#divDoc").hide();
			$("#divAdmin").hide();
			$("#divDemo").hide();
			return false;
		});
		$("#admin").click(function() {
			$("#admin").attr('class', 'section-active');
			$("#doc").attr('class', 'section-not-active');
			$("#about").attr('class', 'section-not-active');
			$("#rest").attr('class', 'section-not-active');
			$("#demo").attr('class', 'section-not-active');
			
			$("#subTitle").html("Admin");
			$("#subTitle").show();
			setBaseUrl('admin');
			
			$("#divRestI").hide();
			$("#divAbout").hide();
			$("#divDoc").hide();
			$("#divAdmin").show();
			$("#divDemo").hide();			
			return false;
		});
		$("#doc").click(function() {
			$("#doc").attr('class', 'section-active');
			$("#rest").attr('class', 'section-not-active');
			$("#about").attr('class', 'section-not-active');
			$("#admin").attr('class', 'section-not-active');
			$("#demo").attr('class', 'section-not-active');
			
			$("#subTitle").html("Doc"); 
			$("#subTitle").show();        
			
			$("#divDoc").show();
			$("#divAbout").hide();
			$("#divRestI").hide();
			$("#divAdmin").hide();
			$("#divDemo").hide();
			return false;
		});
		$("#demo").click(function() {
			$("#demo").attr('class', 'section-active');
			$("#rest").attr('class', 'section-not-active');
			$("#about").attr('class', 'section-not-active');
			$("#admin").attr('class', 'section-not-active');
			$("#doc").attr('class', 'section-not-active');
			
			$("#subTitle").html("Demo"); 
			$("#subTitle").show();        
			
			$("#divDemo").show();
			$("#divDoc").hide();
			$("#divAbout").hide();
			$("#divRestI").hide();
			$("#divAdmin").hide();
			return false;
		});
	});

	function ShowRequest(formData, jqForm, options) {
	    var queryString = $.param(formData);
	    $('#requestResult').html('<font color="grey">Requesting server...</font>');
	    return true;
	}
	
	function AjaxError(jqXHR, textStatus, errorThrown) {
		$('#requestResult').html("<font color='red'>Error encountered while requesting the server.<br/>"+jqXHR.responseText+"</font>");      
		responseJson = null;
	}
	
	function htmll(s) {
    	return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  	}
	
	function SubmitSuccesful(responseText, statusText, xhr) {
		var selected = $('#selectedService option:selected').attr('value');
		var display = "<pre class='prettyprint lang-xml' id='xmlCode'>";  
		var testStr = vkbeautify.xml(responseText);
		
		display += htmll(testStr);

		display += "</pre>";
		$('#requestResult').html(display);
		window.prettyPrint && prettyPrint();
		$('#requestResult').show();
	}
	
	$(document).ready(function() {
	    $(document).on('shown', '#xmlCode', function(event) {
	        prettyPrint();
	    });
	});
	
	function processChange() {
		var selected = $('#selectedService option:selected').attr('value');

		if (selected == 'processHeaderDocument') {
			createInputFile(selected);
			$('#consolidateBlock').show();
			setBaseUrl('processHeaderDocument');
		} 
		else if (selected == 'processFulltextDocument') {
			createInputFile(selected);
			$('#consolidateBlock').show();
			setBaseUrl('processFulltextDocument');
		} 
		else if (selected == 'processDate') {
			createInputTextArea('date');
			$('#consolidateBlock').hide();
			setBaseUrl('processDate');
		} 
		else if (selected == 'processHeaderNames') {
			createInputTextArea('names');
			$('#consolidateBlock').hide();
			setBaseUrl('processHeaderNames');
		} 
		else if (selected == 'processCitationNames') {
			createInputTextArea('names');
			$('#consolidateBlock').hide();
			setBaseUrl('processCitationNames');
		} 
		else if (selected == 'processReferences') {
			createInputFile(selected);
			$('#consolidateBlock').show();
			setBaseUrl('processReferences');
		} 
		else if (selected == 'processAffiliations') {
			createInputTextArea('affiliations');
			$('#consolidateBlock').hide();
			setBaseUrl('processAffiliations');
		} 
		else if (selected == 'processCitation') {
			createInputTextArea('citations');
			$('#consolidateBlock').show();
			setBaseUrl('processCitation');
		} 
		else if (selected == 'processCitationPatentTEI') {
			createInputFile(selected);
			$('#consolidateBlock').show();
			setBaseUrl('processCitationPatentTEI');
		}
		else if (selected == 'processCitationPatentST36') {
			createInputFile(selected);
			$('#consolidateBlock').show();
			setBaseUrl('processCitationPatentST36');
		}
		else if (selected == 'processCitationPatentPDF') {
			createInputFile(selected);
			$('#consolidateBlock').show();
			setBaseUrl('processCitationPatentPDF');
		}
		else if (selected == 'processCitationPatentTXT') {
			createInputTextArea('text');
			$('#consolidateBlock').show();
			setBaseUrl('processCitationPatentTXT');
		}
	}

	function createInputFile(selected) {
		//$('#label').html('&nbsp;'); 
		$('#textInputDiv').hide();
		//$('#fileInputDiv').fileupload({uploadtype:'file'});
		//$('#fileInputDiv').fileupload('reset');
		$('#fileInputDiv').show();
		
		$('#gbdForm').attr('enctype', 'multipart/form-data');
		$('#gbdForm').attr('method', 'post'); 
	}

	function createInputTextArea(nameInput) {
		//$('#label').html('&nbsp;'); 
		$('#fileInputDiv').hide();
		//$('#input').remove();
		
		//$('#field').html('<table><tr><td><textarea class="span7" rows="5" id="input" name="'+nameInput+'" /></td>'+
		//"<td><span style='padding-left:20px;'>&nbsp;</span></td></tr></table>");
		$('#textInputArea').attr('name', nameInput);
		$('#textInputDiv').show();
		
		$('#gbdForm').attr('enctype', '');
		$('#gbdForm').attr('method', 'post');
	}
	
	/** admin functions */
		
	var selectedAdmKey="", selectedAdmValue, selectedAdmType;
		
	function adminShowRequest(formData, jqForm, options) {
		$('#TabAdminProps').show();
		$('#admMessage').html('<font color="grey">Requesting server...</font>');
	    return true;
	}

	function adminAjaxError() {
		$('#admMessage').html("<font color='red'>Autentication error.</font>");
	}

	function adminSubmitSuccesful(responseText, statusText) {
		$('#admMessage').html("<font color='green'>Welcome to the admin console.</font>");
		parseXml(responseText);
		rowEvent();
	}
	
	function parseXml(xml){
		var out="<pre><table class='table-striped table-hover'><thead><tr align='left'><th>Property</th><th align='left'>value</th></tr></thead>";
		$(xml).find("property").each(function(){
			var dsipKey = $(this).find("key").text();
			var key = dsipKey.split('.').join('-');
			var value = $(this).find("value").text();
			var type = $(this).find("type").text();
			out+="<tr class='admRow' id='"+key+"'><td><input type='hidden' value='"+type+"'/>"+dsipKey+"</td><td><div>"+value+"</div></td></tr>";
		});
		out+="</table></pre>";
		$('#TabAdminProps').html(out);
	}
	
	function rowEvent(){
		$('.admRow').click(function() {
			$("#"+selectedAdmKey).find("div").html($("#val"+selectedAdmKey).attr("value"));
			selectedAdmKey=$(this).attr("id");
			selectedAdmValue=$(this).find("div").text();
			selectedAdmType=$(this).find("input").attr("value");
			$(this).find("div").html("<input type='text' id='val"+selectedAdmKey+"' size='80' value='"+selectedAdmValue+"' class='input-xxlarge'/>");
			$("#val"+selectedAdmKey).focus();
		});
		
		$('.admRow').keypress(function(event) {
			var keycode = (event.keyCode ? event.keyCode : event.which);
			selectedAdmKey=$(this).attr("id");
			// Enter key
			if(keycode == '13') {				
				var newVal = $("#val"+selectedAdmKey).val();	
				$("#"+selectedAdmKey).find("div").html(newVal);
				selectedAdmValue=newVal;
				selectedAdmType=$(this).find("input").attr("value");				
				generateXmlRequest();
			}
			// Escape key
			if(keycode == '27') {
				$("#"+selectedAdmKey).find("div").html(selectedAdmValue);
			}
		});
	}
	
	function generateXmlRequest(){
		var xmlReq= "<changeProperty><password>"+$('#admPwd').val()+"</password>";
		xmlReq+="<property><key>"+selectedAdmKey.split('-').join('.')+"</key><value>"+selectedAdmValue+"</value><type>"+selectedAdmType+"</type></property></changeProperty>";
		if("org.grobid.service.admin.pw"==selectedAdmKey.split('-').join('.')){
			$('#admPwd').attr('value', selectedAdmValue);
		}
		$.ajax({
			  type: 'POST',
			  url: $(location).attr('href')+"changePropertyValue",
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
		
})(jQuery);



