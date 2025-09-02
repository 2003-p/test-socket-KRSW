package org.socket.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;

public class Test_memo implements EntryPoint {

    private final TextArea editorArea = new TextArea();
    private final Label statusLabel = new Label();
    private WebSocket webSocket;

    private boolean amIHolder = false;

    public void onModuleLoad() {
        editorArea.addStyleName("note-line");
        editorArea.setSize("100%", "150px");

        RootPanel.get("statusContainer").add(statusLabel);
        RootPanel.get("realtime-editor-container").add(editorArea);

        editorArea.addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                if (webSocket != null) {
                    webSocket.send("LOCK");
                    amIHolder = true;
                }
            }
        });

        editorArea.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                if (webSocket != null) {
                    webSocket.send("UNLOCK");
                    amIHolder = false;
                }
            }
        });
        
        editorArea.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (amIHolder && webSocket != null) {
                    webSocket.send(editorArea.getText());
                }
            }
        });

        connectWebSocket();
    }

    private void connectWebSocket() {
        String url = "ws://" + GWT.getHostPageBaseURL().replaceAll("http://", "").replaceAll("/", "") + "/memo";
        statusLabel.setText("サーバーに接続中...");
        webSocket = new WebSocket(url);

        webSocket.setOnOpen(() -> {
            statusLabel.setText("サーバーに接続しました。編集可能です。");
            editorArea.setEnabled(true);
        });

        webSocket.setOnMessage(event -> {
            String message = event.getData();

            if (message.equals("LOCKED")) {
                if (!amIHolder) {
                    editorArea.setEnabled(false);
                    statusLabel.setText("他のユーザーが編集中です...");
                    // === ここが新しい魔法だ！ ===
                    editorArea.addStyleName("locked-by-other");
                }
            } else if (message.equals("UNLOCKED")) {
                editorArea.setEnabled(true);
                statusLabel.setText("編集可能です。");
                // === ここが魔法を解く呪文だ！ ===
                editorArea.removeStyleName("locked-by-other");
            } else {
                if (!amIHolder) {
                    editorArea.setText(message);
                }
            }
        });

        webSocket.setOnClose(() -> {
            statusLabel.setText("サーバーとの接続が切れました。");
            editorArea.setEnabled(false);
        });
        
        webSocket.setOnError(() -> {
            statusLabel.setText("エラーが発生しました。");
            editorArea.setEnabled(false);
        });
    }

    // --- WebSocketのJSNIラッパー (変更なし) ---
    private static class WebSocket {
        private final com.google.gwt.core.client.JavaScriptObject ws;
        public WebSocket(String url) { this.ws = create(url); }
        public native void send(String data) /*-{ this.@org.socket.client.Test_memo.WebSocket::ws.send(data); }-*/;
        public native void setOnOpen(Runnable handler) /*-{ this.@org.socket.client.Test_memo.WebSocket::ws.onopen = function() { handler.@java.lang.Runnable::run()(); }; }-*/;
        public native void setOnMessage(MessageHandler handler) /*-{ this.@org.socket.client.Test_memo.WebSocket::ws.onmessage = function(msg) { handler.@org.socket.client.Test_memo.MessageHandler::onMessage(Lorg/socket/client/Test_memo$MessageEvent;)(msg); }; }-*/;
        public native void setOnClose(Runnable handler) /*-{ this.@org.socket.client.Test_memo.WebSocket::ws.onclose = function() { handler.@java.lang.Runnable::run()(); }; }-*/;
        public native void setOnError(Runnable handler) /*-{ this.@org.socket.client.Test_memo.WebSocket::ws.onerror = function() { handler.@java.lang.Runnable::run()(); }; }-*/;
        private native com.google.gwt.core.client.JavaScriptObject create(String url) /*-{ return new WebSocket(url); }-*/;
    }
    @FunctionalInterface
    private interface MessageHandler { void onMessage(MessageEvent event); }
    private static class MessageEvent extends com.google.gwt.core.client.JavaScriptObject {
        protected MessageEvent() {}
        public final native String getData() /*-{ return this.data; }-*/;
    }
}