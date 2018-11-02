package cn.kaispace.onlineTalk.Server;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.concurrent.*;

/**
 * @Author KAI
 * @CreateTime 2018/11/1
 * @Describe
 **/
public class ServerInteractive implements Runnable{
    /**
     *@描述 监听已连接Socket的类
     *@创建人  KAI
     *@创建时间  2018/11/2
     *@修改人和其它信息
     */
    /**
     * socketList: 储存socket的容器
     */
    private CopyOnWriteArrayList<Socket> socketList;
    /**
     * clientInfoList: 储存包装对象类的容器
     */
    private CopyOnWriteArrayList<ClientInfo> clientInfoList;
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * curId: 当前最大uid,用于初始化赋uid
     */
    private long curId = 100001;
    /**
     * middleMan: 中间人,作为服务端发送的信使
     */
    private MiddleMan middleMan;
    /**
     * 线程池
     */
    private ThreadFactory namedThreadFactory;
    private ExecutorService executorService;
    ServerInteractive(CopyOnWriteArrayList<Socket> slist) throws IOException {
        this.socketList = slist;
        namedThreadFactory = new ThreadBuilder();
        executorService = new ThreadPoolExecutor(100, 100,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        clientInfoList = new CopyOnWriteArrayList<>();
        middleMan = new MiddleMan(clientInfoList);
    }

    private void checkSocketAliveRoutine() throws IOException, InterruptedException {
        /**
         *@描述 保证socket连接后被包装加入容器
         *@参数  []
         *@返回值  void
         *@创建人  KAI
         *@创建时间  2018/11/2
         *@修改人和其它信息
         */
        synchronized (socketList) {
            ClientInfo ci;
            for (Socket s : socketList
            ) {
                //假如没有进入容器，则初始化后进入，并开一条线程给新的Socket
                if (!isSocketInClientList(s)) {
                    ci = new ClientInfo(s, String.valueOf(curId++), "NONAME", middleMan);
                    clientInfoList.add(ci);
                    executorService.execute(ci);
                }
            }
        }
    }

    private boolean isSocketInClientList(Socket socket) {
        /**
         *@描述 判断Socket是否已经被包装,并检查包装容器中socket的连接情况
         *@参数  [socket]检查的Socket
         *@返回值  boolean
         *@创建人  KAI
         *@创建时间  2018/11/2
         *@修改人和其它信息
         */
        synchronized (clientInfoList) {
            for (ClientInfo ci : clientInfoList
            ) {
                //移除关闭套接字
                if (ci.getSocket().isClosed()) {
                    //提示断开连接
                    System.out.println(ci.getNickName()+"(uid:"+ci.getUid()+") 断开连接");
                    socketList.remove(ci.getSocket());
                    clientInfoList.remove(ci);
                    return true;
                }
                if (ci.getSocket() == socket) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public void run() {
        while (true){
            try {
                checkSocketAliveRoutine();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class ThreadBuilder implements ThreadFactory{
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r);
        }
    }
}

class ClientInfo implements Runnable{
    /**
     *@描述 包装Socket类，附带Socket信息
     *@参数 
     *@返回值 
     *@创建人  KAI
     *@创建时间  2018/11/2
     *@修改人和其它信息
     */
    private Socket socket;
    private String uid;
    private String nickName;
    private InputStream inputStream;
    private byte[] bytes;
    private MiddleMan middleMan;
    public ClientInfo(Socket socket, String uid, String nickName,MiddleMan middleMan) throws IOException {
        this.socket = socket;
        this.uid = uid;
        this.nickName = nickName;
        this.inputStream = socket.getInputStream();
        this.middleMan = middleMan;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getUid() {
        return uid;
    }

    public String getNickName() {
        return nickName;
    }

    private void reciveContent() throws IOException {
        String[] content;
        StringBuilder word;
        String reciver;
        int startIndex = 3;
        while (true){
            bytes = new byte[1024];
            inputStream.read(bytes);
            //处理掉空白 '\\$E'为接受信息终止符,如果没有,会因为缓存大小1024会导致后面都是空白字符
            content = new String(bytes).split("\\$E");
            //信息提取划分 0：发送时间 1：发送指令名称 2：接收对象 (或指令内容) 3：信息内容(Tell下才有)
            content = content[0].split("\\|");
            //准备发送内容 word
            word = new StringBuilder(content[0]+": ");
            //指令类型判断
            switch (content[1].toLowerCase()){
                //注册
                case "register":
                    this.nickName = content[2];
                    word = new StringBuilder(content[0]);
                    //拼装待发送注册信息
                    word.append(" Regitster Name <<"+nickName+">> Success");
                    //确定接受对象uid为自己
                    reciver = String.valueOf(uid);
                    break;
                //聊天
                case "tell":
                    for (int i = startIndex; i < content.length; i++) {
                        word.append(content[i]);
                    }
                    //me即是回执
                    if("Me".equals(content[2])){
                        reciver = String.valueOf(uid);
                    }else{
                        reciver = content[2];
                    }
                    break;
                //获取当前在线用户
                case "onlineuser":
                    reciver = String.valueOf(uid);
                    middleMan.showAllOnlineUser(this);
                    continue;
                //没有对应指令，发送返回 错误信息
                default:
                    reciver = String.valueOf(uid);
                    word.append("ERROR_ORDER");
                    break;
            }
            //服务器打印当前待发送内容
            System.out.println("Movement: "+nickName+" To:"+reciver+" Content: "+word);
            //内容提交给中间人信使发送
            middleMan.passMessage(reciver,this,word);
        }
    }

    @Override
    public void run() {
        try {
            reciveContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
