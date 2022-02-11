---
title: java转换markdown到html笔记
comments: true
date: 2022-02-10 17:35:02
categories: 笔记
tags:
 - markdown 
---
#### 前言
最近遇到需要用java 把markdown解析成html    
这个东西 很多工具 比较出名的 有commonmark-java  和他的衍生版本  flexmark-java   
从性能上来说 肯定是commonmark-java最快  所以我也选择用这个     

> 官网地址:   
> https://github.com/commonmark/commonmark-java   
> https://github.com/vsch/flexmark-java   

#### 示例 
##### 依赖 
```xml
   <commonmark.version>0.18.1</commonmark.version>
    ...
  <dependency>
            <groupId>org.commonmark</groupId>
            <artifactId>commonmark</artifactId>
            <version>${commonmark.version}</version>
        </dependency>
        <dependency>
            <groupId>org.commonmark</groupId>
            <artifactId>commonmark-ext-gfm-tables</artifactId>
            <version>${commonmark.version}</version>
        </dependency>
```

##### 简单demo    
```java
package com.ming;


import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.text.TextContentRenderer;

public class TestMarkdownParse {
    public static void main(String[] args) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse("This is *Sparta*");

        HtmlRenderer renderer = HtmlRenderer.builder().build();
        // "<p>This is <em>Sparta</em></p>\n"
        System.out.println(renderer.render(document));

        TextContentRenderer textContentRenderer = TextContentRenderer.builder().build();
        //This is Sparta
        System.out.println(textContentRenderer.render(document));
    }
}

```

##### 解析service实现 

> 主要就是增加按照不同的类型添加节点的属性的实现  方便美化生成的html   

```java
package com.ming.service;

import lombok.extern.slf4j.Slf4j;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Image;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.text.TextContentRenderer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * markdown parse 服务
 *
 * @author ming
 * @date 2022-01-19 11:43:54
 */
@Service
@Slf4j
public class MarkdownParseService implements CommandLineRunner {

    private Parser parser;
    private HtmlRenderer htmlRenderer;
    private TextContentRenderer textContentRenderer;

    /**
     * Callback used to run the bean.
     *
     * @param args incoming main method arguments
     * @throws Exception on error
     */
    @Override
    public void run(String... args) throws Exception {
        List<Extension> extensions = List.of(TablesExtension.create());
        parser = Parser.builder().extensions(extensions).build();
        htmlRenderer = HtmlRenderer.builder()
                //设置解析节点增加属性
                .attributeProviderFactory(attributeProviderContext -> (node, s, attr) -> {
                    if (node instanceof Image) {
                        attr.put("style", "font: red");
                    }
                    if (node instanceof Link) {

                    }
                })
                .extensions(extensions).build();
        textContentRenderer = TextContentRenderer.builder().build();
    }

    /**
     * markdown 转换为html
     *
     * @param content markdown 内容
     * @return String html
     * @author ming
     * @date 2022-01-19 11:44:05
     */
    public String parseHtml(String content) {
        Node document = parser.parse(content);
        return htmlRenderer.render(document);
    }

    public String parseText(String content) {
        Node document = parser.parse(content);
        return textContentRenderer.render(document);
    }
}

```

#### 总结
解析工具 原理也就是解析markdown的规则 生成文本  
现成的工具  没啥好说的  作为速查   
