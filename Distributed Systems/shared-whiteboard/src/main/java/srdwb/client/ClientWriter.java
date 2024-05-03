package srdwb.client;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;

import srdwb.message.Message;

/**
 * Handles all message writing for Client
 */
public class ClientWriter extends Thread{
	Client client;
	private LinkedBlockingDeque<Message> buffer;
	private BufferedWriter writer;
	public ClientWriter(Client client, BufferedWriter writer) {
		this.client = client;
		buffer = new LinkedBlockingDeque<>();
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
			System.out.println("Client writer interrupted");
		} catch (IOException e) {
			System.out.println("Client writer io Exception");
			System.out.println("Unable to reach server");
			client.processSessionFailure();
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
