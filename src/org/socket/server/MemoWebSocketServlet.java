package org.socket.server;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class MemoWebSocketServlet extends WebSocketServlet {

    @Override
    public void configure(WebSocketServletFactory factory) {
        // 私たちが作ったWebSocketハンドラを登録します
        factory.register(MemoWebSocket.class);
    }
}