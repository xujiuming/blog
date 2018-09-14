package com.ming.common.repository.log;

import com.ming.base.orm.jpa.BaseRepository;
import com.ming.common.entity.log.LogLog4j2;
import org.springframework.stereotype.Repository;

/**
 * log4j2 log 仓库层
 *
 * @author ming
 * @date 2018-09-05 11:11:56
 */
@Repository
public interface Log4j2LogRepository extends BaseRepository<LogLog4j2, Long> {
}
