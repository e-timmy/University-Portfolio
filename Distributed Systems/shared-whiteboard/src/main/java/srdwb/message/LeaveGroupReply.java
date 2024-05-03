package srdwb.message;

@JsonSerializable
public class LeaveGroupReply extends Message {

	@JsonElement
	public String message;
	
	public LeaveGroupReply() {
		
	}
	
	public LeaveGroupReply(String message) {
		this.message = message == null ? "" : message;
	}

}
