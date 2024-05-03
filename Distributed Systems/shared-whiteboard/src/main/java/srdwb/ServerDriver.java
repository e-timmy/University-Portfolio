package srdwb;

import srdwb.utility.CmdConfig;
import srdwb.server.Server;
import srdwb.server.ServerCmlConfig;

import java.io.IOException;

public class ServerDriver {

	private static Server server;

	public static void main(String[] args) {
		CmdConfig cmd = (CmdConfig)new ServerCmlConfig(args);
		int port = ((Number)cmd.getValue("port")).intValue();
		int timeout = ((Number)cmd.getValue("timeout")).intValue();
		String serverSecret = (String)cmd.getValue("server-secret");
		
		System.out.println("port: "+port);
		System.out.println("timeout: " + timeout);
		System.out.println("server secret: " + serverSecret);

		try {
			server = new Server(port, timeout, serverSecret);
			server.start();
			try {
				server.join();
			} catch (InterruptedException e) {
				System.out.println("Couldn't join with server.");
			}
		} catch (IOException e) {
			System.out.println("Server IO exception...");
		}

	}
}
