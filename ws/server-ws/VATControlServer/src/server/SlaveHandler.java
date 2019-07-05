package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedHashMap;

import org.json.*;

import utils.SlaveDevice;
import utils.VATControlServerTools;
import utils.VDAQApplications;

class SlaveHandler extends Thread {
	Socket socket;
	DataInputStream in;
	DataOutputStream out;
	String ipAddr;
	double cpuUsage;
	double ramUsage;
	SlaveDevice sd;
	
	public SlaveHandler(Socket socket) {
		this.socket = socket;
		try {
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		}catch(Exception e) {
			e.printStackTrace();
		}

	}
	
	public void closeConnection() {
		System.out.println("closing connection");
		try {
			out.flush();
			socket.close();
			sd.updateVDAQStatus(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("got new slave");
		sd = new SlaveDevice();
		VATControlServerTools.devices.add(sd);
		while(true) {
			try {
				if(in.available() > 0) {
					
					String input = in.readLine();
					//System.out.println(input);
					
					if(input.equals("closing")) {
						break;
					}
					
					/*
					 * Status message layout
					 * {
					 * 		"ip":"127.0.1.1",
					 * 		"cpu":20.0,
					 * 		("ram": 14.0,)
					 * 		"applications":{
					 * 			"application1":"running",
					 * 			"application2":"idle",
					 * 			"application3":"idle"
					 * 		}
					 * 	}
					 * */

					try{
						JSONObject obj = new JSONObject(input);
						
						if(obj.has("ip")) {
							//System.out.println("has ip");
							sd.ipAddr = obj.getString("ip");
							//sd.registerVDAQ();
							out.writeChars("registered");
						}
						if(obj.has("cpu")) {
							//System.out.println("has cpu");
							sd.cpuUsage.add(Double.parseDouble(obj.getString("cpu")));
							//sd.updateVDAQStatus(false);
							out.writeChars("updated");
						}
						if(obj.has("applications")) {
							sd.apps.clear();
							//System.out.println("has applications");
							JSONArray applications = obj.getJSONArray("applications");
							
							for(int i = 0; i < applications.length(); i++) {
								//System.out.println("app length" + applications.length());
								String appName = applications.getJSONObject(i).getString("applicationname");
								if(appName.equals(""))continue;
								String appStatus = applications.getJSONObject(i).getString("status");
								if(appStatus.equals(""))continue;
								sd.apps.add(new VDAQApplications(appName, appStatus));
								//System.out.println("sd length: " + sd.apps.size());
								
								//System.out.println(appName + " " + appStatus);
							}
							
							sd.updateVDAQStatus(false);
								
							
						}
						if(obj.has("disconnect")) {
							System.out.println("has disconnect");
							sd.updateVDAQStatus(true);
							break;
						}
					}catch (Exception e) {
						//System.out.println("exception");
						//e.printStackTrace();
					}
				}else {
					try{
						sleep(2);
						out.writeChars("\r\n\r\n");
						out.flush();
						out.writeChars("\r\n\r\n");
						out.flush();
					}catch(IOException | InterruptedException e) {		// write to client fails after the 2nd write if tcp connection is lost
						//e.printStackTrace();
						//sd.updateVDAQStatus(true);
						break;
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		//System.out.println("ipAddr: " + ipAddr);
		closeConnection();
		
	}
	
	
	
}