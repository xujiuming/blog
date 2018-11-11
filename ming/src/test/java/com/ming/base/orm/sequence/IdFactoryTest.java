package com.ming.base.orm.sequence;

import com.ming.Start;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Start.class)
@Slf4j
public class IdFactoryTest {

    @Test
    public void newId() throws InterruptedException {
/*
        long now = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            System.out.println(IdFactory.newId(IdFactoryTest.class));
        }
        System.out.println("总耗时:" + (System.currentTimeMillis() - now) + "ms");
*/
        Runnable task = () -> {
            for (int i = 0; i < 10000; i++) {
                log.info(IdFactory.newStringId(IdFactoryTest.class));
            }
        };
        ExecutorService pool = Executors.newFixedThreadPool(10);
        long now = System.currentTimeMillis();
        pool.submit(task);
        pool.submit(task);
        pool.submit(task);
        pool.submit(task);
        pool.submit(task);
        pool.submit(task);
        pool.submit(task);
        pool.submit(task);
        pool.submit(task);
        pool.submit(task);
        System.out.println("总耗时:" + (System.currentTimeMillis() - now) + "ms");
        Thread.sleep(10000000L);
    }
}