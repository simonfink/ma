package utils;

import java.io.IOException;
import java.util.ArrayList;

import javax.websocket.Session;

import org.json.*;

import webclients.WsServer;

public class SlaveDevice {
	public String ipAddr;
	public ArrayList<Double> cpuUsage;
	public ArrayList<Double> memoryUsage;
	public ArrayList<VDAQApplications> apps;
	
	public SlaveDevice() {
		cpuUsage = new ArrayList();
		memoryUsage = new ArrayList();
		apps = new ArrayList<>();
	}
	
	public void registerVDAQ() {
		try {
    		if(WsServer.sessionUserList != null && WsServer.sessionUserList.size() > 0) {
    			String vdaqData = "{\"vdaq\":{\"name\":\""+this.hashCode()+"\",\"ipAddr\":\""+ this.ipAddr + "\"}}";
    			
    			System.out.println("vdaqData: " + vdaqData);
				for(Session s : WsServer.sessionUserList.keySet()) {
					s.getBasicRemote().sendText(vdaqData);
				}
    		}
		} catch (IOException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void registerVDAQ(Session s) {
		try {
    		if(WsServer.sessionUserList != null && WsServer.sessionUserList.size() > 0) {
    			String vdaqData = "{\"vdaq\":{\"name\":\""+this.hashCode()+"\",\"ipAddr\":\""+ this.ipAddr + "\"}}";
				s.getBasicRemote().sendText(vdaqData);
    		}
		} catch (IOException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateVDAQStatus(boolean disconnect) {
		//System.out.println("update status");
    	try {
    		if(WsServer.sessionUserList != null && WsServer.sessionUserList.size() > 0) {
        		JSONObject jsonString = new JSONObject();
        		
        		jsonString.put("vdaq", true);
        		jsonString.put("name", this.hashCode());
        		jsonString.put("ipAddr", this.ipAddr);
        		
    			if(disconnect) {
    				jsonString.put("remove", true);
				}else {
    				jsonString.put("cpu", cpuUsage.get(cpuUsage.size()-1));
    				JSONArray array = new JSONArray();
    				String overallStatus = "";
    				boolean liveRun = false;
    				boolean isRunning = false;
    				JSONObject app = new JSONObject();
    				for(int i = 0; i < apps.size(); i++) {
    					//System.out.println(apps.get(i).status);
    					if(apps.get(i).status.equals("liverun"))liveRun = true;
    					if(apps.get(i).status.equals("running"))isRunning = true;
    					app.put("applicationname", apps.get(i).name);
    					app.put("status", apps.get(i).status);
    					array.put(app);
    					
    					//appObject.put(apps.get(i).name, apps.get(i).status);
    				}
    				
    				if(liveRun)overallStatus = "live run";
    				else if(isRunning)overallStatus = "running";
    				else overallStatus = "idle";
    				
    				jsonString.put("vdaqstatus", overallStatus);
    				
    				jsonString.put("applications", array);

    			}
    			
    			System.out.println("jsonString: " + jsonString.toString());
				for(Session s : WsServer.sessionUserList.keySet()) {
					s.getBasicRemote().sendText(jsonString.toString());
				}
    		}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

}
