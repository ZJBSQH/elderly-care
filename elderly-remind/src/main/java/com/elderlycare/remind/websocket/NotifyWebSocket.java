package com.elderlycare.remind.websocket;

import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 通知推送端点
 *
 * @author 郑
 */
@Slf4j
@Component
@ServerEndpoint("/ws/notify/{userId}")
public class NotifyWebSocket {

    /**
     * 在线用户连接池，key 为 userId
     */
    private static final ConcurrentHashMap<String, Session> sessionPool = new ConcurrentHashMap<>();

    /**
     * WebSocket 连接建立
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        sessionPool.put(userId, session);
        log.info("WebSocket 连接建立, userId: {}, 当前在线人数: {}", userId, sessionPool.size());
    }

    /**
     * WebSocket 连接关闭
     */
    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        sessionPool.remove(userId);
        log.info("WebSocket 连接关闭, userId: {}, 当前在线人数: {}", userId, sessionPool.size());
    }

    /**
     * 接收客户端消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("userId") String userId) {
        log.info("收到客户端消息, userId: {}, message: {}", userId, message);
    }

    /**
     * WebSocket 连接异常
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 连接异常", error);
    }

    /**
     * 发送消息给指定用户
     *
     * @param userId  用户 ID
     * @param message 消息内容
     */
    public static void sendToUser(String userId, String message) {
        Session session = sessionPool.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error("发送 WebSocket 消息失败, userId: {}", userId, e);
            }
        }
    }

    /**
     * 发送消息给指定用户（对象格式）
     *
     * @param userId       用户 ID
     * @param notifyMessage 通知消息对象
     */
    public static void sendToUser(String userId, NotifyMessage notifyMessage) {
        sendToUser(userId, JSONUtil.toJsonStr(notifyMessage));
    }

    /**
     * 广播消息给所有在线用户
     *
     * @param message 消息内容
     */
    public static void sendToAll(String message) {
        sessionPool.forEach((userId, session) -> {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    log.error("群发 WebSocket 消息失败, userId: {}", userId, e);
                }
            }
        });
    }

    /**
     * 通知消息内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotifyMessage {
        /** 消息码 */
        private Integer code;
        /** 消息描述 */
        private String message;
        /** 消息数据 */
        private Object data;

        public static NotifyMessage success(Object data) {
            return new NotifyMessage(200, "success", data);
        }

        public static NotifyMessage error(String message) {
            return new NotifyMessage(500, message, null);
        }
    }
}
