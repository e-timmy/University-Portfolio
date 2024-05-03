package srdwb.group;

import srdwb.Shapes.Shape;
import srdwb.message.GroupEntry;
import srdwb.message.Message;
import srdwb.message.User;

import java.util.ArrayList;
import java.util.HashMap;

/****************************************************************************************/

/**
 * Abstraction of groups
 */
public interface IGroup {
	void join(GroupMember member, User user);
	void leave(GroupMember member, User user);
	void send(Message msg, GroupMember excludingUser);
    boolean testSecret(String groupSecret);

	String getName();
	GroupEntry getGroupEntry();
	ArrayList<Shape> getState();
	HashMap<String, GroupMember> getMemberList();
	HashMap<String, User> getUserList();
	boolean inGroup(GroupMember member);
}
/****************************************************************************************/
