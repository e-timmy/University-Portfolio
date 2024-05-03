package srdwb.message;

import java.util.ArrayList;

@JsonSerializable
public class SearchGroupReply extends Message {

	@JsonElement
	public String message;
	@JsonElement
	public ArrayList<GroupEntry> searchHits;
	
	public SearchGroupReply() {}
	
	public SearchGroupReply(ArrayList<GroupEntry> searchHits, String message) {
		this.searchHits = searchHits;
		this.message = message == null ? "" : message;
	}
}
