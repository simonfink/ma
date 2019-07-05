package clients;


import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.sun.management.OperatingSystemMXBean;

import utils.SlaveParams;

public class SlaveClient extends Client{
	SlaveParams slave;
	
	
	public SlaveClient(){
		super();
		this.port = 1234;
		slave = new SlaveParams();
		
		//System.out.println("new slave");

		connect();
		//this.start();
	}
	
	public void connect() {
		super.connect();
		output.println("ip:"+slave.getIpAddr());
		output.flush();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while((this.transfers--)>0) {
			//System.out.println("transmitting data");
			output.println("cpu:"+slave.getCPU());
			output.flush();
			//output.println("cpu:"+osBean.getSystemCpuLoad());
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			disconnect();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
