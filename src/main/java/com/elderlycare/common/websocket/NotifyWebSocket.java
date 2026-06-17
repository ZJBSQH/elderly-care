package com.elderlycare.common.websocket;

import cn.hutool.json.JSONUtil;
import jakarta.annotation.PostConstruct;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/notify/{userId}")
public class NotifyWebSocket {

    @PostConstruct
    public void init() {
        log.info("✅ WebSocketConfig 配置类已加载！"); // 👈 启动时看有没有这条日志
    }



    // ✅ 键改为 String，与 @PathParam 类型严格一致
    private static final Map<String, Session> SESSION_POOL = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        SESSION_POOL.put(userId, session);
        log.info("用户 {} 连接 WebSocket 成功", userId);
        sendMessage(session, JSONUtil.toJsonStr(new NotifyMessage(200, "连接成功", null)));
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) {
        SESSION_POOL.remove(userId);
        log.info("用户 {} 断开 WebSocket 连接", userId);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("userId") String userId) {
        log.info("收到用户 {} 的消息：{}", userId, message);
    }

    @OnError
    public void onError(Session session, Throwable error, @PathParam("userId") String userId) {
        log.error("用户 {} WebSocket 连接异常", userId, error);
        // 🔑 异常时务必清理会话，防止内存泄漏
        SESSION_POOL.remove(userId);
    }

    public static void sendToUser(String userId, NotifyMessage message) {
        Session session = SESSION_POOL.get(userId);
        if (session != null && session.isOpen()) {
            sendMessage(session, JSONUtil.toJsonStr(message));
        } else {
            log.warn("用户 {} 不在线或会话已关闭", userId);
        }
    }

    public static void sendToAll(NotifyMessage message) {
        SESSION_POOL.forEach((uid, session) -> {
            if (session.isOpen()) {
                sendMessage(session, JSONUtil.toJsonStr(message));
            }
        });
    }

    // 🔑 加 synchronized 防止多线程并发写入导致 IOException
    private static synchronized void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.error("发送消息失败", e);
        }
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class NotifyMessage {
        private Integer code;
        private String message;
        private Object data;
    }
}
