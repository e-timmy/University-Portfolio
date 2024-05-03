package srdwb.clientGUI;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import srdwb.message.GroupEntry;
import srdwb.message.User;


public interface IClientGUI {
	void promptUpdate(String message);
	void promptError(String message, Callable method);
	void promptSearchGroupInfo(ArrayList<GroupEntry> entries);
	boolean promptApproveJoin(User user);
	void processLogout(String message);
	void promptFetchLoginUserInfo(ArrayList<User> users);
	void promptGroupInvitation(User inviter, GroupEntry group);
	void processGetKicked(String message);
	void processGroupClosed(String message);
	boolean promptSavePane();
	File promptChooseSaveFile();
	File promptChooseSaveAs();
	File promptChooseOpenFile();
	int promptGoodbye();
}
