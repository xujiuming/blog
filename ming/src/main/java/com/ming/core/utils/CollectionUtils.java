package com.ming.core.utils;

import java.util.Collection;
import java.util.Map;

/**
 * 针对常用的集合方法封装
 *
 * @author ming
 * @date 2017-08-15 11点
 */
public class CollectionUtils {
    /**
     * @param tCollection
     * @return boolean
     * @author ming
     * @date 2017-08-15 11点
     */
    public static <T> boolean isEmpty(Collection<T> tCollection) {
        return org.springframework.util.CollectionUtils.isEmpty(tCollection);
    }


    /**
     * @param tdMap
     * @return boolean
     * @author ming
     * @date 2017-08-15 11点
     */
    public static <T, V> boolean isEmpty(Map<T, V> tdMap) {
        return org.springframework.util.CollectionUtils.isEmpty(tdMap);
    }


    /**
     * @param tCollection
     * @return boolean
     * @author ming
     * @date 2017-08-15 11点
     */
    public static <T> boolean notEmpty(Collection<T> tCollection) {
        return !isEmpty(tCollection);
    }


    /**
     * @param tdMap
     * @return boolean
     * @author ming
     * @date 2017-08-15 11点
     */
    public static <T, V> boolean notEmpty(Map<T, V> tdMap) {
        return !isEmpty(tdMap);
    }

}
