package srdwb.server;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.*;

import srdwb.group.GroupMgr;

/****************************************************************************************/

/**
 * Managers sessions
 * Mainly creates new session from incoming connections
 */
public class SessionMgr{

	protected ConcurrentLinkedQueue<Session> sessions;
	protected GroupMgr groupMgr;
	protected String serverSecret;
	
	public SessionMgr(GroupMgr groupMgr, String serverSecret) {
		this.sessions = new ConcurrentLinkedQueue<Session>();
		this.serverSecret = serverSecret;
		this.groupMgr = groupMgr; 
	}

	/**
	 * Shut down manager
	 */
	public void shutdown() {
		for (Session s: sessions) {
			s.interrupt();
			try {
				s.join();
			} catch (InterruptedException e) {
				System.out.println("Interrupted while closing session" + s.toString());
			}
			System.out.println("Session " + s.toString() + " shutdown.");
		}
	}

	/**
	 * Create new session given incoming connection
	 * @param socket
	 */
	public void createSession(Socket socket) {
		try {
			Session session = new Session(socket, groupMgr, serverSecret);
			sessions.add(session);
			session.start();
		} catch (IOException e) {
			System.err.println("Session creation failed: " + e.getMessage());
		}
	}
}

/****************************************************************************************/
