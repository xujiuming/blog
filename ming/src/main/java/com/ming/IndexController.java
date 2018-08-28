package com.ming;

import com.ming.base.interceptor.thymeleaf.Layout;
import com.ming.base.web.BaseAbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Layout
public class IndexController extends BaseAbstractController {

    @GetMapping("index")
    public String index() {
        return "index";
    }


}
