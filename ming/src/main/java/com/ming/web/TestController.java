package com.ming.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@RestController
public class TestController {

    @GetMapping("/test")
    public Mono<String> getTest(String id){
        return Mono.justOrEmpty("getTest"+id);
    }

    @PostMapping("/test")
    public Mono<String> postTest(@RequestBody  String id){
        return Mono.justOrEmpty("postTest"+id);
    }
}
