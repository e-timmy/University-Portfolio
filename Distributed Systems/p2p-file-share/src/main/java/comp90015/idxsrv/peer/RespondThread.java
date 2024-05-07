package comp90015.idxsrv.peer;

import comp90015.idxsrv.filemgr.BlockUnavailableException;
import comp90015.idxsrv.filemgr.FileDescr;
import comp90015.idxsrv.filemgr.FileMgr;
import comp90015.idxsrv.message.*;
import comp90015.idxsrv.textgui.ISharerGUI;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.*;

public class RespondThread extends Thread{
    private ISharerGUI tgui;
    private Socket socket;
    private ConcurrentMap<String, ShareRecord> sharedFileRecords;
    public RespondThread(Socket socket, ISharerGUI tgui, ConcurrentMap<String, ShareRecord> sharedFileRecords){
        this.tgui = tgui;
        this.socket = socket;
        this.sharedFileRecords = sharedFileRecords;
    }


    @Override
    public void run(){
        try {
            tgui.logInfo("respond thread running");
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(socket.getOutputStream())));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(socket.getInputStream())));


            LinkedBlockingQueue<BlockRequest> blockRequests = new LinkedBlockingQueue<>();
            BlockRespondWorker blockRespondWorker = new BlockRespondWorker(blockRequests, bufferedWriter);
            blockRespondWorker.start();

            Message msg;
            msg = readMessage(bufferedReader);
            Message nextMessage = msg;
            while (nextMessage.getClass().getName() != Goodbye.class.getName()){
                msg = nextMessage;
                nextMessage = readMessage(bufferedReader);
                if(processError(msg)){
                    ;
                }
                else if (msg.getClass().getName() == BlockRequest.class.getName()){
                    blockRequests.offer((BlockRequest) msg);
                } else{
                    tgui.logWarn("unknown message + " + msg.toString());
                    throw new JsonSerializationException("");
                }
            }
            blockRespondWorker.interrupt();
            blockRespondWorker.join();
            socket.close();
        }
        catch(JsonSerializationException e){
            tgui.logWarn("invalid message.");
        }
        catch(IOException e){
            tgui.logWarn("respond thread io exception.");
            e.printStackTrace();
        }
        catch(InterruptedException e){
            tgui.logWarn("Respond thread interrupted.");
        }
    }


    class BlockRespondWorker extends Thread{
        private FileDescr fileDescr;
        private LinkedBlockingQueue<BlockRequest> blockRequests;
        private FileMgr fileMgr;
        private BufferedWriter bufferedWriter;
        public BlockRespondWorker(LinkedBlockingQueue<BlockRequest> blockRequests, BufferedWriter bufferedWriter){
            this.blockRequests = blockRequests;
            this.bufferedWriter = bufferedWriter;
        }

        private boolean initFileMgr(BlockRequest blockRequest){
            tgui.logInfo("Processing block");
            tgui.logInfo("peer requested " + blockRequest.filename);

            if (fileMgr == null && fileDescr == null) {
                for (Map.Entry<String, ShareRecord> entry : sharedFileRecords.entrySet()) {
                    if (entry.getKey().equals(blockRequest.filename)) {
                        fileMgr = entry.getValue().fileMgr;
                        fileDescr = fileMgr.getFileDescr();
                        break;
                    }
                }
            }

            if(fileMgr == null || fileDescr == null){
                writeMessage(bufferedWriter, new ErrorMsg("File not found"));
                tgui.logWarn("File not found");
                interrupt();
                return false;
            }

            // Tests
            String blockHash = fileDescr.getBlockMd5(blockRequest.blockIdx);
            tgui.logInfo("Block File name: " + blockRequest.filename);
            tgui.logInfo("FileDescr md5: " + fileDescr.getFileMd5());
            tgui.logInfo("Block md5: " + blockHash);

            return true;
        }

        private void processBlockRequest(BlockRequest blockRequest){
            try{
                if(fileMgr == null && !initFileMgr(blockRequest)){
                    writeMessage(bufferedWriter, new ErrorMsg("File not found"));
                    tgui.logWarn("File not found");
                    interrupt();
                    return;
                }

                if (fileDescr.getFileMd5().equals(blockRequest.fileMd5)) {
                    byte[] blockBytes = fileMgr.readBlock(blockRequest.blockIdx);
                    String blockString = Base64.getEncoder().encodeToString(blockBytes);
                    writeMessage(bufferedWriter, new BlockReply(blockRequest.filename, fileDescr.getFileMd5(),
                            blockRequest.blockIdx, blockString));
                    blockBytes = null;
                } else {
                    writeMessage(bufferedWriter, new ErrorMsg("File not available"));
                }
            }
            catch(BlockUnavailableException e){
                tgui.logInfo("Block unavailable: " + blockRequest.blockIdx);
            }
            catch(IOException e){
                tgui.logWarn("RespondThread working thread io exception");
            }
        }
        public void run(){
            // Writing block
            //long time = System.nanoTime();
            while(!isInterrupted()) {
                try {
                    BlockRequest blockRequest = blockRequests.take();
                    processBlockRequest(blockRequest);
                    blockRequest = null;
                }
                catch(InterruptedException e){
                    tgui.logInfo("Respond working thread is disrupted");
                }
            }
            for(BlockRequest blockRequest : blockRequests){
                processBlockRequest(blockRequest);
            }
            try{
                if(fileMgr != null){
                    fileMgr.closeFile();
                }
            }
            catch(Exception e){
                tgui.logWarn("Closing file io exception");
            }
        }
    }


    // imitate aaron's code at Server
    private Message readMessage(BufferedReader reader) throws IOException,
            JsonSerializationException {

        String jsonStr = reader.readLine();

        if (jsonStr != null){
            Message msg = (Message) MessageFactory.deserialize(jsonStr);
            //tgui.logDebug("Respond thread recieves " + msg.toString());
            return msg;
        }
        else{
            throw new IOException("THROWING IO");
        }
    }

    // imitate aaron's code at Server
    private void writeMessage(BufferedWriter writer, Message msg){
        try {
            //tgui.logDebug("Respond thread sends " + msg.toString());
            writer.write(msg.toString());
            writer.newLine();
            writer.flush();
        }
        catch(IOException e){
            tgui.logWarn("RespondThread: writeMessage io exception");
        }
    }

    // process error message if the message is an error message
    // return true if error message
    private boolean processError(Message msg){
        if(msg.getClass().getName() == ErrorMsg.class.getName()){
            tgui.logWarn("Respond thread error message: " + ((ErrorMsg)msg).msg);
            return true;
        }
        return false;
    }

}
