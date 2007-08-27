<?php
$ua = $_SERVER['HTTP_USER_AGENT'];
?>
<html>
<head>
<title>Welcome to MLGK</title>
</head>
<body>
<h3>MLGK Download</h3>
<a href="dist/Nokia-N73-en_US-mlgk.jar">Mobile App (N73 v<?php include("dist/version.html");?>)</a>
<pre>
<?php include("dist/build.html");?>
</pre>
<!-- <p>ua=<?=$ua?></p> -->

</body>
</html>
