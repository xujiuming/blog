package com.ming.base.orm;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 构建查询对Specification进行封装 用searchfilter enum进行封装
 *
 * @author ming
 * @date 2018-09-06 17:39:11
 */
public class DynamicSpecifications {
    @SuppressWarnings("unchecked")
    public static <T> Specification<T> bySearchFilter(final Collection<SearchFilter> filters) {
        return (root, query, builder) -> {
            if (!CollectionUtils.isEmpty(filters)) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                for (SearchFilter filter : filters) {
                    String[] names = StringUtils.split(filter.getName(), ".");
                    Path expression = root.get(names[0]);
                    for (int i = 1; i < names.length; i++) {
                        expression = expression.get(names[i]);
                    }
                    //in 和or 中需要的一个中间变量 用来将filter.value放入数组
                    Object[] objects = new Object[1];
                    switch (filter.getOperator()) {
                        case EQ:
                            predicates.add(builder.equal(expression, filter.getValue()));
                            break;
                        case LIKE:
                            predicates.add(builder.like(expression, "%" + filter.getValue() + "%"));
                            break;
                        case NOTLIKE:
                            predicates.add(builder.notLike(expression, "%" + filter.getValue() + "%"));
                            break;
                        case GT:
                            predicates.add(builder.greaterThan(expression, (Comparable) filter.getValue()));
                            break;
                        case LT:
                            predicates.add(builder.lessThan(expression, (Comparable) filter.getValue()));
                            break;
                        case GTE:
                            predicates.add(builder.greaterThanOrEqualTo(expression, (Comparable) filter.getValue()));
                            break;
                        case LTE:
                            predicates.add(builder.lessThanOrEqualTo(expression, (Comparable) filter.getValue()));
                            break;
                        case IN:
                            /**因为spring data jpa 本身没有对数组进行判断 传入数组的话会失败 所以在此进行是否是数组的判断
                             * 因为expression。in参数是不定参数  理论上是可以传入数组 但是直接传入object不能判断是否为数组
                             * 把他当成一个参数 而不是需要的数组参数
                             * */
                            Object filterValue = filter.getValue();
                            if (filterValue.getClass().isArray()) {
                                objects = (Object[]) filterValue;
                            } else {
                                objects[0] = filterValue;
                            }
                            predicates.add(expression.in(objects));
                            break;
                        case NEQ:
                            predicates.add(builder.notEqual(expression, filter.getValue()));
                            break;
                        case OR:
                            List<Predicate> preList = new ArrayList<>();
                            Object obj = filter.getValue();
                            if (obj.getClass().isArray()) {
                                objects = (Object[]) obj;
                                for (Object object : objects) {
                                    Predicate pp = builder.like(expression, "%" + object + "%");
                                    preList.add(pp);
                                }
                            } else {
                                preList.add(builder.like(expression, "%" + obj + "%"));
                            }
                            Predicate[] pres = preList.toArray(new Predicate[preList.size()]);
                            predicates.add(builder.or(pres));
                            break;
                        default:
                            throw new RuntimeException("没有这个操作");
                    }
                }

                if (predicates.size() > 0) {
                    return builder.and(predicates.toArray(new Predicate[predicates.size()]));
                }
            }

            return builder.conjunction();
        };
    }
}
