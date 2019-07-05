package server;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class ControlServer {
	
	static ArrayList<SlaveHandler> slaveList;

	public ControlServer() throws IOException{
		System.out.println("Starting Control Server");
		
		slaveList = new ArrayList<>();

		Thread slaveThread = new Thread(){
		    public void run(){
		    	System.out.println("starting slave service");
		    	try {
		    		ServerSocket slaveListener = new ServerSocket(1234);
		    		while(true) {
		    			SlaveHandler slave = new SlaveHandler(slaveListener.accept());
		    			if(slave != null) {
		    				slaveList.add(slave);
		    				slave.start();
		    			}
					} 
					
		        }catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		      }
	    };
	    
	    slaveThread.start();

	}	
}






