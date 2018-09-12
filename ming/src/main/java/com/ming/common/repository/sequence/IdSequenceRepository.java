package com.ming.common.repository.sequence;

import com.ming.base.orm.jpa.BaseRepository;
import com.ming.common.entity.sequence.IdSequence;
import org.springframework.stereotype.Repository;

/**
 * id 序列 仓库
 *
 * @author ming
 * @date 2018-09-11 09:59:03
 */
@Repository
public interface IdSequenceRepository extends BaseRepository<IdSequence, Long> {

    /**
     * 根据 实体名称查询 id序列
     *
     * @param entityName
     * @return IdSequence
     * @author ming
     * @date 2018-09-11 10:20:57
     */
    IdSequence findByEntityName(String entityName);
}
