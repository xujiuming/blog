package com.ming;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * @author ming
 * @date 2018-09-04 15:03:28
 */
@RestController
@Slf4j
public class IndexController  {

    /**
     * 返回一个常规的 String
     *
     * @author ming
     * @date 2018-08-30 10:47:06
     */
    @GetMapping("index")
    public Mono<String> index() {
        return Mono.just("nihao  web flux");
    }

    /**
     * 基于sse的 服务端推送   每秒返回一次
     *
     * @author ming
     * @date 2018-08-30 10:47:25
     */
    @GetMapping("/sse")
    public Flux<ServerSentEvent<String>> sse() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(String::valueOf)
                .map(m -> ServerSentEvent.<String>builder().event("sse")
                        .id("sseId" + m)
                        .data("nihao sse")
                        .build()
                );
    }

    @GetMapping("/err")
    public String err() {
        log.error("xxxxxx");
        log.info("xxinfo");
        if (true) {
            throw new NullPointerException();
        }
        return "";
    }

}
