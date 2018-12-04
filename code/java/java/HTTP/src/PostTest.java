import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class PostTest {
	
	static String data = "{\"properties\":{},\"routing_key\":\"test.smartvalve\",\"payload\":\"java http post test\",\"payload_encoding\":\"string\"}";
	static String path = "/api/exchanges/%2f/amq.topic/publish";
	static String username = "fsi";
	static String password = "fsi";

	public static void main(String[] args) {
		System.out.println(executePost(("http://172.18.252.25:15672"+path), data));
	}
	
	public static String executePost(String targetURL, String urlParameters) {
		  HttpURLConnection connection = null;
		  
		  String userPass = username + ":" + password;
		  String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userPass.getBytes()));

		  try {
		    //Create connection
		    URL url = new URL(targetURL);
		    connection = (HttpURLConnection) url.openConnection();
		    connection.setRequestMethod("POST");
		    connection.setRequestProperty("Authorization", basicAuth);
		    connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
		    connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
		    connection.setRequestProperty("Accept", "*/*");
   
		    connection.setRequestProperty("Content-Length",Integer.toString(urlParameters.getBytes().length));

		    connection.setUseCaches(false);
		    connection.setDoOutput(true);

		    //Send request
		    DataOutputStream wr = new DataOutputStream (
		        connection.getOutputStream());
		    wr.writeBytes(urlParameters);
		    wr.close();

		    //Get Response  
		    InputStream is = connection.getInputStream();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		    StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
		    String line;
		    while ((line = rd.readLine()) != null) {
		      response.append(line);
		      response.append('\r');
		    }
		    rd.close();
		    return response.toString();
		  } catch (Exception e) {
		    e.printStackTrace();
		    return null;
		  } finally {
		    if (connection != null) {
		      connection.disconnect();
		    }
		  }
		}
}