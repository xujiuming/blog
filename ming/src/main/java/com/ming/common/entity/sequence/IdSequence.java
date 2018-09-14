package com.ming.common.entity.sequence;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * id 序列表
 *
 * @author ming
 * @date 2018-09-11 09:50:29
 */
@Data
@Entity
public class IdSequence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * entity名  全限定名
     */
    private String entityName;

    /**
     * id 别名
     */
    private String idAlias;
    /**
     * 序列已经申请长度
     */
    private Long sequenceLength;

    /**
     * 序列实际使用长度
     */
    private Long actualLength;
}
