

# Live2dChatWidget JAVA


### 申明
&emsp;&emsp;本小软件使用交谈接口为[茉莉机器人][1],茉莉机器人提供了免费简单的ai服务。

### 开发原因
&emsp;&emsp;痴迷live2d,steam上有个神软件啊,Live2DViewerEX,详细的自己度娘吧。玩久了之后不能满足于简单的互动了，于是找了个ai接口，写了个挂件（可惜官方没有开发接口，也可能是我不知道），用java swing写页面真是蛋疼

### 本项目所用环境
- java version "1.8.0_121"
- 开发ide eclipse
- 运行需要jre运行环境
- **注意:** 目前没有打算做成傻瓜式的（那样工作量太大 看心情填坑）

### 使用方法，直接编译打成jar包即可（后期可能会把适配好的包放上来）
- 直接输入文字 ， 调用api接口
- $shell:[shell脚本] 执行shell    **小白请务必无视这个功能！！！**
- 框体左上角 点击 可以隐藏/显示框体（天知道用swing写渐变有多受苦）
- 更多请看源码

### 备注
&emsp;&emsp; 有很多功能局限性很大，比如shell调用 封装的几个指令只能我自己用，~~暂时也没有做定制化的打算，看什么时候想起来填坑了吧~~ 还是开坑了。同时接口回调的 表情 和json 没有处理，~~暂时懒得引入第三方的json处理包~~。

### 2019年05月09日更新
- 得到了Live2DViewerEX的接口支持，启动时，如果检测到Live2DViewerEX运行，则会默认让1号位的模型负责对话。如果没有Live2DViewerEX运行，则会像之前那样，直接在对话板上对话
- Live2DViewerEX运行的情况下 ~~$菜单~~ 现在只需点击右上角菜单按钮 可以让1号模型开启菜单，内附无聊的小代码 （仅在绑定Live2DViewerEX 成功后可用）
- 每隔~~5分钟~~现在是20分钟 会有一个心跳包（一颗心扑通扑通的狂跳~），这是为了让插件和Live2DViewerEX 本体保持通讯，以免失联

### 2019年05月13日更新
- 初步支持qq邮箱来件提示功能（imap协议）
- 已预留接口 后期可能会适配其他邮箱
- 个性化适配依然是个大坑

### 2019年05月14日更新 & 补充
- 突然发现之前提交的版本 把自己的邮箱账密硬编码了...于是全部重新提交了一遍...
- 将之前的邮件监听实现方式重构了一下，之后想适配多种不同邮箱的话，开发起来更快吧
- 邮箱提示这边有个大坑 可以说是血崩状态， qq和tx企业邮箱 用旧版本的jre运行可能会出SSL上的问题，这是java的一个bug，需要去官方下载补丁替换2个jar包，这一步我感觉对萌新来说可能....就当没有邮箱这个功能吧（具体怎么操作不说了，免得误操作玩坏了jre... 至于技术朋友handshake_failure 错误 百度一下...）
- 用新版本的jre应该没啥问题
- 坑：同时sina 和 网易的163邮箱目前 遇到了监听失败的问题  试着解决一下，不行就只能放弃了....
- 后期准备加上追番提示功能...看心情更新

### 2019年05月16日更新
- 新增追番查看功能（目前只有b站上的新番信息，估计也不会扩展了，找接口太麻烦了....）

### 2019年05月17日更新
- 个性化配置文件算是做出来了，不过目前只写了个qq邮箱的配置
- 在用户的目录下 创建了一个Live2dChatPlugin 文件夹 配置文件就存在里面
- mail.enable=T
是否启动邮箱监听功能  T 开启/F关闭； 默认关闭
- mail.qq.account=418379149@qq.com
需要监听的qq邮箱
- mail.qq.key=xxxxxxxxxxx
qq邮箱授权码 , 在qq邮箱-->设置-->账户 中找到开启 imap/smtp服务后获得
- mail.qq.folders=apple,steam
默认为空则只监听收件箱，如果自己创建过新的收信规则 和自定义收件箱中的话 请加上想监听的自定义文件夹，多个用 英文逗号隔开

### 2019年05月20日 ~~发现bug~~ 已解决
- 保持邮件监听的线程似乎出了一些问题，在调用后监听通知会失效，换句话说就是在~~第一次调用后~~ 9分钟后 邮箱监听失效。目前在尝试解决中....
- bug已解决 已打包修复后的版本


### 2019年05月22日
- 新增了一些启动时的提示
- 修复Folder长时间监后断开的错误，增加了断线重连机制
- 增加腾讯企业邮箱的支持（不支持指定额外收件夹，只监听收件箱）

### 预览
#### 总览
![总览](img/main.png)

#### 隐藏
![隐藏](img/hiden.png)

#### 对话
![对话](img/talk.png)
![智能对答](img/talk2.png)
![天气预报](img/talk3.png)

#### 菜单
![菜单](img/menu.png)

#### 不负责猜双色球
![双色球](img/lottery.png)

#### 邮件提示
![邮件提示](img/mail.png)

#### 番剧查询
![番剧查询](img/bangumi.png)




### 参考资料
- [茉莉机器人API][1]
- [Live2DViewerEX中文文档][2]

[1]: http://www.itpk.cn/
[2]: http://live2d.pavostudio.com/doc/zh-cn/exapi/

## 版权声明

**API 版权属于原作者，仅供研究学习，不得用于商业用途**

MIT © WhiteMagic2014
