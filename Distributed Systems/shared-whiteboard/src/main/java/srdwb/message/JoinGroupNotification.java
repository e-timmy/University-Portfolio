package srdwb.message;

@JsonSerializable
public class JoinGroupNotification extends Message{

	@JsonElement
	public User user;
	@JsonElement
	public String message;
	
	public JoinGroupNotification() {
		
	}
	
	public JoinGroupNotification(User user, String message) {
		this.user = user;
		this.message = message == null ? "" : message;
	}
}
