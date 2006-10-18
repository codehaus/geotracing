<?php
require_once('kwclient.php');

/*
 * Basic app PHP4 client.
 *
 * Author: Just van den Broecke, Bjorn Wijers
 * $Id: upload.php,v 1.5 2005/10/12 08:43:02 maarten Exp $
 */

error_reporting(E_ERROR);

// See keyworx/client/php/kwclient/src
// Get new or existing KW session
// NOTE: YOU NEED TO USE THE =& OTHERWISE YOU GET A COPY
// AND ANY (STATE) CHANGES WILL NOT BE REFLECTED IN KWSession

$kw_session =& KWSession::getKWSession();

switch ($kw_session->getState()) {
	case KW_NOT_LOGGED_IN:
		$response =& $kw_session->login();
		if (!$response->isOk()) {
			$kw_session->_log("Login failed.");
			die("Could not log into portal ".KW_PORTAL." as ".KW_USER.".");
			break;
		}
		$agent =& $kw_session->getAgent();
		$kw_session->_log('personid=' . $agent->getPersonId());
		$kw_session->_log('personname=' . $agent->getPersonName());

		// FALL THROUGH EXPECTED !!
	case KW_LOGGED_IN:
		// Select an application within portal
		$response =& $kw_session->selectApp();

		if (!$response->isOk()) {
			$kw_session->_log("Could not select application.");
			die("Could not select application ".KW_APP.".");
			break;
		}
		$agent =& $kw_session->getAgent();
		$kw_session->_log('appid=' . $agent->getAppId());
		break;

	case KW_APP_SELECTED:
		// This is not the first request to the server. We'll send an
		// echo request to verify that we're still logged in.
		$request = $kw_session->createRequest('echo-req');
		$response = $kw_session->_post($request);
		if (!$response->isOk()) {
			// try restoring the session
			$kw_session->_log("Echo failed. Will try a session restore.");
			$kw_session->_restoreSession();
			if ($kw_session->getState() != KW_APP_SELECTED) {
				$kw_session->_log("Can't reconnect. Giving up.");
				die("Can't reconnect. I give up.");
			}
		}
		break;
}

$kw_agent =& $kw_session->getAgent();

?>
<form name="upload" id="upload" method="post" action="<?= $kw_session->media_path ?>" enctype="multipart/form-data">
<input type="file" name="filename1" id="filename1">
	<input type="submit" name="submit" id="submit">
	<input type="hidden" name="name" value="name_of_php_upload">
	<input type="hidden" name="description" value="php test">
	<input type="hidden" name="agentkey" value="<?= $kw_agent->getKey() ?>">
	<input type="hidden" name="personid" value="<?= $kw_agent->getPersonId() ?>">
	<!--
	<input type="hidden" name="redirecturl" value="php/upload-ok.php">
	<input type="hidden" name="exceptionurl" value="php/upload-not-ok.php">
	-->
	<img src="img/browseBT.gif" /><img src="img/addphotoBT.gif" />
</form>