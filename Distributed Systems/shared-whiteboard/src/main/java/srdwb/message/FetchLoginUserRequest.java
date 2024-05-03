package srdwb.message;

@JsonSerializable
public class FetchLoginUserRequest extends Message{

	@JsonElement
	public String message;
	
	public FetchLoginUserRequest() {
		
	}
	
	public FetchLoginUserRequest(String message) {
		this.message = (message == null)?"":message;
	}
	
}
