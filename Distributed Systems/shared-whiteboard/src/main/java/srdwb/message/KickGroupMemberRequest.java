package srdwb.message;

@JsonSerializable
public class KickGroupMemberRequest extends Message{

	@JsonElement
	public String message;
	@JsonElement
	public User userToKick;

	public KickGroupMemberRequest() {
		
	}
	public KickGroupMemberRequest(User userToKick, String message) {
		this.message = (message == null) ? "" : message;
		this.userToKick = userToKick;
	}
}
