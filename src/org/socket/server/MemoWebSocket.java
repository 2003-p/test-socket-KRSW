package org.socket.server;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

public class MemoWebSocket extends WebSocketAdapter {
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    // === ここから追加 ===
    // ロックを保持しているユーザーのセッションそのものを記憶する場所
    private static volatile Session lockHolderSession = null;
    // === ここまで追加 ===

    @Override
    public void onWebSocketConnect(Session sess) {
        super.onWebSocketConnect(sess);
        sessions.add(sess);
        System.out.println("JETTY WEBSOCKET CONNECTED: " + sess.getRemoteAddress());
    }

    @Override
    public void onWebSocketText(String message) {
        super.onWebSocketText(message);
        
        // === ここから変更 ===
        Session currentSession = getSession(); // このメッセージを送ってきたセッションを取得

        if (message.equals("LOCK")) {
            // もし誰もロックしていなければ
            if (lockHolderSession == null) {
                lockHolderSession = currentSession; // ロックを取得
                broadcast("LOCKED"); // 全員に「ロックされたぞ！」と通知
            }
        } else if (message.equals("UNLOCK")) {
            // もし自分がロック保持者なら
            if (currentSession.equals(lockHolderSession)) {
                lockHolderSession = null; // ロックを解除
                broadcast("UNLOCKED"); // 全員に「解除されたぞ！」と通知
            }
        } else {
            // テキスト更新のメッセージの場合
            // もし自分がロック保持者なら、全員にテキストを送信
            if (currentSession.equals(lockHolderSession)) {
                broadcast(message);
                System.out.println("JETTY WEBSOCKET BROADCAST: " + message);
            }
        }
        // === ここまで変更 ===
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        Session closingSession = getSession(); // 接続が切れたセッションを取得
        sessions.remove(closingSession);
        
        // === ここから追加 (安全装置) ===
        // もし接続が切れたのがロック保持者だったら、ロックを強制解除する
        if (closingSession.equals(lockHolderSession)) {
            lockHolderSession = null;
            broadcast("UNLOCKED"); // 全員に解除を通知
            System.out.println("JETTY WEBSOCKET LOCK RELEASED due to close: " + statusCode);
        }
        // === ここまで追加 ===

        System.out.println("JETTY WEBSOCKET CLOSED: " + statusCode);
        super.onWebSocketClose(statusCode, reason);
    }
    
    // 全員に通知するためのヘルパーメソッド
    private void broadcast(String message) {
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
}