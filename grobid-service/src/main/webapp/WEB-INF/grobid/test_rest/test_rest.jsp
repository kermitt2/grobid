<div id="divRestI">
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
						<option value="processCitations">Process Citations</option>
				</select></td>
			</tr>
			<tr>
				<td><span id="label"></span></td>
				<td><div id="field"></div></td>
			</tr>
			<tr>
				<td colspan="2"><input id="submitRequest" type="submit" value="Submit" class="btn"/></td>
		</table>
	</form>
	<br>
	<div id="requestResult"></div>
</div>



<script type="text/javascript">
	
	function setBaseUrl(ext) {
		var baseUrl = $(location).attr('href')+ext;
		$('#gbdForm').attr('action', baseUrl);
	}

	$(document).ready(function() {
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
	});
	
	function ShowRequest(formData, jqForm, options) {
	    var queryString = $.param(formData);
	    $('#requestResult').html('<font color="grey">Requesting server...</font>');
	    return true;
	}

	function AjaxError() {
		 $('#requestResult').html("<font color='red'>Error encountered while requesting the server.</font>");
	}

	function SubmitSuccesful(responseText, statusText) {
		$('#requestResult').text(responseText);
	}


	function processChange() {

		var selected = $('#selectedService').attr('value');

		if (selected == 'processHeaderDocument') {
			createInputFile();
			setBaseUrl('processHeaderDocument');
		} else if (selected == 'processFulltextDocument') {
			createInputFile();
			setBaseUrl('processFulltextDocument');
		} else if (selected == 'processDate') {
			createInputTextArea('date');
			setBaseUrl('processDate');
		} else if (selected == 'processHeaderNames') {
			createInputTextArea('names');
			setBaseUrl('processHeaderNames');
		} else if (selected == 'processCitationNames') {
			createInputTextArea('names');
			setBaseUrl('processCitationNames');
		} else if (selected == 'processAffiliations') {
			createInputTextArea('affiliations');
			setBaseUrl('processAffiliations');
		} else if (selected == 'processCitations') {
			createInputTextArea('citations');
			setBaseUrl('processCitations');
		}
	}

	function createInputFile() {
		$('#label').html('Select a pdf file');
		$('#input').remove();
		$('#field').append(
				$('<input/>').attr('type', 'file').attr('id', 'input').attr(
						'name', 'input'));
		$('#gbdForm').attr('enctype', 'multipart/form-data');
		$('#gbdForm').attr('method', 'post');
	}
	
	function createInputTextArea(nameInput) {
		$('#label').html('Enter ' + nameInput);
		$('#input').remove();
		$('#field').append(
				$('<textarea rows="15" cols="100" class="input-xxlarge"/>').attr('id', 'input').attr(
						'name', nameInput));
		$('#gbdForm').attr('enctype', '');
		$('#gbdForm').attr('method', 'post');
	}

</script>