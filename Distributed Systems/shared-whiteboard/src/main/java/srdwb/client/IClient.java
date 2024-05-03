package srdwb.client;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JMenuItem;

import srdwb.Shapes.Shape;
import srdwb.message.GroupMessage;
import srdwb.message.Message;
import srdwb.message.User;

/** Interface for clients
 *
 */
public interface IClient {

    /** Functionality **/
    public void shutdown() throws IOException, InterruptedException;
    public void closeProgram();

    /** Requests **/
    public void login(String username, String serverSecret);
    public void startNewGroup(String group, String secret);
    public void searchGroup(ArrayList<String> keywords);
    public void invite(User user);
    public void fetchLoginUsersForInvitation();
    public void closeGroup();
    public void kickUser(User user);
    public void joinGroup(String groupName, String groupSecret, String groupUUID);
    public void leaveGroup();
    public void sendGroupMessage(GroupMessage message);
    public void logout();
    public void pushCanvasState();
    public void syncFromServer();
    public void updateDraggedCanvas(Shape tool);
    public void updateBoardState(Shape shape);
    public void updateSharedCanvas(Shape shape);

    /** File administration **/
    public void newCanvas();
    public void openCanvas();
    public void saveCanvas();
    public void saveCanvasAsJPEG();

}
