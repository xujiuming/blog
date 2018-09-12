package com.ming.base.orm.jpa;

import com.ming.base.orm.InLongId;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 基础仓库
 *
 * @author ming
 * @date 2018-09-04 16:06:42
 */
public class BaseRepositoryImpl<T extends InLongId, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements BaseRepository<T, ID> {
    private final EntityManager entityManager;

    public BaseRepositoryImpl(Class<T> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
        this.entityManager = entityManager;
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<T> findListByNativeSql(String sql, Class<T> clazz) {
        return entityManager.createNativeQuery(sql, clazz).getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends InLongId> List<T> findListById(Long id, T t) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<? extends InLongId> query = criteriaBuilder.createQuery(t.getClass());
        Root<? extends InLongId> root = query.from(t.getClass());
        Predicate predicate = criteriaBuilder.equal(root.get(String.valueOf(t.getId())), id);
        query.where(predicate);
        return (List<T>) entityManager.createQuery(query).getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends InLongId> List<T> findListByIds(Collection<Long> ids, T t) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<? extends InLongId> query = criteriaBuilder.createQuery(t.getClass());
        Root<? extends InLongId> root = query.from(t.getClass());
        Predicate predicate = criteriaBuilder.in(root.get(String.valueOf(t.getId())));
        query.where(predicate);
        return (List<T>) entityManager.createQuery(query).getResultList();
    }
}
