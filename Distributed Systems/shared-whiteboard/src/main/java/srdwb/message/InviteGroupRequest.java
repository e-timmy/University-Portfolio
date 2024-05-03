package srdwb.message;

@JsonSerializable
public class InviteGroupRequest extends Message{

	@JsonElement
	public User inviter;
	@JsonElement
	public GroupEntry group;
	@JsonElement
	public User invitee;
	
	public InviteGroupRequest() {
		
	}
	
	public InviteGroupRequest(User inviter, User invitee, GroupEntry group) {
		this.inviter = inviter;
		this.invitee = invitee;
		this.group = group;
	}
}
