---
title: java识别psd文件笔记
categories: 笔记
tags:
  - java
abbrlink: 94809d83
date: 2021-04-09 00:00:00
---

#### 前言

最近在做网盘的一些需求    
需要预览一些奇奇怪怪的文件    
先记录下psd文件如何生成预览图

#### 例子

> 由于jdk的ImageIO相关class 无法直接解析psd 所以要另寻方案

##### 直接解析psd

> 参考文档:   https://blog.csdn.net/WASONE_WU/article/details/27695723

直接按照psd文件格式进行解析 具体的参考文档博文    

```java

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class PsdReader {

    private BufferedImage img = null;

    private int[] pixels;

    private RandomAccessFile raf;
    private int[] byteArray;
    //用来接住unsignedByte，byte不存作负数（否则抛异常，说越过颜色范围）
    private int[][][] channelColor;
    private int[][] numOfBytePerLine;
    //	private final static int RED = 0;
//	private final static int GREEN = 1;
//	private final static int BLUE = 2;
    private short numOfChannel;
    private int height;
    private int width;
    private short isRle;
    private MappedByteBuffer mbbi;

    public PsdReader(File file) {
        FileChannel fc = null;
        try {
            this.raf = new RandomAccessFile(file, "r");
            fc = raf.getChannel();
            long size = fc.size();
            this.mbbi = fc.map(FileChannel.MapMode.READ_ONLY, 0, size);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.readFile();
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        pixels = new int[width*height];
        this.initPixels(pixels);
        this.setRGB(img, 0, 0, width, height, pixels);
        try {
            fc.close();
            this.raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedImage getImg() {
        return img;
    }

    private void initPixels(int[] pixels) {
        int index = 0;
        int a = 255;
        for(int h=0; h<this.height; h++) {
            for(int w=0; w<this.width; w++) {
                int r = this.channelColor[0][h][w];
                int g = this.channelColor[1][h][w];
                int b = this.channelColor[2][h][w];
                if(this.numOfChannel>3) {
                    a = this.channelColor[3][h][w];
                }

                pixels[index] = (a<<24) | (r<<16)
                        | (g<<8) | b;
                index++;
            }
        }
    }

    private void setRGB( BufferedImage image, int x, int y, int width, int height, int[] pixels ) {
        int type = image.getType();
        if ( type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB )
            image.getRaster().setDataElements( x, y, width, height, pixels );
        else
            image.setRGB( x, y, width, height, pixels, 0, width );
    }

    private void readFile() {
        try {
            //-------第一部分：文件头------------------
            //通道数量
//			this.raf.seek(0x0c);
            this.mbbi.position(0x0c);
//			numOfChannel = this.raf.readShort();
            numOfChannel = this.mbbi.getShort();
            //System.out.println("numOfChannel="+numOfChannel);
            //图像高度
//			height = this.raf.readInt();
            height = this.mbbi.getInt();
            //System.out.println("height="+height);
            //图像宽度
//			width = this.raf.readInt();
            width = this.mbbi.getInt();
            //System.out.println("width="+width);
            //图像深度（每个通道的颜色位数）
//			short depth = this.raf.readShort();
            short depth = this.mbbi.getShort();
            //System.out.println("depth="+depth);
            //是rgb模式则type=3
//			short type = this.raf.readShort();
            short type = this.mbbi.getShort();
            //System.out.println("type="+type);
            //--------第二部分：色彩模式信息，这部分的长度通常为0----
//			int lenOfColorModel = raf.readInt();
            int lenOfColorModel = this.mbbi.getInt();
            //System.out.println("lenOfColorModel="+lenOfColorModel);
//			this.raf.seek(lenOfColorModel+this.raf.getFilePointer());//长度信息占4个字节，但是不用加,下同
            this.mbbi.position(lenOfColorModel+this.mbbi.position());
            //--------第三部分：图像资源数据------------------
//			int lenOfImageResourceBlock = raf.readInt();
            int lenOfImageResourceBlock = this.mbbi.getInt();
            //System.out.println("lenOfImageResourceBlock="+lenOfImageResourceBlock);
//			this.raf.seek(lenOfImageResourceBlock+this.raf.getFilePointer());
            this.mbbi.position(lenOfImageResourceBlock+this.mbbi.position());
            //--------第四部分：图层与蒙版信息----------------
//			int lenOfLayerInfo = raf.readInt();
            int lenOfLayerInfo = this.mbbi.getInt();
            //System.out.println("lenOfLayer="+lenOfLayerInfo);
//			this.raf.seek(lenOfLayerInfo+raf.getFilePointer());
            this.mbbi.position(lenOfLayerInfo+this.mbbi.position());
            //--------第五部分：图像数据--------------------
//			isRle = raf.readShort();
            isRle = this.mbbi.getShort();
            //System.out.println("isRle="+isRle);
//			//System.out.println("nowPosition="+this.raf.getFilePointer());
            //System.out.println("nowPosition="+this.mbbi.position());

        } catch (Exception e1) {
            e1.printStackTrace();
        }

        this.channelColor = new int[numOfChannel][height][width];
        if(isRle==1){
            this.numOfBytePerLine = new int[numOfChannel][height];
            for(int i=0; i<numOfChannel; i++) {
                for(int j=0; j<height; j++) {
                    try {
                        //TODO
//					this.numOfBytePerLine[i][j] = this.raf.readUnsignedShort();
                        int ti = this.mbbi.getShort();
                        if(ti<0) { ti += 65536; }
                        this.numOfBytePerLine[i][j] = ti;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            for(int c=0; c<numOfChannel; c++) {
                for(int h=0; h<height; h++) {
                    this.unpackbits(numOfBytePerLine[c][h],channelColor[c][h]);
                }
            }
        }else if(isRle==0) {
            for(int c=0; c<numOfChannel; c++) {
                for(int h=0; h<height; h++) {
                    for(int w=0; w<width; w++) {
                        try {
//							this.channelColor[c][h][w] = this.raf.readUnsignedByte();
                            int ti = this.mbbi.get();
                            if(ti<0) { ti += 256; }
                            this.channelColor[c][h][w] = ti;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private void unpackbits(int lenOfInput, int[] channelColor) {
        short n = 0;
        int last = 0;

        while(lenOfInput>0){
            try {
//			n = raf.readByte();
                n = this.mbbi.get();
                lenOfInput--;
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(0<=n && n<=127) {
                int repeatTime = n;
                ++repeatTime;
                for(int t=0; t<repeatTime; t++) {
                    try {
//					channelColor[last+t] = raf.readUnsignedByte();
                        int ti = this.mbbi.get();
                        if(ti<0) { ti += 256; }
                        channelColor[last+t] = ti;

                        lenOfInput--;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                last += repeatTime;
            }
            else if(-1>=n && n>=-127) {
                int val = 0;
                int repeatTime = -n;
                ++repeatTime;
                try {
//				val = raf.readUnsignedByte();
                    int ti = this.mbbi.get();
                    if(ti<0) { ti += 256; }
                    val = ti;
                    //System.out.println(val);
                    lenOfInput--;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                for(int t=0; t<repeatTime; t++) {
                    channelColor[last+t] = val;
                }
                last += repeatTime;
            }
            else if(n==-128) {
                //noop
            }
        }
    }
}

```

