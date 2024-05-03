package srdwb.client;

import srdwb.Shapes.Brush;
import srdwb.Shapes.CanvasData;
import srdwb.Shapes.CanvasDataFactory;
import srdwb.Shapes.Line;
import srdwb.Shapes.Nothing;
import srdwb.Shapes.Shape;
import srdwb.clientGUI.ClientGUI;
import srdwb.clientGUI.UserInfoTableModel;
import srdwb.message.*;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import javax.imageio.*;

import static srdwb.clientGUI.ClientState.NOT_IN_GROUP;
import static srdwb.clientGUI.ClientState.START_UP;

/****************************************************************************************/

/**
 * Central representation of client
 * Handles incoming replies/updates
 * Dispatches outgoing requests
 * Channel from GUI to server
 */
public class Client extends Thread implements IClient  {

    // Updates
    private ClientGUI cgui;

    // Connection details
    private InetAddress serverAddress;
    private int serverPort;
    private int port;
    private int timeout;

    // Session details
    private Socket serverSocket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private ClientWriter clientWriter;
    private LinkedList<Predicate<Message>> msgHandlerChain;
    private volatile AtomicBoolean logged_in = new AtomicBoolean(false);
    private volatile AtomicBoolean group_membership = new AtomicBoolean(false);
    private User sessionIdentity;
    private GroupEntry sessionGroup;
    private ArrayList<Shape> state;

    /****************************************************************************************/


    public Client(int port, int timeout, InetAddress serverAddress, int serverPort,
                  String secret, ClientGUI cgui) throws IOException {
        this.port = port;
        this.timeout = timeout;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.cgui = cgui;
        this.state = new ArrayList<>();

        buildMessageHandlerChain();
    }

