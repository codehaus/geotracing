<?php
	// Detect and redirect mobile browsers
	include("mob/mobile-check.php");
	if(detect_mobile_device()){
  		header('Location:mob');
  		exit;
  	}

	//header("Location:stable");
	header("Location:dev");
?>