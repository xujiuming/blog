package com.ming.base.orm;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.Instant;

/**
 * string 格式的id
 *
 * @author ming
 * @date 2018-09-12 13:09:41
 */
@MappedSuperclass
@Data
public class InStringId implements Serializable {
    private static final long serialVersionUID = 1L;


    @Id
    private String id;


    private Instant createInstant;
    private Instant lastUpdateInstant = Instant.now();
    private Boolean isDeleted = Boolean.FALSE;


}