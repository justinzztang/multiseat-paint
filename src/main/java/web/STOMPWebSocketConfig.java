package web;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
@EnableScheduling
public class STOMPWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/update");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) { //sets the endpoint for where you make the connection
        registry.addEndpoint("/update-websocket").setAllowedOriginPatterns("*");
        registry.setPreserveReceiveOrder(true);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry){
        registry.setSendBufferSizeLimit(1024*1024*8);
        registry.setMessageSizeLimit(1024*1024*8);
    }

}
