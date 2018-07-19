---
title: spring boot (二)笔记
categories: 笔记
tags:
  - java
  - spring
abbrlink: 64575eaf
date: 2017-11-11 00:00:00
---
###步骤
1:使用spring boot cli 生成基本项目
2:写dao、controller层代码
3:写thymeleaf 模板
###1:初始化项目
spring init -dweb,data-jpa,h2,thymeleaf
下载demo.zip解压 改名springboot (任意名称)
刷新maven 下载jar包构建项目
###2:dao、controller层代码
entity
```
@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String reader;
    private String isbn;
    private String title;
    private String author;
    private String description;
.....省略setter gettter等方法
}
```
定义jpa book实体的 repository
```
@Repository
public interface BookRepository extends JpaRepository<Book,Long> {
    List<Book> findByReader(String reader);
}
```
正式项目中会设立service层 这里就不写了  就是跟平常写spring 项目一样的玩法
定义 controller层 新增和查看控制器
```
@Controller
public class ReadingListController {

    @Resource
    BookRepository bookRepository;

    @RequestMapping(value = "/{reader}",method = RequestMethod.GET)
    public String readersBook(Model model,
                              @PathVariable(value = "reader",required = false)String reader){
        List<Book> books= bookRepository.findByReader(reader);
        if (books!=null){
            model.addAttribute("books",books);
        }
        return "readingList";
    }

    @RequestMapping(value = "/{reader}",method = RequestMethod.POST)
    public String addToReadingList(@PathVariable(value = "reader",required = false) String reader,Book book){
        book.setReader(reader);
        bookRepository.save(book);
        return "redirect:/{reader}";
    }
}
```
###thymeleaf 模板引擎代码
```
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.w3.org/1999/xhtml">
<head>
    <meta charset="UTF-8"/>
    <title>阅读列表</title>
</head>
<body>
<h2>你的阅读列表</h2>
<div th:unless="${#lists.isEmpty(books)}">
    <dl th:each="book : ${books}">
        <dt class="bookHeadline">
            <span th:text="${book.title}">标题</span>
            <span th:text="${book.author}">作者</span>
            (ISBN: <span th:text="${book.isbn}">ISBN</span>)
        </dt>
        <dd class="bookDescription">
<span th:if="${book.description}"
      th:text="${book.description}">Description</span>
            <span th:if="${book.description eq null}">
description为空</span>
        </dd>
    </dl>
</div>
<div th:if="${#lists.isEmpty(books)}">
    <p>当前没有阅读</p>
</div>

<hr/>
<h3>添加书</h3>
<!--只需要进控制器即可 直接post当前路径-->
<form method="POST">
    <label>标题:</label>
    <input type="text" name="title" size="50"></input><br/>
    <label>作者:</label>
    <input type="text" name="author" size="50"></input><br/>
    <label>ISBN:</label>
    <input type="text" name="isbn" size="15"></input><br/>
    <label>Description:</label><br/>
    <textarea name="description" cols="80" rows="5">
</textarea><br/>
    <input type="submit"></input>
</form>
</body>
</html>
```
##浏览器打开http://localhost:8080/sss(任意字符串 进 get  /{reader}控制器即可)

###代码地址:https://github.com/xuxianyu/myGitHub/tree/master/springboot
