var info = {
	ws: "",
	user: "1",
	rand: "123",
	videoCur:"",
	chatCur:"",
	init: function () {
		info.videoCur=document.getElementById("video-current");
		info.chatCur=document.getElementById("chat-current");
		info.user = sessionStorage.getItem("user");
		document.title = "hello, " + info.user + " :-)";
		info.rand = sessionStorage.getItem("rand");
		info.ws = new WebSocket("wss://mvideotalk.xyz:8081/chat/ws/" + info.user);
		console.log("wss://localhost:8080/chat/ws" + info.user);
		info.ws.onmessage = info.receive;
		info.ws.onerror = function () {
			console.log("Websocket connect error");
		}
		info.ws.onopen = function () {
			console.log("Websocket connect successfuly");
			info.to = info.user;
			friends.getFriendList();
			messageList.getMessageList();
		}
		window.onbeforeunload = function () {
			info.ws.close();
		}
	},
	send: function (msg) {
		info.ws.send(JSON.stringify(msg));
	},
	receive: function (msg) {
		console.log(msg.data);
		let obj = JSON.parse(msg.data);
		switch (obj.type) {
			case "friend-request":
				friends.add(obj.from);
				break;
			case "message":
				messageList.addOne(obj.data[0], obj.from);
				break;
			case "friend-list":
				friends.list = obj.data;
				friends.refresh();
				break;
			case "message-list":
				messageList.receiveMessageList(obj.data);
				break;
			case "new-ice-candidate":
				video.handleNewICECandidateMsg(obj);
				break;
			case "video-offer":
				video.handleVideoOfferMsg(obj);
				break;
			case "video-answer":
				video.handleVideoAnswerMsg(obj);
				break;
			case "hang-up-video":
				video.closeVideoCall();
				break;
			case "error":
				window.alert(obj.data);
				break;
		}
	},
	logout:function(){
		msg = {
			type:"logout",
			user:info.user,
			rand:info.rand
		}
		info.send(msg);
		sessionStorage.removeItem("user");
		sessionStorage.removeItem("rand");
		window.location.href="../index.html";
	}
}
/*
数据格式
let msg = {
			user: info.user,
			rand: info.rand,
			to: messageList.target,
			type: "text",
			date: new Date(),
			data: [text]
		};
*/
var message = {
	msg: "",
	init: function () {
		message.msg = document.getElementById('input-bar');
	},
	add: function (text) {
		message.msg.innerHTML += text;
	},
	addEmotion: function (emotionName) {
		message.add("<img src=../js/img/" + emotionName + ">");
	},
	addPhoto: function (e) {
		let url = window.URL.createObjectURL(e.target.files.item(0));
		message.add("<img src=\"" + url + "\" width=100px height=100px>");
	},
	send: function () {
		let text = btoa(encodeURIComponent(message.msg.innerHTML));
		let msg = {
			user: info.user,
			rand: info.rand,
			to: messageList.target,
			type: "message",
			data: [text]
		};
		info.send(msg);
		let listMsg = {
			from: info.user,
			msg: message.msg.innerHTML,
			date: new Date()
		};
		messageList.add(listMsg);
		if(!messageList.msgs[messageList.target]){
			messageList.msgs[messageList.target]=[];
		}
		messageList.msgs[messageList.target].push(listMsg);
		message.msg.innerHTML = "";
	}
}
var messageList = {
	list: "",
	target: "1", // 当前和user1聊天
	main:"",
	msgs: {
		"1": [ // 和user1的聊天记录
			{
				from: "1",
				msg: "dfa",
				date: new Date()
			}
		],
		"2": [{
			from: "1",
			msg: "jfdlas",
			date: new Date()
		}],
		"3": [{
			from: "2",
			msg: "2432jfdsa",
			date: new Date()
		}]
	},
	getMessageList: function () {
		let msg = {
			user: info.user,
			rand: info.rand,
			to: "",
			type: "request-message-list",
			data: []
		};
		info.send(msg);

	},
	init: function () {
		messageList.list = document.getElementById("message-list");
		messageList.main = document.getElementsByClassName("main");
		console.log(messageList.main);
	},
	scrollToBottom: function () {
		messagesContainerTimer = setTimeout(() => {
			let m = document.getElementById("message-list");
			m.scrollTop = m.scrollHeight;
			// console.log(m.scrollTop, m.scrollHeight);
			// 清理定时器
			clearTimeout(this.messagesContainerTimer);
		}, 0);
	},
	add: function (msg) {
		card = document.createElement("div");
		card.setAttribute("class", "message-card");
		card.style.float = msg.from == info.user ? "right" : "left";
		card.style.clear = "both";
		content = document.createElement("div");
		content.innerHTML = msg.msg;
		date = document.createElement("div");
		date.innerHTML = msg.date;
		content.setAttribute("class", "message-content");
		date.setAttribute("class", "message-date");
		card.appendChild(content);
		card.appendChild(date);
		messageList.list.appendChild(card);
		messageList.scrollToBottom();
	},
	addOne: function (obj, from) {
		if (!messageList.msgs[obj.from]) {
			messageList.msgs[obj.from] = [];
		}
		obj.msg = decodeURIComponent(atob(obj.msg));

		messageList.msgs[obj.from].push(obj);
		if (from == messageList.target) {
			messageList.add(obj);
		}
	},
	refresh: function () {
		messageList.list.innerHTML = "";
		let ms = messageList.msgs[messageList.target];
		if (ms) {
			let l = ms.length;
			for (let i = 0; i < l; i++) {
				messageList.add(ms[i]);
			}
		}
	},
	receiveMessageList: function (data) {
		messageList.msgs = {};
		let len = data.length;
		for (let i = 0; i < len; i++) {
			if (!messageList.msgs[data[i].from]) {
				messageList.msgs[data[i].from] = [];
			}
			data[i].msg = decodeURIComponent(atob(data[i].msg))
			messageList.msgs[data[i].from].push(data[i]);
		}
		messageList.refresh();
	}
};
var friends = {
	list: ["1", "2", "3", "4"],
	listView: "",
	search: "",
	getFriendList: function () {
		let msg = {
			user: info.user,
			rand: info.rand,
			to: "",
			type: "request-friend-list",
			data: []
		};
		info.send(msg);
	},
	init: function () {
		friends.listView = document.getElementById("ts-radio-group-friend");
		friends.search = document.getElementById("search-bar");
		friends.listView.innerHTML = "";
		friends.refresh();
	},
	refresh: function () {
		friends.listView.innerHTML = "";
		let len = friends.list.length;
		for (let i = 0; i < len; i++) {
			let obj = "<div>\n";
			obj += "<input type=\"radio\" name=\"friend-target\" onclick=\"friends.setTarget(this)\" value=\"" + friends.list[i] + "\">\n";
			obj += "<label>" + friends.list[i] + "</label>\n";
			obj += "</div>\n";
			friends.listView.innerHTML += obj;
		}
	},
	add: function (friend) {

		friends.list.push(friend);
		let obj = "<div>\n";
		obj += "<input type=\"radio\" name=\"friend-target\" onclick=\"friends.setTarget(this)\" value=\"" + friend + "\">\n";
		obj += "<label>" + friend + "</label>\n";
		obj += "</div>\n";
		friends.listView.innerHTML += obj;
	},
	requestAdd: function () {
		let msg = {
			user: info.user,
			rand: info.rand,
			to: "",
			type: "friend-request",
			date: new Date(),
			data: [friends.search.value]
		};
		info.send(msg);
		friends.add(friends.search.value);
	},
	setTarget: function (e) {
		messageList.target = e.value;
		info.chatCur.innerHTML="正在和"+e.value+"聊天";
		messageList.refresh();
	}
};

