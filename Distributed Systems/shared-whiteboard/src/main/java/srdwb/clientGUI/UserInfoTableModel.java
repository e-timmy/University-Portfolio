package srdwb.clientGUI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import srdwb.message.GroupEntry;
import srdwb.message.User;

/****************************************************************************************/

/**
 * For user display in GUI
 */
public class UserInfoTableModel extends AbstractTableModel {
	
	private ArrayList<User> users = new ArrayList<>();
	private static final String[] columns = {
		"User",
		"IP",
		"Status"
	};

	public UserInfoTableModel() {}
	
	public UserInfoTableModel(Collection<User> users) {
		this.users = new ArrayList<User>(users);
	}

	/****************************************************************************************/

	public void addUser(User user) {
		users.add(user);
	}

	public void addExistingUsers(HashMap<String, User> users) {
		for (User user: users.values()) {
			this.users.add(user);
		}
	}
	
	public void removeUser(User user) {
		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).uuid.equals(user.uuid)) {
				users.remove(i);
				return;
			}
		}
	}

	public void clearUsers() {
		users.clear();
	}

	/****************************************************************************************/

	/** Getters and Setters **/

	public ArrayList<User> getUsers() {
		return users;
	}

	public User getSelectedUser(int index) {
		if(index >= 0 && index < users.size()) {
			return users.get(index);
		}
		return null;
	}

	@Override
	public int getRowCount() {
		return users.size();
	}

	@Override
	public int getColumnCount() {
		return columns.length;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columns[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		User user = users.get(rowIndex);

		switch(columnIndex) {
		case 0:
			return user.name;
		case 1:
			return user.ip;
		case 2:
			return user.status;
		}
		return "Unknown info";

	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}
}
