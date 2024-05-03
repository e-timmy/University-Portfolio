package srdwb.message;

import java.util.ArrayList;

import srdwb.Shapes.Shape;

/**
 * 
 * Only master use push
 */
@JsonSerializable
public class SyncPushRequest extends Message{
	@JsonElement
	public ArrayList<Shape> state;
	
	public SyncPushRequest() {}
	
	public SyncPushRequest(ArrayList<Shape> state) {
		this.state = state;
	}
}
