package srdwb.message;

@JsonSerializable
public class CloseGroupRequest extends Message{

	@JsonElement
	public String message;
	
	public CloseGroupRequest() {
		
	}
	
	public CloseGroupRequest(String message) {
		this.message = (message == null) ? "" : message;
	}
}
