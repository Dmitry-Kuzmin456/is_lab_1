package lab.ws;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/dragons")
public class DragonSocket {

    private static final Set<Session> SESSIONS = ConcurrentHashMap.newKeySet();

    @OnOpen
    public void open(Session session) {
        SESSIONS.add(session);
    }

    @OnClose
    public void close(Session session) {
        SESSIONS.remove(session);
    }

    @OnError
    public void error(Session session, Throwable error) {
        if (session != null) {
            SESSIONS.remove(session);
        }
    }

    public static void broadcast() {
        for (Session session : SESSIONS) {
            if (!session.isOpen()) {
                SESSIONS.remove(session);
                continue;
            }
            try {
                synchronized (session) {
                    session.getBasicRemote().sendText("changed");
                }
            } catch (IOException exception) {
                SESSIONS.remove(session);
            }
        }
    }
}
