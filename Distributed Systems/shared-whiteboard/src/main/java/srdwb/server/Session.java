package srdwb.server;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.*;
import java.util.LinkedList;

import srdwb.group.*;
import srdwb.message.*;


/****************************************************************************************/

/**
 * Handles communication with single client
 * Protocol is threads-per-connection
 */
public class Session extends Thread {

	// Messages
	private static final String GROUP_JOIN_SUCCESS = "Joined Group Successfully :)";
	private static final String GROUP_JOIN_NOGROUP = "Group doesn't exist.";
	private static final String WELCOME_MSG = "Welcome to Shared Whiteboard Service.";
	private static final String GROUP_CREATION_SUCCESS = "Group successfully created";
	private static final String GROUP_SEARCH_SUCCESS = "Successfully search groups";
	private static final String GROUP_JOIN_ALREADY_IN = "Already in group.";
	private static final String GROUP_JOIN_REJECTED = "The group manager has rejected your request.";
	private static final String GROUP_JOIN_LEAVE_FIRST = "Please leave current group before joining another.";
	private static final String JOIN_NOTIFICATION = " has joined!";
	private static final String USERNAME_NOT_UNIQUE = " username unavailable.";
	private static final String PASSWORD_FAIL = "Password incorrect. Please try again.";
	private static final String KICK_NO_AUTH = "No authorisation to kick group member!";
	private static final String NO_AUTH = "No authorisatio!";
	private static final String USER_NOT_IN_GROUP = "Not in a group!";
	private static final String CANT_KICK_MASTER = "Can not kick a group master!";
	private static final String LEAVE_NOTIFICATION = " has left!";
	private static final String GOODBYE_MSG = "Logout successful. Bye :)";
	private static final String CLOSE_GROUP = " has closed the group.";
	private static final String KICKED_UPDATE = " has been kicked from group.";
	private static final String GROUP_CREATION_ALREADY_IN = "Leave current group before creation.";
	private static final String GROUP_CREATION_NAME_TAKEN = " name already taken. Try another.";

	// Session details
	private Socket socket;
	private GroupMgr groupMgr;
	private BufferedWriter writer;
	private BufferedReader reader;
	private LinkedList<Predicate<Message>> msgHandlerChain;
	private SessionWriter sessionWriter;
	private String serverSecret;

	public final int sessionPort;

	// Client information
	private User user;
	private final GroupMember membership;
	private Group sessionGroup;
	public final String address;
	public int updatePort;
	public final String uuid;

	/****************************************************************************************/

	public Session(Socket socket, GroupMgr groupMgr, String serverSecret) throws IOException {
		this.socket = socket;
		this.socket.setKeepAlive(true);
		this.groupMgr = groupMgr;
		this.serverSecret = serverSecret;
		
		// assign uuid to session, also used as group member uuid
		this.uuid = UUID.randomUUID().toString();
		this.address = socket.getInetAddress().getHostAddress();
		this.sessionPort = socket.getPort();
		this.membership = new GroupMember(uuid, this);

		System.out.println("Session connected by [" + address + ":" + sessionPort + "], associated with uuid: " + uuid);
		
		InputStream inputStream = socket.getInputStream();
		OutputStream outputStream = socket.getOutputStream();
		this.reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		this.writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
		
		buildMessageHandlerChain();
		this.sessionWriter = new SessionWriter(this, this.writer);
		this.sessionWriter.start();

	}
	
	@Override
	public void run() {
		try {
			while (!isInterrupted()) {
				if (reader.ready()) {
					Message msg = readMsg();
					new ProcessMsg(msg).start();
				}
			}
			shutdown();
		}  catch (JsonSerializationException e) {
			System.out.println("Session " + this.toString() + " JsonSerialization Exception");
		} catch (IOException e) {
			System.out.println("Reader fail: " + e.getMessage());
		}
	}

	/**
	 * Called when unabel to reach client
	 * Closes session down through logout request
	 */
	public void processSessionFailure() {
		new ProcessMsg(new LogoutRequest()).start();
	}

	public void shutdown() {
		this.sessionWriter.interrupt();
		try {
			this.sessionWriter.join();
			socket.close();
			groupMgr.removeLoggedInMember(membership);
			sessionGroup = null;
		} catch(InterruptedException e) {
			System.out.println("Session " + uuid + " tries to shutdown fail");
		} catch (IOException e) {
			System.out.println("Failed to close socket: " + e.getMessage());
		}
		System.out.println("Session shut down.");
	}
	
