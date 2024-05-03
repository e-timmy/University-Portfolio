package srdwb.message;

@JsonSerializable
public class ErrorMsg extends Message {

	@JsonElement
	public String message;
	
	public ErrorMsg() {
		
	}
	
	public ErrorMsg(String message) {
		this.message = message == null ? "" : message;
	}
	
}
