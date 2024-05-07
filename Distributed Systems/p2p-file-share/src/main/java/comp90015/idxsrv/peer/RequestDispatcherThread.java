package comp90015.idxsrv.peer;

import comp90015.idxsrv.message.*;
import comp90015.idxsrv.textgui.ISharerGUI;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RequestDispatcherThread extends Thread{
    private ThreadPoolExecutor threadPoolExecutor;
    private final int poolCoreSize = 5;
    private final int poolMaxSize = 5;
    private final long keepAliveTime = 10;
    private final TimeUnit timeUnit = TimeUnit.MINUTES;
    private LinkedBlockingDeque<Runnable> taskDequeue;
    private LinkedBlockingDeque<Socket> incomingConnections;
    private ISharerGUI tgui;
    private ConcurrentMap<String, ShareRecord> sharedFileRecords;
    public RequestDispatcherThread(LinkedBlockingDeque<Socket> incomingConnections, ISharerGUI tgui,
                                   ConcurrentMap<String, ShareRecord> sharedFileRecords){
        this.taskDequeue = new LinkedBlockingDeque<Runnable>();
        this.threadPoolExecutor = new ThreadPoolExecutor(poolCoreSize, poolMaxSize, keepAliveTime, timeUnit, taskDequeue);
        this.incomingConnections = incomingConnections;
        this.tgui = tgui;
        this.sharedFileRecords = sharedFileRecords;
    }

    @Override
    public void run(){
        tgui.logInfo("Request Dispatcher Running");
        while(!isInterrupted()) {
            try {
                Socket socket = incomingConnections.take();
                tgui.logInfo("connected by another peer");
                this.threadPoolExecutor.execute(new RespondThread(socket, tgui, sharedFileRecords));
            }
            catch (InterruptedException e){
                tgui.logWarn("dispatch thread get interrupted");
                break;
            }
        }
        tgui.logInfo("dispatch thread completed.");
    }

    public void shutdown(){
        threadPoolExecutor.shutdown();
    }
}