	@Override
	public String toString() {
		return uuid;
	}

	/****************************************************************************************/

	/** Requests **/

	/**
	 * Process message according to message handling chain built
	 */
	private class ProcessMsg extends Thread {
		private Message msg;

		public ProcessMsg(Message msg) {this.msg = msg;}
		public void run() {
			for (Predicate<Message> func : msgHandlerChain) {
				if (func.test(msg)) {
					break;
				}
			}
		}
	}
	
	/**
	 * Build message handling chain for this session
	 */
	private void buildMessageHandlerChain() {
		msgHandlerChain = new LinkedList<Predicate<Message>>();
		
		msgHandlerChain.addLast(this::handleError);
		msgHandlerChain.addLast(this::handleCanvasUpdate);
		msgHandlerChain.addLast(this::handleGroupMessage);
		msgHandlerChain.addLast(this::handleLoginAttempt);
		msgHandlerChain.addLast(this::handleCreateGroup);
		msgHandlerChain.addLast(this::handleJoinGroupRequest);
		msgHandlerChain.addLast(this::handleJoinGroupReply);
		msgHandlerChain.addLast(this::handleSearchGroupRequest);
		msgHandlerChain.addLast(this::handleLeaveGroupRequest);
		msgHandlerChain.addLast(this::handleInviteGroupRequest);
		msgHandlerChain.addLast(this::handleFetchLoginUserRequest);
		msgHandlerChain.addLast(this::handleKickGroupMemberRequest);
		msgHandlerChain.addLast(this::handleCloseGroupRequest);
		msgHandlerChain.addLast(this::handleLogoutRequest);
		msgHandlerChain.addLast(this::handleSyncPushRequest);
		msgHandlerChain.addLast(this::handleSyncPullRequest);
	}

	/**
	 * Deal with error message
	 * @param msg : Message
	 * @return : correct message
	 */
	private boolean handleError(Message msg) {
		if (!msg.getClass().getName().equals(ErrorMsg.class.getName())) {
			return false;
		}
		
		ErrorMsg errorMsg = (ErrorMsg)msg;
		System.out.println("Session receives error msg: " + errorMsg);
		
		return true;
	}

	/**
	 * Deal with canvas update
	 * @param msg : Message
	 * @return : correct message
	 */
	private boolean handleCanvasUpdate(Message msg) {
		if (!msg.getClass().getName().equals(CanvasUpdate.class.getName())) {
			return false;
		}
		CanvasUpdate reply = (CanvasUpdate) msg;
		System.out.println("Session receives update: " + reply.shape);

		// Must have group
		if (sessionGroup == null) { return false;}

		// Send canvas updates
		sessionGroup.send(reply, membership);

		return true;
	}

	/**
	 * Deal with group chat message
	 * @param msg : Message
	 * @return : correct message
	 */
	private boolean handleGroupMessage(Message msg) {
		if (!msg.getClass().getName().equals(GroupMessage.class.getName())) {
			return false;
		}
		GroupMessage reply = (GroupMessage) msg;
		System.out.println("Session receives group message: " + reply);

		// Must have group
		if (sessionGroup == null) {return false;}
		sessionGroup.send(reply, membership);

		return true;
	}

	/**
	 * Deal with login
	 * @param msg : Message
	 * @return : correct message
	 */
	private boolean handleLoginAttempt(Message msg) {
		if (!msg.getClass().getName().equals(LoginRequest.class.getName())) {
			return false;
		}
		LoginRequest request = (LoginRequest) msg;
		System.out.println("Session receives loginRequest from: " + request.username);

		String username = request.username;
		String userSecret = request.serverSecret;
		this.updatePort = request.updatePort;

		if (userSecret.equals(serverSecret)) {
			if (groupMgr.addUniqueUsername(username)) {
				// Unique username
				membership.addUsername(username);
				user = new User(socket.getInetAddress().getHostAddress(),
						socket.getPort(),
						membership.username,
						membership.uuid);
				groupMgr.addLoggedInMember(membership);
				System.out.println("Member: " + membership.toString());
				writeMsg(new LoginReply(WELCOME_MSG, true, membership.getUser()));
			} else {
				// Login Failure
				System.out.println("Username already in use");
				writeMsg(new LoginReply(username + USERNAME_NOT_UNIQUE, false, membership.getUser()));

			}
		} else {
			System.out.println("Wrong server secret. Shutting down session.");
			writeMsg(new LoginReply(PASSWORD_FAIL, false, membership.getUser()));
			this.shutdown();
		}
		return true;
	}

