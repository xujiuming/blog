package com.ming.base.orm.sequence;

import com.ming.common.entity.sequence.IdSequence;
import com.ming.common.service.sequence.IdSequenceService;
import com.ming.core.utils.SpringBeanManager;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * id 生成器
 *
 * @author ming
 * @date 2018-09-11 14:25:59
 */
public class IdFactory {

    /**
     * id 序列步长
     * 增大步长 性能变高 但是耗费内存 并且容易浪费  序列
     */
    private static final Long SEQUENCE_SIZE = 10L;
    /**
     * Map<entityName,IdInfo>
     */
    private static volatile Map<String, IdInfo> ID_INFO_MAP = new ConcurrentHashMap<>();


    /**
     * new id
     * 本来是准备 模仿 concurrentHashMap 进行并发优化的  仔细想了想 这边 这坨代码 还是得都上锁 不然 锁拆分的太细 反而辣鸡
     *
     * @param clazz
     * @return String
     * @author ming
     * @date 2018-09-11 14:25:41
     */
    public synchronized static String newStringId(Class<?> clazz) {
        IdInfo idInfo = ID_INFO_MAP.get(clazz.getName());
        //当内存中没有 id序列的时候
        if (null == idInfo) {
            idInfo = updateIdInfo(clazz);
        }
        //当id序列步长 大于等于数据库步长的时候 更新数据库
        Long actualLength = Objects.requireNonNull(idInfo).getActualLength() + 1;
        if (actualLength >= idInfo.getSequenceLength()) {
            idInfo = updateIdInfo(clazz);
        }
        idInfo.setActualLength(actualLength);
        ID_INFO_MAP.put(clazz.getName(), idInfo);
        return idInfo.getIdAlias() + actualLength;
    }


    private static IdInfo updateIdInfo(Class<?> clazz) {
        //更新数据库长度
        IdSequence newIdSequence = SpringBeanManager.getBean(IdSequenceService.class).findIdAndUpdateIdLength(clazz, SEQUENCE_SIZE);
        IdInfo idInfo = new IdInfo();
        idInfo.setEntityName(clazz.getName());
        idInfo.setIdAlias(newIdSequence.getIdAlias());
        idInfo.setActualLength(newIdSequence.getActualLength());
        idInfo.setSequenceLength(newIdSequence.getSequenceLength());
        ID_INFO_MAP.put(clazz.getName(), idInfo);
        return idInfo;
    }
}