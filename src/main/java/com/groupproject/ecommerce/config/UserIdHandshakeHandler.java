package com.groupproject.ecommerce.config;


import org.springframework.http.server.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class UserIdHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(@NonNull ServerHttpRequest request,
                                      @NonNull WebSocketHandler wsHandler,
                                      @NonNull Map<String, Object> attributes) {

        Object userId = attributes.get(HttpSessionHandshakeInterceptor.WS_USER_ID);
        if (userId == null) return null;

        String principalName = String.valueOf(userId); // Principal.getName() = userId
        return () -> principalName;
    }
}
