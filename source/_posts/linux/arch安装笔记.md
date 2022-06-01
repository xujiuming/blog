---
title: arch安装笔记
comments: true
abbrlink: b78bd2a6
date: 2022-05-31 16:33:25
categories: 笔记
tags:
 - linux 
 - arch
---
#### 前言 
记录一下 arch 自己安装的过程   免得每次都现查 看不到合适的文章    
主要参考 arch  wiki 
> https://wiki.archlinux.org/

#### 安装    
安装arch系统 也跟其他系统一样     
准备启动盘  引导、分区、安装基本系统、安装组件、配置  只不过要全手动去选择配置      

##### 准备iso、写入u盘、引导启动安装系统 
* 下载iso   
  https://developer.aliyun.com/mirror/ 选择最新的arch镜像下载下来  
* 制作启动u盘   
软碟通之类的软件 写入就行 
* 引导   
按照不同的电脑  f2  f12 之类的 选择对应的u盘引导启动

##### 联网\(网线直连、iwd连接wifi)
* 网线直连  
直接插上网线就行    
* wifi   
使用iwctl 连接wifi 
> https://wiki.archlinux.org/title/Iwd_(%E7%AE%80%E4%BD%93%E4%B8%AD%E6%96%87)
```shell
# 进入iwd
iwctl
# 查看设备列表 例如我的笔记本 是wlan0 
device list
# wlan0 扫描 
station wlan0 scan 
# 获取wlan0 扫描到的wifi 
station wlan0 get-netoworks
# 连接wlan0 的wifi 
station wlan0 connect wifi名称 
```
如果device list显示设备的powered是off状态  
```shell
rfkill list
# 如果硬件被阻塞执行 
rfkill unblock wifi
```

##### 存储分区 
分区 一般来说 新的电脑 如果是全盘格式化 需要格式化一个efi区域和系统盘 就行  如果有需要可以分一个swap 
个人比较懒  就一块500g固态  直接分一个 500MB的efi分区 剩下全系统盘  

* 查看分区信息  
```shell
# 看硬件 一般是 /dev/sdaX 或者/dev/nvme0n1 这样的   我的笔记本是nvme0n1 也就是nvme的固态第一个硬盘
fdisk -l 
```
* 分区   
分区有很多工具  我比较喜欢用cfdisk    
```shell
cfdisk /dev/nvme0n1 
# 删除所有硬盘分区 
# 新增一个efi格式的 500MB boot盘  
# 剩下的全部分区为一个系统盘  
# 保存之后 使用 fdisk -l 查看  应该会看到 一个nvme0n1p1的efi盘 和nvme0n1p2的file system类型的盘 
```

* 格式化   
文件系统 选择一个喜欢用的就行  ext4 、zfs、 btrfs 个人选择ext4       
```shell
# 格式化 efi盘 
mkfs.fat -F 32 /dev/nvme0n1p1 
# 格式化系统盘 
mkfs.ext4 /dev/nvme0n1p2   
# 如果有swap分区 
mkswap /dev/[swap盘符]
```

##### 安装系统和基础组件  
* 连接ntp服务    
```shell
timedatectl set-ntp true
```
* 挂载分区 
/mnt 为以后arch系统的/分区 /mnt/boot 对应/boot 为efi分区       
```shell
# 注意 先挂载系统分区 
mount /dev/nvme0n1p2 /mnt 
# 挂载efi分区 
mount --mkdir /dev/nvme0n1p1 /mnt/boot
#如果有swap 挂载swap 
swapon /dev/[swap盘符] 
```

* 选择合适的镜像源
```shell
# 自动选择 中国区域 前十的镜像源 
reflector -c China -a 10 --sort rate --save /etc/pacman.d/mirrorlist 
# 手动添加 写入 Server = https://mirrors.aliyun.com/archlinux/$repo/os/$arch     
vim /etc/pacman.d/mirrorlist 
```

* 安装基本系统和工具 
```shell
# arch 的最基本的系统 
pacstrap /mnt base base-devel linux  linux-firmware 
```

* 写入分区信息 
```shell
# 使用uuid模式
genfstab -U /mnt >> /mnt/etc/fstab
# 使用标签模式  
genfstab -L /mnt >> /mnt/etc/fstab
```

