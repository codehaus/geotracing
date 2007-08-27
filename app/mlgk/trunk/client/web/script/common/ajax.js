/* usemedia.com . joes koppers . 05.2006 */
/* thnx for reading this code */


//generic AJAX obj constructor

function XMLObj()
{
	this.req = false;
	this.post_vars = false;
	this.init_action = false;
	this.done_action = false;
	this.fail_action = false;
}

XMLObj.prototype.httpRequest = function(url)
{
	this.req = false;
    // branch for native XMLHttprequest object
    if (window.XMLHttpRequest) {
    	try {
			this.req = new XMLHttpRequest();
        } catch(e) {
			this.req = false;
        }
    // branch for IE/Windows ActiveX version
    } else if (window.ActiveXObject) {
       	try {
        	this.req = new ActiveXObject("Msxml2.XMLHTTP");
      	} catch(e) {
        	try {
          		this.req = new ActiveXObject("Microsoft.XMLHTTP");
        	} catch(e) {
          		this.req = false;
        	}
		}
    }
	if (this.req) {
		var obj = this;
		this.req.onreadystatechange = function() { readyStateChange(obj) };
		if (this.post_vars)
		{
			this.req.open('POST',url,true); 
			this.req.setRequestHeader("Content-type","application/x-www-form-urlencoded"); 
			this.req.setRequestHeader("Content-length",this.post_vars.length); 
			this.req.setRequestHeader("Connection","close"); 
			this.req.send(this.post_vars);
		}
		else
		{
			this.req.open('GET',url,true);
			this.req.send("");
		}
	}
	else alert('error creating xml connection');
}

function readyStateChange(obj)
{
	//handler for state=loading
	if (obj.req.readyState==1 && obj.init_action) obj.init_action();

	//handler for state=done
	if (obj.req.readyState==4)
	{
		if (obj.req.status==200) obj.done_action(obj.req.responseText);
		else if (obj.fail_action) obj.fail_action(obj.req.responseText);
		else alert('XHR error');
	}
}