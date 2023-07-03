---
title: spring boot data jpa 扩充JpsRepository
categories: 笔记
tags:
  - spring
abbrlink: 19f1b366
date: 2017-11-11 00:00:00
---
#####jpa扩充jpaRepository接口的方法 
1:定义一个接口 继承于 JpaRepository
2:定义接口实现类 
3:定义 接口加载工厂方法
4:repository 继承自定义接口
5:启动类使用 自定义加载工厂
####1:定义扩充方法接口
```
/**
 * 基于jpa的默认jpaRepository实现自己的repository接口
 * NoRepositoryBean 不会创建接口的实例  必须要加这个注解
 *
 * @author ming
 * @date 2017-08-28 11点
 */
@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

    /**
     * 使用原生sql 查询 list列表
     *
     * @author ming
     * @date 2017-08-29 16点
     */
    List<T> findListByNativeSql(String sql, Class<T> clzss);
}
```
#### 2:定义这个接口的实现类
```

public class BaseRepositoryImpl<T, TD extends Serializable> extends SimpleJpaRepository<T, TD> implements BaseRepository<T, TD> {

    private final EntityManager entityManager; //父类没有不带参数的构造方法，这里手动构造父类

    public BaseRepositoryImpl(Class<T> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
        this.entityManager = entityManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> findListByNativeSql(String sql, Class<T> clzss) {
        return entityManager.createNativeQuery(sql, clzss).getResultList();
    }
}
```
#### 3: 定义加载自定义repository接口工厂
```

public class BaseRepositoryFactoryBean<R extends JpaRepository<T, ID>, T, ID extends Serializable> extends JpaRepositoryFactoryBean<R, T, ID> {

    public BaseRepositoryFactoryBean(Class<? extends R> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {
        return new BaseRepositoryFactory(entityManager);
    }

    //创建一个内部类，该类不用在外部访问
    private static class BaseRepositoryFactory<T, ID extends Serializable>
            extends JpaRepositoryFactory {

        private final EntityManager em;

        public BaseRepositoryFactory(EntityManager em) {
            super(em);
            this.em = em;
        }

        //设置具体的实现类是BaseRepositoryImpl
        @SuppressWarnings("unchecked")
        @Override
        protected Object getTargetRepository(RepositoryInformation information) {
            return new BaseRepositoryImpl<T, ID>((Class<T>) information.getDomainType(), em);
        }

        //设置具体的实现类的class
        @Override
        protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
            return BaseRepositoryImpl.class;
        }
    }
}
```
#### 4:repository继承自定义接口
```
@Repository
public interface UserRepository extends BaseRepository<User, Long> {
}
```
#### 5:启动类使用自定义加载工厂
```
/**
 * 启动类 要位于最顶层包  他只会扫描 同级包和子包
 *
 * @author ming
 * @date 2017-06-17
 */
@SpringBootApplication(scanBasePackages = "com.ming", excludeName = {"classpath*:application.yml",
        "classpath*:application-aliyun.yml"})
// jpa使用自定义加载工厂
@EnableJpaRepositories(repositoryFactoryBeanClass = BaseRepositoryFactoryBean.class)
public class Start {

    public static void main(String[] args) {
        SpringApplication.run(Start.class, args);
    }
}
```
#### 总结:和之前spring中使用jpa差不多套路  主要是自定义一个加载工厂 有点变化
