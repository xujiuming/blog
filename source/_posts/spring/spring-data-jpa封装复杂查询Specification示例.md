---
title: spring-data-jpa封装复杂查询Specification示例
comments: true
categories: 实战
tags:
  - spring-data-jpa
  - 示例
abbrlink: c207a9ae
date: 2020-04-21 15:18:09
---
#### 前言
从个人角度来说 我更加喜欢jpa  因为是正儿八经的orm框架 很多时候 只需要处理java代码即可 不需要去梭sql  
虽然真正去梭sql 性能更好 也更加灵活 可以使用各种各样的操作   如果项目是做一些复杂的查询 我觉得mybatis 或者直接jdbc之类的会更加合适
此示例 只面向懂一部分jpa的工程师  如果都不知道jpa是啥 建议还是先看看官方文档  
#### 基础示例
jpa 做条件查询的时候 要么直接写queryDSL 要么构建example(简单条件可以选择使用这个)  或者构建specification 
在复杂条件的时候 肯定是使用specification 
* specification  规则 是多个Predicate 组合成的访问规则  
* Predicate 相当于 查询语句的 where之后的各种条件 如aaa=value xxx>value
* Root 对象映射的根对象 相当于一个起点  如 Staff实体 中间有个name属性 那么需要判断name等于xxx 需要使用Root.get("name")来获取Path 如何在这个Path上构建Predicate  Root代表本实体Staff 

```java
package com.ming;

import com.ming.core.utils.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试specification 基本构建方法
 *
 * @author ming
 * @date 2020-04-21 15:30:17
 */
public class TestSpecification {
    /**
     * 构建 specification
     *
     * @author ming
     * @date 2020-04-21 15:42:27
     */
    public <T> Specification<T> buildSpecification() {
        return new Specification<T>() {
            /**
             *  创建where条件    
             *  流程 
             *  -> 根据root的层级构建predicate: xxx = value   ,  aaa =value 
             *  -> 使用criteriaBuilder 将多个Predicate 拼接起来 : xxx = value and aaa=value 
             *  -> entityManager 根据生成的Specification对象 访问db
             *
             * @param root            实体的根映射对象
             * @param query           must not be {@literal null}.
             * @param criteriaBuilder 拼接多个Predicate构建器
             * @return a {@link Predicate}, may be {@literal null}.
             */
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                //构建 实体的路径  
                Path expression = root.get("实体的字段");
                //根部不同的要求构建断言 下面列举了大多数的比较方式 如eq 、like 大于 小于等等  
//                predicates.add(criteriaBuilder.equal(expression, value));
//                predicates.add(criteriaBuilder.like(expression, "%" + value + "%"));
//                predicates.add(criteriaBuilder.notLike(expression, "%" + value + "%"));
//                predicates.add(criteriaBuilder.greaterThan(expression, (Comparable) value));
//                predicates.add(criteriaBuilder.lessThan(expression, (Comparable) value));
//                predicates.add(criteriaBuilder.greaterThanOrEqualTo(expression, (Comparable) value));
//                predicates.add(criteriaBuilder.lessThanOrEqualTo(expression, (Comparable) value));
//                predicates.add(criteriaBuilder.notEqual(expression, value));
//                predicates.add(criteriaBuilder.or(Predicate[]));
                //当存在部分断言 用and拼接起来
                if (CollectionUtils.notEmpty(predicates)) {
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
                return criteriaBuilder.conjunction();
            }
        };
    }
}

```

#### 高度封装示例
##### 思路
1. 使用注解标明字段 增加规则方式 和应用规则的entity 字段 
2. 编写工具类根据注解 构建specification 
3. 将Repository 继承  JpaSpecificationExecutor 使用specification去访问数据 
##### 注解
* MyJpaSpecifications 注解 

```java
package com.ming.base.orm.annotatioin;

import com.ming.base.orm.JpaOperator;

import java.lang.annotation.*;

/**
 * 标注为 jpa查询的条件字段
 *
 * @author ming
 * @date 2020-04-21 13:18:14
 */
@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface MyJpaSpecifications {

    /**
     * 操作
     * {@linkplain com.ming.base.orm.JpaOperator}
     *
     * @author ming
     * @date 2020-04-21 13:36:32
     */
    JpaOperator operator() default JpaOperator.EQ;

    /**
     * 实体字段
     */
    String entityField() default "";
}

```

