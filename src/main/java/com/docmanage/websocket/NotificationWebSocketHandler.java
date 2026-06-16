package com.docmanage.websocket;

import com.docmanage.security.JwtTokenProvider;
import com.docmanage.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final WebSocketNotificationService webSocketNotificationService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = resolveUserId(session);
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        session.getAttributes().put("userId", userId);
        String role = (String) session.getAttributes().get("role");
        boolean isAdmin = "admin".equals(role) || "super".equals(role);
        webSocketNotificationService.registerSession(userId, session, isAdmin);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            webSocketNotificationService.unregisterSession(userId, session);
        }
    }

    private Long resolveUserId(WebSocketSession session) {
        String token = (String) session.getAttributes().get("token");
        if (!StringUtils.hasText(token) || !jwtTokenProvider.validateToken(token)) {
            return null;
        }
        return jwtTokenProvider.getUserIdFromToken(token);
    }
}
