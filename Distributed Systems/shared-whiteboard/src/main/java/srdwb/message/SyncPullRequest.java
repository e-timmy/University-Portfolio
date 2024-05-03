package srdwb.message;

/**
 * Anyone can use pull
 *
 */
@JsonSerializable
public class SyncPullRequest extends Message{
	@JsonElement
	public String message;
	
	public SyncPullRequest() {}
	
	public SyncPullRequest(String message) {
		this.message = message==null?"":message;
	}
}
