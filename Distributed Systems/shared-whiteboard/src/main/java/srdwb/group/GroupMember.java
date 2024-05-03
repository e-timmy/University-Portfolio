package srdwb.group;

import srdwb.message.User;
import srdwb.server.Session;

/**
 * Defines clients within group
 */
public class GroupMember {
	public String username;
	public String uuid;
	public Session session;

	public GroupMember() {}

	public GroupMember(String uuid, Session session) {
		this.uuid = uuid;
		this.session = session;
	}

	public void addUsername(String username) {
		this.username = username;
	}
	
	public User getUser() {
		return new User(session.address, session.sessionPort, username, uuid);
	}

}
