# master-key

## 介绍
用来实现接口参数解密的工具，只需引入依赖，在配置文件写明加密的配置，在接口上使用指定注解即可实现该接口的参数解密。并支持使用validation模块的注解进行参数校验，支持分组校验功能

## 软件架构
使用java8，springboot2.x.x，一个简单的springboot starter 启动器，功能中用到的工具类是hutool


## 安装教程

### 1.  依赖
**目前还没有发布到中央仓库，只能把代码拉到本地打包使用**
**使用时的依赖**
```
        <dependency>
            <groupId>com.vhukze</groupId>
            <artifactId>master-key-spring-boot-starter</artifactId>
            <version>{{last-version}}</version>
        </dependency>
```
### 2.  配置
>支持的对称加密方式：SM4，AES，DES，DESede
>支持的非对称加密方式：RSA，SM2
---
**在配置类中注册参数解析器**
```
import com.vhukze.masterkey.master.DecodeResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.util.List;

/**
 * webmvc配置
 */
@Configuration
public class MyWebConfig implements WebMvcConfigurer {

    @Resource
    private DecodeResolver decodeResolver;

    /**
     * 注册自定义HandlerMethodArgumentResolver  接口参数解密
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(decodeResolver);
    }
}
```
---
**对称加密配置示例(配置到application.yml中)**
```
master-key:
  # 加密方式
  encode: SM4
  # 使用json格式参数时，解密之前json的key 不配置此参数则代表使用text格式参数，只传递加密后的字符串
  json-key: str
  # 加密模式
  mode: CBC
  # 填充方式
  padding: ISO10126Padding
  # 秘钥
  key: 1234123412ABCDEF
  # 盐值
  salt: ABCDEF1234123412
```
---
**对称加密配置项的可配置值**
加密方式（encode）|加密模式（mode）|填充方式（padding）
--|:--:|--:
|SM4|NONE|NoPadding|
|AES|CBC|ZeroPadding|
|DES|CFB|ISO10126Padding|
|DESede|CTR|OAEPPadding|
||CTS|PKCS1Padding|
||ECB|PKCS5Padding|
||OFB|SSL3Padding|
||PCBC||

**非对称加密配置示例**
```
master-key:
  # 加密方式
  encode: SM2
  # 使用json格式参数时，解密之前json的key 不配置此参数则代表使用text格式参数，只传递加密后的字符串
  json-key:
  # 公钥
  public-key: MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEDRhJQbkA5SKceAaJmtdOBiRzCqwei4WRzAkBrZ9SkBZhZ1zC4nteRLVi754MsI/8vsiNK2lV518E8RaNw+mnLA==
  # 私钥
  private-key: MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQ
```

### 3.  注意事项
**使用SM4和SM2国密加密算法时，需要引入国密加密的依赖 如下**
```
        <dependency>
            <groupId>org.bouncycastle</groupId>
            <artifactId>bcprov-jdk15to18</artifactId>
            <version>1.69</version>
        </dependency>
```

## 使用说明
>支持自定义实体类、基础数据类型及其包装类、集合类型、map类型。**加密前参数格式跟正常请求接口时相同**

**例如，自定义实体类方式：首先前端把待传递的json参数使用配置的加密方式加密模式等进行加密，例：加密前：{"id":3,"count":4}，加密后：c7dc378bf0c4da001466818765813a506b1a6b37e960b7ca**

**接口用来接收参数的实体类：**
```
@Data
public class Stock {
    private Integer id;
    private Integer count;
}
```
**接口使用@ParamsDecode注解，标明此接口需要参数解密，如下**
```
    @ParamsDecode
    @PostMapping("decode")
    public String decode(Stock stock){
        return "";
    }
```
### 1.  json格式传参
在配置文件配置好json-key，并使用配置的json-key构建json字符串，比如配置的json-key为str，json字符串如下
```
{
    "str":"c7dc378bf0c4da001466818765813a506b1a6b37e960b7ca"
}
```
**接下来使用构建好的json字符串作为参数请求接口即可**
---
### 2. text格式传参

**注意，不配置json-key即为使用text格式传参**

参数直接就是加密后的字符串，即c7dc378bf0c4da001466818765813a506b1a6b37e960b7ca

**注意，不是表单传参，Content-Type为application/text**
---
### 3.  支持validation模块校验参数，支持分组校验功能
例如：
```
@Data
public class Stock {
    @Max(3)
    @NotNull(groups = Edit.class)
    private Integer id;
    @NotBlank(groups = {Add.class, Edit.class})
    private String name;
    @Min(3)
    private Integer count;
    
    public interface Add {

    }

    public interface Edit {

    }
}
```
```
@ParamsDecode
@PostMapping("decode")
public String decode(@Validated(value = Add.class) Stock stock) {
    return "";
}
```

## 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


## 特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
