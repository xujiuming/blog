package com.ming;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.List;

/**web socket 示例
 * @author ming
 * @date 2018-08-30 10:48:43
 */
@Component
public class EchoWebSocket  implements WebSocketHandler {
    @Override
    public List<String> getSubProtocols() {
        return Lists.newArrayList("ming");
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.send(session.receive().map(msg-> session.textMessage("nihao  websocket")));
    }
}
