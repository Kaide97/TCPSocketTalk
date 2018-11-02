package cn.kaispace.onlineTalk.Server;

import java.io.IOException;

/**
 * @Author KAI
 * @CreateTime 2018/11/1
 * @Describe
 **/
public class ServerEntrance {
    public static void main(String[] args){
        try {
            ServerControl sc = new ServerControl(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
