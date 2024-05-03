package srdwb.message;

import java.util.ArrayList;

@JsonSerializable
public class SearchGroupRequest extends Message{

	@JsonElement
	public ArrayList<String> keywords;
	@JsonElement
	public String message;

	public SearchGroupRequest() {}
	
	public SearchGroupRequest(ArrayList<String> keywords, String message) {
		this.keywords = keywords;
		this.message = message == null ? "" : message;
	}
}
