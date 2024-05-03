package srdwb.clientGUI;

import java.util.ArrayList;

import javax.swing.event.TableModelListener;
import javax.swing.table.*;

import srdwb.client.Client;
import srdwb.message.GroupEntry;

/****************************************************************************************/

/**
 * Group members display
 */
public class GroupInfoTableModel implements TableModel {
	
	private ArrayList<GroupEntry> groupInfo = new ArrayList<GroupEntry>();
	private static final String[] columns = {
			"GroupName",
			"IP"
	};

	/****************************************************************************************/

	public GroupInfoTableModel(Client client, ArrayList<GroupEntry> groupInfo) {
		this.groupInfo = groupInfo;
	}

	@Override
	public int getRowCount() {
		return groupInfo.size();
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
		switch(getColumnName(columnIndex)) {
		case "GroupName":
			return String.class;
		case "IP":
			return String.class;
		default:
			return Object.class;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch(getColumnName(columnIndex)) {
		case "GroupName":
			return groupInfo.get(rowIndex).groupName;
		case "IP":
			return groupInfo.get(rowIndex).master.ip;
		default:
			return "Unknown";
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		// TODO Auto-generated method stub
		
	}
	
	public GroupEntry getSelectedGroup(int index) {
		if(index >= 0 && index < groupInfo.size()) {
			return groupInfo.get(index);
		}
		return null;
	}
}
/****************************************************************************************/
