# Dainty
拥有基本功能的小型浏览器


    
## 效果图
![image](https://github.com/Z-bm/Dainty/blob/master/img/start.gif) &emsp; ![image](https://github.com/Z-bm/Dainty/blob/master/img/query.gif) &emsp;
![image](https://github.com/Z-bm/Dainty/blob/master/img/label.gif) &emsp;
![image](https://github.com/Z-bm/Dainty/blob/master/img/settings.gif)

<br>注：以上为三星真机（API 21）测试效果，效果图更新不与项目运行效果同步。

    `WebView`采用腾讯TBS内核,支持登录信息保存及网页视频全屏播放（根据App是否联网设置不同的网页缓存模式）；<br>
    多标签页浏览窗口采用`ViewPager+Fragment`方式设计；<br>
    三个数据库表“historyTB”、“queryTB”、“collectionTB”，分别记录浏览历史信息，搜索历史信息，收藏网址信息；<br>
    historyTB表有待添加自动删除一个月前历史的功能。
    
### 基本功能 <br>
* 天气定位<br>
* 语音识别搜索<br>
* 账户登陆 <br>
* 文件下载，支持断点续传，最大同时下载数3<br>
* 历史/书签记录，网页浏览历史只显示最近一个月的记录<br>
* 收藏书签<br>
* 网页浏览基本操作<br>
* 软件设置<br>

### 使用到的第三方库 <br>
* EventBus &nbsp;&nbsp; ——用于触发标签页删除功能
* Butterknife &nbsp;&nbsp;——替代findViewById和点击事件监听
* TBS_sdk &nbsp;&nbsp;——替代原生WebView
* BaiduLBS &nbsp;&nbsp;——用于侧滑菜单的城市显示和天气显示
* audiobd_speech_sdk &nbsp;&nbsp;——用于支持搜索功能语音输入

---
已适配Android 6.0+的动态权限管理，目前仍在不断优化中...<br>

    觉得不错的话就Star一下，若发现bug请指出，谢谢<br>
    本人QQ号：1638072596，微信号：zbm33960。
