/* usemedia.com . joes koppers . 12.2006 */
/* thnx for reading this code */


//extends js Date object with human readable formatting

Date.months = ['January','February','March','April','May','June','July','August','September','October','November','December'];
Date.days = ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'];

Date.prototype.format = function()
{
/*	pre-defined date formats: use (u) for US date order (dec 22, 2006)

	day					=> 'friday'
	shortday			=> 'fri'
	(u)date				=> '22/12/2006'
	(u)shortdate		=> '22/12/06'
	(u)humandate		=> '22 december 2006'
	(u)shorthumandate	=> '22 dec 2006'
	(u)shorterhumandate	=> '22 dec';
	(u)longhumandate	=> 'friday 22 december, 2006';
	time				=> '14:56'
	longtime			=> '14:56:23'
	(u)relative			=> 'x minutes ago', 'x hours x minutes ago', '1 day x hours ago', 'x days ago',
							prints regular (u)shorthumandate at 14+ days
							
	example: new Date().format('Current time and date: ','time',' on ','day',' ','humandate'); */						

	var day = this.getDay();
	var date = this.getDate();
	var month = this.getMonth()+1;
	var year = this.getFullYear();
	var h = this.getHours();
	var m = this.getMinutes(); if (m<10) m = '0'+m;
	var s = this.getSeconds(); if (s<10) s = '0'+s;

	var str = '';
	for (var i=0; i<arguments.length; i++)
	{
		switch(arguments[i])
		{
			default: str+= arguments[i]; break; //non-format strings are printed
			
			case 'day': str+= Date.days[day]; break;
			case 'shortday': str+= Date.days[day].substring(0,3); break;
			case 'date': str+= date+'/'+month+'/'+year; break;
			case 'udate': str+= month+'/'+date+'/'+year; break;
			case 'shortdate': str+= date+'/'+month+'/'+String(year).substr(2); break;
			case 'ushortdate': str+= month+'/'+date+'/'+String(year).substr(2); break;
			case 'humandate': str+= date+' '+Date.months[month-1]+' '+year; break;
			case 'uhumandate': str+= Date.months[month-1]+' '+date+', '+year; break;
			case 'shorthumandate': str+= date+' '+Date.months[month-1].substring(0,3)+' '+year; break;
			case 'ushorthumandate': str+= Date.months[month-1].substring(0,3)+' '+date+', '+year; break;
			case 'shorterhumandate': str+= date+' '+Date.months[month-1].substring(0,3)+' \''+String(year).substr(2); break;
			case 'ushorterhumandate': str+= Date.months[month-1].substring(0,3)+' '+date+', \''+String(year).substr(2); break;
			case 'longhumandate': str+= Date.days[day]+' '+date+' '+Date.months[month-1]+', '+year; break;
			case 'ulonghumandate': str+= Date.days[day]+' '+Date.months[month-1]+' '+date+', '+year; break;
			case 'time': str+= h+':'+m; break;
			case 'longtime': str+= h+':'+m+':'+s; break;
			case 'relative':
			case 'urelative':
				var now = new Date();
				var diff = now-this;
				if (diff<60000) diff = 0;
				var mins = Math.floor(diff/1000/60);
				var hrs = Math.floor(mins/60);
				var days = (hrs>now.getHours())? Math.floor((hrs)/24):0;

				if (days==0)
				{
					mins = mins-(hrs*60);
					hrs = hrs;
					if (hrs==0 && mins==0) str+= 'less than a minute ';
					else
					{
						if (hrs>0) str+= (hrs==1)? '1 hour ':hrs+' hours ';
						if (mins>0) str+= (mins==1)? '1 minute ':mins+' minutes ';
					}
				}
				else if (days==1)
				{
					str+= '1 day ';
					hrs = hrs - 24;
					if (hrs>0) str+= (hrs==1)? '1 hour ':hrs+' hours ';
				}
				else if (days<=14)
				{
					str+= days+' days ';
				}
				
				if (days>14)
				{
					str+= 'on ';
					str+= (arguments[i]=='relative')? date+' '+Date.months[month-1].substring(3,0)+' '+year:Date.months[month-1].substring(3,0)+' '+date+', '+year;
					str+= ' at '+h+':'+m;
				}
				else
				{
					str+= 'ago';
					if (days>1) str+= ' at '+h+':'+m;
				}
				break;
				
			//utils
			case 'tolower': str = str.toLowerCase(); break;
			case 'nonbreaking': str = str.replace(/ /g,'&nbsp;'); break;
		}
	}
	return str;
}