<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*,java.text.*" %>
<%@ page import="kiwanami.web.cont.*" %>
<html>
<head>
<title>継続セッション管理</title>
<link href="article-blue.css" type="text/css" rel="stylesheet">
<script src="prototype.js"></script>
<script src="decode_url.js"></script>
<script>

//==================================================
// Ajax multi thread queue framework
 
function WorkerQueue(__polling_time) {
	var self = this;
	var queue = [];
	var workingFlag = false;
	var runningFlag = false;
	var debug = false;
	var polling_time = __polling_time;

	function clog(a) {
		if (!debug) return;
		if (console) {
			console.log(a);
		} else {
			var logElm = $('log');
			if (logElm) {
				logElm.innerHTML = logElm.innerHTML + a;
			}
		}
	}

	this.setPollingTime = function(i) {
		if ((typeof i) == "number" && i > 300) {
			polling_time = i;
		}
	}

	this.getPollingTime = function() {
		return polling_time;
	}

	this.setDebug = function(a) {
		debug = a;
	}

	this.size = function() {
		return queue.length;
	}

	this.isRunning = function() {
		return runningFlag;
	}

	this.addJob = function(job) {
		queue.push(job);
	}

	function nextSchedule() {
		if (runningFlag) {
			setTimeout(doJob,polling_time);
			clog("NEXT TIMEOUT");
		} else {
			clog("RECEIVED STOP SIGNAL");
		}
	}

	function finishJob() {
		nextSchedule();
		workingFlag = false;
	}

	function doJob() {
		clog("doJob...");
		if (workingFlag) {
			nextSchedule();
			return;
		}
		clog("incoming job..");
		workingFlag = true;
		try {
			var job = queue.shift();
			if (!job) {
				nextSchedule();
				workingFlag = false;
				return;
			}
			clog("DO JOB");
			job(finishJob);
		} catch (e) {
			clog(e);
			workingFlag = false;
		}
	}
	
	this.start = function(){
		if (runningFlag) return;
		runningFlag = true;
		nextSchedule();
		clog("START");
	}

	this.stop = function() {
		runningFlag = false;
		clog("SEND STOP SIGNAL");
	}
}

function makeAjaxJob(url,params,succJob,failJob) {
	return function(fj) {
		try {
			new Ajax.Request(
				url,
				{
					method: 'get', 
					parameters: params,
					onComplete: function(req,obj) {
						try {
							succJob(req,obj);
						} catch (e) {
							console.log(e);
						} finally {
							fj();
						}
					},
					onFailure: function() {
						try {
							if (failJob) failJob(req,obj);
						} catch (e) {
							console.log(e);
						} finally {
							fj();
						}
					}
				});
		} catch (e) {
			console.log(e);
		}
	};
}

//==================================================
// Reload

var log_pos = null;
var log_time = 5;
var session_time = 1;

var queue = new WorkerQueue(3000);
function updatePollingTime() {
	queue.setPollingTime(parseInt($('update_time').value));
}
function scrollLogToBottom() {
	var holderElm = $('log_area').parentNode;
	holderElm.scrollTop = holderElm.scrollHeight - holderElm.offsetHeight;
}
function updateLog(req) {
	var logElm = $('log_area');
	eval("var json = "+req.responseText+";");
	if (log_pos != json.pos) {
		log_pos = json.pos;
		var texts = json.text;
		var strs = [];
		for(var i=0;i<texts.length;i++) {
			strs.push("<div style=\"display:block\">"+decodeURL(texts[i])+"</div>");
		}
		logElm.innerHTML = logElm.innerHTML + strs.join("");
		applyFilter();
	}
	queue.addJob(makeAjaxJob("sessions.jsp","",updateSessions));
}
function applyFilter() {
	var maxLines = parseInt( $('lines').value );
	maxLines = maxLines || 1000;
	var filter = $('filter').value;
	var logElm = $('log_area');
	var spans = $A( logElm.getElementsByTagName("div") );
	for(var i=0,len = spans.length;i<len;i++) {
		var t = spans[i].innerHTML;
		if (i < (len - maxLines)) {
			logElm.removeChild(spans[i]);
			continue;
		}
		if (t) {
			if (!filter) spans[i].style.display = "";
			else spans[i].style.display = (t.indexOf(filter)>=0) ? "" : "none";
		}
	}
	scrollLogToBottom();
}
function updateSessions(req) {
	$('sessions').innerHTML = req.responseText;
	queue.addJob(makeAjaxJob("log.jsp",((log_pos) ? "pos="+log_pos : ""),updateLog));
}

Event.observe(window,"load",function() {
	queue.addJob(makeAjaxJob("sessions.jsp","",updateSessions));
	
	function startQueue() {
		queue.start();
		$('start_button').disabled = true;
		$('stop_button').disabled = false;
	}
	function stopQueue() {
		queue.stop();
		$('start_button').disabled = false;
		$('stop_button').disabled = true;
	}
	Event.observe($('clear_button'),"click",function() {
		log_pos = null;
		$('log_area').innerHTML = "";
	});
	Event.observe($('start_button'),"click",function() {
		startQueue();
	});
	Event.observe($('stop_button'),"click",function() {
		stopQueue();
	});

	var lastUpdateTime = null;
	new Form.Element.Observer($('update_time'),1,function(elm,value) {
		if (value == lastUpdateTime) return;
		lastUpdateTime = value;
		updatePollingTime();
	});
	var lastFilterValue = null;
	new Form.Element.Observer($('filter'),1,function(elm,value) {
		if (value == lastFilterValue) return;
		lastFilterValue = value;
		applyFilter();
	});
	startQueue();
});
</script>
</head>
<body>

<h1 class="chapter">継続セッション管理</h1>

<div style="text-align:right"> Updating status: <button
id="start_button">START</button> <button
id="stop_button">STOP</button>
/ Update time: <input type="text" size="10" id="update_time" value="3000"/>msec
</div>

<div id="sessions"></div>

<hr>

<button id="clear_button">Clear</button> / Filter: <input type="text" id="filter" value=""/>  / Lines: <input type="text" id="lines" value="200"/>
<div style="overflow-y:scroll; overflow-x:auto; height:400px; width:100%;">
<div id="log_area">
</div>
<div>

</body>
</html>