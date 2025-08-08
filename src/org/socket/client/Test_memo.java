package org.socket.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class Test_memo implements EntryPoint {
	
	private final TextBox nameField = new TextBox();
	private final Button sendButton = new Button("Send");
	private final Label statusLabel = new Label();
	private final VerticalPanel messagesPanel = new VerticalPanel(); // メッセージ表示用
	
	private WebSocket webSocket;

	public void onModuleLoad() {
		sendButton.addStyleName("sendButton");
		
		VerticalPanel mainPanel = new VerticalPanel();
		mainPanel.add(new Label("Please enter your message:"));
		mainPanel.add(nameField);
		mainPanel.add(sendButton);
		mainPanel.add(statusLabel);
		mainPanel.add(messagesPanel);
		
		RootPanel.get("nameFieldContainer").add(mainPanel);
		
		nameField.setFocus(true);
		
		//【修正点1】setEnabledを使用
		sendButton.setEnabled(false);

		// WebSocketに接続
		connectWebSocket();

		// ボタンのクリックハンドラ
		sendButton.addClickHandler(event -> {
			sendMessage();
		});

		// テキストボックスでEnterキーを押しても送信できるようにする
		nameField.addKeyUpHandler(event -> {
			if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
				sendMessage();
			}
		});
	}
	
	/**
	 * メッセージをWebSocketで送信する
	 */
	private void sendMessage() {
		String message = nameField.getValue();
		if (!sendButton.isEnabled() || message == null || message.trim().isEmpty() || webSocket == null) {
			return;
		}
		webSocket.send(message);
		nameField.setValue("");
	}

	/**
	 * WebSocket接続を開始する
	 */
	private void connectWebSocket() {
	    // "moduleName" の行を削除し、下の行を修正します。
	    String url = "ws://" + GWT.getHostPageBaseURL().split("/")[2] + "/memo";
	    
	    statusLabel.setText("Connecting to: " + url);

	    webSocket = new WebSocket(url);
		//【修正点2】ラムダ式を引数なし () -> に修正
		webSocket.setOnOpen(() -> {
			statusLabel.setText("Connection established.");
			sendButton.setEnabled(true);
		});

		webSocket.setOnMessage(event -> {
			String message = event.getData();
			messagesPanel.add(new HTML(message));
		});

		//【修正点2】ラムダ式を引数なし () -> に修正
		webSocket.setOnClose(() -> {
			statusLabel.setText("Connection closed.");
			sendButton.setEnabled(false);
		});
		
		//【修正点2】ラムダ式を引数なし () -> に修正
		webSocket.setOnError(() -> {
			statusLabel.setText("Connection error.");
			sendButton.setEnabled(false);
		});
	}

	// JSNIを使ってJavaScriptのWebSocketをラップする
	private static class WebSocket {
		private final com.google.gwt.core.client.JavaScriptObject ws;

		public WebSocket(String url) {
			this.ws = create(url);
		}

		public native void send(String data) /*-{
			this.@org.socket.client.Test_memo.WebSocket::ws.send(data);
		}-*/;

		public native void setOnOpen(Runnable handler) /*-{
			this.@org.socket.client.Test_memo.WebSocket::ws.onopen = function() {
				handler.@java.lang.Runnable::run()();
			};
		}-*/;

		public native void setOnMessage(MessageHandler handler) /*-{
			this.@org.socket.client.Test_memo.WebSocket::ws.onmessage = function(msg) {
				handler.@org.socket.client.Test_memo.MessageHandler::onMessage(Lorg/socket/client/Test_memo$MessageEvent;)(msg);
			};
		}-*/;

		public native void setOnClose(Runnable handler) /*-{
			this.@org.socket.client.Test_memo.WebSocket::ws.onclose = function() {
				handler.@java.lang.Runnable::run()();
			};
		}-*/;
		
		public native void setOnError(Runnable handler) /*-{
			this.@org.socket.client.Test_memo.WebSocket::ws.onerror = function() {
				handler.@java.lang.Runnable::run()();
			};
		}-*/;

		private native com.google.gwt.core.client.JavaScriptObject create(String url) /*-{
			return new WebSocket(url);
		}-*/;
	}

	// onmessageイベントを処理するためのインターフェース
	@FunctionalInterface
	private interface MessageHandler {
		void onMessage(MessageEvent event);
	}

	// JavaScriptのMessageEventオブジェクトをラップ
	private static class MessageEvent extends com.google.gwt.core.client.JavaScriptObject {
		protected MessageEvent() {}
		public final native String getData() /*-{ return this.data; }-*/;
	}
}