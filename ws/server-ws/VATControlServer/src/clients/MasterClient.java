package clients;

import java.io.IOException;
import java.net.UnknownHostException;

public class MasterClient extends Client{

	public MasterClient(){
		super();
		this.port = 2345;
		connect();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
