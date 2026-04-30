package piJava.Controllers.group;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class GroupChatServer {
    private static final GroupChatServer INSTANCE = new GroupChatServer();

    private final ExecutorService clientPool = Executors.newCachedThreadPool();
    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    private volatile boolean running;
    private ServerSocket serverSocket;

    private GroupChatServer() {
    }

    public static GroupChatServer getInstance() {
        return INSTANCE;
    }

    public synchronized boolean start(int port) {
        if (running) {
            return true;
        }

        try {
            serverSocket = new ServerSocket(port);
            running = true;
            Thread acceptThread = new Thread(this::acceptLoop, "group-chat-server-acceptor");
            acceptThread.setDaemon(true);
            acceptThread.start();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void acceptLoop() {
        while (running && serverSocket != null && !serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                clientPool.submit(handler);
            } catch (SocketException ignored) {
                running = false;
            } catch (IOException ignored) {
                // Continue accepting other clients.
            }
        }
    }

    public void broadcast(String encodedMessage) {
        for (ClientHandler client : clients) {
            client.send(encodedMessage);
        }
    }

    private final class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter writer;

        private ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                writer = new PrintWriter(socket.getOutputStream(), true);
                String line;
                while ((line = reader.readLine()) != null) {
                    broadcast(line);
                }
            } catch (IOException ignored) {
                // Client disconnected.
            } finally {
                clients.remove(this);
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }

        private void send(String encodedMessage) {
            if (writer != null) {
                writer.println(encodedMessage);
            }
        }
    }
}