```java
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class main {
    public static void main(String[] args) throws IOException {
        long now = System.currentTimeMillis();
        PsdReader psdReader = new PsdReader(new File("02.psd"));
        BufferedImage bufferedImage = psdReader.getImg();
        ImageIO.write(bufferedImage,"png",new File("02.png"));
        System.out.println((System.currentTimeMillis() - now));
    }
}

```

##### twelvemonkeys + thumbnailator

> 参考文档:
> https://www.cnblogs.com/interdrp/p/7076202.html
> https://github.com/haraldk/TwelveMonkeys
> https://github.com/coobird/thumbnailator
>
> 利用twelevemonkeys提供对ImageIo的增强直接读取 psd

```xml
   <!-- https://mvnrepository.com/artifact/net.coobird/thumbnailator -->
<dependency>
    <groupId>net.coobird</groupId>
    <artifactId>thumbnailator</artifactId>
    <version>0.4.14</version>
</dependency>

<dependency>
    <groupId>com.twelvemonkeys.imageio</groupId>
    <artifactId>imageio-core</artifactId>
    <version>3.6.4</version>
</dependency>
        <!-- https://mvnrepository.com/artifact/com.twelvemonkeys.imageio/imageio-psd -->
<dependency>
    <groupId>com.twelvemonkeys.imageio</groupId>
    <artifactId>imageio-psd</artifactId>
    <version>3.6.4</version>
</dependency>
```

```java
import net.coobird.thumbnailator.Thumbnails;

import java.io.File;
import java.io.IOException;

public class main {
    public static void main(String[] args) {
        long now = System.currentTimeMillis();
        try {
            Thumbnails.of(new File("03.psd"))
                    .size(1080, 720)
                    .toFile(new File("03.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println((System.currentTimeMillis() - now));
    }
}
```

##### Aspose.psd for java

> 参考文档: https://downloads.aspose.com/psd/java

```xml

<repository>
    <id>AsposeJavaAPI</id>
    <name>Aspose Java API</name>
    <url>http://repository.aspose.com/repo/</url>
</repository>

。。。。。


<dependency>
    <groupId>com.aspose</groupId>
    <artifactId>aspose-psd</artifactId>
    <version>20.9</version>
    <classifier>jdk16</classifier>
</dependency>
```

```java
import net.coobird.thumbnailator.Thumbnails;

import java.io.File;
import java.io.IOException;

public class main {
    public static void main(String[] args) {
        long now = System.currentTimeMillis();
        // Load image
        Image img = Image.load("03.psd");
        //按需选择不同类型options 例如 png pdf jpeg等等
        PngOptions options = new PngOptions();
        // Convert PSD to png
        img.save("03.png", options);
        System.out.println((System.currentTimeMillis() - now));
    }
}

```

#### 总结   
如果要直接粗暴读取 直接按照psd 格式硬读取即可     
如果有其他的一些需求 建议 twelvemonkeys + thumbnailator组合  不仅仅是读取psd 还可以做一些奇奇怪怪的事情    
Aspose.psd for java方案的话 如果是部署在windows下 可以使用 如果要部署linux下 需要将windows字体库安装到linux下 否则会报错  
个人建议还是 twelvemonkeys + thumbnailator 读取、打水印乱七八糟的操作 都可以简单直接快速处理          
