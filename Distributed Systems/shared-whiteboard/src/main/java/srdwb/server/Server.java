package srdwb.server;

import srdwb.group.GroupMgr;

import java.io.*;
import java.net.*;
import java.util.concurrent.LinkedBlockingDeque;

/****************************************************************************************/

/**
 * Sets up classes to handle new connections, sessions, and groups
 */
public class Server extends Thread {
	private String serverSecret;
	private int timeout;
	private int port;
	private String welcome;
	private IOThread ioThread;
	private LinkedBlockingDeque<Socket> incomingConnections;
	private SessionMgr sessionMgr;
	private GroupMgr groupMgr;

	public Server(int port, int timeout, String serverSecret) throws IOException {
		this.welcome = "Welcome to Shared Whiteboard Service.";
		this.port = port;
		this.timeout = timeout;
		this.serverSecret = serverSecret;
		
		this.groupMgr = new GroupMgr();
		this.sessionMgr = new SessionMgr(this.groupMgr, this.serverSecret);
		
		incomingConnections = new LinkedBlockingDeque<Socket>();
		ioThread = new IOThread(port, incomingConnections, timeout);
		ioThread.start();

	}

	@Override
	public void run() {
		System.out.println("Server thread running");

		while(!isInterrupted()) {
			try {
				Socket socket = incomingConnections.take();
				System.out.println("New incoming connection");
				// create a session and handle requests from this socket
				sessionMgr.createSession(socket);
				
			} catch (InterruptedException e) {
				System.out.println("Server interrupted.");
				break;
			}
		}
		// Safe stop IO
		ioThread.interrupt();
		System.out.println("Waiting for IO thread to return");
		try {
			ioThread.join();
		} catch (InterruptedException e) {
			System.out.println("Interrupted whilst joining IO thread.");
		}
		System.out.println("Server thread completed");
	}
}

/****************************************************************************************/
