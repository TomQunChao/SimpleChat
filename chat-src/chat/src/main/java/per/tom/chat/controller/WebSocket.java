package per.tom.chat.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import per.tom.chat.model.Friends;
import per.tom.chat.model.HistoryMessage;
import per.tom.chat.model.LoginUser;
import per.tom.chat.model.MessageToReceive;
import per.tom.chat.model.RegisterUser;
import per.tom.chat.repo.FriendsRepo;
import per.tom.chat.repo.HistoryMessageRepo;
import per.tom.chat.repo.LoginUserRepo;
import per.tom.chat.repo.MessageToReceiveRepo;
import per.tom.chat.repo.RegisterUserRepo;

@ServerEndpoint(value = "/ws/{id}")
public class WebSocket {

	private static FriendsRepo mFriendsRepository = new FriendsRepo();
	private static HistoryMessageRepo mHistoryMessageRepo = new HistoryMessageRepo();
	private static MessageToReceiveRepo mMessageToReceiveRepo = new MessageToReceiveRepo();
	private static LoginUserRepo mLoginUserRepo = new LoginUserRepo();
	private static RegisterUserRepo mRegisterUserRepo = new RegisterUserRepo();

	private static ConcurrentMap<String, WebSocket> mWebSocketMap = new ConcurrentHashMap<String, WebSocket>();

	private Session session;
	private String user;

	@OnOpen
	public void onOpen(Session session, @PathParam(value = "id") String user) {
		session.setMaxTextMessageBufferSize(819200);
		System.out.println("max buffer: " + session.getMaxTextMessageBufferSize());
		this.session = session;
		this.user = user;
		mWebSocketMap.put(user, this);
		System.out.println(user + " connected");
	}

