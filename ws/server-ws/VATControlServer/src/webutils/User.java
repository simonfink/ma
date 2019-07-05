package webutils;

public class User {
	private static int idCounter = 0;
	public String name;
	public String ipAddr;
	public int id;
	public UserStatus currentStatus;
	
	public enum UserStatus{
		VIEW_REMOTEMANAGEMENT,
		VIEW_TESTS,
		CREATE_TEST,
		RUN_TEST,
		UPLOAD_FILE
	}
	
	public User(String name, String ipAddr) {
		this.name = name;
		this.ipAddr = ipAddr;
		this.currentStatus = UserStatus.VIEW_REMOTEMANAGEMENT;
		
		this.id = getFreeId();

	}
	
	private int getFreeId() {
		if(WebUtils.users.size() > 0) {
			for(int i = 0; i < WebUtils.users.size(); i++) {
				if(WebUtils.users.get(i).id != i) {
					idCounter = i;
				}
			}
		}
		
		return idCounter;
	}
	
	public void disconnectUser() {
		WebUtils.users.remove((int)this.id);
	}


}