var button = {
	init: function () {

		document.getElementById("send-text-button").onclick = message.send;
		document.getElementById("open-video-button").onclick = video.invite;
		document.getElementById("video-close-button").onclick = video.hangUpVideo;
		document.getElementById("logout-button").onclick = info.logout;
		document.getElementById("send-photo-button").onclick = function () {
			document.getElementById('image-input').click();
		}

		document.getElementById('image-input').onchange = message.addPhoto;
		//
		document.getElementById("search-bar").onkeydown = function (e) {
			if (e.code == "Enter") {
				friends.requestAdd();
			}
		}
		document.getElementById("send-emotion-button").onclick = emotion.switchVisible;
	}
};
var emotion = {
	v: false, // visible
	e: "", // emotion
	init: function () {
		var tab = document.createElement("table")
		let row = tab.insertRow(-1);

		for (let i = 0; i < 122; i++) {
			let cell = row.insertCell();
			cell.innerHTML = "<img src=\"../js/img/" + i + ".gif\" name=\"" + i + ".gif\" onclick=\"emotion.select(this)\">";
			if (i % 6 == 5) {
				row = tab.insertRow(-1);
			}
		}
		emotion.e = document.getElementById("emotion-table");
		emotion.e.appendChild(tab);
		emotion.e.style.display = 'none';
		visible = false;
	},
	show: function () {
		visible = true;
		emotion.e.style.display = 'inline';
	},
	close: function () {
		visible = false;
		emotion.e.style.display = 'none';
	},
	select: function (elem) {
		message.addEmotion(elem.name);
	},
	switchVisible: function () {
		emotion.v = !emotion.v;
		console.log(emotion.v);
		if (emotion.v) {
			emotion.show();
		} else {
			emotion.close();
		}
	},

}
var video = {
	target: "",
	selfPreview: "",
	targetPlayer: "",
	peerConnection: "",
	windwos: "",
	localStream: "",
	mediaConstraints: {
		audio: true,
		video: true
	},
	init: function () {
		video.selfPreview = document.getElementById("video-self-preview");
		video.targetPlayer = document.getElementById("video-target-player");
		video.windwos = document.getElementById("video-windows");
		video.hideWindow();
	},
	hideWindow(){
		video.windwos.style.display="none";
		info.videoCur.innerHTML="";
		let m = messageList.main;
		let len = m.length;
		for(let i=0;i<len;i++){
			m[i].style.right="0px";
		}
	},
	showWindow(){
		video.windwos.style.display="inline";
		info.videoCur.innerHTML="正在和"+video.target+"视频";
		let m = messageList.main;
		let len = m.length;
		for(let i=0;i<len;i++){
			m[i].style.right="21%";
		}
	},
	createRtcPeerConnection: function () {
		video.peerConnection = new RTCPeerConnection({
			iceServers: [{
				urls: "stun:stun.stunprotocol.org" 
			}]
		});

		video.peerConnection.onicecandidate = video.handleICECandidateEvent;
		video.peerConnection.ontrack = video.handleTrackEvent;
		video.peerConnection.onnegotiationneeded = video.handleNegotiationNeededEvent;
		video.peerConnection.onremovetrack = video.handleRemoveTrackEvent;
		video.peerConnection.oniceconnectionstatechange = video.handleICEConnectionStateChangeEvent;
		video.peerConnection.onicegatheringstatechange = video.handleICEGatheringStateChangeEvent;
		video.peerConnection.onsignalingstatechange = video.handleSignalingStateChangeEvent;
	},
	invite: function () {
		video.target = messageList.target;
		video.showWindow();
		video.createRtcPeerConnection();

		navigator.mediaDevices.getUserMedia(video.mediaConstraints)
			.then(function (localStream) {
				video.selfPreview.srcObject = localStream;
				video.peerConnection.addStream(localStream);
			})
			.catch(video.handleGetUserMediaError);
	},
	handleICECandidateEvent: function (event) {
		if (event.candidate) {
			info.send({
				user: info.user,
				type: "new-ice-candidate",
				rand: info.rand,
				to: video.target,
				data: [{
					candidate: event.candidate
				}]

			});
		}
	},
	handleTrackEvent: function (event) {
		video.targetPlayer.srcObject = event.streams[0];
		console.log(video.targetPlayer);
	},
	handleNegotiationNeededEvent: function () {
		video.peerConnection.createOffer().then(function (offer) {
			video.peerConnection.setLocalDescription(offer)
		}).then(function () {
			info.send({
				user: info.user,
				type: "video-offer",
				rand: info.rand,
				to: video.target,
				data: [{
					sdp: video.peerConnection.localDescription
				}]
			})
		})
	},
	handleRemoveTrackEvent: function () {
		let stream = video.targetPlayer.srcObject;
		let trackList = stream.getTracks();

		if (trackList.length == 0) {
			closeVideoCall();
		}
	},
	handleICEConnectionStateChangeEvent: function () {

	},
	handleICEGatheringStateChangeEvent: function () {

	},
	handleSignalingStateChangeEvent: function () {

	},
	handleVideoOfferMsg: function (msg) {
		video.target = msg.from;
		video.showWindow();
		targetUsername = msg.from;
		video.createRtcPeerConnection();

		let desc = new RTCSessionDescription(msg.data[0].sdp);
		console.log(msg)

		video.peerConnection.setRemoteDescription(desc).then(function () {
				return navigator.mediaDevices.getUserMedia(video.mediaConstraints);
			})
			.then(function (stream) {
				video.localStream = stream;
				video.selfPreview.srcObject = video.localStream;
				video.localStream.getTracks().forEach(track => video.peerConnection.addTrack(track, video.localStream));
			})
			.then(function () {
				return video.peerConnection.createAnswer();
			})
			.then(function (answer) {
				return video.peerConnection.setLocalDescription(answer);
			})
			.then(function () {
				let msg = {
					user: info.user,
					to: video.target,
					type: "video-answer",
					rand: info.rand,
					data: [{
						sdp: video.peerConnection.localDescription
					}]
				};

				info.send(msg);
			})
			.catch(video.handleGetUserMediaError);
	},
	handleVideoAnswerMsg:async function (msg) {
		let desc = new RTCSessionDescription(msg.data[0].sdp);
		console.log(desc);
		await video.peerConnection.setRemoteDescription(desc).catch(video.reportError);
	},
	handleNewICECandidateMsg: function (msg) {
		if (msg.data[0].candidate != null) {

			let candidate = new RTCIceCandidate(msg.data[0].candidate);
			console.log(candidate);
			video.peerConnection.addIceCandidate(candidate)
				.catch(video.reportError);
		}
	},
	reportError: function (e) {
		console.log("Report Error", e);
	},
	handleAddStreamEvent: function (event) {
		video.targetPlayer.srcObject = event.stream;
	},
	closeVideoCall: function () {
		video.hideWindow();
		let remoteVideo = video.targetPlayer;
		let localVideo = video.selfPreview;

		if (video.peerConnection) {
			video.peerConnection.ontrack = null;
			video.peerConnection.onremovetrack = null;
			video.peerConnection.onremovestream = null;
			video.peerConnection.onicecandidate = null;
			video.peerConnection.oniceconnectionstatechange = null;
			video.peerConnection.onsignalingstatechange = null;
			video.peerConnection.onicegatheringstatechange = null;
			video.peerConnection.onnegotiationneeded = null;

			if (remoteVideo.srcObject) {
				remoteVideo.srcObject.getTracks().forEach(track => track.stop());
			}

			if (localVideo.srcObject) {
				localVideo.srcObject.getTracks().forEach(track => track.stop());
			}

			video.peerConnection.close();
			video.peerConnection = null;
		}

		remoteVideo.removeAttribute("src");
		remoteVideo.removeAttribute("srcObject");
		localVideo.removeAttribute("src");
		remoteVideo.removeAttribute("srcObject");

	},
	handleGetUserMediaError: function (e) {
		switch (e.name) {
			case "NotFoundError":
				alert("Unable to open your call because no camera and/or microphone" +
					"were found.");
				break;
			case "SecurityError":
			case "PermissionDeniedError":
				// Do nothing; this is the same as the user canceling the call.
				break;
			default:
				alert("Error opening your camera and/or microphone: " + e.message);
				break;
		}

		video.closeVideoCall();
	},
	hangUpVideo: function () {
		video.closeVideoCall();
		info.send({
			user: info.user,
			to: video.target,
			rand: info.rand,
			type: "hang-up-video"
		});
	}
};
info.init();
message.init();
friends.init();
messageList.init();
button.init();
emotion.init();
video.init();


