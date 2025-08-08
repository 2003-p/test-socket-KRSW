package org.socket.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;

public class Test_memo implements EntryPoint {

    // メモ帳のテキストエリア
    private final TextArea memoArea = new TextArea();

    // WebSocketオブジェクトを保持するためのフィールド。
    // JSNIからアクセスするため、特定の型は不要（JavaScriptオブジェクトとして扱う）
    // このフィールドはJava側のコードからは直接使わなくても、JSNIからアクセスできればよい
    private Object ws; // WebSocketという型が存在しないため、汎用的なObject型を使用する

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        memoArea.addStyleName("memoArea");
        memoArea.setSize("100%", "500px");
        RootPanel.get().add(memoArea);

        connectWebSocket();

        memoArea.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                String message = memoArea.getText();
                sendMessage(message);
            }
        });
    }

    private native void connectWebSocket() /*-{
        var protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
        var url = protocol + "//" + window.location.host + "/test_memo/websocket/memo";
        var self = this;

        $wnd.console.log("Attempting to connect to WebSocket at: " + url);
        
        var ws = new WebSocket(url);
        
        ws.onopen = function(event) {
            $wnd.console.log("WebSocket connection successfully opened.", event);
        };
        
        ws.onmessage = function(event) {
            $wnd.console.log("WebSocket message received: ", event.data);
            self.@org.socket.client.Test_memo::updateMemo(Ljava/lang/String;)(event.data);
        };
        
        ws.onclose = function(event) {
            if (event.wasClean) {
                $wnd.console.log("WebSocket connection closed cleanly. Code: " + event.code + ", Reason: " + event.reason);
            } else {
                // e.g. server process killed or network down
                // event.code is usually 1006 in this case
                $wnd.console.warn("WebSocket connection died. Code: " + event.code);
            }
        };
        
        ws.onerror = function(event) {
            $wnd.console.error("A WebSocket error occurred:", event);
        };
        
        this.@org.socket.client.Test_memo::ws = ws;
    }-*/;
    
    private void updateMemo(String message) {
        int cursorPosition = memoArea.getCursorPos();
        int scrollTop = memoArea.getElement().getScrollTop();
        memoArea.setText(message);
        memoArea.setCursorPos(cursorPosition);
        memoArea.getElement().setScrollTop(scrollTop);
    }
    
    private native void sendMessage(String message) /*-{
        if (this.@org.socket.client.Test_memo::ws && this.@org.socket.client.Test_memo::ws.readyState === WebSocket.OPEN) {
            this.@org.socket.client.Test_memo::ws.send(message);
        } else {
            $wnd.console.warn("WebSocket is not open. Message not sent: " + message);
        }
    }-*/;
}