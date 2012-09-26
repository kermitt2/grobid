<!DOCTYPE html>
<html lang="fr">

<head>
<meta charset="utf-8">
<title>Welcome</title>
<script src="resources/js/jquery-1.8.1.min.js" />
</head>

<body>
	<h1>Welcome to grobid</h1>
	<form method="post" action="" enctype="multipart/form-data" id="gbdForm">
		<input type="file" name="nom" /> <input type="submit" value="Submit" id="tst"/>
	</form>

	<script type="text/javascript">
		$(document).ready(function() {
			$("#gbdForm").click(
			function() {
				var test = $('#tst').attr('value','clicked');
				alert("clicked");
				return false;
			});
		});
	</script>
</body>
</html>
