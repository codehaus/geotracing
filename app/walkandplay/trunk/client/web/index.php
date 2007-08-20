<?php
	// Detect and redirect mobile browsers
	include("mobile-check.php");
	if(detect_mobile_device()){
  		header('Location:mob');
  		exit;
  	}

	header("Location:stable");
	//include("dev.php");
?>