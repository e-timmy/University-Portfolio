package srdwb.group;

import srdwb.message.GroupEntry;
import srdwb.message.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;

/****************************************************************************************/

/**
 * Manages different groups for shared whiteboards
 */
public class GroupMgr {

	private ConcurrentHashMap<String, Group> groups;
	private ArrayList<String> usernames;
	private HashMap<String, GroupMember> loginMembers;
	
	public GroupMgr() {
		groups = new ConcurrentHashMap<String, Group>();
		usernames = new ArrayList<String>();
		loginMembers = new HashMap<String, GroupMember>();
	}

	/****************************************************************************************/

	/** Group Administration **/

	/**
	 * Create group
	 * @param groupMaster : GroupMember
	 * @param user : User
	 * @param groupName : String
	 * @param groupSecret : String
	 * @param groupMemberLimit : int
	 * @return : Group
	 */
	public Group createGroup(GroupMember groupMaster, User user, String groupName, String groupSecret,
							 int groupMemberLimit) {

		// Create and insert group
		Group newGroup = new Group(groupMaster, user, groupName, groupSecret, groupMemberLimit);
		System.out.println("CreateGroup: " + newGroup.uuid);
		groups.put(newGroup.uuid, newGroup);

		return newGroup;
	}

	/**
	 * Close group
	 * @param groupUUID : String
	 */
	public synchronized void closeGroup(String groupUUID) {
		Group group = groups.get(groupUUID);
		if (group != null) {
			HashMap<String, GroupMember> groupMembers = group.getMemberList();
			for (GroupMember member : groupMembers.values()) {
				member.session.informGroupClosed();
			}
			groups.remove(groupUUID);
		}
	}

	/**
	 * Searches for groups based off keywords
	 * @param keywords : ArrayList
	 * @return list of groups matching keywords
	 */
	public ArrayList<GroupEntry> searchGroups(ArrayList<String> keywords) {
		ArrayList<GroupEntry> searchResult = new ArrayList<GroupEntry>();
		if (keywords == null) {
			for(String key : groups.keySet()) {
				Group group = groups.get(key);
				searchResult.add(group.getGroupEntry());
			}
		} else {
			for(String key : groups.keySet()) {
				Group group = groups.get(key);
				for (int i = 0; i < keywords.size(); i++) {
					if(group.getName().toLowerCase().contains(keywords.get(i).toLowerCase())){
						searchResult.add(group.getGroupEntry());
						break;
					}
				}
			}
		}
		return searchResult;
	}

	/**
	 * Retrieve group
	 * @return group if present, else null
	 */
	public Group getGroup(String groupUUID) {
		return groups.getOrDefault(groupUUID, null);
	}

	/**
	 * Find whether group exists by name
	 * @param groupName : String
	 * @return : group if existws
	 */
	public Group findGroup(String groupName) {
		for(String key : groups.keySet()) {
			Group group = groups.get(key);
			if (group.groupName.equals(groupName)) {
				return group;
			}
		}
		return null;
	}

	/****************************************************************************************/

	/** User Administration **/

	/**
	 * Controls unique usernames
	 * @param username : String
	 * @return : boolean
	 */
	synchronized public boolean addUniqueUsername(String username) {
		if (usernames.contains(username)) {
			return false;
		} else {
			usernames.add(username);
			return true;
		}
	}

	/**
	 * Removes unique username on logout
	 * @param username : String
	 */
	private void removeUniqueUsername(String username) {
		boolean outcome = usernames.remove(username);
		if (outcome == false) {System.out.println("Failed to remove username???");}
	}

	/**
	 * Add logged in member
	 * @param member : GroupMember
	 * @return : boolean
	 */
	synchronized public boolean addLoggedInMember(GroupMember member) {
		if(!loginMembers.containsKey(member.uuid)) {
			loginMembers.put(member.uuid, member);
			return true;
		}
		return false;
	}

	/**
	 * Removes logged in member
	 * @param member : GroupMember
	 * @return : boolean
	 */
	synchronized public boolean removeLoggedInMember(GroupMember member) {
		if(loginMembers.containsKey(member.uuid)) {
			loginMembers.remove(member.uuid);
			removeUniqueUsername(member.username);
			return true;
		}
		return false;
	}

	/**
	 * Retrieve specific logged certain member
	 * @param uuid : String
	 * @return : GroupMember
	 */
	public GroupMember getLoggedInMember(String uuid) {
		if (loginMembers.containsKey(uuid)) {
			return loginMembers.get(uuid);
		}
		return null;
	}

	/**
	 * Retrieved list of logged in users
	 * @return : ArrayList
	 */
	public ArrayList<User> getLoggedInMemberAsUserList(){
		ArrayList<User> ret = new ArrayList<>();
		for (GroupMember member : loginMembers.values()) {
			ret.add(member.getUser());
		}
		return ret;
	}
}
/****************************************************************************************/
