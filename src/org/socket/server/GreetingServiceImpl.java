package org.socket.server;

import org.socket.client.GreetingService;
import org.socket.shared.FieldVerifier;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

// ... import文 ...

public class GreetingServiceImpl extends RemoteServiceServlet implements GreetingService {

	public String greetServer(String input) throws IllegalArgumentException {
		// FieldVerifierのチェックは残してもOK
		if (!FieldVerifier.isValidName(input)) {
			throw new IllegalArgumentException("Name must be at least 4 characters long");
		}

		// ↓↓↓ ここから下のWebSocket関連コードをすべて削除する ↓↓↓
		// WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		// Session session = container.connectToServer( ... );
		// session.getBasicRemote().sendText(input);
		// session.close();
		// ↑↑↑ ここまでを削除 ↑↑↑

		// 本来のRPCの処理（もしあれば）
		String serverInfo = getServletContext().getServerInfo();
		String userAgent = getThreadLocalRequest().getHeader("User-Agent");
		return "Hello, " + input + "!<br><br>I am running " + serverInfo + ".<br><br>It looks like you are using:<br>"
				+ userAgent;
	}
}