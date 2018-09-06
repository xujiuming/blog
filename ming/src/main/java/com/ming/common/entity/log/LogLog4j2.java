package com.ming.common.entity.log;

import com.ming.base.orm.InId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * 日志 类
 *
 * @author ming
 * @date 2018-08-28 09:27:15
 * @see org.apache.logging.log4j.core.LogEvent
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class LogLog4j2 extends InId {

    /**
     * 日志记录类
     */
    private String loggerFqcn;
    /**
     * 日志等级
     */
    private String level;
    /**
     * 日志名称
     */
    private String loggerName;
    /**
     * 日志信息
     */
    @Column(length = 1000)
    private String messageFormat;
    @Column(length = 1000)
    private String messageFormattedMessage;


    /**
     * 执行时间
     */
    private Long timeMillis;
    /**
     * 线程名字
     */
    private String threadName;
    /**
     * 线程id
     */
    private Long threadId;
    /**
     * 线程优先级
     */
    private Integer threadPriority;
    /**
     * 产生日志的数据来源
     */
    private String sourceMethodName;
    private String sourceFileName;
    private Integer sourceLineNumber;
    private String sourceClassName;
    private Boolean sourceNativeMethod;


    /**
     * 日志 event 记录
     */
    @Column(length = 10000)
    private String logEvent;

}
