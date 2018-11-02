package cn.kaispace.onlineTalk.Client;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;

/**
 * @Author KAI
 * @CreateTime 2018/11/1
 * @Describe
 **/
public class ClientControl {
    private Socket socket;
    public ClientControl(Socket socket) throws IOException {
        this.socket = socket;
        InputStream socketInputStream = socket.getInputStream();
        OutputStream socketOutputStream = socket.getOutputStream();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        ThreadFactory namedThreadFactory = new ThreadBuilder();
        //发送线程池
        ExecutorService writeExecutorService = new ThreadPoolExecutor(1, 100,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        writeExecutorService.execute((new writeData(df,socketOutputStream)));

        //接收线程池
        ExecutorService readExecutorService = new ThreadPoolExecutor(1, 100,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        readExecutorService.execute((new readData(socketInputStream)));
    }

    class ThreadBuilder implements ThreadFactory{
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r);
        }
    }

    class writeData implements Runnable{
        private SimpleDateFormat df;
        private OutputStream os;
        public writeData(SimpleDateFormat df,OutputStream os){
            this.df = df;
            this.os = os;
        }

        public synchronized void write() throws IOException {
            StringBuilder content;
            String inputValue;
            String[] order;
            String talkPar = null;
            //读取Console输入
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while(true){
                //转变为|划分符
                inputValue = br.readLine().replace(' ','|');
                //split提取指令进行预处理
                order = inputValue.split("\\|");
                switch (order[0].toLowerCase()){
                    //注册的缩写
                    case "reg":
                        inputValue = inputValue.replaceFirst("reg","register");
                        break;
                    //查看在线用户的缩写
                    case "ou":
                        inputValue = "onlineuser";
                        break;
                    //加入聊天室 (便捷不用打Tell <uid>)
                    case "join":
                        talkPar = order[1];
                        System.out.println("加入与"+order[1]+"的聊天室");
                        continue;
                    //离开聊天室
                    case "leave":
                        System.out.println("退出与"+talkPar+"的聊天室");
                        talkPar = null;
                        continue;
                    //退出程序 离开
                    case "exit":
                        socket.close();
                        System.exit(1);
                        break;
                    //获取指令帮助
                    case "help":
                        System.out.println("register(reg) <name> 注册名字\ntell <uid> <content> 发送消息\n" +
                                "join <uid> 加入聊天室(直接书写内容发送就可以送达)\n" +
                                "leave 退出聊天室\n" +
                                "onlineuser(ou) 显示当前的所有在线人员\n" +
                                "exit 退出程序\n");
                        continue;
                     default:
                         break;
                }
                content=new StringBuilder();
                if(talkPar==null){
                    //不在聊天室内
                    content.append(df.format(new Date())+"|"+inputValue+"$E");
                }else{
                    //在聊天室内
                    content.append(df.format(new Date())+"|tell|"+talkPar+"|"+inputValue+"$E");
                }
                os.write(content.toString().getBytes());
                os.flush();
            }
        }

        @Override
        public void run() {
            try {
                write();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class readData implements Runnable{
        private InputStream is;
        private byte[] bytes;
        public readData(InputStream is){
            this.is = is;
        }

        public synchronized void read() throws IOException {
            String content;
            while(true){
                bytes = new byte[1024];
                is.read(bytes);
                content = new String(bytes);
                //打印至屏幕
                System.out.println(content);
            }
        }

        @Override
        public void run() {
            try {
                read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
