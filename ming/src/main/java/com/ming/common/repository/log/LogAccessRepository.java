package com.ming.common.repository.log;

import com.ming.base.orm.jpa.BaseRepository;
import com.ming.common.entity.log.LogAccess;
import org.springframework.stereotype.Repository;

/**
 * 访问日志 仓库
 *
 * @author ming
 * @date 2018-09-07 14:43:34
 */
@Repository
public interface LogAccessRepository extends BaseRepository<LogAccess, Long> {
}
