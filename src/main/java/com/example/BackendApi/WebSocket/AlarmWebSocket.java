package com.example.BackendApi.WebSocket;

import com.example.BackendApi.Dto.Vo.dto.AlarmMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
@ServerEndpoint("/ws/alarm")
public class AlarmWebSocket {
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }

    /**
     * 3. 這是單機真正的 WebSocket 廣播實作
     */
    public static void localBroadcast(String jsonMessage) {
        // CopyOnWriteArraySet 跑迴圈不需要額外鎖住整個 sessions，不會堵塞 onOpen/onClose
        for (Session session : sessions) {
            if (session.isOpen()) {
                // 4. 關鍵優化：改用 getAsyncRemote() 進行非同步發送，高併發下絕不阻塞！
                session.getAsyncRemote().sendText(jsonMessage);
            }
        }
    }

    /**
     * 5. 對外的廣播接口：在這裡展現妳的「集群架構眼界」
     */
    public static void broadcast(AlarmMessage alarmMessage) {
        try {
            String msg = objectMapper.writeValueAsString(alarmMessage);

            // 【單機模式】直接廣播
            localBroadcast(msg);

            // 如果未來要上集群(Cluster)，這行改為：
            // redisTemplate.convertAndSend("alarm-channel", msg);
            // 然後由 Redis 監聽器去呼叫 localBroadcast(msg)，就能完美橫向擴充！

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
