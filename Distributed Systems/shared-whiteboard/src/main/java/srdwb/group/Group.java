package srdwb.group;

import java.util.*;

import srdwb.Shapes.Shape;
import srdwb.message.CanvasUpdate;
import srdwb.message.GroupEntry;
import srdwb.message.Message;
import srdwb.message.User;

/****************************************************************************************/

/**
 * Represents a shared whiteboard group
 */
public class Group implements IGroup{
	private HashMap<String, GroupMember> memberList;
	private HashMap<String, User> userList;
	public final String groupName;
	public final String groupSecret;
	public final GroupMember groupMaster;
	public final String uuid;
	private int groupMemberLimit;
	public ArrayList<Shape> state;
	
	public Group(GroupMember groupMaster, User user, String groupName, String groupSecret, int groupMemberLimit) {
		this.groupMaster = groupMaster;
		this.groupName = groupName;
		this.groupSecret = groupSecret;
		this.groupMemberLimit = groupMemberLimit;
		this.uuid = UUID.randomUUID().toString();
		memberList = new HashMap<String, GroupMember>();
		userList = new HashMap<String, User>();
		state = new ArrayList<>();
		System.out.println("GroupMaster: " + groupMaster.toString());
		memberList.put(groupMaster.uuid, groupMaster);
		userList.put(groupMaster.uuid, user);
	}

	/****************************************************************************************/

	/** Unique group administration **/

	/**
	 * Join group
	 * @param member : GroupMember
	 * @param user : User
	 */
	@Override
	public void join(GroupMember member, User user) {
		if(!memberList.containsKey(member.uuid)) {
			System.out.println("Joining group!");
			memberList.put(member.uuid, member);
			userList.put(member.uuid, user);
		} else {
			System.out.println("Already in group!");
		}
	}

	/**
	 * Leave group
	 * @param member : Member
	 * @param user : User
	 */
	@Override
	public void leave(GroupMember member, User user) {
		if(memberList.containsKey(member.uuid)) {
			System.out.println("Removing Member");
			GroupMember kicked = memberList.get(member.uuid);
			kicked.session.revokeGroupMembership();
			GroupMember outcome = memberList.remove(member.uuid);
			userList.remove(member.uuid);
		}
	}

	/**
	 * Send message to group
	 * @param msg : Message
	 * @param excludingWriter : GroupMember
	 */
	@Override
	public void send(Message msg, GroupMember excludingWriter) {
		// Maintain canvas state
		if (msg.getClass().getName().equals(CanvasUpdate.class.getName())) {
			CanvasUpdate update = (CanvasUpdate) msg;
			state.add(update.shape);
		}

		for (GroupMember m: memberList.values()) {
			if (!m.equals(excludingWriter)) {
				m.session.writeMsg(msg);
			}
		}
	}

	/**
	 * Test group secret
	 * @param secret : String
	 * @return : outcome
	 */
	@Override
	public boolean testSecret(String secret) {
		if (secret.equals(this.groupSecret)) {
			return true;
		} else {
			return false;
		}
	}

	/****************************************************************************************/

	/** Getters **/

	@Override
	public String getName() {return groupName;}
	@Override
	public GroupEntry getGroupEntry() {return new GroupEntry(uuid, groupName, groupSecret, groupMaster.getUser());}
	@Override
	public ArrayList<Shape> getState() {return state;}
	@Override
	public HashMap<String, GroupMember> getMemberList() {return memberList;}
	@Override
	public HashMap<String, User> getUserList() {return userList;}
	@Override
	public boolean inGroup(GroupMember member) {
		return memberList.containsKey(member.uuid);
	}

}
/****************************************************************************************/
