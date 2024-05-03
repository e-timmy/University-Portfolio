package srdwb.message;

import java.util.ArrayList;
import srdwb.Shapes.Shape;

@JsonSerializable
public class SyncPullReply extends Message{

	@JsonElement
	public ArrayList<Shape> state;
	@JsonElement
	public boolean outcome;
	
	public SyncPullReply() {}
	
	public SyncPullReply(ArrayList<Shape> state, boolean outcome) {
		this.state = state;
		this.outcome = outcome;
	}
}
