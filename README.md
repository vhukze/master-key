# master-key

#### 介绍
用来实现接口参数解密的工具，只需引入依赖，在配置文件写明加密的配置，在接口上使用指定注解即可实现该接口的参数解密。并支持使用validation模块的注解进行参数校验

#### 软件架构
就是一个简单的springboot starter 启动器，功能中用到的工具类是用的hutool


#### 安装教程

##### 1.  依赖

##### 2.  配置
>支持的对称加密方式：SM4，AES，DES，DESede
>支持的非对称加密方式：RSA，SM2
---
对称加密配置示例
(```)
master-key:
  # 加密方式
  encode: SM4
  # 解密前json的key 默认str
  json-key: vvv
  # 加密模式
  mode: CBC
  # 填充方式
  padding: ISO10126Padding
  # 秘钥
  key: 1234123412ABCDEF
  # 盐值
  salt: ABCDEF1234123412
(```)
---
**对称加密配置项的可配置值**
加密方式（encode）|加密模式（mode）|填充方式（padding）
--|:--:|--:
SM4|NONE|NoPadding
AES|CBC|ZeroPadding
DES|CFB|ISO10126Padding
DESede|CTR|OAEPPadding
|CTS|PKCS1Padding
|ECB|PKCS5Padding
|OFB|SSL3Padding
|PCBC|

##### 3.  xxxx

#### 使用说明

1.  xxxx
2.  xxxx
3.  xxxx

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
