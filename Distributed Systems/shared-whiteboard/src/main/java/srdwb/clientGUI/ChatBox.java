package srdwb.clientGUI;

import java.awt.Font;

import javax.swing.JTextArea;

import srdwb.message.GroupMessage;
import srdwb.message.User;

/****************************************************************************************/

/**
 * Displays chatbox for users
 */
public class ChatBox extends JTextArea {
	private static final long serialVersionUID = 1L;

	public ChatBox() {
		init();
	}
	
	public void init() {
		clear();
		setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setEditable(false);
	}

	/****************************************************************************************/

	/**
	 * Info for whiteboard
	 * @param info : String
	 */
	public void addInfo(String info) {
		append(info + "\n");
	}

	/**
	 * Add group members message
	 * @param message : Message
	 */
	public void addGroupMessage(GroupMessage message) {
		StringBuilder strBuilder = new StringBuilder();
		User user = message.sender;
		strBuilder.append(user.name);
		strBuilder.append("@");
		strBuilder.append(user.ip);
		strBuilder.append(": ");
		strBuilder.append(message.message);
		strBuilder.append("\n");
		append(strBuilder.toString());		
	}

	/**
	 * Add client's message
	 * @param message : message
	 */
	public void addClientMessage(GroupMessage message) {
		StringBuilder strBuilder = new StringBuilder();
		User user = message.sender;
		strBuilder.append("Me");
		strBuilder.append("@");
		strBuilder.append(user.ip);
		strBuilder.append(": ");
		strBuilder.append(message.message);
		strBuilder.append("\n");
		append(strBuilder.toString());
	}

	/**
	 * Clear chat
	 */
	public void clear() {
		setText("");
	}
}

/****************************************************************************************/
