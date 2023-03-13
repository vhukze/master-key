package com.vhukze.masterkey.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 参数解析配置实体
 * @author vhukze
 * date 2023/3/8 - 12:13
 */
@Data
@Component
@ConfigurationProperties(prefix = "master-key")
public class ParamsDecodeConfig {
    /**
     * 解密前的json键值对的key名称配置，默认为str
     */
    private String jsonKey;

    /**
     * 使用的加密方式
     */
    private String encode;

    /**
     * 使用的加密方式中的加密模式
     */
    private String mode;

    /**
     * 使用的加密方式中的填充方式
     */
    private String padding;

    /**
     * 盐值
     */
    private String salt;

    /**
     * 使用的秘钥
     */
    private String key;

    /**
     * 非对称加密使用的公钥
     */
    private String publicKey;

    /**
     * 非对称加密使用的私钥
     */
    private String privateKey;

    /**
     * 是否开启全局解密 默认false
     */
    private Boolean globalDecode = false;

}