	/**
	 * Deal with group creation
	 * @param msg : Message
	 * @return : correct message
	 */
	private boolean handleCreateGroup(Message msg) {
		if (!msg.getClass().getName().equals(CreateGroupRequest.class.getName())) {
			return false;
		}
		
		CreateGroupRequest request = (CreateGroupRequest)msg;
		System.out.println("Session recieves createGroupRequest: " + request.message);
		
		String groupName = request.groupName;
		String groupSecret = request.groupSecret;
		int groupMemberLimit = 5;

		System.out.println("Membership: " + membership);
		
		Group group = groupMgr.findGroup(groupName);
		if (sessionGroup == null) {
			// Not in group
			if (group == null) {
				System.out.println("Creating new group");

				// Group non exists
				sessionGroup = groupMgr.createGroup(membership, user, groupName, groupSecret, groupMemberLimit);
				user.updateStatus("Manager");
				writeMsg(new CreateGroupReply(GROUP_CREATION_SUCCESS, groupName, user, true,
						sessionGroup.getGroupEntry()));
			} else {
				// Group under that name already exists
				System.out.println("Group name already exists");
				writeMsg(new CreateGroupReply(groupName + GROUP_CREATION_NAME_TAKEN, null, null, false,
						null));
			}

		} else {
			// Already in group
			writeMsg(new CreateGroupReply(GROUP_CREATION_ALREADY_IN, null, null, false,
					null));
		}
		
		return true;
	}

	/**
	 * Deal with join group request
	 * @param msg : Message
	 * @return : correct message
	 */
	private boolean handleJoinGroupRequest(Message msg) {
		if (!msg.getClass().getName().equals(JoinGroupRequest.class.getName())) {
			return false;
		}

		JoinGroupRequest request = (JoinGroupRequest)msg;
		System.out.println("Session receives joinGroupRequest: " + request.groupName);

		String groupName = request.groupName;
		String groupSecret = request.groupSecret;
		String groupUUID = request.groupUUID;

		// Checking if in group already
		Group group = groupMgr.findGroup(groupName);

		if (group == null) {
			// Group doesn't exist - cannot join...
			writeMsg(new JoinGroupReply(GROUP_JOIN_NOGROUP,null, null, null, null, false, user));
			return true;
		} else if (sessionGroup != null && sessionGroup.inGroup(membership)) {
			// Already in group ...
			if (sessionGroup.uuid.equals(groupUUID)) {
				writeMsg(new JoinGroupReply(GROUP_JOIN_ALREADY_IN,null, null, null, null, false, user));

				return true;
			} else {
				// Already in another group
				writeMsg(new JoinGroupReply(GROUP_JOIN_LEAVE_FIRST, null, null,
						null, null, false, user));
			}
		} else {
			// Join group if pass secret and approved by manager
			if (group.testSecret(groupSecret)) {
				// forwarding message to group manager
				group.groupMaster.session.writeMsg(request);
				return true;
			}
			else {
				// Incorrect secret
				writeMsg(new JoinGroupReply(PASSWORD_FAIL, null, null, null, null, false, user));
			}
		}

		return true;
	}

	/**
	 * Deal with manager's answer to group join
	 * @param msg : Message
	 * @return : correct message
	 */
	private boolean handleJoinGroupReply(Message msg) {
		if (!msg.getClass().getName().equals(JoinGroupReply.class.getName())) {
			return false;
		}
		
		JoinGroupReply reply = (JoinGroupReply)msg;
		
		if (!reply.user.uuid.equals(membership.uuid)) {
			GroupMember member = groupMgr.getLoggedInMember(reply.user.uuid);
			if (member != null) {
				member.session.handleJoinGroupReply(msg);
			}
			return true;
		}
		
		Group group = groupMgr.getGroup(reply.group.groupUUID);
		
		if (reply.outcome && group != null) {
			user.updateStatus("Member");
			group.join(membership, user);
			sessionGroup = group;
			sessionGroup.send(new JoinGroupNotification(user, user.name + JOIN_NOTIFICATION), membership);

			writeMsg(new JoinGroupReply(GROUP_JOIN_SUCCESS, sessionGroup.groupName, sessionGroup.getUserList(),
					sessionGroup.getState(), group.getGroupEntry(), true, user));
		}
		else {
			writeMsg(new JoinGroupReply(GROUP_JOIN_REJECTED, null, null,
					null, null, false, user));
		}

		return true;
	}

