package srdwb.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.*;
import srdwb.message.*;

/****************************************************************************************/

/**
 * 
 * Maintaining a concurrent queue for each socket
 * both GroupMember and Session will write to this buffer
 */
public class SessionWriter extends Thread{
	private LinkedBlockingDeque<Message> buffer;
	private Session session;
	private BufferedWriter writer;
	public SessionWriter(Session session, BufferedWriter writer) {
		buffer = new LinkedBlockingDeque<>();
		this.session = session;
		this.writer = writer;
	}
	
	@Override
	public void run() {
		try {
			while(!interrupted()) {
				Message msg = buffer.take();
				writeMsg(msg);
			}
			ArrayList<Message> remaining = new ArrayList<>();
			buffer.drainTo(remaining);
			for(Message m: remaining) {
				writeMsg(m);
			}
		} catch(InterruptedException e) {
			System.out.println("Session: " + session.uuid + " writer interrupted");
		} catch (IOException e) {
			System.out.println("Session: " + session.uuid + " writer io Exception.");
			System.out.println("Unable to contact. Shutting down session.");
			session.processSessionFailure();

		}
	}
	
	public void writeToBuffer(Message msg) {
		buffer.offer(msg);
	}
	
	synchronized private void writeMsg(Message msg) throws IOException {
		System.out.println("sending: "+msg.toString());
		writer.write(msg.toString());
		writer.newLine();
		writer.flush();
	}
}

/****************************************************************************************/