* JpaOperator 枚举类 
```java
package com.ming.base.orm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("查询过滤器-操作类型")
public enum JpaOperator {
    @ApiModelProperty("精确比较")
    EQ,
    @ApiModelProperty("模糊查询")
    LIKE,
    @ApiModelProperty("模糊查询取反")
    NOT_LIKE,
    @ApiModelProperty("大于")
    GT,
    @ApiModelProperty("小于")
    LT,
    @ApiModelProperty("大于等于")
    GTE,
    @ApiModelProperty("小于等于")
    LTE,
    @ApiModelProperty("在这部分参数中")
    IN,
    @ApiModelProperty("不等于")
    NEQ,
    @ApiModelProperty("或")
    OR;
}
```
##### 根据注解构建specification
* SpecificationUtils 
```java
package com.ming.base.orm;

import com.ming.base.CodeException;
import com.ming.base.ResponseBody;
import com.ming.base.orm.annotatioin.MyJpaSpecifications;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ReflectionUtils;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 构建查询对Specification进行封装 用searchfilter enum进行封装
 *
 * @author ming
 * @date 2020-04-21 09:25:33
 */
@Slf4j
public class SpecificationUtils {

    /**
     * 根据class 获取字段 和字段的注解 和值  构建 条件
     *
     * @author ming
     * @date 2020-04-21 13:26:13
     */
    @SuppressWarnings("unchecked")
    public static <T, V> Specification<T> buildSpecificationByMyJpaSpecifications(Class<V> clazz, V val) {

        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<Predicate>();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(Boolean.TRUE);
                //获取注解
                MyJpaSpecifications myJpaSpecifications = field.getAnnotation(MyJpaSpecifications.class);
                if (Objects.isNull(myJpaSpecifications)) {
                    //如果没有注解 忽略此字段 不进行构建处理
                    continue;
                }
                //如果没有输入实体字段 默认为当前属性字段的名称
                String nameStr = myJpaSpecifications.entityField();
                if (StringUtils.isEmpty(nameStr)) {
                    nameStr = field.getName();
                }
                String[] names = StringUtils.split(nameStr, ".");
                Path expression = root.get(names[0]);
                for (int i = 1; i < names.length; i++) {
                    expression = expression.get(names[i]);
                }
                //in 和or 中需要的一个中间变量 用来将filter.value放入数组
                Object[] objects = new Object[1];
                switch (myJpaSpecifications.operator()) {
                    case EQ:
                        predicates.add(builder.equal(expression, ReflectionUtils.getField(field, val)));
                        break;
                    case LIKE:
                        predicates.add(builder.like(expression, "%" + ReflectionUtils.getField(field, val) + "%"));
                        break;
                    case NOT_LIKE:
                        predicates.add(builder.notLike(expression, "%" + ReflectionUtils.getField(field, val) + "%"));
                        break;
                    case GT:
                        predicates.add(builder.greaterThan(expression, (Comparable) ReflectionUtils.getField(field, val)));
                        break;
                    case LT:
                        predicates.add(builder.lessThan(expression, (Comparable) ReflectionUtils.getField(field, val)));
                        break;
                    case GTE:
                        predicates.add(builder.greaterThanOrEqualTo(expression, (Comparable) ReflectionUtils.getField(field, val)));
                        break;
                    case LTE:
                        predicates.add(builder.lessThanOrEqualTo(expression, (Comparable) ReflectionUtils.getField(field, val)));
                        break;
                    case IN:
                        //因为spring data jpa 本身没有对数组进行判断 传入数组的话会失败 所以在此进行是否是数组的判断
                        //因为expression。in参数是不定参数  理论上是可以传入数组 但是直接传入object不能判断是否为数组
                        //把他当成一个参数 而不是需要的数组参数
                        Object filterValue = ReflectionUtils.getField(field, val);
                        if (filterValue.getClass().isArray()) {
                            objects = (Object[]) filterValue;
                        } else {
                            objects[0] = filterValue;
                        }
                        predicates.add(expression.in(objects));
                        break;
                    case NEQ:
                        predicates.add(builder.notEqual(expression, ReflectionUtils.getField(field, val)));
                        break;
                    case OR:
                        List<Predicate> preList = new ArrayList<>();
                        Object obj = ReflectionUtils.getField(field, val);
                        if (obj.getClass().isArray()) {
                            objects = (Object[]) obj;
                            for (Object object : objects) {
                                Predicate pp = builder.like(expression, "%" + object + "%");
                                preList.add(pp);
                            }
                        } else {
                            preList.add(builder.like(expression, "%" + obj + "%"));
                        }
                        Predicate[] pres = preList.toArray(new Predicate[0]);
                        predicates.add(builder.or(pres));
                        break;
                    default:
                        throw new CodeException(ResponseBody.CodeEnum.DATA_NOT_FOUND, "没有" + myJpaSpecifications.operator().name() + "操作");
                }
            }

            if (CollectionUtils.notEmpty(predicates)) {
                return builder.and(predicates.toArray(new Predicate[0]));
            }
            return builder.conjunction();
        };
    }
}

```
##### 使用实例  
* 定义查询对象 xxxQuery  使用    @MyJpaSpecifications 来标识改字段如何构建查询规则 
```java
package com.ming.common.controller.query;

import com.ming.base.mvc.BaseQuery;
import com.ming.base.orm.JpaOperator;
import com.ming.base.orm.SpecificationUtils;
import com.ming.base.orm.annotatioin.MyJpaSpecifications;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

/**
 * 查询api耗时log分页条件
 *
 * @author ming
 * @date 2020-04-21 10:49:23
 */
@Data
public class ApiConsumingLogPageQuery implements BaseQuery {
    @ApiModelProperty("访问方式")
    @MyJpaSpecifications(operator = JpaOperator.EQ, entityField = "method")
    private String httpMethod;
    @ApiModelProperty("请求地址")
    @MyJpaSpecifications(operator = JpaOperator.LIKE, entityField = "uri")
    private String uri;
    @ApiModelProperty("耗时范围-最小耗时")
    @MyJpaSpecifications(operator = JpaOperator.GTE, entityField = "timeConsuming")
    private Long minTimeConsuming;
    @ApiModelProperty("耗时范围-最大耗时")
    @MyJpaSpecifications(operator = JpaOperator.LTE, entityField = "timeConsuming")
    private Long maxTimeConsuming;
    @ApiModelProperty("时间范围-开始时间")
    @MyJpaSpecifications(operator = JpaOperator.GTE, entityField = "createTime")
    private LocalDateTime startTime;
    @ApiModelProperty("时间范围-结束时间")
    @MyJpaSpecifications(operator = JpaOperator.LTE, entityField = "createTime")
    private LocalDateTime endTime;
    @Override
    public <T> Specification<T> toSpecification() {
        return SpecificationUtils.buildSpecificationByMyJpaSpecifications(ApiConsumingLogPageQuery.class, this);
    }
}

```

* 调用findAll函数
```java
package com.ming.common.service.impl;

import com.ming.common.controller.query.ApiConsumingLogPageQuery;
import com.ming.common.entity.ApiConsumingLog;
import com.ming.common.repository.ApiConsumingLogRepository;
import com.ming.common.service.ApiConsumingLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * api耗时日志 服务实现
 *
 * @author ming
 * @date 2020-04-21 11:26:34
 */
@Service
@Primary
@Slf4j
public class ApiConsumingLogServiceImpl implements ApiConsumingLogService {

    @Autowired
    private ApiConsumingLogRepository apiConsumingLogRepository;

    @Override
    public Page<ApiConsumingLog> page(Pageable pageable, ApiConsumingLogPageQuery apiConsumingLogPageQuery) {
        return apiConsumingLogRepository.findAll(apiConsumingLogPageQuery.toSpecification(), pageable);
    }
}

```

###### 注意
 repository必须继承JpaSpecificationExecutor 才能使用	Page<T> findAll(@Nullable Specification<T> spec, Pageable pageable); 类似的规则查询函数
 


#### 总结
使用Specification去访问复杂条件的对象 如果全手工构建是比较麻烦的 
使用注解 降低操作难度  会让jpa用起来更加顺畅 
 



