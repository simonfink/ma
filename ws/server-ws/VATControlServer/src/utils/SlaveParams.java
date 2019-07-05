package utils;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.sun.management.OperatingSystemMXBean;

public class SlaveParams {
	String ipAddr;
	
	OperatingSystemMXBean osBean;
	InetAddress inetAddress;
	double cpuUsage = 0.0;
	double ramUsage = 0.0;
	boolean testRunning = false;
	
	public SlaveParams() {
		inetAddress = null;
		try {
			inetAddress = InetAddress.getLocalHost();
			ipAddr = inetAddress.getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*if(inetAddress != null) {
        System.out.println("IP Address:- " + inetAddress.getHostAddress());
        System.out.println("Host Name:- " + inetAddress.getHostName());
		}*/
		
		osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
		// What % CPU load this current JVM is taking, from 0.0-1.0
		//System.out.println(osBean.getProcessCpuLoad());
		
		// What % load the overall system is at, from 0.0-1.0
		//System.out.println(osBean.getSystemCpuLoad());
	}
	
	public String getIpAddr() {
		return ipAddr;
	}
	
	public String getCPU() {
		cpuUsage = osBean.getSystemCpuLoad();
		return String.format("%.1f", (cpuUsage*100));
	}
	
	public String getRAM() {
		ramUsage = (osBean.getFreePhysicalMemorySize() - osBean.getTotalPhysicalMemorySize())/osBean.getTotalPhysicalMemorySize();
		return String.format("%.1f", (ramUsage*100));
	}

}
