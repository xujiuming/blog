package com.ming.base.orm.jpa;

import com.ming.base.orm.InId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 基于jpa的默认jpaRepository实现自己的repository接口
 * NoRepositoryBean 不会创建接口的实例
 *
 * @author ming
 * @date 2017-08-28 11点
 */
@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

    /**
     * 使用原生sql 查询 list列表
     *
     * @param sql
     * @param clzss
     * @return List<T>
     * @author ming
     * @date 2017-08-29 16点
     */
    List<T> findListByNativeSql(String sql, Class<T> clzss);

    /**
     * 根据ids查询集合
     *
     * @param ids
     * @param t
     * @return List<T>
     * @author ming
     * @date 2018-09-06 17:37:39
     */
    <T extends InId> List<T> findListByIds(Collection<Long> ids, T t);


    /**
     * 根据id查询集合
     *
     * @param id
     * @param t
     * @return List<T>
     * @author ming
     * @date 2018-09-06 17:37:58
     */
    @SuppressWarnings("unchecked")
    <T extends InId> List<T> findListById(Long id, T t);
}
