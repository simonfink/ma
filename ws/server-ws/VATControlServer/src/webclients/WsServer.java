package webclients;
 
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.websocket.Session;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import utils.SlaveDevice;
import utils.VATControlServerTools;
import webutils.User;
import webutils.User.UserStatus;
import webutils.WebUtils;

 
@ServerEndpoint("/websocketendpoint/{username}")
public class WsServer {
	public static HashMap<Session, User> sessionUserList = new HashMap<>();
	
    @OnOpen
    public void onOpen(@PathParam("username") String username, Session session){
    	sessionUserList.put(session, WebUtils.getUserByName(username));
    	updateAllUsers(session);
    	updateSingleUser(session);
        System.out.println("Open Connection ...:" + sessionUserList.size());
    }
     
    @OnClose
    public void onClose(Session session){
    	User discoUser =  sessionUserList.get(session);
    	discoUser.disconnectUser();
    	sessionUserList.remove(session);
    	
        System.out.println("Close Connection ...:" + sessionUserList.size());
        
        try {
    		if(sessionUserList != null && sessionUserList.size() > 0) {
    			String userData = "";
    			if(WebUtils.users.size() > 0) {
    				System.out.println("users size:" + WebUtils.users.size());
    				
    				userData = "{\"user\":{\"name\":\""+discoUser.name+"\",\"ipAddr\":\""+discoUser.ipAddr+"\",\"remove\":\"0\"}}";
    			}
    			System.out.println("userData: " + userData);
				for(Session s : sessionUserList.keySet()) {
					s.getBasicRemote().sendText(userData);
				}
    		}
		} catch (IOException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
    }
     
    @OnMessage
    public void onMessage(String message){
        System.out.println("Message from the client: " + message);
        String[] split = message.split(":");
        for(User u : WebUtils.users) {
        	System.out.println("for each");
        	
        	if(split[0].equals(u.name)) {
        		System.out.println(split[0] + ":" + split[1]);
            	if(split[1].equals("testArea")){
            		u.currentStatus = UserStatus.VIEW_TESTS;
            		System.out.println("changed status from user " + u.name + " to VIEW_TESTS");
            		break;
            	}else if(split[1].equals("remoteMGMT")) {
            		u.currentStatus = UserStatus.VIEW_REMOTEMANAGEMENT;
            		System.out.println("changed status from user " + u.name + " to VIEW_REMOTEMANAGEMENT");
            	}else if(split[1].equals("fileUpload")) {
            		u.currentStatus = UserStatus.UPLOAD_FILE;
            		System.out.println("changed status from user " + u.name + " to UPLOAD_FILE");
            	}
        	}
        }
        //return echoMsg;
    }
    
    
 
    @OnError
    public void onError(Throwable e){
        e.printStackTrace();
    }
    
    public void updateUserStatus() {
    	try {
    		if(sessionUserList != null && sessionUserList.size() > 0) {
    			
    			String testData = "{\"user\":{\"name\":\"testName\",\"ipAddr\":\"127.0.0.1\"}}";
    			
    			String userData = "";
    			if(WebUtils.users.size() > 0) {
    				System.out.println("users size:" + WebUtils.users.size());
	    			for(int i = 0; i < WebUtils.users.size(); i++) {
	    				userData = "{\"user\":{\"name\":\""+WebUtils.users.get(i).name+"\",\"ipAddr\":\""+WebUtils.users.get(i).ipAddr+"\"}}";
	    				//userData += WebUtils.users.get(i).name + "\r\n";
	    			}
    			}
    			System.out.println("testData: " + testData);
    			System.out.println("userData: " + userData);
				for(Session s : sessionUserList.keySet()) {
					s.getBasicRemote().sendText(userData);
				}
    		}
		} catch (IOException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void updateSingleUser(Session session) {		// updates 1 new user to all active users
    	try {
    		if(sessionUserList != null && sessionUserList.size() > 0) {
    			String userData = "";
				for(Session s : sessionUserList.keySet()) {
					if(s == session) continue;
					userData = "{\"user\":{\"name\":\""+sessionUserList.get(session).name+"\",\"ipAddr\":\""+sessionUserList.get(session).ipAddr+"\"}}";
					System.out.println("userData single to " + sessionUserList.get(s).name + ": " + userData);
					s.getBasicRemote().sendText(userData);
				}
    		}
		} catch (IOException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void updateAllUsers(Session s) {		// updates all active users to the new user
    	try {
    		if(sessionUserList != null && sessionUserList.size() > 0) {
    			
    			String userData = "";
    			
    			for(int i = 0; i < WebUtils.users.size(); i++) {
    				if(WebUtils.users.get(i) != sessionUserList.get(s)) {
	    				System.out.println("users size:" + WebUtils.users.size());
	    				userData = "{\"user\":{\"name\":\""+WebUtils.users.get(i).name+"\",\"ipAddr\":\""+WebUtils.users.get(i).ipAddr+"\"}}";
	    				System.out.println(i+" userData all: " + userData);
	    				s.getBasicRemote().sendText(userData);
    				}
    			}
    			
    			for(int i = 0; i < VATControlServerTools.devices.size(); i++) {
    				VATControlServerTools.devices.get(i).registerVDAQ(s);
    			}
					
    		}
		} catch (IOException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    	
}