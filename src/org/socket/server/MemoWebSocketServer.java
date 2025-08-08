package org.socket.server;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/websocket/memo")
public class MemoWebSocketServer {

    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("新規クライアントが接続しました: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        System.out.println("クライアント " + session.getId() + " からメッセージを受信しました: " + message);
        broadcast(message, session);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println("クライアントが切断されました: " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("セッション " + session.getId() + " でエラーが発生しました: " + throwable.getMessage());
    }

    private static void broadcast(String message, Session senderSession) {
        sessions.forEach(session -> {
            if (!session.equals(senderSession)) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    System.err.println("セッション " + session.getId() + " へのメッセージ送信に失敗しました: " + e.getMessage());
                }
            }
        });
    }
}