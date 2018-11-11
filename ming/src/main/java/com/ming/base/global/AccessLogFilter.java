package com.ming.base.global;

import com.ming.common.entity.log.LogAccess;
import com.ming.core.utils.JacksonSingleton;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * web flux filter
 *
 * @author ming
 * @date 2018-10-25 00:10:32
 */
@Component
@Order(-5)
public class AccessLogFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        LogAccess logAccess = new LogAccess();
        logAccess.setCreateInstant(Instant.now());
        logAccess.setCookies(JacksonSingleton.writeAsString(request.getCookies()));
        logAccess.setUri(request.getURI().getPath());
        logAccess.setHost(request.getURI().getHost());
        logAccess.setHeaders(JacksonSingleton.writeAsString(request.getHeaders()));
        logAccess.setMethod(request.getMethodValue());
        return chain.filter(exchange);
    }
}
