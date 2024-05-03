package srdwb.message;

/**
 * For updating clients about other members
 * Protected representation of GroupMember
 */
public class User {
	public String ip;
	public int port;
	public String name;
	public String uuid;
	public String status;
	
	public User() {
		ip = "unknown";
		port = -1;
		name = "unknown";
		uuid = "invalid";
		status = "Unassigned";
	}
	
	public User(String ip, int port, String name, String uuid) {
		this.ip = ip;
		this.port = port;
		this.name = name;
		this.uuid = uuid;
	}

	/**
	 * Status reflects type of membership
	 * @param status
	 */
	public void updateStatus(String status) {
		this.status = status;
	}

}
