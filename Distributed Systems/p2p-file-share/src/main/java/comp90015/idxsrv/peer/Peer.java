package comp90015.idxsrv.peer;


import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;

import comp90015.idxsrv.filemgr.FileDescr;
import comp90015.idxsrv.filemgr.FileMgr;
import comp90015.idxsrv.message.*;
import comp90015.idxsrv.server.IOThread;
import comp90015.idxsrv.server.IndexElement;
import comp90015.idxsrv.textgui.ISharerGUI;

import javax.naming.AuthenticationNotSupportedException;

import java.nio.file.Paths;

/**
 * Skeleton Peer class to be completed for Project 1.
 * @author aaron
 *
 */
public class Peer implements IPeer {

	private IOThread ioThread;
	
	private LinkedBlockingDeque<Socket> incomingConnections;
	
	private ISharerGUI tgui;
	
	private String basedir;
	
	private int timeout;
	
	private int port;
	private RequestDispatcherThread requestDispatcherThread;

	// key: relative path, value: shareRecord
	private ConcurrentMap<String, ShareRecord> sharedFileRecords;
	
	public Peer(int port, String basedir, int socketTimeout, ISharerGUI tgui) throws IOException {
		this.tgui=tgui;
		this.port=port;
		this.timeout=socketTimeout;
		this.basedir=new File(basedir).getCanonicalPath();
		this.sharedFileRecords = new ConcurrentHashMap<String, ShareRecord>();
		incomingConnections=new LinkedBlockingDeque<Socket>();
		ioThread = new IOThread(port,incomingConnections,socketTimeout,tgui);
		ioThread.start();
		// Peer incoming connections dispatcher
		requestDispatcherThread = new RequestDispatcherThread(incomingConnections, tgui, sharedFileRecords);
		requestDispatcherThread.start();
	}
	
	public void shutdown() throws InterruptedException, IOException {
		ioThread.shutdown();
		ioThread.interrupt();
		ioThread.join();

		requestDispatcherThread.shutdown();
		requestDispatcherThread.interrupt();
		requestDispatcherThread.join();
	}
	
	/*
	 * Students are to implement the interface below.
	 */

	/** TODO: Things to consider:
	 *  	When to drop search records? When attempt download? When drop sharing as well?
	 *      Failing to share non text files.
	 *      Failing to download more than one block.
	 *      Concurrency of download.
	 */

	@Override
	public void shareFileWithIdxServer(File file, InetAddress idxAddress, int idxPort, String idxSecret,
									   String shareSecret) {
		new ShareWithIdxServerThread(file, idxAddress, idxPort, idxSecret, shareSecret, sharedFileRecords).start();
	}

	class ShareWithIdxServerThread extends Thread{
		private File file;
		private InetAddress idxAddress;
		private int idxPort;
		private String idxSecret;
		private String shareSecret;
		private ConcurrentMap<String, ShareRecord> sharedFileRecords;

		public ShareWithIdxServerThread(File file, InetAddress idxAddress, int idxPort, String idxSecret,
										String shareSecret, ConcurrentMap<String, ShareRecord> sharedFileRecords){
			this.file = file;
			this.idxAddress = idxAddress;
			this.idxPort = idxPort;
			this.idxSecret = idxSecret;
			this.shareSecret = shareSecret;
			this.sharedFileRecords = sharedFileRecords;
		}

