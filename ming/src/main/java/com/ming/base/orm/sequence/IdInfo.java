package com.ming.base.orm.sequence;

import lombok.Data;
import org.springframework.lang.NonNull;

/**
 * id 信息
 *
 * @author ming
 * @date 2018-09-11 15:01:35
 */
@Data
public class IdInfo {

    /**
     * 实体名称
     */
    @NonNull
    private String entityName;
    /**
     * 数据库存储id长度
     */
    @NonNull
    private Long sequenceLength;
    /**
     * id 别名
     */
    @NonNull
    private String idAlias;
    /**
     * 实际使用长度
     */
    @NonNull
    private Long actualLength;

    public IdInfo() {
    }

}
