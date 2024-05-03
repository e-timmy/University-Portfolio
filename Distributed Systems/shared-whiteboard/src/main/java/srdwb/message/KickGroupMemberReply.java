package srdwb.message;

@JsonSerializable
public class KickGroupMemberReply extends Message{

	@JsonElement
	public String message;
	@JsonElement
	public boolean outcome;

	public KickGroupMemberReply() {}
	
	public KickGroupMemberReply(boolean outcome, String message) {
		this.message = (message == null)?"":message;
		this.outcome = outcome;
	}
}