	@OnClose
	public void onClose() {
		System.out.println("Session closed");
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		System.out.println("message" + message);
		try {
			JSONObject obj = JSONObject.parseObject(message);
			if(obj.getString("type")=="logout"){
				logout(obj);
			}
			if (!vertifyRand(obj.getString("user"), obj.getString("rand"))) {
				sendError("Random vertify failed");
				return;
			}
			switch (obj.getString("type")) {
			case "request-friend-list":
				sendFriendList(obj);
				break;
			case "request-message-list":
				sendToReceiveMessageList(obj.getString("user"));
				break;
			case "friend-request":
				addFriend(obj);
				break;
			case "message":
				sendMessage(obj);
				break;
			case "video-offer":
			case "video-answer":
			case "new-ice-candidate":
			case "hang-up-video":
				forwardMessage(obj);
				break;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@OnError
	public void onError(Session session, Throwable error) {
		System.out.println("Error occured");
		error.printStackTrace();
		mWebSocketMap.remove(this.user);
	}

	public void send(String message) {
		try {
			this.session.getBasicRemote().sendText(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void logout(JSONObject obj){
		if(Utility.vertifyUser(obj.getString("user"),obj.getString("rand"))){
			mWebSocketMap.remove(obj.getString("user"));
		}
	}
	public void sendMessage(JSONObject obj) {
		if(!vertifyRand(obj.getString("user"),obj.getString("rand"))) {
			sendError("Rand Vertify failed");
		}
		String user = obj.getString("user");
		JSONArray ja = JSONArray.parseArray(obj.getString("data"));
		if(ja.size()<=0)return;
		String message = ja.getString(0);
		String to = obj.getString("to");
		List<Friends> fs = mFriendsRepository.findByHostAndFriend(user, to);
		if(fs.size()<=0) {
			sendError("Your not friend of him/her, please add frist");
			return;
		}
		if(mWebSocketMap.containsKey(to)) {
			JSONObject msgObj = new JSONObject();
			msgObj.put("from",user);
			msgObj.put("msg",message);
			msgObj.put("date",new Date().toString());
//			System.out.println(msgObj);
			
			JSONObject toObj = new JSONObject();
			toObj.put("from",user);
			toObj.put("type","message");
			ja = new JSONArray();
			ja.add(msgObj);
			toObj.put("data",ja);
			mWebSocketMap.get(to).send(toObj.toString());
			
			HistoryMessage hm = new HistoryMessage();
			hm.setId(mHistoryMessageRepo.selectMaxId()+1);
			hm.setMessage(message);
			hm.setSendDate(new Date());
			hm.setReadDate(new Date());
			hm.setReceiveUser(to);
			hm.setSendUser(user);
			mHistoryMessageRepo.save(hm);
		}else {
			MessageToReceive mtr = new MessageToReceive();
			mtr.setDate(new Date());
			mtr.setId(mMessageToReceiveRepo.selectMaxId()+1);
			mtr.setReceiveUser(to);
			mtr.setSendUser(user);
			mtr.setMessage(message);
			mMessageToReceiveRepo.save(mtr);
		}
	}
	public boolean vertifyRand(String user, String rand) {
		if(rand==null)return false;
		LoginUser lu = mLoginUserRepo.findByUserName(user).get(0);
		return lu.getRand() == Integer.parseInt(rand);
	}

	public void sendError(String error) {
		JSONObject obj = new JSONObject();
		obj.put("type", "error");
		obj.put("data", "[\"" + error + "\"]");
		send(obj.toString());
	}

	public void sendFriendList(JSONObject uObj) {
		if(!vertifyRand(uObj.getString("user"),uObj.getString("rand"))) {
			sendError("Rand Vertify failed");
			return;
		}
		JSONObject obj = new JSONObject();
		List<Friends> fl = mFriendsRepository.findByFriend(user);
		JSONArray ja = new JSONArray();
		
		for (Friends f : fl) {
			ja.add(f.getFriend());
		}
		obj.put("type","friend-list");
		obj.put("data",ja);
		send(obj.toString());
	}

	public void sendToReceiveMessageList(String user) {
		JSONObject obj = new JSONObject();
		List<MessageToReceive> mtrs = mMessageToReceiveRepo.findByReceiveUser(user);
		JSONArray ja = new JSONArray();
		for(MessageToReceive mtr:mtrs) {
			JSONObject mtrObj = new JSONObject();
			mtrObj.put("from",mtr.getSendUser());
			mtrObj.put("msg",mtr.getMessage());
			mtrObj.put("date",mtr.getDate().toString());
			ja.add(mtrObj);
			
			HistoryMessage hm = new HistoryMessage();
			hm.setId(mHistoryMessageRepo.selectMaxId()+1);
			hm.setMessage(mtr.getMessage());
			hm.setReadDate(new Date());
			hm.setSendDate(mtr.getDate());
			hm.setReceiveUser(mtr.getReceiveUser());
			hm.setSendUser(mtr.getSendUser());
			mHistoryMessageRepo.save(hm);
			mMessageToReceiveRepo.deleteById(mtr.getId());
		}
		obj.put("type", "message-list");
		obj.put("data",ja);
		send(obj.toString());
	}
	public void addFriend(JSONObject obj) {
		
		Friends f = new Friends();
		f.setHost(obj.getString("user"));
		// 获取Friend
		JSONArray ja = JSONArray.parseArray(obj.getString("data"));
		if(ja.size()<=0)return;
		String friend = ja.getString(0);
		
		List<RegisterUser> rus = mRegisterUserRepo.findByUserName(friend);
		if(rus.size()<=0) {
			sendError("Target not Found");
			return;
		}
		f.setFriend(friend);
		mFriendsRepository.save(f);
		if(mWebSocketMap.containsKey(f.getFriend())) {
			WebSocket ws = mWebSocketMap.get(f.getFriend());
			obj.clear();
			obj.put("type","friend-request");
			obj.put("data",f.getFriend());
			obj.put("from",f.getHost());
			if(ws!=null)ws.send(obj.toString());
		}
		
	}
	public void forwardMessage(JSONObject obj) {
		System.out.println("Forwarding: "+obj.toString());
		if(!mWebSocketMap.containsKey(obj.getString("to"))) {
			sendError("Target Not online");
			return;
		}
		JSONObject toObj = new JSONObject();
		toObj.put("from",obj.getString("user"));
		toObj.put("type",obj.getString("type"));
		toObj.put("data",obj.get("data"));
		mWebSocketMap.get(obj.getString("to")).send(toObj.toString());
	}

}
