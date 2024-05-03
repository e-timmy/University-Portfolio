package srdwb.message;

@JsonSerializable
public class LeaveGroupNotification extends Message {

	@JsonElement
	public User user;
	@JsonElement
	public String message;

	public LeaveGroupNotification() {
		
	}
	
	public LeaveGroupNotification(User user, String message) {
		this.user = user;
		this.message = message == null ? "" : message;
	}
}
