package Server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Stream;

public class OwnServerTest {
	
/*	static ArrayList<ClientHandler> clientList;

	public static void main(String[] args) throws IOException{
		System.out.println("Starting SFMP Server");
		ServerSocket listener = new ServerSocket(9090);
		BufferedReader input;
		
		clientList = new ArrayList<>();
		
		try {
			while(true) {
				ClientHandler client = new ClientHandler(listener.accept());
				clientList.add(client);
				System.out.println("got new client");
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
	PrintWriter output;
	long uniqueID;
	static long uniqueIDCounter = 0;
	
	public ClientHandler(Socket socket) {
		this.socket = socket;
		uniqueID = uniqueIDCounter;
		uniqueIDCounter++;
		try {
			in = new DataInputStream(socket.getInputStream());
			this.topic = in.readLine();
			System.out.println("published to topic: " + topic);
			this.clientID = in.readLine();
			System.out.println("clientID: " + clientID);
			//this.mode = Integer.parseInt(input.readLine());
			output = new PrintWriter(socket.getOutputStream(), true);
			output.println("CONNECTED");
			this.start();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		try {
			
			while(true) {
			
				if(in.available() > 2) {
					byte typeByte = in.readByte();
					//System.out.println("typeByte: " + typeByte);
					byte lengthByte = in.readByte();
					//System.out.println("lengthByte: " + lengthByte);
					
					String data = "";
					
					for(int i = 0; i < lengthByte; i++) {
						byte inChar = in.readByte();
						
						//System.out.print(inChar);
						
						data+=inChar;
					}
					safePrint(uniqueID+"\t"+data);
					
					String command = in.readLine();
					if(command != null) {
						safePrint(topic+":  " +command);
					
						if(command.equals("QUIT")) {
							output.println("CLOSE");
							safePrint("closing connection: " + uniqueID);
							//this.socket.close();
							break;
						}
					}
				}

			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			this.socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void safePrint(String s) {
		synchronized (System.out) {
			System.out.println(s);
		}
	}*/
}
