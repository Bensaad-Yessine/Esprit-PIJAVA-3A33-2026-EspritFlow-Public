package piJava.Controllers.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import piJava.Controllers.group.GroupChatMessage;
import piJava.Controllers.group.GroupChatServer;
import piJava.entities.ChatMessage;
import piJava.services.ChatMessageService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    public ChatMessageController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @PostMapping
    public ResponseEntity<ChatMessage> saveMessage(@RequestBody ChatMessage message) {
        try {
            if (message.getSentAt() == null) {
                message.setSentAt(LocalDateTime.now());
            }

            // Save to database
            chatMessageService.save(message);

            // Broadcast instantly via sockets
            GroupChatMessage groupMessage = new GroupChatMessage(
                    message.getGroupId(),
                    message.getSender(),
                    message.getContent(),
                    message.getSentAt()
            );
            
            GroupChatServer.getInstance().broadcast(groupMessage.encode());

            return ResponseEntity.ok(message);
        } catch (Exception e) {
            e.printStackTrace();
            ChatMessage errorMsg = new ChatMessage();
            errorMsg.setContent("SQL ERROR: " + e.getMessage() + " | Cause: " + e.getCause());
            return ResponseEntity.internalServerError().body(errorMsg);
        }
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<ChatMessage>> getMessagesByGroup(@PathVariable int groupId) {
        try {
            List<ChatMessage> messages = chatMessageService.getByGroupId(groupId);
            return ResponseEntity.ok(messages);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
