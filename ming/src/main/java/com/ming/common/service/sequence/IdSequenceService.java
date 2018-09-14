package com.ming.common.service.sequence;

import com.ming.common.entity.sequence.IdSequence;

/**
 * @author ming
 * @date 2018-09-11 10:00:30
 */
public interface IdSequenceService {
    /**
     * 获取当前最大的id 并且根据预设步长更新 此id序列
     *
     * @param entity
     * @param idLength
     * @return IdSequence
     * @author ming
     * @date 2018-09-11 10:03:58
     */
    IdSequence findIdAndUpdateIdLength(Class<?> entity, Long idLength);

}
