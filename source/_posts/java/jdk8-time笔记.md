---
title: jdk8-time笔记
comments: true
categories: 笔记
tags:
  - jdk8
  - time
abbrlink: 9c26946f
date: 2023-03-06 18:47:13
---
#### 前言
在jdk8之前 如果要使用date 的类型 就需要把java.util.Date 之类的类组合起来使用   
特别是在常用的时间加减、获取特定时间、格式化的时候 都必须要java.util.Calendar 、java.util.TimeZone、java.text.SimpleDateFormat这些类进行封装 或者需要引入第三方工具包来简化操作

在jdk8中 time是以jodaTime这个工具包为模板 加入到jdk中的 对于时间的加减、获取特定时间、格式化、变更时区、获取更加精确的时间等等操作变的更加简单  而且是不可变并且线程安全的方法 

#### 核心类和核心方法说明
##### 核心类
###### 核心时间对象类
|名称|功能|备注|
|:--|:--|:---|
|Instant|获取从1970年开始的时间点|类似之前的java.util.Date、通过这个类可以获取到非常精确的时间|
|LocalDate|本地年月日Date对象|获取当前年月日信息的对象|
|LocalTime|本地时分秒Date对象|获取当前时分秒信息的对象|
|LocalDateTime|本地年月日时分秒Date对象|获取当前年月日时分秒信息的对象|
|OffsetTime|获取时分秒Date对象并且带上偏移时间信息||
|OffsetDateTime|获取带年月日时分秒的Date对象并且带上偏移时间信息||
|ZonedDateTime|获取带时区信息的年月日时分秒对象 并且带上时区信息||
|Year|年对象||
|YearMonth|年月对象||
|MonthDay|月日对象||
|Period|时间间隔区间对象|表示以年、月、日衡量的时长|
|Duration|时间间隔区间对象|表示以秒和纳秒为基准的时长|

###### 核心枚举类  

* ChronoUnit 时间单位

|名称|含义|备注|
|:--|:--|:---|
|NANOS|纳秒||
|MICROS|微秒||
|MILLIS|毫秒||
|SECONDS|秒||
|MINUTES|分||
|HOURS|小时||
|HALF_DAYS|半天|12个小时|
|DAYS|一天|24个小时|
|WEEKS|一周|7天|
|MONTHS|一个月||
|YEARS|一年||
|DECADES|十年||
|CENTURIES|百年||
|MILLENNIA|千年||
|ERAS|十亿年||
|FOREVER|永远|Long.MAX_VALUE|


* DateTimeFormatter 时间格式化格式

|名称|表达式|备注|
|:--|:----|:--|
|ISO_LOCAL_DATE|yyyy-MM-dd||
|ISO_OFFSET_DATE| yyyy-MM-dd+offset||
|ISO_DATE| 'yyyy-MM-dd' or 'yyyy-MM-dd+offset'||
|ISO_LOCAL_TIME |HH:mm or HH:mm:ss||
|ISO_OFFSET_TIME| HH:mm+offset or HH:mm:ss+offset||
|ISO_TIME |HH:mm or HH:mm:ss or HH:mm:ss+offset||
|ISO_LOCAL_DATE_TIME| yyyy-MM-ddTHH:mm:ss||
|ISO_OFFSET_DATE_TIME| yyyy-MM-ddTHH:mm:ss+offset||
|ISO_ZONED_DATE_TIME |yyyy-MM-ddTHH:mm:ss+offset\[zone]||
|ISO_ORDINAL_DATE |yyyy-days||
|ISO_WEEK_DATE |yyyy-week-days||
|ISO_INSTANT |yyyy-MM-ddTHH:mm:ssZ||
|BASIC_ISO_DATE |yyyyMMdd||
|RFC_1123_DATE_TIME |'Tue, 3 Jun 2008 11:05:30 GMT'||