    /**
     * Listens for incoming updates
     */
    @Override
    public void run() {

        while(!isInterrupted()) {
            try {
                if (reader != null && reader.ready()) {
                    System.out.println("Handling asynchronous message.");
                    Message msg = readMessage(reader);
                    new ProcessMsg(msg).start();
                }
            } catch (IOException e) {
                System.out.println("Reader fail: " + e.getMessage());
            } catch (JsonSerializationException e) {
                System.out.println("Reader fail: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * Shutdown connection
     */
    @Override
    public void shutdown() {new ShutdownSocket().start();}
    private class ShutdownSocket extends Thread {
        @Override
        public void run() {
            try {
                System.out.println("Shutdown socket");
                if (serverSocket != null) {
                    writer.close();
                    reader.close();
                    serverSocket.close();
                    reader = null;
                    writer = null;
                    serverSocket = null;
                }
            } catch (IOException e) {
                System.out.println("Socket closure fail: " + e.getMessage());
            }
        }
    }

    /**
     * Called when unable to reach server
     * 'Logs' user out - closes socket
     */
    public void processSessionFailure() {new ProcessMsg(new LogoutReply("Unable to reach server...",
            true)).start();}

    /**
     * Exit mechanism
     */
    @Override
    public void closeProgram() {
        if (this.group_membership.get()) {
            if (sessionGroup != null && this.sessionIdentity.uuid.equals(sessionGroup.master.uuid)) {
                // Only manager can save file
                if (cgui.promptSavePane()) {
                    saveCanvas();
                }
                closeGroup();
            }
            else {
                leaveGroup();
            }
        }
        cgui.promptGoodbye();
        logout();
        try {
            while(logged_in.get()) {
                Thread.sleep(100);
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        shutdown();
        System.exit(0);
    }

    /****************************************************************************************/

    /** Requests **/

    /**
     * Login to server
     * @param username unique name
     * @param serverSecret server password
     */
    public void login(String username, String serverSecret) {
        new ServerLogin(this, username, serverSecret).start();
    }
    private class ServerLogin extends Thread {

        private Client client;
        private String username;
        private String serverSecret;
        public ServerLogin(Client client, String username, String serverSecret) {
            this.client = client;
            this.username = username;
            this.serverSecret = serverSecret;
        }

        @Override
        public void run() {
            System.out.println("Attempting to login to server");
            try {
                // Initialise session connection
                serverSocket = new Socket(serverAddress, serverPort);
                serverSocket.setSoTimeout(timeout);
                reader = getBufferedReader(serverSocket);
                writer = getBufferedWriter(serverSocket);
                clientWriter = new ClientWriter(client, writer);
                clientWriter.start();

                writeMessage(writer, new LoginRequest(username, serverSecret));
                Message reply = readMessage(reader);
                new ProcessMsg(reply).start();

            } catch (SocketException e) {
                System.out.println("Exception on socket: " + e.getMessage());
                cgui.promptError("Exception on socket: " + e.getMessage(), null);
            } catch (IOException e) {
                System.out.println("IO Exception on Socket: " + e.getMessage());
                cgui.promptError("IO Exception on Socket: " + e.getMessage(), null);
            } catch (JsonSerializationException e) {
                System.out.println("Read message failure " + e.getMessage());
                cgui.promptError("Read message failure " + e.getMessage(), null);
            }
        }

    }

    /**
     * Create group
     * @param name: String
     * @param secret: String
     */
    public void startNewGroup(String name, String secret) {
        new StartNewGroup(name, secret).start();
    }
    private class StartNewGroup extends Thread {

        private String name;
        private String secret;

        public StartNewGroup(String name, String secret) {
            this.name = name;
            this.secret = secret;
        }
        @Override
        public void run() {
            System.out.println("Starting new group");

            try {
                writeMessage(writer, new CreateGroupRequest(name, secret, "New group please!", port));
                Message reply = readMessage(reader);
                new ProcessMsg(reply).start();
            } catch (IOException e) {
                System.out.println("Read fail: " + e.getMessage());
            } catch (JsonSerializationException e) {
                System.out.println("Write fail: " + e.getMessage());
            }
        }
    }

    /**
     * Search for group
     * @param keywords : List of strings
     */
    public void searchGroup(ArrayList<String> keywords) {
        new SearchGroup(keywords).start();
    }
    private class SearchGroup extends Thread {

        private ArrayList<String> keywords;


        public SearchGroup(ArrayList<String> keywords) {
            this.keywords = keywords;
        }

        @Override
        public void run() {
            System.out.println("Searching groups");

            try {
                writeMessage(writer, new SearchGroupRequest(keywords, ""));
                Message reply = readMessage(reader);
                new ProcessMsg(reply).start();
            } catch (IOException e) {
                System.out.println("Read fail: " + e.getMessage());
            } catch (JsonSerializationException e) {
                System.out.println("Write fail: " + e.getMessage());
            }
        }
    }

    /**
     * Invite another user
     * @param user : user
     */
    @Override
    public void invite(User user) {
        if (this.logged_in.get() && this.group_membership.get()) {
            new Invite(user).start();
        }
    }
    private class Invite extends Thread {

        private User invitee;

        public Invite(User invitee) {
            this.invitee = invitee;
        }

        @Override
        public void run() {
            System.out.println("Searching groups");

            try {
                writeMessage(writer, new InviteGroupRequest(sessionIdentity, this.invitee, sessionGroup));
            } catch (IOException e) {
                System.out.println("Read fail: " + e.getMessage());
            }
        }
    }

    /**
     * Get currently logged in users
     */
    public void fetchLoginUsersForInvitation() {
        new FetchLoginUsersForInvitation().start();
    }
    private class FetchLoginUsersForInvitation extends Thread {

        public FetchLoginUsersForInvitation() {}

        @Override
        public void run() {
            System.out.println("Searching groups");

            try {
                writeMessage(writer, new FetchLoginUserRequest(null));
                Message reply = readMessage(reader);
                new ProcessMsg(reply).start();
            } catch (IOException e) {
                System.out.println("Read fail: " + e.getMessage());
            } catch (JsonSerializationException e) {
                System.out.println("Write fail: " + e.getMessage());
            }
        }
    }

    /**
     * Close group
     */
    @Override
    public void closeGroup() {
        if (this.group_membership.get() && this.sessionGroup.master.uuid.equals(sessionIdentity.uuid)) {
            new CloseGroup().start();
        }
    }
    private class CloseGroup extends Thread{
        public CloseGroup() {}

        @Override
        public void run() {
            System.out.println("Close group");

            try {
                writeMessage(writer, new CloseGroupRequest(null));
                Message reply = readMessage(reader);
                new ProcessMsg(reply).start();
            } catch (IOException e) {
                System.out.println("Read fail: " + e.getMessage());
            } catch (JsonSerializationException e) {
                System.out.println("Write fail: " + e.getMessage());
            }
        }
    }

    /**
     * Remove user from group
     * @param user : User
     */
    @Override
    public void kickUser(User user) {
        if (this.group_membership.get() && sessionIdentity!=null && !user.uuid.equals(sessionIdentity.uuid)) {
            new KickUser(user).start();
        }
    }
    private class KickUser extends Thread{
        private User userToKick;
        public KickUser(User userToKick) {
            this.userToKick = userToKick;
        }

        @Override
        public void run() {
            System.out.println("Kick user: " + userToKick.name + "@" + userToKick.ip );

            try {
                writeMessage(writer, new KickGroupMemberRequest(userToKick, "Please leave :)"));
                Message reply = readMessage(reader);
                new ProcessMsg(reply).start();
            } catch (IOException e) {
                System.out.println("Write fail: " + e.getMessage());
            } catch (JsonSerializationException e) {
                System.out.println("Read fail: " + e.getMessage());
            }

        }
    }


    /**
     * Join group
     * @param groupName : String
     * @param groupSecret : String
     * @param groupUUID : String
     */
    public void joinGroup(String groupName, String groupSecret, String groupUUID) {
        new JoinGroup(groupName, groupSecret, groupUUID).start();
    }
    private class JoinGroup extends Thread {

        private String groupName;
        private String groupSecret;
        private String groupUUID;

        public JoinGroup(String groupName, String groupSecret, String groupUUID) {
            this.groupName = groupName;
            this.groupSecret = groupSecret;
            this.groupUUID = groupUUID;
        }
        @Override
        public void run() {
            System.out.println("Joining group");

            try {
                writeMessage(writer, new JoinGroupRequest(groupName, groupSecret, groupUUID, sessionIdentity));
                Message reply = readMessage(reader);
                new ProcessMsg(reply).start();
            } catch (IOException e) {
                System.out.println("Write fail: " + e.getMessage());
            } catch (JsonSerializationException e) {
                System.out.println("Read fail: " + e.getMessage());
            }

        }
    }

    /**
     * Leave group
     */
    @Override
    public void leaveGroup() { new LeaveGroup().start();}
    private class LeaveGroup extends Thread {

        public LeaveGroup() {}

        @Override
        public void run() {
            System.out.println("Leaving group");

            try {
                writeMessage(writer, new LeaveGroupRequest("Leaving group please"));
                Message reply = readMessage(reader);
                new ProcessMsg(reply).start();
            } catch (IOException e) {
                System.out.println("Write fail: " + e.getMessage());
            } catch (JsonSerializationException e) {
                System.out.println("Read fail: " + e.getMessage());
            }
        }
    }

    /**
     * Shares change in canvas with server
     * @param shape : Shape
     */
    public void updateSharedCanvas(Shape shape) {new UpdateSharedCanvas(shape).start();}
    private class UpdateSharedCanvas extends Thread {

        private Shape shape;

        public UpdateSharedCanvas(Shape shape) {
            this.shape = shape;
        }
        @Override
        public void run() {
            if (!logged_in.get() || shape instanceof Nothing || !group_membership.get()) { return;}

            System.out.println("Sharing shape");
            try {
                writeMessage(writer, new CanvasUpdate(shape));
                updateBoardState(shape);
            } catch (IOException e) {
                System.out.println("Update canvas write fail: " + e.getMessage());
            }
        }
    }

    /**
     * Sends chat message
     * @param message : Message
     */
    @Override
    public void sendGroupMessage(GroupMessage message) { new SendGroupMessage(message).start();}
    private class SendGroupMessage extends Thread {

        public GroupMessage message;
        public SendGroupMessage(GroupMessage message) {
            this.message = message;
        }

        @Override
        public void run() {
            System.out.println("Sending message to group");

            try {
                writeMessage(writer, message);
            } catch (IOException e) {
                System.out.println("Group message write fail: " + e.getMessage());
            }
        }
    }

    /**
     * Request for logout
     */
    @Override
    public void logout() { new LogoutAttempt().start();}
    private class LogoutAttempt extends Thread {

        public LogoutAttempt() {}

        @Override
        public void run() {
            System.out.println("Sending logout request");

            try {
                writeMessage(writer, new LogoutRequest(null));
                Message reply = readMessage(reader);
                new ProcessMsg(reply).start();
            } catch (IOException e) {
                System.out.println("Logout write fail: " + e.getMessage());
            } catch (JsonSerializationException e) {
                System.out.println("Logout read fail: " + e.getMessage());
            }
        }
    }

    /**
     * Push board state
     */
    @Override
    public void pushCanvasState() {
        if (this.group_membership.get() && this.sessionGroup.master.uuid.equals(sessionIdentity.uuid)) {
            new PushCanvasState(state).start();
        }
    }
    private class PushCanvasState extends Thread{
        private ArrayList<Shape> state;
        public PushCanvasState(ArrayList<Shape> state) {
            this.state = state;
        }

        @Override
        public void run() {
            System.out.println("Sending push canvas request");

            try {
                writeMessage(writer, new SyncPushRequest(state));
            } catch (IOException e) {
                System.out.println("Sync write fail: " + e.getMessage());
            }
        }
    }

    /**
     * Get sync state
     */
    @Override
    public void syncFromServer() {
        new SyncFromServer().start();
    }
    private class SyncFromServer extends Thread{
        public SyncFromServer() {

        }

        @Override
        public void run() {
            System.out.println("Sending sync request");

            try {
                writeMessage(writer, new SyncPullRequest("Current state pls"));
                Message reply = readMessage(reader);
                new ProcessMsg(reply).start();
            } catch (IOException e) {
                System.out.println("Sync write fail: " + e.getMessage());
            } catch (JsonSerializationException e) {
                System.out.println("Sync read fail: " + e.getMessage());
            }
        }
    }

    /**
     * For dragged instance which finishes execution on release
     * Checks for click shapes which also click release on each click...
     * @param tool
     */
    @Override
    public void updateDraggedCanvas(Shape tool) {
        if (tool instanceof Brush || tool instanceof Line) {
            System.out.println("Sharing dragged shape");
            updateSharedCanvas(tool);
        }

    }

    /**
     * Update clients state
     */
    @Override
    public void updateBoardState(Shape shape) {
        state.add(shape);
    }


    /****************************************************************************************/

    /** Replies and Updates **/

    /**
     * Build message handling chain for session
     */
    private void buildMessageHandlerChain() {
        msgHandlerChain = new LinkedList<>();

        msgHandlerChain.addLast(this::handleCanvasUpdate);
        msgHandlerChain.addLast(this::handleGroupMessage);
        msgHandlerChain.addLast(this::handleError);
        msgHandlerChain.addLast(this::handleLoginReply);
        msgHandlerChain.addLast(this::handleCreateGroupReply);
        msgHandlerChain.addLast(this::handleJoinGroupReply);
        msgHandlerChain.addLast(this::handleJoinGroupRequest);
        msgHandlerChain.addLast(this::handleJoinGroupNotification);
        msgHandlerChain.addLast(this::handleSearchGroupReply);
        msgHandlerChain.addLast(this::handleLeaveGroupReply);
        msgHandlerChain.addLast(this::handleLeaveGroupNotification);
        msgHandlerChain.addLast(this::handleFetchLoginUserReply);
        msgHandlerChain.addLast(this::handleInviteGroupRequest);
        msgHandlerChain.addLast(this::handleLeaveGroupNotification);
        msgHandlerChain.addLast(this::handleCloseGroupReply);
        msgHandlerChain.addLast(this::handleLogoutReply);
        msgHandlerChain.addLast(this::handleSyncPushRequest);
        msgHandlerChain.addLast(this::handleSyncPullReply);
    }

    /**
     * Process message according to message handling chain built
     */
    private class ProcessMsg extends Thread {

        private Message msg;

        public ProcessMsg(Message msg) {
            this.msg = msg;
        }

        public void run() {
            for(Predicate<Message> func : msgHandlerChain) {
                if(func.test(msg)) {
                    break;
                }
            }
        }
    }

    /**
     * Chat update
     * @param msg: chat
     * @return correct message
     */
    private boolean handleGroupMessage(Message msg) {
        if (!msg.getClass().getName().equals(GroupMessage.class.getName())) {
            return false;
        }
        GroupMessage reply = (GroupMessage) msg;
        cgui.getChatBox().addGroupMessage(reply);

        return true;
    }

    /**
     * Whiteboard update
     * @param msg: shape message
     * @return correct message
     */
    private boolean handleCanvasUpdate(Message msg) {
        if (!msg.getClass().getName().equals(CanvasUpdate.class.getName())) {
            return false;
        }

        CanvasUpdate update = (CanvasUpdate) msg;
        updateBoardState(update.shape);
        cgui.updateCanvas(update.shape);

        return true;
    }


    /**
     * Invalid message
     * @param msg: error
     * @return correct message
     */
    private boolean handleError(Message msg) {
        if (!msg.getClass().getName().equals(ErrorMsg.class.getName())) {
            return false;
        }
        ErrorMsg errorMsg = (ErrorMsg)msg;
        System.out.println("Client receives error msg: " + errorMsg);

        return true;
    }

    /**
     * Checks outcome of login, reports back to user
     * @param msg: outcome
     * @return correct message
     */
    private boolean handleLoginReply(Message msg) {
        if (!msg.getClass().getName().equals(LoginReply.class.getName())) {
            return false;
        }
        System.out.println("Handling Login Reply");

        LoginReply reply = (LoginReply) msg;
        // Check success
        if (reply.outcome == true) {
            System.out.println("Login success");
            logged_in.set(true);
            cgui.promptUpdate(reply.message);
            sessionIdentity = reply.sessionIdentity;
            // TODO: update gui to reflect connection with server
            cgui.setState(NOT_IN_GROUP);
            // TODO: Method to check connection with server periodically?
        } else {
            System.out.println("Login failure.");
            new ShutdownSocket().start();
            cgui.promptError(reply.message, new ClientGUI.PromptLogin());
            cgui.setState(START_UP);
        }
        return true;
    }

    /**
     * Processes outcome of create group request
     * @param msg: outcome
     * @return correct message
     */
    private boolean handleCreateGroupReply(Message msg) {
        if (!msg.getClass().getName().equals(CreateGroupReply.class.getName())) {
            return false;
        }
        System.out.println("Handling Create Group Reply.");
        CreateGroupReply reply = (CreateGroupReply) msg;

        if (reply.outcome == true) {
            System.out.println("Group creation success");
            group_membership.set(true);
            sessionGroup = reply.group;
            cgui.promptUpdate(reply.message);
            cgui.setGroupMasterBtnState();
            cgui.getUserInfoTable().addUser(reply.user);
            cgui.getUserInfoTable().fireTableDataChanged();
            cgui.updateGroupName(reply.groupName);
            clearBoardState();
            cgui.getWhiteBoard().repaint();
            cgui.userPane.repaint();
            cgui.synchroniseBoard();
        } else {
            System.out.println("Group creation failure");
            cgui.promptError(reply.message, new ClientGUI.PromptGroupCreation());
        }
        return true;
    }

    /**
     * Gathers outcome of join group
     * @param msg: outcome
     * @return correct message
     */
    private boolean handleJoinGroupReply(Message msg) {
        if (!msg.getClass().getName().equals(JoinGroupReply.class.getName())) {
            return false;
        }
        System.out.println("Handling Join Group Reply.");
        JoinGroupReply reply = (JoinGroupReply) msg;
        UserInfoTableModel table = cgui.getUserInfoTable();

        if (reply.outcome == true) {
            System.out.println("Join group success.");
            // Change state
            group_membership.set(true);
            sessionGroup = reply.group;
            cgui.promptUpdate(reply.message);
            cgui.setGroupMemberBtnState();
            // Clear current board
            //clearBoardState();
            //cgui.getWhiteBoard().repaint();
            // Display users
            //table.clearUsers();
            table.addExistingUsers(reply.userList);
            table.fireTableDataChanged();
            cgui.updateGroupName(reply.groupName);
            cgui.userPane.repaint();
            // Display new board
            this.state = reply.state;
            cgui.synchroniseBoard();
        } else {
            System.out.println("Join group failure.");
            cgui.promptError(reply.message, new ClientGUI.PromptGroupJoin());
        }
        return true;
    }

    /**
     * For group master, confirm whether someone can join group
     * @param msg: query
     * @return correct message
     */
    private boolean handleJoinGroupRequest(Message msg) {
        if (!msg.getClass().getName().equals(JoinGroupRequest.class.getName())) {
            return false;
        }
        System.out.println("Handling Join Group Request.");
        JoinGroupRequest request = (JoinGroupRequest) msg;
        
        if (this.logged_in.get() && this.group_membership.get() && 
        		this.sessionGroup != null && this.sessionGroup.master.uuid.equals(sessionIdentity.uuid) &&
        		request.groupUUID.equals(sessionGroup.groupUUID)) {
            // Asking for approval
        	if (cgui.promptApproveJoin(request.user)) {
        		clientWriter.writeToBuffer(new JoinGroupReply("", request.groupName, null, null,
        				new GroupEntry(request.groupUUID, request.groupName, request.groupSecret, sessionIdentity), true, request.user));
        		return true;
        	}
        }
        clientWriter.writeToBuffer(new JoinGroupReply("", request.groupName, null, null,
				new GroupEntry(request.groupUUID, request.groupName, request.groupSecret, sessionIdentity), false, request.user));
        
        return true;
    }

    /**
     * Update group membership changes
     * @param msg: update
     * @return correct message
     */
    private boolean handleJoinGroupNotification(Message msg) {
        if (!msg.getClass().getName().equals(JoinGroupNotification.class.getName())) {
            return false;
        }
        System.out.println("Handling Join Group Notification.");
        UserInfoTableModel table = cgui.getUserInfoTable();

        JoinGroupNotification reply = (JoinGroupNotification) msg;
        table.addUser(reply.user);
        table.fireTableDataChanged();
        cgui.userPane.repaint();
        cgui.promptUpdate(reply.message);

        return true;
    }

    /**
     * 
     * @param msg
     * @return
     */
    private boolean handleLeaveGroupReply(Message msg) {
        if (!msg.getClass().getName().equals(LeaveGroupReply.class.getName())) {
            return false;
        }
        System.out.println("Handling Leave Group Reply.");
        LeaveGroupReply reply = (LeaveGroupReply) msg;
        UserInfoTableModel table = cgui.getUserInfoTable();
        cgui.promptUpdate(reply.message);
        table.clearUsers();
        table.fireTableDataChanged();
        cgui.updateGroupName(null);
        cgui.resetDisplay();
        if (!cgui.getGuiState().equals(START_UP)) {
            // In case of logout race condition
            cgui.setState(NOT_IN_GROUP);
        }

        // Update client
        group_membership.set(false);
        sessionGroup = null;

        return true;
    }

    /**
     * User has left, update user information
     */
    private boolean handleLeaveGroupNotification(Message msg) {
        if (!msg.getClass().getName().equals(LeaveGroupNotification.class.getName())) {
            return false;
        }
        System.out.println("Handling leave group notification");
        LeaveGroupNotification reply = (LeaveGroupNotification) msg;
        UserInfoTableModel table = cgui.getUserInfoTable();

        if (reply.user.uuid.equals(sessionIdentity.uuid)) {
        	// It's me being kicked :)
        	this.group_membership.set(false);
        	cgui.processGetKicked(reply.message);
        } else {
            cgui.promptUpdate(reply.message);
            table.removeUser(reply.user);
            table.fireTableDataChanged();
            cgui.userPane.repaint();
        }

        return true;
    }

    /**
     * Get searching results from server
     * then call ClientGUI to display
     * @param msg
     * @return 
     */
    private boolean handleSearchGroupReply(Message msg) {
    	if (!msg.getClass().getName().equals(SearchGroupReply.class.getName())) {
            return false;
        }
        System.out.println("Handling search group reply");
        SearchGroupReply reply = (SearchGroupReply) msg;
        System.out.println("Search hit: " + reply.searchHits.size());
        cgui.promptSearchGroupInfo(reply.searchHits);
        
        return true;
    }

    /**
     * Retrieve users logged in
     * @param msg result
     * @return correct message
     */
    private boolean handleFetchLoginUserReply(Message msg) {
    	if (!msg.getClass().getName().equals(FetchLoginUserReply.class.getName())) {
            return false;
        }
        System.out.println("Handling search group reply");


        FetchLoginUserReply reply = (FetchLoginUserReply) msg;
        cgui.promptFetchLoginUserInfo(reply.loginUsers);
        
        return true;
    }

    /**
     * Invitation to join group
     * @param msg invitation
     * @return correct message
     */
    private boolean handleInviteGroupRequest(Message msg) {
    	if (!msg.getClass().getName().equals(InviteGroupRequest.class.getName())) {
            return false;
        }
        System.out.println("Handling invite group request");


        InviteGroupRequest reply = (InviteGroupRequest) msg;
        if (!this.group_membership.get()) {
        	cgui.promptGroupInvitation(reply.inviter, reply.group);
        }
        
        return true;
    }

    /**
     * Update when group closed
     * @param msg update
     * @return correct message
     */
    private boolean handleCloseGroupReply(Message msg) {
    	if (!msg.getClass().getName().equals(CloseGroupReply.class.getName())) {
            return false;
        }
        System.out.println("Handling close group reply");

        CloseGroupReply reply = (CloseGroupReply) msg;
        if (reply.outcome) {
            cgui.processGroupClosed(reply.message);
            this.group_membership.set(false);
            this.sessionGroup = null;
            this.state = new ArrayList<>();
            cgui.resetTool();
            cgui.resetDisplay();
            cgui.synchroniseBoard();
        }
        return true;
    }

    /**
     * Outcome of logging out
     * @param msg outcome
     * @return correct message
     */
    private boolean handleLogoutReply(Message msg) {
        if (!msg.getClass().getName().equals(LogoutReply.class.getName())) {
            return false;
        }

        System.out.println("Handling logout reply");
        LogoutReply reply = (LogoutReply) msg;

        if (reply.outcome) {
        	System.out.println("logged out");
            this.group_membership.set(false);
            this.sessionGroup = null;
            this.sessionIdentity = null;
            this.logged_in.set(false);
            clientWriter.interrupt();
            new ShutdownSocket().start();
            cgui.processLogout(reply.message);
        }

        return true;
    }

    /**
     * Synchronise board from manager
     * @param msg current board
     * @return correct message
     */
    private boolean handleSyncPushRequest(Message msg) {
    	if (!msg.getClass().getName().equals(SyncPushRequest.class.getName())) {
            return false;
        }

        System.out.println("Handling sync push request");
        SyncPushRequest reply = (SyncPushRequest) msg;

        if (this.group_membership.get()) {
        	state = reply.state;
            cgui.synchroniseBoard();
        }
        
    	return true;
    }

    /**
     * Synchronise board
     * @param msg current board
     * @return correct message
     */
    private boolean handleSyncPullReply(Message msg) {
    	if (!msg.getClass().getName().equals(SyncPullReply.class.getName())) {
            return false;
        }

        System.out.println("Handling sync push request");
        SyncPullReply reply = (SyncPullReply) msg;

        if (this.group_membership.get()) {
            // New state
        	state = reply.state;
            cgui.synchroniseBoard();
        }
        
    	return true;
    }

    /****************************************************************************************/

    /** Manager Changes **/

    /**
     * Implement File/new
     */
    @Override
    public void newCanvas() {
        if (this.logged_in.get() && this.group_membership.get() && this.sessionGroup != null && sessionIdentity != null
                && this.sessionGroup.master.uuid.equals(sessionIdentity.uuid)) {
            state = new ArrayList<>();
            pushCanvasState();
            cgui.synchroniseBoard();
        }
    }

    /**
     * Implement File/open
     */
    @Override
    public void openCanvas() {
        if (this.logged_in.get() && this.group_membership.get() && this.sessionGroup != null && sessionIdentity != null
                && this.sessionGroup.master.uuid.equals(sessionIdentity.uuid)) {
            File datFile = cgui.promptChooseOpenFile();
            if (datFile == null) {
                return;
            }
            CanvasData data = CanvasDataFactory.getInstance().readFromFile(datFile);
            if (data != null) {
                System.out.println("Read data successfully");
                state = data.state;
                pushCanvasState();
                cgui.synchroniseBoard();
            }
        }
    }

    /**
     * Implement File/save
     */
    @Override
    public void saveCanvas() {
        if (this.logged_in.get() && this.group_membership.get() && this.sessionGroup != null) {
            File datFile = cgui.promptChooseSaveFile();
            if (datFile == null) {
                return;
            }
            String filepath = datFile.getAbsolutePath();
            if (!filepath.endsWith(".json")) {
                datFile = new File(filepath + ".json");
            }

            boolean res = CanvasDataFactory.getInstance().writeToFile(new CanvasData(state), datFile);
            if (res) {
                System.out.println("Write data successfully");
                cgui.promptUpdate("Saved!");
            } else {
                cgui.promptUpdate("Save Fail");
            }
        }
    }

    /**
     * implement File/saveAs
     */
    @Override
    public void saveCanvasAsJPEG() {
        if (this.logged_in.get() && this.group_membership.get() && this.sessionGroup != null) {
            File datFile = cgui.promptChooseSaveAs();
            if (datFile == null) {
                return;
            }
            String filepath = datFile.getAbsolutePath();
            BufferedImage canvasBuffer = cgui.getWhiteBoard().getCanvasSnapshot();
            if (canvasBuffer == null) {
                System.out.println("SaveAs: No canvas available");
                return ;
            }

            String suffix = "jpg";
            int i = filepath.lastIndexOf('.');
            if (i > 0) {
                suffix = filepath.substring(i+1);
            } else {
                datFile = new File(datFile.getAbsolutePath() + "." + suffix);
            }

            boolean res = false;
            try {
                datFile.createNewFile();
                System.out.println("Write img: " + suffix + ", " + datFile.getAbsolutePath());
                res = ImageIO.write(canvasBuffer, suffix, datFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (res) {
                System.out.println("Write data successfully");
                cgui.promptUpdate("Saved!");
            } else {
                cgui.promptUpdate("Save Fail");
            }
        }
    }

    /****************************************************************************************/

    /** Helper functions **/

    /**
     * Reads messages
     * @param reader : BufferedReader
     * @return Message
     * @throws IOException : on reading
     * @throws JsonSerializationException : on parsing
     */
    synchronized private Message readMessage(BufferedReader reader) throws IOException, JsonSerializationException {

        if (reader != null) {
            String jsonStr = reader.readLine();
            if (jsonStr != null) {
                Message msg = (Message) MessageFactory.fromJsonString(jsonStr);
                System.out.println("Client receives " + msg.toString());
                return msg;
            } else {
                throw new IOException();
            }
        }
        return null;
    }

    /**
     * Writes message
     * @param writer : BuffferedWriter
     * @param msg : Message
     * @throws IOException : on write
     */
    synchronized private void writeMessage(BufferedWriter writer, Message msg) throws IOException {
        System.out.println("Client sends " + msg.toString());
        clientWriter.writeToBuffer(msg);
    }

    /****************************************************************************************/

    /** Getters **/

    private BufferedReader getBufferedReader(Socket socket) throws IOException{
        BufferedInputStream bufferedInputStream = new BufferedInputStream(socket.getInputStream());
        return new BufferedReader(new InputStreamReader(bufferedInputStream));
    }

    private BufferedWriter getBufferedWriter(Socket socket) throws IOException{
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
        return new BufferedWriter(new OutputStreamWriter(bufferedOutputStream));
    }

    public User getSessionIdentity() {
        return sessionIdentity;
    }

    public ArrayList<Shape> getBoardState() {
        return state;
    }
    private void clearBoardState() { state = new ArrayList<>();}

}
/****************************************************************************************/
