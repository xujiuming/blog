package com.ming.base.global;


import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 启动初始化
 *
 * @author ming
 * @date 2018-10-19 01:37:23
 */
@Component
public class StartInit implements CommandLineRunner {



    /**
     * bean加载完成之后初始化
     * 初始化一些 需要依赖spring bean的功能
     *
     * @author ming
     * @date 2018-10-19 01:38:26
     */
    @Override
    public void run(String... args) throws Exception {
        long now = System.currentTimeMillis();
        System.out.println("开始初始化。。。。。。。");
        System.out.println("初始化结束。。。。。。。耗时(ms):" + (System.currentTimeMillis() - now));
    }


}
