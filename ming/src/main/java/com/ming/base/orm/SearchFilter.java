package com.ming.base.orm;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Map.Entry;

/**
 * 查询过滤参数封装
 *
 * @author ming
 * @date 2017-11-06 18:14
 */
public class SearchFilter {

    private String name;
    private Object value;
    private Operator operator;

    public SearchFilter(String name, Operator operator, Object value) {
        this.name = name;
        this.value = value;
        this.operator = operator;
    }

    /**
     * searchParams中key的格式为OPERATOR_FIELDNAME
     */
    public static Map<String, SearchFilter> parse(Map<String, Object> searchParams) {
        Map<String, SearchFilter> filters = Maps.newHashMap();

        for (Entry<String, Object> entry : searchParams.entrySet()) {
            // 过滤掉空值
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String && StringUtils.isBlank((String) value)) {
                continue;
            }

            //todo 参数规则 拆分operator与filedAttribute 使用中划线分割 参数类型和参数具体的key例如  EQ-name  精确匹配name字段
            String[] names = StringUtils.split(key, "-");
            if (names.length != 2) {
                throw new IllegalArgumentException(key + " is not a valid search filter name");
            }
            String filedName = names[1];
            Operator operator = Operator.valueOf(names[0]);

            // 创建searchFilter
            SearchFilter filter = new SearchFilter(filedName, operator, value);
            filters.put(key, filter);
        }

        return filters;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public Operator getOperator() {
        return operator;
    }

    public enum Operator {
        /**
         * EQ:精确比较
         * LIKE：模糊查询
         * NOTLIKE:反向模糊查询
         * GT:大于
         * LT:小于
         * GTE:大于或等于
         * LTE:小于或等于p-p
         * IN:在这中间
         * NEQ：不等于
         * OR: 或
         */
        EQ, LIKE, NOTLIKE, GT, LT, GTE, LTE, IN, NEQ, OR
    }
}
