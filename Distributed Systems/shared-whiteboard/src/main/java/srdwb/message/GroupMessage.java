package srdwb.message;

@JsonSerializable
public class GroupMessage extends Message{

	@JsonElement
	public User sender;
	@JsonElement
	public String message;
	public GroupMessage() {
		
	}
	public GroupMessage(User sender, String message) {
		this.sender = sender;
		this.message = message == null ? "" : message;
	}
}
