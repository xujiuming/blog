---
title: 中文写java
categories: 骚想法
tags:
  - java基础
abbrlink: e8ebc393
date: 2017-11-11 00:00:00
---

###偶然间发现java底层是采用utf16做编码集的  突发奇想 既然是utf16做底层编码 拿起不是可以拿非英文开发   毕竟编译器可以识别中文那么肯定也可以编译中文
###代码尝试：
```
class  垃圾{
  public void 一个垃圾方法(){
      System.out.println("真的垃圾");
  }
  public void 两个垃圾方法(String 垃圾参数){
      System.out.println("第二个垃圾方法的垃圾参数:"+垃圾参数);
  }
    public static void main(String[] args) {
        垃圾 垃圾的实例=new 垃圾();
        垃圾的实例.一个垃圾方法();
        垃圾的实例.两个垃圾方法("啦等等单打独斗");
    }
}
```
####总结:java真的贼几把神奇、这样看来几乎可以用任何utf16包含的语言去编写java了
