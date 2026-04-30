package piJava.Controllers.group;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GroupChatClient {
    private Socket socket;
    private PrintWriter writer;
    private Thread readerThread;

    public void connect(String host, int port, GroupChatListener listener) throws IOException {
        disconnect();

        socket = new Socket(host, port);
        writer = new PrintWriter(socket.getOutputStream(), true);

        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        readerThread = new Thread(() -> readLoop(reader, listener), "group-chat-client-reader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void readLoop(BufferedReader reader, GroupChatListener listener) {
        try {
            listener.onConnectionStatusChanged("Connecte au chat en direct");
            String line;
            while ((line = reader.readLine()) != null) {
                GroupChatMessage message = GroupChatMessage.decode(line);
                listener.onMessageReceived(message);
            }
        } catch (Exception e) {
            listener.onConnectionStatusChanged("Connexion chat interrompue");
        } finally {
            disconnect();
        }
    }

    public void sendMessage(GroupChatMessage message) {
        if (writer != null) {
            writer.println(message.encode());
        }
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        }

        socket = null;
        writer = null;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}
