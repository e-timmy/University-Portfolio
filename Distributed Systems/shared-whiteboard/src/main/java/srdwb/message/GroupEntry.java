package srdwb.message;

public class GroupEntry {
	public String groupUUID;
	public String groupName;
	public String groupSecret;
	public User master;
	
	public GroupEntry() {
		
	}
	
	public GroupEntry(String groupUUID, String groupName, String groupSecret, User master) {
		this.groupUUID = groupUUID;
		this.groupName = groupName;
		this.groupSecret = groupSecret;
		this.master = master;
	}
}
