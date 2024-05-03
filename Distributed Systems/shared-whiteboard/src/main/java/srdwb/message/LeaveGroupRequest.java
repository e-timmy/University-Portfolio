package srdwb.message;

@JsonSerializable
public class LeaveGroupRequest extends Message{

	@JsonElement
	public String message;

	public LeaveGroupRequest() {}
	
	public LeaveGroupRequest(String message) {
		this.message = message == null ? "" : message;
	}
}
