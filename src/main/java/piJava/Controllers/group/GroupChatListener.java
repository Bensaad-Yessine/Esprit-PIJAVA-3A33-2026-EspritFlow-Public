package piJava.Controllers.group;

public interface GroupChatListener {
    void onMessageReceived(GroupChatMessage message);

    void onConnectionStatusChanged(String statusMessage);
}
