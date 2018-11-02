package cn.kaispace.onlineTalk.Client;

import java.io.IOException;
import java.net.Socket;

/**
 * @Author KAI
 * @CreateTime 2018/11/1
 * @Describe
 **/
public class ClientEntrance {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1", 27728);
            if (socket.isConnected()) {
                System.out.println("connected Success");
            }
            ClientControl clientControl = new ClientControl(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
