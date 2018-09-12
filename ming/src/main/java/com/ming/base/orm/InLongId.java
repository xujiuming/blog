package com.ming.base.orm;


import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.Instant;

/**
 * 继承映射父类  所有entity 继承这个类
 *
 * @author ming
 * @date 2017-08-28 11点
 */
@MappedSuperclass
@Data
public class InLongId implements Serializable {
    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private Instant createInstant;
    private Instant lastUpdateInstant = Instant.now();
    private Boolean isDeleted = Boolean.FALSE;


}
