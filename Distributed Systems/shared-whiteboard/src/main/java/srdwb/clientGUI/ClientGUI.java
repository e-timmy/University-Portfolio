package srdwb.clientGUI;
import srdwb.Shapes.*;
import srdwb.Shapes.Shape;
import srdwb.client.Client;
import srdwb.message.GroupMessage;
import srdwb.message.GroupEntry;
import srdwb.message.User;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.Callable;


/****************************************************************************************/

/**
 * Handles all display to client
 */
public class ClientGUI extends JFrame implements IClientGUI {

	// General information
	public static final long serialVersionUID = 1L;
	public static final String VERSION_INFO = "Shared Whiteboard Client 0.1.0";
	private ClientState guiState;

	/****************************************************************************************/
	/** Drawing tools **/

	// Buttons
	private JButton triBtn;
	private JButton circleBtn;
	private JButton lineBtn;
	private JButton recBtn;
	private JButton textBtn;
	private JButton freeBtn;
	private JButton colourBtn;
	private JButton bigBtn;
	private JButton lilBtn;
	// Chat
	private JButton sendBtn;
	private JTextField sendMsg;

	// Panes
	private static WhiteBoard whiteBoard;
	public JScrollPane userPane;
	private static JPanel mainPane;

	// Tables
	private UserInfoTableModel userInfoTable = new UserInfoTableModel();
	private ChatBox chatBox = new ChatBox();

	// Borders
	private JPanel userInfoBorder;
	private JPanel contentPaneBorder;


	// Drawing
	private Color defaultColor = new Color(173,216,230);	// light blue
	private Color currentColour = defaultColor;
	private double defaultBrushSize = 4;
	private double size = defaultBrushSize;
	private Shape tool;
	private ArrayList<GroupEntry> groupSearchCache;

	/****************************************************************************************/
	/** Menu Buttons */
	private JMenuItem shareMenuNewItem;
	private JMenuItem shareMenuSearchItem;
	private JMenuItem shareMenuJoinItem;
	private JMenuItem shareMenuLeaveItem;
	private JMenuItem shareMenuSyncItem;
	
	private JMenuItem inviteMenuItem;
	private JMenuItem kickMenuItem;
	
	private JMenuItem fileMenuNewItem;
	private JMenuItem fileMenuOpenItem;
	private JMenuItem fileMenuSaveItem;
	private JMenuItem fileMenuSaveAsItem;
	private JMenuItem fileMenuCloseItem;

	private JMenuItem loginMenuLogin;
	private JMenuItem loginMenuLogout;
	
	private JMenu shareMenu;

	/****************************************************************************************/

	/** Connection details */
	private InetAddress serverAddress;
	private int serverPort;
	private String serverSecret;
	private static Client client;
	private File baseDir;
	

	/****************************************************************************************/

	/** Initialisation **/

	public ClientGUI(InetAddress serverAddress, int serverPort, String serverSecret, File baseDir) {
		this.serverPort = serverPort;
		this.serverAddress = serverAddress;
		this.serverSecret = serverSecret;
		this.baseDir = baseDir;
		resetTool();
	}

	public void addClient(Client client) {this.client = client;}

	/**
	 * States for user access to GUI functionality
	 * Startup - can only login
	 * Not in group - can only join or create group
	 * Group master - can kick member, close group, save/open/new file
	 * Group member - can draw and chat
	 * @param state
	 */
	public void setState(ClientState state) {
		resetBtnState();
		switch(state) {
		case START_UP:
			guiState = ClientState.START_UP;
			setStartUpBtnState();
			break;
		case NOT_IN_GROUP:
			guiState = ClientState.NOT_IN_GROUP;
			setNotInGroupBtnState();
			break;
		case GROUP_MASTER:
			guiState = ClientState.GROUP_MASTER;
			setGroupMasterBtnState();
			break;
		case GROUP_MEMBER:
			guiState = ClientState.GROUP_MEMBER;
			setGroupMemberBtnState();
			break;
		default:
			break;
		}
	}

	public void setup() {
		// get from configuration file
		this.setSize(new Dimension(1000, 600));
		//this.setSize(new Dimension(1400, 800));
		this.setTitle(VERSION_INFO);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setLocationByPlatform(true);
		this.setContentPane(createMainPane());
		this.setVisible(true);
		this.setState(ClientState.START_UP);
		this.setResizable(false);
		this.setupWindowListener();
		whiteBoard.clearCanvas();

		// Begin
		new PromptLogin().call();

	}

