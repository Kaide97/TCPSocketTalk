package cn.kaispace.onlineTalk.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author KAI
 * @CreateTime 2018/11/1
 * @Describe
 **/
public class ServerConnection implements Runnable{
    private ServerSocket serverSocket;
    private CopyOnWriteArrayList<Socket> socketList;
    /**
     * 引入waitList的原因是该线程在监听的时候会对容器上锁，导致另一个Socket交互线程被阻塞
     * 因此引入一个暂时容器waitList，当获取到socket进入，socket先进入waitlist后再传入socketList
     * 达到线程同步且容器不被阻塞
     */
    private CopyOnWriteArrayList<Socket> waitList;
    private SimpleDateFormat df;
    private final static int LISTEN_PORT = 27728;
    ServerConnection(CopyOnWriteArrayList<Socket> slist) throws IOException {
        this.socketList = slist;
        serverSocket = new ServerSocket(LISTEN_PORT);
        waitList = new CopyOnWriteArrayList<>();
        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public void run() {
        System.out.println(df.format(new Date())+":Start Listening Socket Connect");
        listenSocketConnect();
    }

    private void listenSocketConnect(){
        synchronized (waitList){
            while (true){
                try {
                    waitList.add(serverSocket.accept());
                    System.out.println(df.format(new Date())+waitList.get(waitList.size()-1)+":Socket Add");
                    socketList.add(waitList.get(waitList.size()-1));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
