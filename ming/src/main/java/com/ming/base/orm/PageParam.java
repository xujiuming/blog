package com.ming.base.orm;

import com.google.common.collect.Maps;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collection;
import java.util.Map;

/**
 * 根据data jpa 相关使用规则 封装一个pageParam对象
 *
 * @author ming
 * @date 2017-11-06 16:29
 */
public class PageParam {
    /**
     * 默认值
     */
    private static final Integer DEFAULT_NUMBER = 1;
    private static final Integer DEFAULT_SIZE = 15;
    private static final Sort DEFAULT_SORT = new Sort(Sort.Direction.DESC, "id");
    /**
     * 第几页
     */
    private Integer number;
    /**
     * 每页大小
     */
    private Integer size;
    /**
     * 排序规则
     */
    private Sort sort;
    /**
     * 过滤参数
     */
    private Map<String, Object> filter;
    /**
     * 查询参数
     */
    private Collection<SearchFilter> searchFilters;

    /**
     * 获取pageable
     *
     * @return Pageable
     * @author ming
     * @date 2017-11-06 16:28
     */
    public Pageable getPageable() {
        return PageRequest.of(getNumber(), getSize(), getSort());
    }

    /**
     * 获取 Map<String,SearchFilter>
     *
     * @return Map
     * @author ming
     * @date 2017-11-06 16:34
     */
    public Map<String, SearchFilter> getSearchFilterMap() {
        if (filter == null) {
            return Maps.newHashMap();
        }
        return SearchFilter.parse(filter);
    }

    public Collection<SearchFilter> getSearchFilters() {
        return getSearchFilterMap().values();
    }

    public Integer getNumber() {
        if (number == null) {
            number = DEFAULT_NUMBER;
        }
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getSize() {
        if (size == null) {
            size = DEFAULT_SIZE;
        }
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Sort getSort() {
        if (sort == null) {
            sort = DEFAULT_SORT;
        }
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public Map<String, Object> getFilter() {
        return filter;
    }

    public void setFilter(Map<String, Object> filter) {

        this.filter = filter;
    }


}