##### 核心方法
###### 通用核心方法
|名称|功能|备注|
|:--|:--|:--|
|of|根据传入的数值转换成相应的时间对象||
|parse|根据传入的字符串格式和DateTimeFormatter枚举转换成相应的时间对象||
|get|根据时间对象获取相应的时间属性||
|is|判断时间的某些属性是否符合方法的意义|使用isBefore或者isAfter来判断时间的前后|
|with|获取一些特殊时间对象|例如这个月第一天之类的|
|plus|时间相加|可以根据不同的时间单位进行相加|
|minus|时间相减|可以根据不同的时间单位进行相减|
|to|时间类型转换成其他时间类型|例如LocalDateTime to 成 LocalTime|
|at|转换成带偏移量、时区之类的时间对象操作||
|format|格式化时间类型|根据DateTimeFormatter对象来转换|

###### 特殊方法
* 判断是否是闰年
LocalDate#isLeapYear();

#### 实际案例
```
package com.ming.admin;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Date;

/**
 * 时间示例 测试用例
 * 该包的API提供了大量相关的方法，这些方法一般有一致的方法前缀：
 * <p>
 * of：静态工厂方法。
 * <p>
 * parse：静态工厂方法，关注于解析。
 * <p>
 * get：获取某些东西的值。
 * <p>
 * is：检查某些东西的是否是true。
 * <p>
 * with：不可变的setter等价物。
 * <p>
 * plus：加一些量到某个对象。
 * <p>
 * minus：从某个对象减去一些量。
 * <p>
 * to：转换到另一个类型。
 * <p>
 * at：把这个对象与另一个对象组合起来，例如： date.atTime(time)。
 * <p>
 * format：按照合适的格式 格式化成相应的格式的字符串
 * <p>
 *
 * @author ming
 * @date 2023-03-06 15:19:56
 */
public class DateExampleTest {

    /**
     * 基础用法
     *
     * @author ming
     * @date 2023-03-06 18:49:09
     */
    @Test
    public void basicUsage() {
        LocalDateTime localDateTime = LocalDateTime.now();
        System.out.println("获取年月日:" + localDateTime.toLocalDate());
        System.out.println("获取时分秒:" + localDateTime.toLocalTime());
        System.out.println("获取当天最早时间:" + LocalDateTime.of(LocalDate.now(), LocalTime.MIN));
        System.out.println("获取当天最晚时间:" + LocalDateTime.of(LocalDate.now(), LocalTime.MAX));
        //格式化
        System.out.println("格式化年月日时分秒:" + localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        //加减时间
        System.out.println("加1(TemporalAmount及其子类):" + localDateTime.plus(Duration.ofDays(1)));
        System.out.println("加1(ChronoUnit):" + localDateTime.plus(1, ChronoUnit.DAYS));
        System.out.println("加1纳秒:" + localDateTime.plusNanos(1));
        System.out.println("加1秒:" + localDateTime.plusSeconds(1));
        System.out.println("加1分:" + localDateTime.plusMinutes(1));
        System.out.println("加1小时:" + localDateTime.plusHours(1));
        System.out.println("加1天:" + localDateTime.plusDays(1));
        System.out.println("加1周:" + localDateTime.plusWeeks(1));
        System.out.println("加1月:" + localDateTime.plusMonths(1));
        System.out.println("加1年:" + localDateTime.plusYears(1));
        System.out.println("减1(TemporalAmount及其子类):" + localDateTime.minus(Duration.ofDays(1)));
        System.out.println("减1(ChronoUnit):" + localDateTime.minus(1, ChronoUnit.DAYS));
        System.out.println("减1纳秒:" + localDateTime.minusNanos(1));
        System.out.println("减1秒:" + localDateTime.minusSeconds(1));
        System.out.println("减1分:" + localDateTime.minusMinutes(1));
        System.out.println("减1小时:" + localDateTime.minusHours(1));
        System.out.println("减1天:" + localDateTime.minusDays(1));
        System.out.println("减1周:" + localDateTime.minusWeeks(1));
        System.out.println("减1月:" + localDateTime.minusMonths(1));
        System.out.println("减1年:" + localDateTime.minusYears(1));

        //修改部分时间
        System.out.println("修改为第1纳秒:" + localDateTime.withNano(1));
        System.out.println("修改为第1秒:" + localDateTime.withSecond(1));
        System.out.println("修改为第1分钟:" + localDateTime.withMinute(1));
        System.out.println("修改为第1小时:" + localDateTime.withHour(1));
        System.out.println("修改为第1月:" + localDateTime.withMonth(1));
        System.out.println("修改为第1年:" + localDateTime.withYear(1));
        System.out.println("修改为当周第1天:" + localDateTime.with(WeekFields.ISO.dayOfWeek(), 1));
        System.out.println("修改为当月第1天:" + localDateTime.withDayOfMonth(1));
        System.out.println("修改为当年第1天:" + localDateTime.withDayOfYear(1));

        //判断时间先后顺序
        LocalDateTime localDateTime1 = LocalDateTime.now();
        LocalDateTime localDateTime2 = LocalDateTime.now().plusDays(1);
        System.out.println("localDateTime1是否在localDateTime2之前:" + localDateTime1.isBefore(localDateTime2));
        System.out.println("localDateTime1是否在localDateTime2之后:" + localDateTime1.isAfter(localDateTime2));
        System.out.println("localDateTime1和localDateTime2是否相等:" + localDateTime1.isEqual(localDateTime2));
        System.out.println("localDateTime1和localDateTime2是否相等:" + localDateTime1.equals(localDateTime2));
    }


    /**
     * 时间戳和LocalDateTime 互转
     *
     * @author ming
     * @date 2023-03-06 15:27:25
     */
    @Test
    public void timeMillisConvertEachOtherLocalDateTime() {
        ZoneOffset zoneOffset = ZoneOffset.of("+8");
        LocalDateTime localDateTime = LocalDateTime.now();
        System.out.println("当前时间:" + localDateTime);
        long timeMillis = localDateTime.toInstant(zoneOffset).toEpochMilli();
        System.out.println("转换为时间戳:" + timeMillis);
        System.out.println("时间戳再次转换为时间" + LocalDateTime.ofEpochSecond(timeMillis / 1000, 0, zoneOffset));
    }

    /**
     * date和LocalDateTime 互转
     *
     * @author ming
     * @date 2023-03-06 15:27:25
     */
    @Test
    public void dateConvertEachOtherLocalDateTime() {
        LocalDateTime localDateTime = LocalDateTime.now();
        //localDateTime转instant 然后Date#from
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        System.out.println("转换date:" + date);
        //date转instant 然后LocalDateTime#ofInstant
        System.out.println("转换localDateTime:" + LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
    }

    /**
     * timestamp和LocalDateTime 互转
     *
     * @author ming
     * @date 2023-03-06 15:27:25
     */
    @Test
    public void timestampConvertEachOtherLocalDateTime() {
        LocalDateTime localDateTime = LocalDateTime.now();
        Timestamp timestamp = Timestamp.valueOf(localDateTime);
        System.out.println("转换为timestamp:" + timestamp);
        System.out.println("转换为localDateTime" + timestamp.toLocalDateTime());
    }


    /**
     * 获取周的第N天
     *
     * @author ming
     * @date 2023-03-06 17:49:54
     */
    @Test
    public void getWeekDay() {
        LocalDate localDate = LocalDate.now();
        System.out.println("周一:" + localDate.with(WeekFields.ISO.dayOfWeek(), DayOfWeek.MONDAY.getValue()));
        System.out.println("周二:" + localDate.with(WeekFields.ISO.dayOfWeek(), DayOfWeek.TUESDAY.getValue()));
        System.out.println("周三:" + localDate.with(WeekFields.ISO.dayOfWeek(), DayOfWeek.WEDNESDAY.getValue()));
        System.out.println("周四:" + localDate.with(WeekFields.ISO.dayOfWeek(), DayOfWeek.THURSDAY.getValue()));
        System.out.println("周五:" + localDate.with(WeekFields.ISO.dayOfWeek(), DayOfWeek.FRIDAY.getValue()));
        System.out.println("周六:" + localDate.with(WeekFields.ISO.dayOfWeek(), DayOfWeek.SATURDAY.getValue()));
        System.out.println("周日:" + localDate.with(WeekFields.ISO.dayOfWeek(), DayOfWeek.SUNDAY.getValue()));
        //获取上周
        LocalDate lastWeekLocalDate = localDate.minusWeeks(1);
        System.out.println("上周一:" + lastWeekLocalDate.with(WeekFields.ISO.dayOfWeek(), DayOfWeek.MONDAY.getValue()));
        System.out.println("上周二:" + lastWeekLocalDate.with(WeekFields.ISO.dayOfWeek(), DayOfWeek.TUESDAY.getValue()));
        System.out.println("上周三:" + lastWeekLocalDate.with(WeekFields.ISO.dayOfWeek(), DayOfWeek.WEDNESDAY.getValue()));
        System.out.println("上周四:" + lastWeekLocalDate.with(WeekFields.ISO.dayOfWeek(), DayOfWeek.THURSDAY.getValue()));
        System.out.println("上周五:" + lastWeekLocalDate.with(WeekFields.ISO.dayOfWeek(), DayOfWeek.FRIDAY.getValue()));
        System.out.println("上周六:" + lastWeekLocalDate.with(WeekFields.ISO.dayOfWeek(), DayOfWeek.SATURDAY.getValue()));
        System.out.println("上周日:" + lastWeekLocalDate.with(WeekFields.ISO.dayOfWeek(), DayOfWeek.SUNDAY.getValue()));
        //获取下周
        LocalDate nextWeekLocalDate = localDate.plusWeeks(1);
        System.out.println("下周一:" + nextWeekLocalDate.with(WeekFields.ISO.dayOfWeek(), DayOfWeek.MONDAY.getValue()));
        System.out.println("下周二:" + nextWeekLocalDate.with(WeekFields.ISO.dayOfWeek(), DayOfWeek.TUESDAY.getValue()));
        System.out.println("下周三:" + nextWeekLocalDate.with(WeekFields.ISO.dayOfWeek(), DayOfWeek.WEDNESDAY.getValue()));
        System.out.println("下周四:" + nextWeekLocalDate.with(WeekFields.ISO.dayOfWeek(), DayOfWeek.THURSDAY.getValue()));
        System.out.println("下周五:" + nextWeekLocalDate.with(WeekFields.ISO.dayOfWeek(), DayOfWeek.FRIDAY.getValue()));
        System.out.println("下周六:" + nextWeekLocalDate.with(WeekFields.ISO.dayOfWeek(), DayOfWeek.SATURDAY.getValue()));
        System.out.println("下周日:" + nextWeekLocalDate.with(WeekFields.ISO.dayOfWeek(), DayOfWeek.SUNDAY.getValue()));
    }

    /**
     * 获取月的第N天
     *
     * @author ming
     * @date 2023-03-06 17:49:54
     */
    @Test
    public void getMonthDay() {
        LocalDate localDate = LocalDate.now();
        System.out.println("当月第一天:" + localDate.with(TemporalAdjusters.firstDayOfMonth()));
        System.out.println("当月最后一天:" + localDate.with(TemporalAdjusters.lastDayOfMonth()));
        System.out.println("当月第十天:" + localDate.withDayOfMonth(10));
        //上月
        LocalDate lastMonthLocalDate = localDate.minusMonths(1);
        System.out.println("上月第一天:" + lastMonthLocalDate.with(TemporalAdjusters.firstDayOfMonth()));
        System.out.println("上月最后一天:" + lastMonthLocalDate.with(TemporalAdjusters.lastDayOfMonth()));
        //下月
        LocalDate nextMonthLocalDate = localDate.plusMonths(1);
        System.out.println("下月第一天(方法1):" + localDate.with(TemporalAdjusters.firstDayOfNextMonth()));
        System.out.println("下月第一天(方法2):" + nextMonthLocalDate.with(TemporalAdjusters.firstDayOfMonth()));
        System.out.println("下月最后一天:" + nextMonthLocalDate.with(TemporalAdjusters.lastDayOfMonth()));
    }

    /**
     * 获取年的第N天
     *
     * @author ming
     * @date 2023-03-06 17:49:54
     */
    @Test
    public void getYearDay() {
        LocalDate localDate = LocalDate.now();
        System.out.println("当年第一天" + localDate.with(TemporalAdjusters.firstDayOfYear()));
        System.out.println("当年最后一天" + localDate.with(TemporalAdjusters.lastDayOfYear()));
        System.out.println("当年第十天:" + localDate.withDayOfYear(10));
        //上年
        LocalDate lastYearLocalDate = localDate.minusYears(1);
        System.out.println("上年第一天" + lastYearLocalDate.with(TemporalAdjusters.firstDayOfYear()));
        System.out.println("上年最后一天" + lastYearLocalDate.with(TemporalAdjusters.lastDayOfYear()));
        //下年
        LocalDate nextYearLocalDate = localDate.plusYears(1);
        System.out.println("下年第一天" + nextYearLocalDate.with(TemporalAdjusters.firstDayOfYear()));
        System.out.println("下年最后一天" + nextYearLocalDate.with(TemporalAdjusters.lastDayOfYear()));
    }

    /**
     * 获取当前时间
     *
     * @author ming
     * @date 2018-06-30 15:31:33
     */
    @Test
    public void testNewDate() {
        System.out.println("获取带纳秒的时间:" + Instant.now());
        System.out.println("获取年月日:" + LocalDate.now());
        System.out.println("获取时分秒:" + LocalTime.now());
        System.out.println("获取年月日时分秒:" + LocalDateTime.now());
        System.out.println("获取时分秒带时区:" + OffsetTime.now());
        System.out.println("获取年月日时分秒带时区:" + OffsetDateTime.now());
        System.out.println("获取时分秒带详细时区信息:" + ZonedDateTime.now());
        System.out.println("获取年:" + Year.now());
        System.out.println("获取年月:" + YearMonth.now());
        System.out.println("获取月日:" + MonthDay.now());
    }

    /**
     * 直接根据参数转换成时间类型
     *
     * @author ming
     * @date 2018-06-30 15:32:59
     */
    @Test
    public void testOf() {
        System.out.println("根据秒数获取时间点:" + Instant.ofEpochSecond(1000));
        System.out.println("根据年月日获取年月日时间对象:" + LocalDate.of(2018, 11, 11));
        System.out.println("根据时分秒获取时分秒对象:" + LocalTime.of(11, 11, 11));
        System.out.println("根据年月日时分秒获取年月日时分秒时间对象:" + LocalDateTime.of(2018, 11, 11, 11, 11, 11));
        System.out.println("根据时分秒获取时分秒带时区对象:" + OffsetTime.of(11, 11, 11, 1, ZoneOffset.UTC));
        System.out.println("根据年月日时分秒获取年月日时分秒时间带时区对象:" + OffsetDateTime.of(2018, 11, 11, 11, 11, 11, 11, ZoneOffset.UTC));
        System.out.println("根据年月日时分秒获取年月日时分秒时间带时区对象:" + ZonedDateTime.of(2018, 11, 11, 11, 11, 11, 11, ZoneId.systemDefault()));
        System.out.println("根据年数转会成年对象:" + Year.of(2018));
        System.out.println("根据年月数获取年月对象:" + YearMonth.of(2018, 11));
        System.out.println("根据月日数获取月日对象" + MonthDay.of(11, 11));
    }


    /**
     * 设置时区
     *
     * @author ming
     * @date 2018-07-05 16:53:29
     */
    @Test
    public void testAt() {
        System.out.println(Instant.now().atOffset(ZoneOffset.UTC));
        System.out.println(Instant.now().atZone(ZoneId.systemDefault()));
    }

    /**
     * 判断是否是闰年
     *
     * @author ming
     * @date 2018-07-05 17:09:34
     */
    @Test
    public void testLeapYear() {
        System.out.println(LocalDateTime.now().toLocalDate().isLeapYear());
    }

    /**
     * 计算两个时间之间的差值
     *
     * @author ming
     * @date 2018-07-05 17:12:37
     */
    @Test
    public void testPeriod() {
        LocalDateTime localDateTime = LocalDateTime.of(2018, 1, 11, 11, 11, 11);
        Period period = Period.between(localDateTime.toLocalDate(), LocalDate.now());
        System.out.println("间隔时间:" + period.getYears() + "年" + period.getMonths() + "个月" + period.getDays() + "天");
    }

    /**
     * 根据formatter枚举格式化时间
     *
     * @author ming
     * @date 2018-07-05 17:32:32
     * @see DateTimeFormatter#ofPattern(String) 自定义格式化格式
     * @see DateTimeFormatter#ISO_LOCAL_DATE yyyy-MM-dd
     * @see DateTimeFormatter#ISO_OFFSET_DATE yyyy-MM-dd+offset
     * @see DateTimeFormatter#ISO_DATE 'yyyy-MM-dd' or 'yyyy-MM-dd+offset'.
     * @see DateTimeFormatter#ISO_LOCAL_TIME HH:mm or HH:mm:ss
     * @see DateTimeFormatter#ISO_OFFSET_TIME HH:mm+offset or HH:mm:ss+offset
     * @see DateTimeFormatter#ISO_TIME HH:mm or HH:mm:ss or HH:mm:ss+offset
     * @see DateTimeFormatter#ISO_LOCAL_DATE_TIME yyyy-MM-ddTHH:mm:ss
     * @see DateTimeFormatter#ISO_OFFSET_DATE_TIME yyyy-MM-ddTHH:mm:ss+offset
     * @see DateTimeFormatter#ISO_ZONED_DATE_TIME yyyy-MM-ddTHH:mm:ss+offset[zone]
     * @see DateTimeFormatter#ISO_ORDINAL_DATE yyyy-days
     * @see DateTimeFormatter#ISO_WEEK_DATE yyyy-week-days
     * @see DateTimeFormatter#ISO_INSTANT yyyy-MM-ddTHH:mm:ssZ
     * @see DateTimeFormatter#BASIC_ISO_DATE yyyyMMdd
     * @see DateTimeFormatter#RFC_1123_DATE_TIME 'Tue, 3 Jun 2008 11:05:30 GMT'
     */
    @Test
    public void testFormat() {
        System.out.println(LocalDate.parse("20181111", DateTimeFormatter.BASIC_ISO_DATE));
    }
}

```
#### 总结 
时间对象的操作在jdk8之前 其实很操蛋 只能通过使用一些自己封装或者 一些组织封装的dateUtils 来操作 有时候一些特殊的时间处理只能单独写工具了 很麻烦   
现在jdk8 的time包 直接继承了jodaTime的操作 常用操作变成了 不可变而且线程安全的操作了 并且增强了对时间的 偏移和时区的处理 增加了 很多常规的时间处理方法 
有点蛋疼的是需要考虑 框架之类的对于time包的兼容  特别是jdbc对于time包的对象的支持程度  
不过  我可以使用Date转换成time的时间对象 在进行操作 然后再转换回去即可 