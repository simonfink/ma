package Server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import Server.SFMPUtils;

public class SFMPServer {
	
	static ArrayList<ClientHandler> clientList;

	public static void main(String[] args) throws IOException{
		System.out.println("Starting SFMP Server");
		ServerSocket listener = new ServerSocket(SFMPUtils.PORT);
		
		clientList = new ArrayList<>();
		
		try {
			while(true) {
				ClientHandler client = new ClientHandler(listener.accept());
				clientList.add(client);
				//System.out.println("got new client");
			}
			
		}finally {
			listener.close();
		}
	}
	
}

class ClientHandler extends Thread {
	int mode;
	String topic;
	String clientID;
	Socket socket;
	DataInputStream in;
	DataOutputStream out;
	//PrintWriter output;
	long uniqueID;
	static long uniqueIDCounter = 0;
	ArrayList<V1Payload> data;
	
	public ClientHandler(Socket socket) {
		this.socket = socket;
		uniqueID = uniqueIDCounter;
		uniqueIDCounter++;
		data = new ArrayList<>();
		try {
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			//output = new PrintWriter(socket.getOutputStream(), true);
			byte code = readCode();
			in.readLine();
			System.out.println("code: " + code + ", Connect: " + SFMPUtils.CONNECT);
			if(code == SFMPUtils.CONNECT) {
				System.out.println("got connect");
				while(in.available() < 1);// System.out.println("client");
				this.clientID = in.readLine();
				System.out.println("clientid: " + clientID);
				while(in.available() < 1);// System.out.println("user");
				String username = in.readLine();
				System.out.print(username+":");
				while(in.available() < 1);// System.out.println("password");
				String password = in.readLine();
				System.out.println(password);
				while(in.available() < 1);// System.out.println("topic");
				this.topic = in.readLine();
				System.out.println(topic);
				//System.out.println("published to topic: " + topic);


				//System.out.println("clientID: " + clientID);
				if(correctUser(username, password)){
					System.out.println("correct user");
					sendCode(SFMPUtils.CONACK);
					out.flush();
					this.start();
				}else {
					closeConnection();
				}
				
				
			}else {
				closeConnection();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void closeConnection() throws IOException {
		System.out.println("closing connection");
		sendCode(SFMPUtils.CONNAK);
		out.flush();
		socket.close();
		SFMPServer.clientList.remove(this);
	}
	
	private boolean correctUser(String username, String password) {
		return true;
	}
	
	public void run() {
		System.out.println("running");
		try {
			while(true) {
			
				if(in.available() > 0) {
					System.out.println("input available");
					byte typeByte = readCode();
					
					System.out.println(""+typeByte);
					
					switch(typeByte) {
					case SFMPUtils.PUBLISH:
						System.out.println("got new publish");
						V1Payload payload = new V1Payload();
						payload.timestamp = in.readLong();
						System.out.println("ts: " + payload.timestamp);
						payload.tsSubMilli = in.readInt();
						System.out.println("tsSubMilli: " + payload.tsSubMilli);
						payload.temp1 = in.readFloat();
						System.out.println("temp1: " + payload.temp1);
						payload.temp2 = in.readFloat();
						System.out.println("temp2: " + payload.temp2);
						payload.accx = in.readFloat();
						System.out.println("accx: " + payload.accx);
						payload.accy = in.readFloat();
						System.out.println("accy: " + payload.accy);
						payload.accz = in.readFloat();
						System.out.println("accz: " + payload.accz);
						
						/*System.out.println("ts bin: " + Long.toBinaryString(payload.timestamp));
						System.out.println("tssm bin: " + Integer.toBinaryString(payload.tsSubMilli));
						System.out.println("temp1 hex: " + Float.toHexString(payload.temp1));
						System.out.println("temp2 hex: " + Float.toHexString(payload.temp2));
						System.out.println("accx hex: " + Float.toHexString(payload.accx));
						System.out.println("accy hex: " + Float.toHexString(payload.accy));
						System.out.println("accz hex: " + Float.toHexString(payload.accz));*/
						
						data.add(payload);
						sendCode(SFMPUtils.PUBACK);
						out.flush();
						System.out.println("published new data to: " + topic);
						break;
					case SFMPUtils.DISCONNECT:
						System.out.println("disconnecting");
						sendCode(SFMPUtils.DISCONACK);
						out.flush();
						socket.close();
						break;
					}
				}

			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			System.out.println("catch block");
			e1.printStackTrace();
		}finally {
			try {
				System.out.println("disconnecting");
				this.socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	private void sendCode(byte code) throws IOException {
		out.writeByte(code);
	}
	private byte readCode() throws IOException {
		return in.readByte();
	}
	
	public static void safePrint(String s) {
		synchronized (System.out) {
			System.out.println(s);
		}
	}
}
