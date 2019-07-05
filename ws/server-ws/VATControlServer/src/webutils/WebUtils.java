package webutils;

import java.util.ArrayList;

public class WebUtils {
	public static ArrayList<User> users = new ArrayList<User>();
	
	public static User getUserByName(String username) {
		for(int i = 0; i < users.size(); i++) {
			if(users.get(i).name.equals(username))
				return users.get(i);
		}
		return null;
	}
	
}