		@Override
		public void run(){
			// Connection
			Socket socket = null;
			ShareRequest shareRequest;
			Message reply;
			ShareRecord shareRecord;
			ShareReply shareReply;
			String relativePathname;

			tgui.logInfo("Sharing...");

			try {

				// File details
				// TODO: Initalise details in object creation?
				//relativePathname = getRelativePath(file);
				relativePathname = file.getPath().replace(basedir + "/", "");
				RandomAccessFile ranAccFile = new RandomAccessFile(file, "r");
				tgui.logInfo("Reading file...");
				FileDescr fileDescr = new FileDescr(ranAccFile);
				// TODO: changed from canonical to relative
				FileMgr fileMgr = new FileMgr(relativePathname, fileDescr);
				tgui.logInfo("Reading file completed");

				// Connect and intialise I/O
				tgui.logDebug("Connecting to server");
				socket = new Socket(idxAddress, idxPort);
				socket.setSoTimeout(timeout);
				BufferedReader bufferedReader = getBufferedReader(socket);
				BufferedWriter bufferedWriter = getBufferedWriter(socket);


				// Begin session
				if (!initRequest(socket, bufferedReader, bufferedWriter, idxSecret)) {
					throw new ConnectionException("Session Handshake Failure.");
				}

				// Generate request, send, receive reply
				tgui.logInfo("Generating share request.");
				shareRequest = new ShareRequest(fileDescr, relativePathname, shareSecret, port);
				writeMessage(bufferedWriter, shareRequest);
				reply = readMessage(bufferedReader);

				if (processError(reply)) {
					throw new ConnectionException("Error Message Received.");
					// TODO: close connection?
				}
				if (!(reply.getClass().getName() == ShareReply.class.getName())) {
					throw new JsonSerializationException("Unknown Server Reply.");
				}

				// Unpack message and create record
				shareReply = (ShareReply) reply;
				shareRecord = new ShareRecord(fileMgr, shareReply.numSharers, "Sharing",
						idxAddress, idxPort, idxSecret, shareSecret);
				tgui.addShareRecord(relativePathname, shareRecord);
				tgui.logInfo("Now sharing new file: " + relativePathname);
				sharedFileRecords.put(relativePathname, shareRecord);

			} catch (ConnectionException e) {
				tgui.logWarn("Connection error.");
			} catch (NullPointerException e) {
				tgui.logWarn("Couldn't find file.");
			} catch (FileNotFoundException e) {
				tgui.logWarn("RandomAccessFile Creation Failed.");
			} catch (JsonSerializationException e) {
				tgui.logWarn("Invalid Server Reply.");
			} catch (IOException e) {
				tgui.logWarn("Client io exception.");
			} catch(NoSuchAlgorithmException e){
				tgui.logWarn("No algorithm implemented for Md5");
			} finally {
				if(socket!=null) try {
					socket.close();
					tgui.logInfo("Closing socket connection.");
				} catch (IOException e) {
					tgui.logWarn("close: "+e.getMessage());
				}
			}
		}
	}


	@Override
	public void searchIdxServer(String[] keywords, int maxhits, InetAddress idxAddress, int idxPort, String idxSecret) {
		tgui.logInfo("Searching...");
		new SearchIdxServerThread(keywords, maxhits, idxAddress, idxPort, idxSecret).start();
	}

	class SearchIdxServerThread extends Thread{
		private String[] keywords;
		private int maxhits;
		private InetAddress idxAddress;
		private int idxPort;
		private String idxSecret;

		public SearchIdxServerThread(String[] keywords, int maxhits, InetAddress idxAddress, int idxPort, String idxSecret){
			this.keywords = keywords;
			this.maxhits = maxhits;
			this.idxAddress = idxAddress;
			this.idxPort = idxPort;
			this.idxSecret = idxSecret;
		}

		@Override
		public void run(){

			// Communication
			Socket socket = null;
			SearchRequest searchRequest;
			Message reply;
			SearchReply searchReply;
			SearchRecord searchRecord;

			// Search Record Details
			String filename;
			int seedCounts;
			FileDescr fileDescr;
			String fileSecret;

			// TODO: remove?
			tgui.clearSearchHits();

			try {
				// Connect and initialise I/O
				tgui.logInfo("Connecting to server.");
				socket = new Socket(idxAddress, idxPort);
				socket.setSoTimeout(timeout);
				BufferedReader bufferedReader = getBufferedReader(socket);
				BufferedWriter bufferedWriter = getBufferedWriter(socket);

				// Begin session
				if (!initRequest(socket, bufferedReader, bufferedWriter, idxSecret)) {
					throw new ConnectionException("Session Handshake Failure.");
				}

				// Create, send request, receive reply.
				tgui.logInfo("Generating search request.");
				searchRequest = new SearchRequest(maxhits, keywords);
				writeMessage(bufferedWriter, searchRequest);
				reply = readMessage(bufferedReader);

				// Check reply correctness
				if (processError(reply)) {
					throw new ConnectionException("Error Message Received.");
				} else if (!(reply.getClass().getName() == SearchReply.class.getName())) {
					throw new JsonSerializationException("Unknown Server Reply.");
				}

				// Unpack, gather hits, add records
				searchReply = (SearchReply) reply;
				for (int i=0; i < searchReply.hits.length; i++) {
					// Record details
					seedCounts = searchReply.seedCounts[i];
					fileDescr = searchReply.hits[i].fileDescr;
					filename = searchReply.hits[i].filename;
					fileSecret = searchReply.hits[i].secret;

					// Create record
					searchRecord = new SearchRecord(fileDescr, seedCounts, idxAddress, idxPort,
							idxSecret, fileSecret);
					tgui.addSearchHit(filename, searchRecord);
					tgui.logInfo("New search hit: " + filename);
				}
				tgui.logInfo("Finished searching index server.");

			} catch (ConnectionException e) {
				tgui.logWarn("Connection failure.");
			} catch (NullPointerException e) {
				tgui.logWarn("Couldn't find file.");
			} catch (JsonSerializationException e) {
				tgui.logWarn("Invalid Server Reply.");
			} catch (IOException e) {
				tgui.logWarn("Client io exception.");
			} finally {
				if(socket!=null) try {
					socket.close();
					tgui.logInfo("Closing socket connection.");
				} catch (IOException e) {
					tgui.logWarn("close: "+e.getMessage());
				}
			}
		}
	}

