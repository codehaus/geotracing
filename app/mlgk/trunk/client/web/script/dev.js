/* usemedia.com . joes koppers . 10.2006 */
/* thnx for reading this code */


/* these are temporary development functions */

function tmp_create_tags()
{
	//[['apple',0,1],['media',5,4],['cool',1,2],['free',2,3],['zap',1,2]];
	var tags = ['net','ajax','apple','art','article','audio','blog','blogging','blogs','books','business','comics','computer','cool','css','daily','design','development','diy','education','entertainment','fashion','firefox','flash','flickr','food','forum','free','freeware','fun','funny','games','google','graphics','gtd','hardware','history','home','howto','humor','illustration','inspiration','interesting','internet','java','javascript','jobs','lifehacks','linux','mac','marketing','media','microsoft','mobile','movies','mp3','music','news','online','opensource','osx','photo','photography','photos','photoshop','php','podcast','politics','productivity','programming','python','rails','recipes','reference','research','ruby','science','search','security','shopping','social','software','tech','technology','tips','tool','tools','toread','travel','tutorial','tutorials','ubuntu','video','web','web2.0','webdesign','webdev','wiki','windows','wishlist','wordpress','work','writing','youtube'];


	var tagsarray = new Array();
	
	for (var i=0; i<tags.length; i++)
	{
		tagsarray.push([tags[i],Math.round(Math.random()*5),Math.round(Math.random()*10)]);
	}
	return tagsarray;
//		

}


/* debugging mode */

function tmp_dev()
{
	var str='';
	str+= '<div id="development" ondblclick="tmp_debug(\'toggle\')">';
	str+= '<div id=debug1 style="top:3px;">debug1</div>';
	str+= '<div id=debug2 style="top:15px;">debug2</div>';
	str+= '<div id=debug3 style="top:27px;">debug3</div>';
	str+= '</div>';
	
	return str;
}

debugging = false;

function tmp_debug(target)
{
	if (target=='toggle')
	{
		debugging = !debugging;
		//for (var i=1; i<=3; i++) document.getElementById('debug'+i).style.display = (debugging)? 'block':'none';
		document.getElementById('development').style.display = (debugging)? 'block':'none';
		//document.getElementById('bliin_logo').style.display = (!debugging)? 'block':'none';
		return;
	}
	
	if (!debugging) return;
	
	var t = new Date().getTime();
	var str = t+'> ';
	for (var i=1; i<arguments.length; i++)
	{
		if (arguments[i]=='querytime') str+= ', returned in '+(t-tmp_querytime)+'ms'
		else str+= arguments[i];
	}
	if (target<4) document.getElementById('debug'+target).innerHTML = str;
	else document.getElementById('footer').innerHTML = str;
	
	return t;
}

//haveqt = false;