package dev.jotxee.todo.config;

import dev.jotxee.todo.controller.TodoWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final TodoWebSocketHandler todoWebSocketHandler;

    public WebSocketConfig(TodoWebSocketHandler todoWebSocketHandler) {
            this.todoWebSocketHandler = todoWebSocketHandler;
        }

        @Override
        public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
            // Registrar el WebSocket en el endpoint /ws/todo
            registry.addHandler(todoWebSocketHandler, "/ws/todo")
                    .setAllowedOrigins("*")
                    .addInterceptors(new HttpSessionHandshakeInterceptor());
        }
}
