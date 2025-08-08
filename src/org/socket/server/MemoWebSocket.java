package org.socket.server;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

public class MemoWebSocket extends WebSocketAdapter {
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void onWebSocketConnect(Session sess) {
        super.onWebSocketConnect(sess);
        sessions.add(sess);
        System.out.println("JETTY WEBSOCKET CONNECTED: " + sess.getRemoteAddress());
    }

    @Override
    public void onWebSocketText(String message) {
        super.onWebSocketText(message);
        System.out.println("JETTY WEBSOCKET RECEIVED: " + message);
        for (Session session : sessions) {
            if (session.isOpen()) {
                try {
                    session.getRemote().sendString(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        sessions.remove(getSession());
        System.out.println("JETTY WEBSOCKET CLOSED: " + statusCode);
        super.onWebSocketClose(statusCode, reason);
    }
}