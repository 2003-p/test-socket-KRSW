package org.socket.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea; // TextBoxからTextAreaに変更

public class Test_memo implements EntryPoint {
	
	// 黒枠に合うように、一行のTextBoxから複数行のTextAreaに変更します
	private final TextArea editorArea = new TextArea();
	
	private final Label statusLabel = new Label();
	private WebSocket webSocket;

	// 無限ループを防止するための「更新中」フラグ
	private boolean isUpdating = false;

	public void onModuleLoad() {
		// エディタ領域の見た目を設定します
		editorArea.addStyleName("note-line"); // 既存のCSSを流用
		editorArea.setSize("100%", "150px"); // サイズを調整

		// HTMLの各コンテナにUI部品を配置します
		RootPanel.get("statusContainer").add(statusLabel);
		RootPanel.get("realtime-editor-container").add(editorArea);

		// キー入力のイベントハンドラを設定します
		editorArea.addKeyUpHandler(event -> {
			// 更新中フラグが立っている間は、新たな送信を行いません
			if (isUpdating) {
				return;
			}
			if (webSocket != null) {
				// 現在の全テキストをそのまま送信します
				webSocket.send(editorArea.getText());
			}
		});

		connectWebSocket();
	}

	private void connectWebSocket() {
		String url = "ws://" + GWT.getHostPageBaseURL().split("/")[2] + "/memo";
		statusLabel.setText("サーバーに接続中...");
		webSocket = new WebSocket(url);

		webSocket.setOnOpen(() -> {
			statusLabel.setText("サーバーに接続しました。");
		});

		webSocket.setOnMessage(event -> {
			String newText = event.getData();
			
			// 更新前にフラグを立てます
			isUpdating = true; 
			
			// サーバーから受信したテキストでエディタの内容を更新します
			editorArea.setText(newText);
			
			// 更新が終わったらフラグを下ろします
			isUpdating = false; 
		});

		webSocket.setOnClose(() -> {
			statusLabel.setText("サーバーとの接続が切れました。");
		});
		
		webSocket.setOnError(() -> {
			statusLabel.setText("エラーが発生しました。");
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