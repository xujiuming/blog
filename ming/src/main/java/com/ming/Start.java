package com.ming;

import com.ming.base.orm.jpa.BaseRepositoryFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author ming
 * @date 2018-09-04 15:03:34
 */
@SpringBootApplication
@EnableJpaRepositories(repositoryFactoryBeanClass = BaseRepositoryFactoryBean.class)
public class Start {

    public static void main(String[] args) {
        SpringApplication.run(Start.class, args);
    }
}
