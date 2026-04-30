package piJava.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import piJava.services.ChatMessageService;

@Configuration
public class AppConfig {

    @Bean
    public ChatMessageService chatMessageService() {
        return new ChatMessageService();
    }
}
