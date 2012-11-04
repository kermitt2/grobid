<div id="divAbout"></div>

<script type="text/javascript">
	
$(document).ready(function() {
	$.get('<%=request.getRequestURL() +"grobid"%>', {}, function(data) {
		$("#divAbout").html(data);
	});
});
</script>