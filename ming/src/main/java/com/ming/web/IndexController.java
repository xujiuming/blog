package com.ming.web;

import com.ming.base.interceptor.thymeleaf.Layout;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Layout
public class IndexController {

    @GetMapping("index")
    public String index(){
        return "index";
    }
}
