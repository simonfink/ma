package clients;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


abstract class Client extends Thread {
	
	Socket socket;
	DataInputStream in;
	DataOutputStream out;
	PrintWriter output;
	int port;
	int transfers;

	final String serverIP = "localhost";

	public Client() {


	}
	
	public abstract void run();
	
	
	public void connect() {
		try {
			System.out.println("connecting to server");
			socket = new Socket(serverIP, port);
			
			System.out.println("connected");
			
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			
			output = new PrintWriter(out);
		}catch (Exception e) {
			System.out.println("failed to connect");
			e.printStackTrace();
		}
		
	}
	
	public void disconnect() throws Exception{
		System.out.println("disconnected");
		socket.close();
	}
	
	public void setTransfers(int transfers) {
		this.transfers = transfers;
	}
	
}
