package com.lovecube.backend.websockets;

import com.google.gson.Gson;
import com.lovecube.backend.models.ChatMessage;
import com.lovecube.backend.services.ChatMessageService;
import com.lovecube.backend.utils.ApplicationContextProvider;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@ServerEndpoint("/chat/{userId}")
public class ChatWebSocket
{
    private static Map<Long, Session> onlineUsers = new ConcurrentHashMap<>();
    private static ChatMessageService chatMessageService = ApplicationContextProvider.getBean(ChatMessageService.class);
    private static Gson gson = new Gson();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") long userId)
    {
        onlineUsers.put(userId, session);
        System.out.println("U\uD83D\uDD17 用户 \" + userId + \" 连接成功");

        // 发送未读消息
        chatMessageService.getOfflineMessages(userId).forEach(msg -> sendMessage(userId, msg));
    }

    @OnMessage
    public void onMessage(String message, Session session)
    {
        ChatMessage msg = gson.fromJson(message, ChatMessage.class);
        msg.setTimestamp(System.currentTimeMillis());

        if (onlineUsers.containsKey(msg.getReceiverId())) {
            sendMessage(msg.getReceiverId(), msg);
        } else {
            chatMessageService.saveMessage(msg);
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") long userId)
    {
        onlineUsers.remove(userId);
        System.out.println("U\uD83D\uDD17 用户 \" + userId + \" 断开连接");
    }

    private void sendMessage(long receiverId, ChatMessage msg)
    {
        try {
            if (onlineUsers.containsKey(receiverId)) {
                onlineUsers.get(receiverId).getBasicRemote().sendText(gson.toJson(msg));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
