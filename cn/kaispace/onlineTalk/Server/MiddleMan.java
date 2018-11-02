package cn.kaispace.onlineTalk.Server;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author KAI
 * @CreateTime 2018/11/1
 * @Describe
 **/
public class MiddleMan<T> {
    private CopyOnWriteArrayList<T> clientInfoList;
    public MiddleMan(CopyOnWriteArrayList<T> clientInfoList){
        this.clientInfoList = clientInfoList;
    }
    public boolean passMessage(String uid,ClientInfo sender,StringBuilder message){
        try {
            ClientInfo ci = getClientInfo(uid);
            OutputStream outputStream;
            //ci==null为找不到对应用户
            if(ci==null){
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                outputStream = sender.getSocket().getOutputStream();
                outputStream.write(("From: "+df.format(new Date())+" Admin \nUid doesn't exist").getBytes());
            }else{
                outputStream = ci.getSocket().getOutputStream();
                outputStream.write(("From: "+sender.getNickName()+" \n"+message.toString()).getBytes());
            }
            outputStream.flush();
            return true;
        } catch (IllegalAccessException | IOException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }

    private ClientInfo getClientInfo(String uid) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String cuid;
        Method m;
        for (Object clientInfo: clientInfoList
             ) {
            m = clientInfo.getClass().getMethod("getUid");
            cuid = String.valueOf(m.invoke(clientInfo));
            if(cuid.equals(uid))
            {
                return (ClientInfo)clientInfo;
            }
        }
        return null;
    }

    public void showAllOnlineUser(ClientInfo sender){
        StringBuilder userTable = new StringBuilder("OnlineUser: ");
        String nickName;
        String uid;
        for (T ci:clientInfoList
             ) {
            try {
                uid = String.valueOf(ci.getClass().getMethod("getUid").invoke(ci));
                nickName = String.valueOf(ci.getClass().getMethod("getNickName").invoke(ci));
                userTable.append(nickName).append("(uid:").append(uid).append(")");
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            OutputStream outputStream = sender.getSocket().getOutputStream();
            outputStream.write(("From: "+df.format(new Date())+" Admin \n"+userTable).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
