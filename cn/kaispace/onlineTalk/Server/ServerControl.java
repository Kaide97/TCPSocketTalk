package cn.kaispace.onlineTalk.Server;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * @Author KAI
 * @CreateTime 2018/11/1
 * @Describe
 **/
public class ServerControl {
    private CopyOnWriteArrayList<Socket> socketList;
    public ServerControl(int interactiveThreadCount) throws IOException {
        socketList = new CopyOnWriteArrayList<>();
        startServer(interactiveThreadCount);
    }

    private void startServer(int interactiveThreadCount) throws IOException {
        ThreadFactory namedThreadFactory = new ThreadBuilder();
        //监听Socket连接线程池
        ExecutorService reciveSocketThreadPool = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        reciveSocketThreadPool.execute(new ServerConnection(socketList));
        //Socket交互监听线程池
        ExecutorService interactiveThreadPool = new ThreadPoolExecutor(1, interactiveThreadCount,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        interactiveThreadPool.execute(new ServerInteractive(socketList));
    }

    class ThreadBuilder implements ThreadFactory{
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r);
        }
    }
}