	/**
	 * Deal with leave group request
	 * @param msg : Message
	 * @return : correct message
	 */
	private boolean handleLeaveGroupRequest(Message msg) {
		if (!msg.getClass().getName().equals(LeaveGroupRequest.class.getName())) {
			return false;
		}

		// Notify other members
		sessionGroup.send(new LeaveGroupNotification(user, user.name + LEAVE_NOTIFICATION), membership);
		writeMsg(new LeaveGroupReply("You have left " + sessionGroup.groupName  + "."));
		// Remove from group
		sessionGroup.leave(membership, user);
		sessionGroup = null;

		return true;
	}

	/**
	 * Deal with search group
	 * @param msg : Message
	 * @return : correct message
	 */
	private boolean handleSearchGroupRequest(Message msg) {
		if (!msg.getClass().getName().equals(SearchGroupRequest.class.getName())) {
			return false;
		}

		SearchGroupRequest request = (SearchGroupRequest)msg;
		System.out.println("Session receives searchGroupRequest: " + request.keywords);

		ArrayList<String> keywords = request.keywords;

		System.out.println("Membership: " + membership);

		ArrayList<GroupEntry> searchHits = groupMgr.searchGroups(keywords);
		
		writeMsg(new SearchGroupReply(searchHits, GROUP_SEARCH_SUCCESS));

		return true;
	}

	/**
	 * Deal with invite
	 * @param msg : Message
	 * @return : correct message
	 */
	private boolean handleInviteGroupRequest(Message msg) {
		if (!msg.getClass().getName().equals(InviteGroupRequest.class.getName())) {
			return false;
		}

		InviteGroupRequest request = (InviteGroupRequest)msg;
		System.out.println("Session receives inviteGroupRequest from: " + request.inviter.name + "@" + request.inviter.ip);
		
		System.out.println("Membership: " + membership);
		
		GroupMember invitee = groupMgr.getLoggedInMember(request.invitee.uuid);
		if(invitee != null) {
			invitee.session.writeMsg(msg);
		}

		return true;
	}

	/**
	 * Retrieve logged in users for client
	 * @param msg : Message
	 * @return : correct message
	 */
	private boolean handleFetchLoginUserRequest(Message msg) {
		if(!msg.getClass().getName().equals(FetchLoginUserRequest.class.getName())) {
			return false;
		}
		FetchLoginUserRequest request = (FetchLoginUserRequest)msg;
		System.out.println("Session receives fetchLoginUserRequest");
		System.out.println("Membership: " + membership);
		
		writeMsg(new FetchLoginUserReply(groupMgr.getLoggedInMemberAsUserList()));

		return true;
	}

	/**
	 * Kick member if manager's request
	 * @param msg : Message
	 * @return : correct message
	 */
	private boolean handleKickGroupMemberRequest(Message msg) {
		if(!msg.getClass().getName().equals(KickGroupMemberRequest.class.getName())) {
			return false;
		}
		KickGroupMemberRequest request = (KickGroupMemberRequest)msg;
		System.out.println("Session receives kickGroupMemberRequest");
		System.out.println("Membership: " + membership);
		
		if (sessionGroup == null) {
			writeMsg(new KickGroupMemberReply(false, USER_NOT_IN_GROUP));
		} else {
			if (sessionGroup.groupMaster.uuid.equals(membership.uuid)) {
				if (membership.uuid.equals(request.userToKick.uuid)) {
					writeMsg(new KickGroupMemberReply(false, CANT_KICK_MASTER));
					return true;
				}
				// Has authorisation, not self
				// inform all group members
				GroupMember memberToKick = groupMgr.getLoggedInMember(request.userToKick.uuid);
				sessionGroup.send(new LeaveGroupNotification(request.userToKick, 
						request.userToKick.name + KICKED_UPDATE), null);
				// kick member
				if(memberToKick != null) {
					sessionGroup.leave(memberToKick, request.userToKick);
				}
			}
			else {
				writeMsg(new KickGroupMemberReply(false, KICK_NO_AUTH));
			}
		}
		return true;
	}