	/****************************************************************************************/

	/** GUI DISPLAY **/

	/**
	 * Constructs main panel and helper panels within
	 * Each panel implementation found below
	 * @return JPanel
	 */
	public JPanel createMainPane() {
		// menuBar
		mainPane = new JPanel(new BorderLayout());
		mainPane.add(createContentPane(), BorderLayout.CENTER);
		mainPane.add(createPalettePane(), BorderLayout.WEST);
		mainPane.add(createMenuBar(), BorderLayout.NORTH);
		mainPane.add(createInfoPane(), BorderLayout.EAST);
		mainPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		return mainPane;
	}

	/**
	 * Application closure
	 */
	public void setupWindowListener() {
		this.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {}
			@Override
			public void windowClosing(WindowEvent e) {
				client.closeProgram();
			}
			@Override
			public void windowClosed(WindowEvent e) {}
			@Override
			public void windowIconified(WindowEvent e) {}
			@Override
			public void windowDeiconified(WindowEvent e) {}
			@Override
			public void windowActivated(WindowEvent e) {}
			@Override
			public void windowDeactivated(WindowEvent e) {}
		});
	}

	/****************************************************************************************/

	/** User Information Design & Functionality **/

	/**
	 * Display group members and chat
	 * @return
	 */
	public JPanel createInfoPane() {
		JPanel pane = new JPanel(new BorderLayout());
		pane.setSize(new Dimension(50, 50));
		
		pane.add(createUserInfo(), BorderLayout.NORTH);
		pane.add(createChatArea(), BorderLayout.CENTER);

		return pane;
	}

	/**
	 * Displays user info
	 * @return JScrollPane
	 */
	public JScrollPane createUserInfo() {
		userInfoBorder = new JPanel();
		updateGroupName(null);
		JTable userTable = new JTable(userInfoTable);
		userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		userTable.getTableHeader().setReorderingAllowed(false);
		
		userInfoBorder.add(userTable);
		userPane = new JScrollPane(userInfoBorder, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		userPane.setPreferredSize(new Dimension(100, 200));
		userPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10, 0, 10, 0),
				BorderFactory.createLineBorder(Color.BLACK, 1)));
		return userPane;
	}

	/**
	 * Updates group name display
	 * @param name : String
	 */
	public void updateGroupName(String name) {
		if (name == null) {
			userInfoBorder.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
					"No group active", TitledBorder.CENTER, TitledBorder.TOP));
		} else {
			userInfoBorder.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
					name, TitledBorder.CENTER, TitledBorder.TOP));
		}
	}

	/**
	 * Creates user chat
	 * @return : JPanel
	 */
	public JPanel createChatArea() {
		JPanel msgPane = new JPanel(new BorderLayout());
		
		chatBox.addInfo("<<Welcome to SharedWhiteboard!>>");
		JScrollPane msgPoolScrollPane = new JScrollPane(chatBox, 
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		msgPoolScrollPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(2, 2, 2, 2),
				BorderFactory.createLineBorder(Color.BLACK, 0)));
		msgPane.add(msgPoolScrollPane, BorderLayout.CENTER);
		
		JPanel sendMsgPane = new JPanel(new FlowLayout());
		sendBtn = new JButton("Send");
		sendMsg = new JTextField(20);
		sendMsgPane.add(sendMsg);
		sendMsgPane.add(sendBtn);
		setChatListener();
		
		msgPane.add(sendMsgPane, BorderLayout.SOUTH);
		msgPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10, 0, 10, 0),
				BorderFactory.createLineBorder(Color.BLACK, 1)));
		return msgPane;
	}

	/**
	 * Listeners for chat
	 * Handles chat messages for client
	 */
	public void setChatListener() {

		sendMsg.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendChatMessage();
			}
		});
		sendBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendChatMessage();
			}
		});

	}

	/**
	 * Dispatches chat message from client
	 */
	private void sendChatMessage() {
		String text = sendMsg.getText();
		sendMsg.setText("");
		GroupMessage message = new GroupMessage(client.getSessionIdentity(), text);
		chatBox.addClientMessage(message);
		client.sendGroupMessage(message);
	}

	/****************************************************************************************/

	/** Design and Functionality for Whiteboard **/

	/**
	 * Creates whiteboard itself
	 * @return : JPanel
	 */
	public JPanel createContentPane() {
		contentPaneBorder = new JPanel(new BorderLayout());
		contentPaneBorder.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(5, 10, 5, 10),
				BorderFactory.createLineBorder(Color.BLACK, 1)));
		whiteBoard = new WhiteBoard(client);
		whiteBoard.setBackground(Color.WHITE);

		contentPaneBorder.add(whiteBoard, BorderLayout.CENTER);
		setContentListener();

		return contentPaneBorder;
	}

	/**
	 * Listens to events on whiteboard
	 * Responds by activating current tool (a shape/brush)
	 */
	private void setContentListener() {

		whiteBoard.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				System.out.println("Clicking pane");
				tool.draw(whiteBoard, e, currentColour, size);
				whiteBoard.paintImmediately();
				if (tool.finished) {
					client.updateSharedCanvas(tool);
				}
				super.mouseClicked(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				System.out.println("Mouse released");

				// Allow object to finalise
				tool.finalise(whiteBoard, e, currentColour);
				whiteBoard.paintImmediately();
				if (tool.finished) {
					client.updateDraggedCanvas(tool);
				}
				// Refresh object on release
				tool = tool.refresh();

				super.mouseReleased(e);
			}
		});

		whiteBoard.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				tool.dragDraw(whiteBoard, e, currentColour, size);
				whiteBoard.paintImmediately();
				super.mouseDragged(e);
			}
		});
		whiteBoard.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				super.mouseDragged(e);
			}
		});
	}


	/****************************************************************************************/

	/** Design and Functionality for Palette **/

	/**
	 * Creates drawing palette
	 * @return : JPanel
	 */
	public JPanel createPalettePane() {
		JPanel palette = new JPanel(new GridLayout(2, 1));

		JPanel shapePane = new JPanel(new GridLayout(10, 1));


		lineBtn = new JButton("Line");
		circleBtn = new JButton("Circle");
		triBtn = new JButton("Triangle");
		recBtn = new JButton("Rectangle");
		textBtn = new JButton("Text");
		freeBtn = new JButton("Free Draw");

		shapePane.add(lineBtn);
		shapePane.add(circleBtn);
		shapePane.add(triBtn);
		shapePane.add(recBtn);
		shapePane.add(textBtn);
		shapePane.add(freeBtn);

		JPanel stylePane = new JPanel(new GridLayout(10, 1));

		// Drawing adjustments
		colourBtn = new JButton("Colour");
		bigBtn = new JButton("+");
		lilBtn = new JButton("-");
		stylePane.add(colourBtn);
		stylePane.add(bigBtn);
		stylePane.add(lilBtn);

		setButtonListeners();

		palette.add(shapePane, BorderLayout.NORTH);
		palette.add(stylePane, BorderLayout.SOUTH);

		return palette;
	}


	/**
	 * Initialises events for palette buttons
	 * Primarily changes tool given button click
	 * Option to change colour - displays colour panel
	 */
	private void setButtonListeners() {

		// Line
		lineBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!lineBtn.isEnabled())
					return;
				tool = new srdwb.Shapes.Line();
				System.out.println("Clicked Line");
				super.mouseClicked(e);
			}
		});

		// Circle
		circleBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!circleBtn.isEnabled())
					return;
				tool = new srdwb.Shapes.Circle();
				System.out.println("Clicked Circle");
				super.mouseClicked(e);
			}
		});

		// Rectangle
		recBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!recBtn.isEnabled())
					return;
				tool = new srdwb.Shapes.Rectangle();
				System.out.println("Clicked Rectangle");
				super.mouseClicked(e);
			}
		});

		// Triangle
		triBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!triBtn.isEnabled())
					return;
				tool = new Triangle();
				System.out.println("Clicked Triangle");
				super.mouseClicked(e);
			}
		});

		textBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!textBtn.isEnabled())
					return;
				tool = new TextBox();
				System.out.println("Clicked Text");
				super.mouseClicked(e);
			}
		});

		freeBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(!freeBtn.isEnabled())
					return;
				System.out.println("Clicked brush.");
				tool = new Brush();
				super.mouseClicked(e);
			}
		});

		colourBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!colourBtn.isEnabled())
					return;
				System.out.println("Clicked colour");
				Color colour = JColorChooser.showDialog(null, "Choose Colour",
						Color.WHITE);
				if (colour != null) {
					currentColour = colour;
				}
				super.mouseClicked(e);
			}
		});

		bigBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!bigBtn.isEnabled())
					return;
				System.out.println("Clicked +");

				size *= 1.1;
				System.out.println("Size: " + size);
				super.mouseClicked(e);
			}
		});

		lilBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!lilBtn.isEnabled())
					return;
				System.out.println("Clicked -");

				if (size >= 1.1) {
					size /= 1.1;
				}
				System.out.println("Size: " + size);
				super.mouseClicked(e);
			}
		});
	}

	/**
	 * Updates canvas from group drawing
	 * @param shape
	 */
	public void updateCanvas(srdwb.Shapes.Shape shape) {
		shape.drawGraphics(whiteBoard.getGraphics());
		whiteBoard.paintImmediately();
	}

	/**
	 * Resets shape after use
	 */
	public void resetTool() {
		tool = new Nothing();
		currentColour = defaultColor;
		size = defaultBrushSize;
	}

	/**
	 * Updates board on new client state
	 */
	public void synchroniseBoard() {
		whiteBoard.synchroniseBoard();
	}

	/**
	 * Resets Gui
	 */
	public void resetDisplay() {
		updateGroupName(null);
		this.userInfoTable.clearUsers();
		this.userInfoTable.fireTableDataChanged();
		chatBox.clear();
		resetTool();
		whiteBoard.clearCanvas();
	}

	/****************************************************************************************/

	/** Menu Panes and Buttons **/

	/**
	 * Creates menu
	 * @return : JMenuBar
	 */
	public JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		createFileMenu(menuBar);
		createLoginMenu(menuBar);
		createManageMenu(menuBar);
		createShareMenu(menuBar);
		createAboutMenu(menuBar);
		return menuBar;
	}

	/**
	 * Creates about menu
	 * @param menuBar
	 */
	public void createAboutMenu(JMenuBar menuBar) {
		JMenu menu = new JMenu("About");
		menuBar.add(menu);
		
		JMenuItem item1 = new JMenuItem("Version");
		JMenuItem item2 = new JMenuItem("Help");
		
		menu.add(item1);
		menu.add(item2);
		
		JFrame frame = this;
		
		item1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(frame, VERSION_INFO);
			}
		});
		
		item2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(frame, "Use java -cp <path to jar> srdwb.ClientDriver --help on command line");
			}
		});
	}

	/**
	 * Creates group functionality for menu
	 * @param menuBar
	 */
	public void createShareMenu(JMenuBar menuBar) {
		shareMenu = new JMenu("Share");
		menuBar.add(shareMenu);

		shareMenuNewItem = new JMenuItem("Create");
		shareMenuSearchItem = new JMenuItem("Search");
		shareMenuJoinItem = new JMenuItem("Join");
		shareMenuLeaveItem = new JMenuItem("Leave");
		shareMenuSyncItem = new JMenuItem("Sync");

		shareMenu.add(shareMenuNewItem);
		shareMenu.add(shareMenuSearchItem);
		shareMenu.add(shareMenuJoinItem);
		shareMenu.add(shareMenuLeaveItem);
		shareMenu.add(shareMenuSyncItem);

		setShareListeners();
	}

	/**
	 * Listeners for share menu
	 */
	private void setShareListeners() {
		JFrame frame = this;
		
		shareMenuNewItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new PromptGroupCreation().call();
			}
		});
		
		shareMenuSearchItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String input = JOptionPane.showInputDialog(frame, "Input keywords : separate by space", "Keywords", JOptionPane.PLAIN_MESSAGE);
				String[] keywords = input.split(" ");
				ArrayList<String> list = new ArrayList<>();
				for(int i = 0; i < keywords.length; i++) {
					list.add(keywords[i]);
				}
				client.searchGroup(list);
			}
			
		});

		shareMenuJoinItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new PromptGroupJoin().call();
			}
		});

		shareMenuLeaveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				client.leaveGroup();
			}
		});
		
		shareMenuSyncItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				client.syncFromServer();
			}
		});
	}

	/**
	 * Creates manger menu
	 * @param menuBar
	 */
	public void createManageMenu(JMenuBar menuBar) {
		JMenu menu = new JMenu("Manage");
		menuBar.add(menu);
		
		inviteMenuItem = new JMenuItem("Invite");
		kickMenuItem = new JMenuItem("Kick");
		
		inviteMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				client.fetchLoginUsersForInvitation();
			}
		});
		
		kickMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UserInfoTableModel model = new UserInfoTableModel(userInfoTable.getUsers());
				JTable table = new JTable(model);
				table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				table.getTableHeader().setReorderingAllowed(false);
				
				JScrollPane userPane = new JScrollPane(table);
				
				int selection = JOptionPane.showConfirmDialog(mainPane, userPane, "Kick User", JOptionPane.YES_OPTION);
				int selectedRow = table.getSelectedRow();
				if (selection == JOptionPane.YES_OPTION && selectedRow != -1) {
					User user = model.getSelectedUser(selectedRow);
					client.kickUser(user);
					System.out.println("Kick user : " + user.name + "@" + user.ip);
				}
				else {
					System.out.println("Fail to kick user");
				}
			}
		});
		
		menu.add(inviteMenuItem);
		menu.add(kickMenuItem);
	}

	/**
	 * Creates login menu
	 * @param menuBar
	 */
	private void createLoginMenu(JMenuBar menuBar) {
		JMenu menu = new JMenu("Login");
		menuBar.add(menu);

		loginMenuLogin = new JMenuItem("Login");
		loginMenuLogout = new JMenuItem("Logout");

		menu.add(loginMenuLogin);
		menu.add(loginMenuLogout);
		setLoginListeners();
	}

	/**
	 * Listeners for login
	 */
	private void setLoginListeners() {

		loginMenuLogin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new PromptLogin().call();
			}
		});

		loginMenuLogout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				client.logout();
			}
		});

	}

	/**
	 * Creates file menu
	 * @param menuBar
	 */
	public void createFileMenu(JMenuBar menuBar) {
		JMenu menu = new JMenu("File");
		menuBar.add(menu);
		
		fileMenuNewItem = new JMenuItem("New");
		fileMenuOpenItem = new JMenuItem("Open");
		fileMenuSaveItem = new JMenuItem("Save");
		fileMenuSaveAsItem = new JMenuItem("SaveAs");
		fileMenuCloseItem = new JMenuItem("Close");
		
		menu.add(fileMenuNewItem);
		menu.add(fileMenuOpenItem);
		menu.add(fileMenuSaveItem);
		menu.add(fileMenuSaveAsItem);
		menu.add(fileMenuCloseItem);
		
		fileMenuNewItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (promptSavePane()) {
					client.saveCanvas();
				}
				client.newCanvas();
			}
		});
		
		fileMenuOpenItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (promptSavePane()) {
					client.saveCanvas();
				}
				client.openCanvas();
			}
			
		});
		
		fileMenuSaveItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				client.saveCanvas();
			}
			
		});
		
		fileMenuSaveAsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				client.saveCanvasAsJPEG();
			}
		});
		
		fileMenuCloseItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				client.closeProgram();
			}
		});
	}


	/****************************************************************************************/

	/** GUI States **/

	private void setStartUpBtnState() {
		shareMenuNewItem.setEnabled(false);
		shareMenuSearchItem.setEnabled(false);
		shareMenuJoinItem.setEnabled(false);
		shareMenuLeaveItem.setEnabled(false);
		shareMenuSyncItem.setEnabled(false);
		
		inviteMenuItem.setEnabled(false);
		kickMenuItem.setEnabled(false);
		
		loginMenuLogin.setEnabled(true);
		loginMenuLogout.setEnabled(false);
		
		fileMenuNewItem.setEnabled(false);
		fileMenuOpenItem.setEnabled(false);
		fileMenuSaveItem.setEnabled(false);
		fileMenuSaveAsItem.setEnabled(false);
		fileMenuCloseItem.setEnabled(false);

		setChatEnabled(false);
		setDrawBtnEnabled(false);
	}
	
	public void setNotInGroupBtnState() {
		shareMenuNewItem.setEnabled(true);
		shareMenuSearchItem.setEnabled(true);
		shareMenuJoinItem.setEnabled(true);
		
		// change Leave action item to Leave
		shareMenu.remove(shareMenuLeaveItem);
		shareMenuLeaveItem = new JMenuItem("Leave");
		shareMenuLeaveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				client.leaveGroup();
			}
		});
		shareMenu.add(shareMenuLeaveItem);
		shareMenuLeaveItem.setEnabled(false);
		shareMenuSyncItem.setEnabled(false);

		inviteMenuItem.setEnabled(false);
		kickMenuItem.setEnabled(false);
		
		loginMenuLogin.setEnabled(false);
		loginMenuLogout.setEnabled(true);
		
		fileMenuNewItem.setEnabled(false);
		fileMenuOpenItem.setEnabled(false);
		fileMenuSaveItem.setEnabled(false);
		fileMenuSaveAsItem.setEnabled(false);
		fileMenuCloseItem.setEnabled(false);

		setChatEnabled(false);
		setDrawBtnEnabled(false);
	}
	
	public void setGroupMasterBtnState() {
		shareMenuNewItem.setEnabled(false);
		shareMenuSearchItem.setEnabled(false);
		shareMenuJoinItem.setEnabled(false);
		// change Leave action to close
		shareMenu.remove(shareMenuLeaveItem);
		shareMenuLeaveItem = new JMenuItem("Close");
		shareMenuLeaveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				client.closeGroup();
			}
		});
		shareMenu.add(shareMenuLeaveItem);
		shareMenuLeaveItem.setEnabled(true);
		shareMenuSyncItem.setEnabled(true);
		
		inviteMenuItem.setEnabled(true);
		kickMenuItem.setEnabled(true);
		
		fileMenuNewItem.setEnabled(true);
		fileMenuOpenItem.setEnabled(true);
		fileMenuSaveItem.setEnabled(true);
		fileMenuSaveAsItem.setEnabled(true);
		fileMenuCloseItem.setEnabled(true);

		setChatEnabled(true);
		setDrawBtnEnabled(true);
	}
	
	public void setGroupMemberBtnState() {
		shareMenuNewItem.setEnabled(false);
		shareMenuSearchItem.setEnabled(false);
		shareMenuJoinItem.setEnabled(false);
		shareMenuLeaveItem.setEnabled(true);
		shareMenuSyncItem.setEnabled(true);

		inviteMenuItem.setEnabled(true);
		kickMenuItem.setEnabled(false);
		
		fileMenuNewItem.setEnabled(false);
		fileMenuOpenItem.setEnabled(false);
		fileMenuSaveItem.setEnabled(false);
		fileMenuSaveAsItem.setEnabled(false);
		fileMenuCloseItem.setEnabled(false);

		setChatEnabled(true);
		setDrawBtnEnabled(true);
	}
	
	public void resetBtnState() {
		shareMenuNewItem.setEnabled(true);
		shareMenuSearchItem.setEnabled(true);
		shareMenuJoinItem.setEnabled(true);
		shareMenuLeaveItem.setEnabled(true);
		shareMenuSyncItem.setEnabled(true);

		inviteMenuItem.setEnabled(true);
		kickMenuItem.setEnabled(true);
		
		fileMenuNewItem.setEnabled(true);
		fileMenuOpenItem.setEnabled(true);
		fileMenuSaveItem.setEnabled(true);
		fileMenuSaveAsItem.setEnabled(true);
		fileMenuCloseItem.setEnabled(true);

		setChatEnabled(true);
		setDrawBtnEnabled(true);
	}
	
	private void setDrawBtnEnabled(boolean enabled) {
		triBtn.setEnabled(enabled);
		circleBtn.setEnabled(enabled);
		lineBtn.setEnabled(enabled);
		recBtn.setEnabled(enabled);
		textBtn.setEnabled(enabled);
		freeBtn.setEnabled(enabled);
		colourBtn.setEnabled(enabled);
		bigBtn.setEnabled(enabled);
		lilBtn.setEnabled(enabled);
	}

	private void setChatEnabled(boolean enabled) {
		sendBtn.setEnabled(enabled);
		sendMsg.setEnabled(enabled);
	}


	/****************************************************************************************/

	/** INTERFACE FUNCTION */

	/** Reusable message prompts */

	/**
	 * Update message - invoked with any string
	 * @param message : String
	 */
	@Override
	public void promptUpdate(String message) {
		JOptionPane.showMessageDialog(mainPane, message, "Update", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Error message - invoked with any string
	 * @param message : String
	 * @param method : recallable prompt
	 */
	@Override
	public void promptError(String message, Callable method) {
		JOptionPane.showMessageDialog(mainPane, message, "Error", JOptionPane.ERROR_MESSAGE);
		if (method != null) {
			try {
				method.call();
			} catch (Exception e) {
				System.out.println("Call Failed");
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Login prompt
	 */
	public static class PromptLogin implements Callable {
		public Object call() {
			System.out.println("Login Clicked.");
			JTextField usernameField = new JTextField(10);
			JTextField secretField = new JTextField(10);
			JPanel newGroupPanel = new JPanel(new GridLayout(2, 1));
			newGroupPanel.add(new JLabel("Username: "));
			newGroupPanel.add(usernameField);
			newGroupPanel.add(Box.createVerticalStrut(20));
			newGroupPanel.add(new JLabel("Server secret: "));
			newGroupPanel.add(secretField);

			System.out.println("Login panel launched");
			int result = JOptionPane.showConfirmDialog(mainPane, newGroupPanel,
					"Login", JOptionPane.OK_CANCEL_OPTION);
			if (result == JOptionPane.OK_OPTION) {
				String username = usernameField.getText();
				String serverSecret = secretField.getText();
				System.out.println("Username: " + username);
				System.out.println("Server secret: " + serverSecret);
				client.login(username, serverSecret);
			} else {
				System.out.println("Login cancelled.");
			}
			return null;
		}
	}

	/**
	 * Invocation for group creation.
	 * Passable as callable for error on group creation.
	 */
	public static class PromptGroupCreation implements Callable {

		public Object call() {

			JTextField nameField = new JTextField(10);
			JTextField secretField = new JTextField(10);
			JPanel newGroupPanel = new JPanel(new GridLayout(2, 1));
			newGroupPanel.add(new JLabel ("Name of group: "));
			newGroupPanel.add(nameField);
			newGroupPanel.add(Box.createVerticalStrut(20));
			newGroupPanel.add(new JLabel("Secret for group: "));
			newGroupPanel.add(secretField);

			System.out.println("Create New Group Clicked");
			int result = JOptionPane.showConfirmDialog(mainPane, newGroupPanel,
					"New Group Details", JOptionPane.OK_CANCEL_OPTION);
			if (result == JOptionPane.OK_OPTION) {
				String groupName = nameField.getText();
				String groupSecret = secretField.getText();
				System.out.println("Group name: " + groupName);
				System.out.println("Secret: " + groupSecret);
				client.startNewGroup(groupName, groupSecret);
			} else {
				System.out.println("New group creation cancelled.");
			}
			return null;
		}

	}

	/**
	 * Join group prompt
	 */
	public static class PromptGroupJoin implements Callable {
		public Object call() {
			// Fetch all group info
			client.searchGroup(null);
			return null;
		}
	}

	/**
	 * Callback from client, after using join function
	 * @param entries : List
	 */
	@Override
	public void promptSearchGroupInfo(ArrayList<GroupEntry> entries) {
		groupSearchCache = entries;
		GroupInfoTableModel model = new GroupInfoTableModel(client, entries);
		JTable table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getTableHeader().setReorderingAllowed(false);

		JScrollPane newGroupPanel = new JScrollPane(table);
		// newGroupPanel.add(table, BorderLayout.CENTER);
		System.out.println("Search Group Panel");
		
		int result = JOptionPane.showConfirmDialog(mainPane, newGroupPanel,
				"Join Group", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			int selection = table.getSelectedRow();
			if(selection == -1) {
				System.out.println("No Group Selected.");
			}
			else {
				GroupEntry group = model.getSelectedGroup(selection);
				System.out.println("Join group" + group.groupName);
				String pwd = JOptionPane.showInputDialog(mainPane, "Enter password:");
				client.joinGroup(group.groupName, pwd, group.groupUUID);
			}
		} else {
			System.out.println("Join Group cancelled.");
		}
	}

	/**
	 * Prompt approve user join group
	 * @param user : User
	 * @return : boolean
	 */
	@Override
	public boolean promptApproveJoin(User user) {
		int res = JOptionPane.showConfirmDialog(mainPane, "Approve user " + user.name + "@"+user.ip + " joining?", "Join Request",
				JOptionPane.YES_NO_OPTION);
		return JOptionPane.YES_OPTION == res;
	}

	/**
	 * Prompt for logging out
	 * @param message : String
	 */
	@Override
	public void processLogout(String message) {
		this.setState(ClientState.START_UP);
		promptUpdate(message);
		whiteBoard.repaint();
		resetDisplay();
	}

	/**
	 * Prompt for being kicked
	 * @param message : String
	 */
	@Override
	public void processGetKicked(String message) {
		this.setState(ClientState.NOT_IN_GROUP);
		JOptionPane.showMessageDialog(mainPane, "You are being kicked!");
		resetDisplay();
	}

	/**
	 * Prompt for group closure
	 * @param message
	 */
	@Override
	public void processGroupClosed(String message) {
		this.setState(ClientState.NOT_IN_GROUP);
		JOptionPane.showMessageDialog(mainPane,  message);
		resetDisplay();
	}
	
	/**
	 * Callback from client, after using invite function
	 * @param users : List
	 */
	@Override
	public void promptFetchLoginUserInfo(ArrayList<User> users) {
		UserInfoTableModel model = new UserInfoTableModel(users);
		JTable table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getTableHeader().setReorderingAllowed(false);

		JScrollPane newUserPanel = new JScrollPane(table);
		// newGroupPanel.add(table, BorderLayout.CENTER);
		System.out.println("Online User Panel");
		
		int result = JOptionPane.showConfirmDialog(mainPane, newUserPanel,
				"Invite User", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			int selection = table.getSelectedRow();
			if(selection == -1) {
				System.out.println("No User Selected.");
			}
			else {
				User user = model.getSelectedUser(selection);
				System.out.println("Invite user " + user.name);
				client.invite(user);
				JOptionPane.showMessageDialog(mainPane, "Invitation sent!");
			}
		} else {
			System.out.println("Join Group cancelled.");
		}
	}

	/**
	 * Group Invitation prompt
	 * @param inviter : User
	 * @param group : GroupEntry
	 */
	@Override
	public void promptGroupInvitation(User inviter, GroupEntry group) {
		StringBuilder builder = new StringBuilder();
		builder.append("Group invitation from ");
		builder.append(inviter.name);
		builder.append("@");
		builder.append(inviter.ip);
		builder.append("\n");
		builder.append("Join?");
		int outcome = JOptionPane.showConfirmDialog(mainPane, builder.toString(), "Group Invitation", JOptionPane.YES_NO_OPTION);
		if (outcome == JOptionPane.YES_OPTION) {
			client.joinGroup(group.groupName, group.groupSecret, group.groupUUID);
			System.out.println("Choose to join group!");
		}
	}

	/**
	 * Prompt save
	 * @return boolean
	 */
	@Override
	public boolean promptSavePane() {
		int selection = JOptionPane.showConfirmDialog(mainPane, "Save Canvas?", "save", JOptionPane.YES_OPTION);
		return selection == JOptionPane.YES_OPTION;
	}

	/**
	 * Prompt choose save
	 * @return File
	 */
	@Override
	public File promptChooseSaveFile() {
		FileFilter fileFilter = new FileNameExtensionFilter("Choose file name, (json) ", "json");
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(fileFilter);
		fileChooser.setCurrentDirectory(baseDir);
		int ret = fileChooser.showSaveDialog(mainPane);
		if (ret == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			String filepath = file.getAbsolutePath();
			if (!filepath.endsWith(".json")) {
				return new File(filepath + ".json");
			}
			return fileChooser.getSelectedFile();
		}
		return null;
	}

	/**
	 * Prompt Save As
	 * @return File
	 */
	@Override
	public File promptChooseSaveAs() {
		FileFilter fileFilter = new FileNameExtensionFilter("Enter file name (JPEG, BMP, GIF, TIFF, WBMP", "jpg", "jpeg", "bmp", "gif", "png", "tiff");
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(fileFilter);
		fileChooser.setCurrentDirectory(baseDir);
		int ret = fileChooser.showSaveDialog(mainPane);
		if (ret == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile();
		}
		return null;
	}

	/**
	 * Prompt open file
	 * @return File
	 */
	@Override
	public File promptChooseOpenFile() {
		FileFilter fileFilter = new FileNameExtensionFilter("Choose saved file, (json): ", "json");
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(fileFilter);
		fileChooser.setCurrentDirectory(baseDir);
		int ret = fileChooser.showOpenDialog(mainPane);
		if (ret == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile();
		}
		return null;
	}

	/**
	 * Prompt on closure
	 * @return success
	 */
	@Override
	public int promptGoodbye() {
		JOptionPane.showMessageDialog(mainPane, "Goodbye! Thanks for sharing!");
		int res = 1;
		return res;
	}



	/****************************************************************************************/

	/** Getters **/

	public WhiteBoard getWhiteBoard() {return whiteBoard;}
	public ClientState getGuiState() {return guiState;}
	public ChatBox getChatBox() {return chatBox;}
	public UserInfoTableModel getUserInfoTable() {return userInfoTable;}

}

/****************************************************************************************/
