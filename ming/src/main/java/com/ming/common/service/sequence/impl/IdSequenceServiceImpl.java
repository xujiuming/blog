package com.ming.common.service.sequence.impl;

import com.ming.common.entity.sequence.IdSequence;
import com.ming.common.repository.sequence.IdSequenceRepository;
import com.ming.common.service.sequence.IdSequenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * id序列服务 实现
 *
 * @author ming
 * @date 2018-09-11 10:06:12
 */
@Service
public class IdSequenceServiceImpl implements IdSequenceService {
    @Autowired
    private IdSequenceRepository idSequenceRepository;

    @Override
    public IdSequence findIdAndUpdateIdLength(Class<?> clazz, Long idLength) {
        IdSequence idSequence = idSequenceRepository.findByEntityName(clazz.getName());
        if (null == idSequence) {
            //当id 序列不存在的时候 初始化一个 id序列
            idSequence = new IdSequence();
            idSequence.setEntityName(clazz.getName());
            idSequence.setSequenceLength(0L);
            idSequence.setActualLength(0L);
            idSequence.setIdAlias(clazz.getSimpleName());
            idSequence = idSequenceRepository.save(idSequence);
        }
        //更新步长
        idSequence = updateIdSequenceLength(idSequence, idLength);
        return idSequence;
    }

    /**
     * 根据旧的id序列和步长 更新 此id序列
     *
     * @author ming
     * @date 2018-09-11 10:31:03
     */
    private IdSequence updateIdSequenceLength(IdSequence oldIdSequence, Long idLength) {
        oldIdSequence.setActualLength(oldIdSequence.getSequenceLength());
        oldIdSequence.setSequenceLength(oldIdSequence.getSequenceLength() + idLength);
        return idSequenceRepository.save(oldIdSequence);
    }
}
