package com.vhukze.masterkey.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 参数解析配置实体
 * @author vhukze
 * @date 2023/3/8 - 12:13
 */
@Data
@Component
@ConfigurationProperties(prefix = "master-key")
public class ParamsDecodeConfig {
    /**
     * 解密前的json键值对的key名称配置，默认为str
     */
    private String jsonKey = "str";

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
     * 使用的秘钥 （对称加密配置秘钥，非对称加密配置私钥）
     */
    private String key;
}
