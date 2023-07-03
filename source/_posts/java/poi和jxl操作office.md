---
title: 'java操控excel相关(poi,jxl)笔记'
categories: 笔记
tags:
  - java
  - tools
abbrlink: d3259856
date: 2017-11-11 00:00:00
---


1. [poi和jxl介绍](#m1)
2. [poi和jxl操作excel不同点](#m2)
3. [poi读写excel实例](#m3)
4. [jxl读写excel实例](#m4)
5. [poi读写word实例](#m5)
6. [poi和jxl选择](#m6)

<h3 id="m1">1:poi和jxl介绍</h3>

####    1.1:poi
         poi是对所有office资源进行读写的一套工具包、属于apache开源组织。
#### 1.2:jxl
         jxl只能对excel进行操作的一套工具包。
<h3 id="m2">2:两者操作excel不同点</h3>

         poi和jxl都是封装了对excel操作方法;
         poi是把整个文件的属性都封装在HSSFWorkbook 中;
         通过HSSFWorkbook来操作单个工作薄。然后通过工作薄来操作行;
         在通过行来操控单元格。这样一级一级的分拆下来;
         HSSFWorkbook---->HSSFSheet----->HSSFRow---->HSSFCell;
         由于是基于HSSFWorkbook对象一步步创建起来的。所以不用把创建好的单元格添加进这个对象中、
         如果需要对部分表格进行设置样式什么的。就可以创立HSSFCellStyle对象来进行设定样式;

          jxl是把整个文件封装在Workbook相关对象中;
          通过Workbook去创建sheet工作薄;但是和poi不一样的地方是
          jxl是通过向sheet中使用label(单元格)来进行读取写入;
          Workbook----->sheet------>label ;
          jxl是先创建一个工作区域、然后区创立单元格、单元格包含这个单元格的位置、内容等信息;然后把这个单元格加入工作区;

###3：poi读写excel文件的实例(代码中fileURL是你存放路径)
```
//写入
public void poiWriteExcel() {
        //创建excel工作薄
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        //在里面创建一个sheet 名字为工作薄1
        HSSFSheet hssfSheet = hssfWorkbook.createSheet("工作薄1");
        //在索引为o的位置创建行 也就是第一行
        HSSFRow oneRow = hssfSheet.createRow(0);
        //创建红色字体
        HSSFFont font = hssfWorkbook.createFont();
        font.setColor(HSSFFont.COLOR_RED);
        //创建格式、
        HSSFCellStyle cellStyle = hssfWorkbook.createCellStyle();
        cellStyle.setFont(font);
        //在第o行创建第一个单元格
        HSSFCell cell = oneRow.createCell(0);
        //使用单元格格式
        cell.setCellStyle(cellStyle);
        //在第一个单元格输入内容
        cell.setCellValue("xu");
        //创建一个第十行
        HSSFRow tenRow = hssfSheet.createRow(9);
        //创建输出流
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileURL + "xianyu.xls");
            //存储工作博
            hssfWorkbook.write(fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            System.out.println("无法写入");
            e.printStackTrace();
        }
    }

//读取
public void poiReadExcel() {

        HSSFWorkbook hssfWorkBook = null;
        try {
            hssfWorkBook = new HSSFWorkbook(new FileInputStream(fileURL + "xianyu.xls"));
            //获取第一个工作薄
            HSSFSheet hssfSheet = hssfWorkBook.getSheetAt(0);
            //获取第一行
            HSSFRow row = hssfSheet.getRow(0);
            System.out.println(row.getCell(0));
        } catch (IOException e) {
            System.out.println("无法读取");
            e.printStackTrace();
        }
    }
```
###4:jxl读写excel
```
//写入
public void jxlWriteExcel() {
        try {
            //创建xls
            WritableWorkbook wwb = Workbook.createWorkbook(
                    new FileOutputStream(fileURL + "xianyujxl.xls"));
            //添加工作薄
            WritableSheet sheet = wwb.createSheet("工作薄1", 0);
            //添加单元格 Label(x,y,z) x=列 y=行 z=内容
            Label label = new Label(0, 0, "xu");
            //添加进工作薄
            sheet.addCell(label);
            // 写数据
            wwb.write();
            wwb.close();
        } catch (IOException e) {
            System.out.println("写入失败");
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

//读取
public void jxlReadExcel() {
        try {
            //获取excel
            Workbook wb = Workbook.getWorkbook(
                    new FileInputStream(fileURL + "xianyujxl.xls"));
            //获取sheet工作薄
            Sheet sheet = wb.getSheet(0);
            System.out.println(sheet.getCell(0, 0).getContents());
        } catch (IOException | BiffException e) {
            e.printStackTrace();
        }
    }

```
###5:poi读写word
```
//写入
public void poiWriteWord() {
        try {
            //创建word文件
            XWPFDocument xwpfDocument = new XWPFDocument();
            //新建段落
            XWPFParagraph xwpfP = xwpfDocument.createParagraph();
            //创建文本
            XWPFRun xwpfR = xwpfP.createRun();
            xwpfR.setText("xu");
            xwpfDocument.write(new FileOutputStream(fileURL + "xianyu.doc"));
        } catch (IOException e) {
            System.out.println("无法写入");
            e.printStackTrace();
        }
    }

//读取
public void poiReadWord() {
        //获取doc对象
        XWPFDocument xwpfDocument;
        try {
            xwpfDocument = new XWPFDocument(new FileInputStream(fileURL + "xianyu.doc"));
            //获取段落并且遍历
            xwpfDocument.getParagraphs().forEach(xwpfParagraph
                    -> System.out.println(xwpfParagraph.getText()));
        } catch (IOException e) {
            System.out.println("无法读取");
            e.printStackTrace();
        }
    }
```

<h3 id="m6">6：poi和jxl选择<h3>
不用想了、肯定poi  因为jxl很久不更新了。poi现在是apache的项目、前景好、文档全