* 登录到新安装的系统 
```shell
arch-chroot /mnt  
```

* 设置时区信息 
```shell
# 建立上海的时区的软连接
ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
# 更新到硬件 
hwclock --systohc
```

* 设置本地化信息 
```shell
# 编辑本地化信息 将常见的 en_US  zh_CN 等配置为UTF-8  
vim /etc/locale.gen 
# 生成信息 
locale-gen
# 写入LANG  LANG=en_US.UTF-8 
vim /etc/locale.conf
# 配置键盘格式 (可选) KEYMAP=de-latin1 参考 wiki  
vim /etc/vconsole.conf 
```

* 配置主机名 
```shell
# 第一行写入主机名   ming
vim /etc/hostname
```

* 设置root密码
```shell
passwd 
```

* 安裝基本组件
```shell
pacman -S iwd networkmanager   bash-comletion   vim 
```

* 安装微指令 
```shell
# intel cpu
pacman -S intel-ucode
# amd cpu 
pacman -S amd-ucode 
```

* 安装grub和配置 
```shell
pacman -S  os-prober  grub efibootmgr 
# 部署GRUB 
grub-install --target=x86_64-efi --efi-directory=/boot --bootloader-id=grub           
# 生成GRUB配置文件
grub-mkconfig -o /boot/grub/grub.cfg      
## 查看生成的配置文件，是否包含`initramfs-linux-fallback.img initramfs-linux.img amd-ucode.img vmlinuz-linux`            
cat /boot/grub/grub.cfg         
```

* 设置必要开机启动
```shell
# 设置网络管理器
systemctl enable NetworkManager
systemctl start NetworkManager
# 设置dhcpcd 
systemctl enable dhcpcd 
systemctl start dhcpcd 
```

* 重启  
```shell
# 卸载  
umount /dev/nvme0n1p2 
umount /dev/nvme0n1p1 
```
断电之后拔u盘 然后启动的时候 看到grub引导页面 选择第一个就行 

##### 配置系统 

* 选择合适的源
```shell
# 自动选择 中国区域 前十的镜像源 
reflector -c China -a 10 --sort rate --save /etc/pacman.d/mirrorlist 
# 手动添加 写入 Server = https://mirrors.aliyun.com/archlinux/$repo/os/$arch     
vim /etc/pacman.d/mirrorlist 
# 更新缓存 
sudo pacman -Syyu 
```

* 新增用户 
```shell
# 新增ming 用户组
groupadd ming 
# 新增用户 
useradd -m -G ming ming                
# 设置ming用户的密码 
passwd ming 
# 配置sudo
vim /etc/sudoers  
```

* 安装显卡驱动
```shell
pacman -S xf86-video-amdgpu mesa
```

* 安装gnome
个人比较习惯 gnome   
> https://wiki.archlinux.org/title/GNOME_(%E7%AE%80%E4%BD%93%E4%B8%AD%E6%96%87)   
```shell
# 安装gnome 
pacman -S gnome gnome-extra gdm
systemctl enable gdm 
# 重启  
reboot now  
```

* 增加中文社区仓库 
在/etc/pacman.d/mirrorlist中增加
```text
[archlinuxcn]
Server = https://mirrors.zju.edu.cn/archlinuxcn/$arch
# Server = https://cdn.repo.archlinuxcn.org/$arch
```
```shell
sudo pacman -Syy
sudo pacman -Syu
sudo pacman -S archlinuxcn-keyring
sudo pacman -S archlinuxcn-mirrorlist-git
```

* 安装aur助手 
yay paru 都可以  yay粗暴点 
```shell
#配置了中文社区仓库 可以直接安装 
sudo pacman -S yay
yay --aururl "https://aur.tuna.tsinghua.edu.cn" --save
yay -Syy
yay  
########## 未配置中文社区仓库 
git clone https://aur.archlinux.org/yay
cd yay
# 配置go的代理  
export GO111MODULE=on
export GOPROXY=https://goproxy.cn
makepkg -si
```

#### 总结 
安装过很多次 arch  一直懒的记笔记  
就是一个系统安装 按照wiki来   















