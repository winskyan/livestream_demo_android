# Introduction to Agora Live Chat Room

## Introduction

**The agora live chat room (hereinafter referred to as the agora chat room) demonstrates the ability of the agora SDK to provide live chat rooms. In addition to basic chat, it also provides three kinds of custom messages: gift giving, likes and barrage messages. Developers can add new custom messages according to their actual needs. **

**Introduction to core classes:**

- LiveAudienceActivity: Audience live room page</br>
- LiveAnchorActivity: anchor live page</br>
- LiveAudienceFragment: Integrate audience chat room related logic</br>
- LiveAnchorFragment: Integrate the logic related to the chat room on the anchor side</br>

**Other side open source address:**

- iOS:    https://github.com/easemob/livestream_demo_ios
- App Server:     https://github.com/easemob/easemob-im-app-server

## Integrate Agora IM SDK

### Development Environment Requirements

- Android Studio 3.2 or higher. </br>
- SDK targetVersion is at least 26.

### Add remote dependencies

```
api 'io.agora.rtc:chat-sdk:1.0.3'
```

**Integration Documentation:**</br>

- [Import Android SDK](http://docs-im.easemob.com/im/android/sdk/import)；</br>
- [Android SDK Changelog](http://docs-im.easemob.com/im/android/sdk/releasenote)；

## Use Custom Message Library

**In order to facilitate developers to use custom messages, the a chat room encapsulates the logic related to custom messages into the custom message library. **

Developers can make changes to this library according to their needs.

### Core class introduction

- EaseLiveMessageHelper: used to listen to receive custom messages and send custom messages. </br>
- EaseLiveMessageType: The user defines the custom message type (gift message, like message and barrage message) used in the demo.

### Specific usage

#### 1. When the live room page is initialized, set the room information.

```Java
EaseLiveMessageHelper.getInstance().init(chatroomId);
```

#### 2. Set custom message listener

```Java
        EaseLiveMessageHelper.getInstance().setLiveMessageListener(new OnLiveMessageListener() {
          @Override
          public void onMessageReceived(List<ChatMessage> messages) {

          }

          @Override
          public void onGiftMessageReceived(ChatMessage message) {
          
          }
          
          @Override
          public void onPraiseMessageReceived(ChatMessage message) {
          
          }
          
          @Override
          public void onBarrageMessageReceived(ChatMessage message) {
          
          }
          
          @Override
          public void onMessageChanged() {
          
          }
        });
```

#### 4. 发送自定义消息可以调用如下方法

```Java
//如果所传参数与library中相同，可以直接调用此方法
public void sendGiftMsg(String giftId, int num, OnMsgCallBack callBack);        //礼物消息

public void sendPraiseMsg(int num, OnMsgCallBack callBack);                     //点赞消息

public void sendBarrageMsg(String content, OnMsgCallBack callBack);             //弹幕消息

//有其他参数或者与demo中定义的参数不同，调用此方法
public void sendGiftMsg(Map<String, String> params, OnMsgCallBack callBack);    //礼物消息

public void sendPraiseMsg(Map<String, String> params, OnMsgCallBack callBack);  //点赞消息

public void sendBarrageMsg(Map<String, String> params, OnMsgCallBack callBack); //弹幕消息

//甚至也可以调用如下方法发送自定义消息
public void sendCustomMsg(String event, Map<String, String> params, OnMsgCallBack callBack);

public void sendCustomMsg(String to, ChatMessage.ChatType chatType, String event
, Map<String, String> params, OnMsgCallBack callBack);
```

#### 5. 解析自定义消息</br>

（1）如果发送的自定义参数与library中相同，可以直接调用如下方法，获得所传的数据

```Java
//获取礼物消息中礼物的id
public String getMsgGiftId(ChatMessage msg);
//获取礼物消息中礼物的数量
public int getMsgGiftNum(ChatMessage msg);
//获取点赞消息中点赞的数目
public int getMsgPraiseNum(ChatMessage msg);
//获取弹幕消息中的文本内容
public String getMsgBarrageTxt(ChatMessage msg);
```

（2）如果自定义消息参数与library中不同，可以调用如下方法，获取消息中的参数

```Java
public Map<String, String> getCustomMsgParams(ChatMessage message);
```

#### 6. library中还提供了，判断自定义消息类型的方法

```Java
public boolean isGiftMsg(ChatMessage msg);    //礼物消息判断

public boolean isPraiseMsg(ChatMessage msg);  //点赞消息判断

public boolean isBarrageMsg(ChatMessage msg); //弹幕消息判断
```

## 环信直播聊天室架构介绍

![](https://developer.android.google.cn/topic/libraries/architecture/images/final-architecture.png)</br>
环信聊天室中有两个repository，ChatClientRepository及AppServerRepository。其中ChatClientRepository用户处理环信SDK提供
的相关请求，AppServerRepository用户处理app server提供的接口。每个页面有相应的ViewModel以生命周期的方式存储和管
理与UI相关的数据。LiveData是一个具有生命周期感知特性的可观察的数据保持类，一般位于ViewModel中，用于观察数据变化。</br>

## 集成视频直播SDK

环信聊天室提供了多种直播类型：

#### [融合CDN直播](https://docs.agora.io/cn/fusion-cdn-streaming/landing-page?platform=RESTful)

超低卡顿、全链路质量透明的标准 CDN 直播

#### [极速直播](https://docs.agora.io/cn/live-streaming/landing-page?platform=Android)

低延时、强同步、高质量直播，观众与主播进行低频音视频互动

#### [互动直播](https://docs.agora.io/cn/Interactive%20Broadcast/landing-page?platform=Android)

超低延时直播，观众频繁上麦与主播进行实时音视频互动

## 文档

- [iOS端开源地址](https://github.com/easemob/livestream_demo_ios)
- [App Server开源地址](https://github.com/easemob/easemob-im-app-server)
- [环信直播聊天室集成介绍](http://docs-im.easemob.com/im/other/integrationcases/live-chatroom)
- [环信Android SDK 导入](http://docs-im.easemob.com/im/android/sdk/import)

## 针对非AndroidX构建的方案 ##

### 一、在非AndroidX构建的情况下运行demo，可进行如下工作：

#### 1. 注释掉demo中gradle.properties的如下设置：

```Java
#android.enableJetifier=true //Android 插件会通过重写现有第三方库的二进制文件，自动将这些库迁移为使用 AndroidX
#android.useAndroidX=true    //Android 插件会使用对应的 AndroidX 库而非支持库
```

#### 2. 将AndroidX构建工件替换为旧构建工件

```Java
dependencies {
    ...
    implementation "com.jakewharton:butterknife:$butterknife_version"
    annotationProcessor "com.jakewharton:butterknife-compiler:$butterknife_version"
    implementation 'com.google.android.material:material:1.1.0'
    implementation 'androidx.constraintlayout:constraintlayout:1.1.3'
    implementation "androidx.lifecycle:lifecycle-livedata:$ax_lifecycle_version"
    implementation "androidx.lifecycle:lifecycle-viewmodel:$ax_lifecycle_version"
    implementation "androidx.lifecycle:lifecycle-extensions:$ax_lifecycle_version"
    implementation "androidx.room:room-runtime:$ax_room_version"
    annotationProcessor "androidx.room:room-compiler:$ax_room_version"
    implementation 'androidx.legacy:legacy-support-v4:1.0.0'
    ...
}
```

修改为：

```Java
dependencies {
    ...
    implementation "com.jakewharton:butterknife:9.0.0"
    annotationProcessor "com.jakewharton:butterknife-compiler:9.0.0"
    implementation 'com.android.support:design:28.0.0'
    implementation 'com.android.support.constraint:constraint-layout:1.1.3'
    implementation "android.arch.lifecycle:livedata:$ax_lifecycle_version"
    implementation "android.arch.lifecycle:viewmodel:$ax_lifecycle_version"
    implementation "android.arch.lifecycle:extensions:$ax_lifecycle_version"
    implementation "android.arch.persistence.room:runtime:$ax_room_version"
    annotationProcessor "android.arch.persistence.room:compiler:$ax_room_version"
    implementation 'com.android.support:support-v4:28.0.0'
    ...
}
```

注：

- butterknife因10.0.0以上支持androidX，故需降为9.0.0。
- ax_lifecycle_version等的版本号，可以通过Android Stuido的Add Library Dependency去搜索。File ->Project Structure ->
  app ->Dependencies ->点击右上角添加+ ->Library dependency ->输入要搜索的远程库名称，如 design。

如果遇到与迁移有关的问题，请参考下面这些表来确定从支持库到对应的 AndroidX 工件和类的正确映射：</br>

- [Maven 工件映射](https://developer.android.google.cn/jetpack/androidx/migrate/artifact-mappings)</br>
- [类映射](https://developer.android.google.cn/jetpack/androidx/migrate/class-mappings)</br>

#### 3. 全局替换androidX下的控件的引用路径及xml中的控件路径，如androidx.recyclerview.widget.RecyclerView -> android.support.v7.widget.RecyclerView。</br>

#### 4. 替换ViewPager2为ViewPager，参考：[Migrate from ViewPager to ViewPager2](https://developer.android.google.cn/training/animation/vp2-migration?hl=zh_cn)</br>

#### 5. 其他未提到的事项。</br>

### 二、仅使用demo中的核心类

> 如果只打算使用demo的核心类，建议您关注于com.easemob.livedemo.ui.live目录下相关类，核心类为LiveAnchorActivity和LiveAudienceActivity，以及他们相应的fragment。然后从这两个activity出发，逐步替换需要的类中有关androidX的控件。

