package srdwb;

import javax.swing.SwingUtilities;

import srdwb.client.Client;
import srdwb.clientGUI.ClientGUI;
import srdwb.client.ClientCmdConfig;
import srdwb.utility.CmdConfig;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientDriver {
	public static void main(String[] args) throws NoSuchFieldException, SecurityException, UnknownHostException {	
		CmdConfig cmd = (CmdConfig)(new ClientCmdConfig(args));
		
		InetAddress address = InetAddress.getByName((String)cmd.getValue("address")); // default 127.0.0.1
		int port = ((Number)cmd.getValue("port")).intValue();
		
		InetAddress serverAddress = InetAddress.getByName((String)cmd.getValue("server-address"));	// default 127.0.0.1
		int serverPort = ((Number)cmd.getValue("server-port")).intValue();
		String serverSecret = (String)cmd.getValue("server-secret");
		
		int timeout = ((Number)cmd.getValue("timeout")).intValue();
		File dir = new File((String)cmd.getValue("directory"));
		
		System.out.println("using internet address ["+address.getHostName()+":"+port+"]");
		System.out.println("using basedir ["+dir.getAbsolutePath()+"] - all filenames will be relative to this basedir");
		System.out.println("using sharer secret ["+serverSecret+"]");
		System.out.println("socket timeout ["+timeout+"]");

		
		try {
			// TODO: Maybe deal with this exception in client?
			ClientGUI cGui = new ClientGUI(serverAddress, serverPort, serverSecret, dir);

			Client client = new Client(port, timeout, serverAddress, serverPort, serverSecret, cGui);
			client.start();
			cGui.addClient(client);

			SwingUtilities.invokeLater(() -> cGui.setup());

			// Add?
			//client.shutdown();
		} catch (IOException e) {
			System.out.println("Client thrown IO exception. " + e.getMessage());
		}
	}
}
