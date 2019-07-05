package webclients;
 
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.ServerEndpoint;

import webutils.User;
import webutils.WebUtils;
 
@ServerEndpoint("/controlendpoint")
public class VDAQDisplay {
     
    @OnOpen
    public void onOpen(){
        System.out.println("Open Connection ...");
    }
     
    @OnClose
    public void onClose(){
        System.out.println("Close Connection ...");
    }
     
    @OnMessage
    public String onMessage(String message){
        System.out.println("Message from the client: " + message);
        String echoMsg = "Echo from the server : " + message;
        return echoMsg;
    }
 
    @OnError
    public void onError(Throwable e){
        e.printStackTrace();
    }
    
    public String updateUser() {
    	String data = "";
    	for(User u : WebUtils.users) {
    		data += u.name+"\r\n";
    	}
    	return data;
    }
 
}