package srdwb.message;

import java.util.ArrayList;

@JsonSerializable
public class FetchLoginUserReply extends Message{

	@JsonElement
	public ArrayList<User> loginUsers;

	public FetchLoginUserReply() {
		
	}
	
	public FetchLoginUserReply(ArrayList<User> loginUsers) {
		this.loginUsers = loginUsers;
	}
}
