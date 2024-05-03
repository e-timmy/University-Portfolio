package srdwb.message;

@JsonSerializable
public class CloseGroupReply extends Message{

	@JsonElement
	public String message;
	@JsonElement
	public Boolean outcome;
	
	public CloseGroupReply() {
		
	}
	
	public CloseGroupReply(String message, Boolean outcome) {
		this.message = message;
		this.outcome = outcome;
	}
}