	@Override
	public boolean dropShareWithIdxServer(String relativePathname, ShareRecord shareRecord) {

		try{
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(shareRecord.idxSrvAddress, shareRecord.idxSrvPort), timeout);
			tgui.logInfo("Connecting to server");

			BufferedReader bufferedReader = getBufferedReader(socket);
			BufferedWriter bufferedWriter = getBufferedWriter(socket);

			// Begin session
			if (!initRequest(socket, bufferedReader, bufferedWriter, shareRecord.idxSrvSecret)) {
				throw new ConnectionException("Session Handshake Failure.");
			}

			// send drop request
			FileDescr fileDescr = shareRecord.fileMgr.getFileDescr();
			DropShareRequest dropShareRequest = new DropShareRequest(relativePathname,
					fileDescr.getFileMd5(), shareRecord.sharerSecret, port);
			writeMessage(bufferedWriter, dropShareRequest);
			// server reply
			Message reply;
			reply = readMessage(bufferedReader);

			// Check reply correctness
			if (processError(reply)) {
				throw new ConnectionException("Error Message Received.");
			} else if (!(reply.getClass().getName() == DropShareReply.class.getName())) {
				throw new JsonSerializationException("Unknown Server Reply.");
			}

			// process server reply
			tgui.logInfo("Dropped " + relativePathname +  " from sharing.");
			DropShareReply dropShareReply = (DropShareReply) reply;

			// delete local record
			this.sharedFileRecords.remove(relativePathname);
			return dropShareReply.success;

		} catch (ConnectionException e) {
			tgui.logWarn("Connection failure.");
		} catch(JsonSerializationException e){
			tgui.logWarn("Invalid message.");
		} catch(IOException e){
			tgui.logWarn("Client IO exception.");
		}
		return false;
	}

	@Override
	public void downloadFromPeers(String relativePathname, SearchRecord searchRecord) {
		new DownloadFromPeerThread(relativePathname, searchRecord).start();
	}

	class DownloadFromPeerThread extends Thread{
		public String relativePathname;
		public SearchRecord searchRecord;
		public DownloadFromPeerThread(String relativePathname, SearchRecord searchRecord){
			this.relativePathname = relativePathname;
			this.searchRecord = searchRecord;
		}

		@Override
		public void run(){

			try {
				ExecutorService executorService = new ThreadPoolExecutor(2, 2, 20, TimeUnit.SECONDS,
						new LinkedBlockingDeque<>());
				tgui.logInfo("Connecting to server");
				Socket socket = new Socket();
				socket.connect(new InetSocketAddress(searchRecord.idxSrvAddress, searchRecord.idxSrvPort), timeout);
				BufferedReader bufferedReader = getBufferedReader(socket);
				BufferedWriter bufferedWriter = getBufferedWriter(socket);

				if (!initRequest(socket, bufferedReader, bufferedWriter, searchRecord.idxSrvSecret)) {
					throw new ConnectionException("Session Handshake Failed.");
				}

				// Generate download request
				LookupRequest lookupRequest = new LookupRequest(relativePathname, searchRecord.fileDescr.getFileMd5());
				writeMessage(bufferedWriter, lookupRequest);
				Message reply = readMessage(bufferedReader);

				// Check reply correctness
				if (processError(reply)) {
					throw new ConnectionException("Error Message Received.");
				} else if (!(reply.getClass().getName() == LookupReply.class.getName())) {
					throw new JsonSerializationException("Unknown Server Reply.");
				}

				// Unpack reply and check hits
				socket.close();
				LookupReply lookupReply = (LookupReply) reply;
				if (lookupReply.hits.length == 0) {
					// Clear search records when no hit.
					tgui.clearSearchHits();
					throw new NoPeersException();
				}

				// Gather file details
				FileDescr fileDescr = searchRecord.fileDescr;
				tgui.logInfo("Received lookup: " + lookupReply.toString());
				String filename = (Paths.get(relativePathname)).getFileName().toString();
				tgui.logInfo("Filename: " + filename);
				String savePath = basedir + "/" + filename;
				tgui.logInfo("Saving to: " + savePath);
				FileMgr fileMgr = new FileMgr(filename, searchRecord.fileDescr);

				// Download from peers
				for (IndexElement element : lookupReply.hits) {

					if (fileMgr.isComplete()) {
						break;
					}
					try {
						LinkedBlockingQueue<Block> blocksToWrite = new LinkedBlockingQueue<>();
						BlockWritingThread blockWriting = new BlockWritingThread(fileMgr, blocksToWrite);
						blockWriting.start();

						// Connecting to peer
						tgui.logInfo("Trying peer " + element.ip + ":" + element.port);
						Socket peerSocket = new Socket();
						peerSocket.connect(new InetSocketAddress(element.ip, element.port));
						BufferedReader reader = getBufferedReader(peerSocket);
						BufferedWriter writer = getBufferedWriter(peerSocket);

						// Requesting blocks sequentially
						for (int i = 0; i < fileDescr.getNumBlocks(); i++) {
							if (!fileMgr.isBlockAvailable(i)) {
								writeMessage(writer, new BlockRequest(relativePathname, fileDescr.getFileMd5(), i));
							}
						}
						writeMessage(writer, new Goodbye());

						// Writing blocks sequentially
						for (int i = 0; i < fileDescr.getNumBlocks(); i++) {
							if (!fileMgr.isBlockAvailable(i)) {
								reply = readMessage(reader);
								executorService.execute(new MessageProcessingThread(i, reply, blocksToWrite));

							}
						}

						// Waiting for writing all blocks
						executorService.shutdown();
						while (!executorService.isTerminated()) {
						}

						blockWriting.interrupt();
						blockWriting.join();

						peerSocket.close();
					} catch (IOException e) {
						tgui.logInfo("cannot reach " + element.ip + ":" + element.port);
					}
				}

				// Check file download complete and close
				// TODO: Forge while loop to not exit until complete.
				// TODO: Unpack while loop with failure timeout.
				if (!fileMgr.isComplete()) {
					tgui.logWarn("File transfer error: file not complete");
				} else {
					tgui.logInfo("File transfer complete.");
				}
				fileMgr.closeFile();

			} catch (ConnectionException e) {
				tgui.logWarn("Connection failed.");
			} catch(NoSuchAlgorithmException e){
				tgui.logWarn("No algorithm implemented for Md5");
			} catch(JsonSerializationException e){
				tgui.logWarn("invalid message");
			} catch(IOException e){
				tgui.logWarn("client io exception");
			} catch (NoPeersException e) {
				tgui.logInfo("No peers actively sharing file. Please search and try again.");
			}
			catch(InterruptedException e){
				tgui.logInfo("Peer download being interrupted.");
			}
		}
	}

	class MessageProcessingThread extends Thread {
		private int blockid;
		private Message msg;
		private LinkedBlockingQueue<Block> blocksToWrite;

		public MessageProcessingThread(int blockId, Message msg, LinkedBlockingQueue<Block> blocksToWrite){
			this.blockid = blockId;
			this.msg = msg;
			this.blocksToWrite = blocksToWrite;
		}

		@Override
		public void run() {
			try {
				// Don't read messages inside a task...
				if (processError(msg)) {
					;
				} else if (msg.getClass().getName() == BlockReply.class.getName()) {

					// Decode bytes
					BlockReply blockReply = (BlockReply) msg;
					byte[] blockBytes = Base64.getDecoder().decode(blockReply.bytes);
					blocksToWrite.offer(new Block(blockid, blockBytes));
					blockBytes = null;
				} else {
					tgui.logWarn("Unknown message");
					throw new JsonSerializationException("");
				}
			}
//												catch(IOException e){
//													tgui.logWarn("Downloading block " + blockid + " io exception ");
//												}
			catch (JsonSerializationException e) {
				tgui.logWarn("Downloading block " + blockid + " invalid message");
			}
		}

	}

	class Block{
		public int blockid;
		public byte[] blockBytes;

		public Block(int blockid, byte[] blockBytes){
			this.blockid = blockid;
			this.blockBytes = blockBytes;
		}
	}

	class BlockWritingThread extends Thread{
		private FileMgr fileMgr;
		private LinkedBlockingQueue<Block> blocksToWrite;
		public BlockWritingThread(FileMgr fileMgr, LinkedBlockingQueue<Block> blocksToWrite){
			this.fileMgr = fileMgr;
			this.blocksToWrite = blocksToWrite;
		}

		private void writeToFile(Block block) throws IOException{
			// byte[] blockBytes = Base64.getDecoder().decode(blockReply.bytes);
			byte[] blockBytes = block.blockBytes;
			int blockid = block.blockid;
//			if (fileMgr.checkBlockHash(blockid, blockBytes)) {
//				tgui.logDebug("Block Hash Success.");
//			}

			// Write Block
			if (fileMgr.writeBlock(blockid, blockBytes)) {
				// tgui.logInfo("Writing block: " + blockBytes.toString());
				tgui.logInfo("Writing Block " + blockid + "Successful.");
			} else {
				tgui.logInfo("Write Fail");
			}
			blockBytes = null;
		}

		@Override
		public void run(){

			while(!isInterrupted()){
				try{
					Block block = blocksToWrite.take();
					writeToFile(block);
				}catch(InterruptedException e){
					break;
				}
				catch(IOException e){
					tgui.logWarn("Peer writing file io exception");
				}
			}

			for (Block block: blocksToWrite){
				try {
					writeToFile(block);
				}
				catch(IOException e){
					tgui.logWarn("Peer writing file finish io exception");
				}
			}
		}
	}

	public class ConnectionException extends Exception {

		public ConnectionException() {

		}

		public ConnectionException(String message) {
			tgui.logWarn(message);
		}

	}

	public class NoPeersException extends Exception {
		public NoPeersException(String errorMessage) {
			super(errorMessage);
		}

		public NoPeersException() {
		}
	}

	// imitate aaron's code at Server
	private Message readMessage(BufferedReader reader) throws IOException, JsonSerializationException{
		String jsonStr = reader.readLine();
		if (jsonStr != null){
			Message msg = (Message)MessageFactory.deserialize(jsonStr);
			//tgui.logDebug("Client recieves " + msg.toString());
			return msg;
		} else{
			throw new IOException();
		}
	}

	// imitate aaron's code at Server
	private void writeMessage(BufferedWriter writer, Message msg) throws IOException {
		tgui.logDebug("Client sends " + msg.toString());
		writer.write(msg.toString());
		writer.newLine();
		writer.flush();
	}

	// initialize request to server
	private boolean initRequest(Socket socket, BufferedReader bufferedReader,
								BufferedWriter bufferedWriter, String idxSecret) {

		Message msg;

		try {
			// Attempt read message
			try {
				msg = readMessage(bufferedReader);
			} catch (JsonSerializationException e) {
				tgui.logWarn("Invalid message.");
				return false;
			}

			// Check if error message
			if (msg.getClass().getName() == ErrorMsg.class.getName()) {
				tgui.logWarn("Error in request: " + ((ErrorMsg) msg).msg);
				return false;
			}

			// Begin handshake
			// TODO: Make elif statement?
			if (msg.getClass().getName() == WelcomeMsg.class.getName()) {

				// Authenticate
				writeMessage(bufferedWriter, new AuthenticateRequest(idxSecret));

				// Fetch reply
				try {
					msg = readMessage(bufferedReader);
				} catch (JsonSerializationException e) {
					tgui.logWarn("Invalid message");
					return false;
				}
				// Check error
				if (msg.getClass().getName() == ErrorMsg.class.getName()) {
					tgui.logWarn("Error in authentication:  " + msg.toString());
					return false;
				}

				// Check success
				if (msg.getClass().getName() == AuthenticateReply.class.getName()) {
					AuthenticateReply ar = (AuthenticateReply) msg;
					if (!ar.success) {
						tgui.logWarn("authentication failed");
						return false;
					}
					else{
						return true;
					}
				}
			}
		} catch(IOException e) {
			tgui.logWarn("client recieves io exception");
		}

		return false;
	}

	private String getRelativePath(File file){
		return new File(basedir).toPath().relativize(file.toPath()).toString();
	}

	private BufferedReader getBufferedReader(Socket socket) throws IOException{
		BufferedInputStream bufferedInputStream = new BufferedInputStream(socket.getInputStream());
		return new BufferedReader(new InputStreamReader(bufferedInputStream));
	}

	private BufferedWriter getBufferedWriter(Socket socket) throws IOException{
		BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
		return new BufferedWriter(new OutputStreamWriter(bufferedOutputStream));
	}

	// process error message if the message is an error message
	// return true if error message
	private boolean processError(Message msg){
		if(msg.getClass().getName() == ErrorMsg.class.getName()){
			tgui.logWarn("error message: " + ((ErrorMsg)msg).msg);
			return true;
		}
		return false;
	}
	
}