	/**
	 * Close group if manager's request
	 * @param msg : Message
	 * @return : correct message
	 */
	private boolean handleCloseGroupRequest(Message msg) {
		if(!msg.getClass().getName().equals(CloseGroupRequest.class.getName())) {
			return false;
		}
		CloseGroupRequest request = (CloseGroupRequest)msg;
		System.out.println("Session receives closeGroupRequest");
		System.out.println("Membership: " + membership);
		
		if (sessionGroup == null) {
			writeMsg(new CloseGroupReply(USER_NOT_IN_GROUP, false));
			System.out.println("USER not in group");
		} else {
			if (sessionGroup.groupMaster.uuid.equals(membership.uuid)) {
				// inform users
				sessionGroup.send(new CloseGroupReply(membership.username + CLOSE_GROUP, true), null);
				System.out.println("Inform group members");
				groupMgr.closeGroup(sessionGroup.uuid);
			}
			else {
				writeMsg(new CloseGroupReply(NO_AUTH, false));
				System.out.println("Session no authorisation");
			}
		}

		return true;
	}

	/**
	 * Logout request
	 * @param msg : Message
	 * @return : correct message
	 */
	private boolean handleLogoutRequest(Message msg) {
		if (!msg.getClass().getName().equals(LogoutRequest.class.getName())) {
			return false;
		}
		System.out.println("Session receives logout request");
		LogoutRequest request = (LogoutRequest) msg;

		if (sessionGroup != null) {
			ProcessMsg goodbyeThread;
			if (membership.equals(sessionGroup.groupMaster)) {
				// Close group if master
				goodbyeThread = new ProcessMsg(new CloseGroupRequest("Closing group!"));
				goodbyeThread.start();
			} else {
				// Leave group if member
				goodbyeThread = new ProcessMsg(new LeaveGroupRequest("Leaving group"));
				goodbyeThread.start();
			}
			// Wait for messages to finish
			try {
				goodbyeThread.join();
			} catch (InterruptedException e) {
				System.out.println("Thread interrupted");
			}
		}
		writeMsg(new LogoutReply(GOODBYE_MSG, true));

		this.shutdown();

		return true;
	}

	/**
	 * Get board state from group master
	 * @param msg : Message
	 * @return : correct message
	 */
	public boolean handleSyncPushRequest(Message msg) {
		if (!msg.getClass().getName().equals(SyncPushRequest.class.getName())) {
			return false;
		}
		System.out.println("Session receives syncPush request");
		SyncPushRequest request = (SyncPushRequest) msg;

		if (sessionGroup != null) {
			if (membership.equals(sessionGroup.groupMaster)) {
				// Proceed if group master
				sessionGroup.state = request.state;
				sessionGroup.send(new SyncPushRequest(request.state), membership);
			}
		}

		return true;
	}

	/**
	 * Request up to date board state
	 * @param msg : Message
	 * @return : correct message
	 */
	public boolean handleSyncPullRequest(Message msg) {
		if (!msg.getClass().getName().equals(SyncPullRequest.class.getName())) {
			return false;
		}
		System.out.println("Session receives syncPull request");
		SyncPullRequest request = (SyncPullRequest) msg;

		if (sessionGroup != null) {
			writeMsg(new SyncPullReply(sessionGroup.getState(), true));
		}
		else {
			writeMsg(new SyncPullReply(null, false));
		}

		return true;
	}

	/**
	 * Informed that group has been closed from GroupMgr
	 */
	public void informGroupClosed() {
		this.sessionGroup = null;
	}

	/**
	 * Sets session's group to null
	 * Used when kicking member from group
	 */
	public void revokeGroupMembership() {
		this.sessionGroup = null;
	}


	/****************************************************************************************/

	/** Helper Functions **/

	/**
	 * Write message to client
	 * @param msg : Message
	 */
	public void writeMsg(Message msg){
		sessionWriter.writeToBuffer(msg);
	}

	/**
	 * Read message from client
	 * @return Message
	 * @throws JsonSerializationException if unmarshal fail
	 * @throws IOException if reader fails
	 */
	synchronized public Message readMsg() throws JsonSerializationException, IOException {
		String jsonStr = null;

		jsonStr = reader.readLine();
		if(jsonStr!=null) {
			Message msg = (Message) MessageFactory.fromJsonString(jsonStr);
			System.out.println("received: " + msg.toString());
			return msg;
		}
		return null;
	}
}

/****************************************************************************************/
