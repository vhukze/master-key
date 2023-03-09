package com.vhukze.masterkey.config;

import com.vhukze.masterkey.entity.ParamsDecodeConfig;
import com.vhukze.masterkey.master.DecodeResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * 自动配置类
 *
 * @author vhukze
 * @date 2023/3/8 - 16:04
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({DecodeResolver.class})
@EnableConfigurationProperties({ParamsDecodeConfig.class})
public class DecodeAutoConfiguration {

    @Resource
    private ParamsDecodeConfig config;

    @Bean
    @ConditionalOnMissingBean
    public DecodeResolver decodeResolver() {
        DecodeResolver decodeResolver = new DecodeResolver();
        decodeResolver.setConfig(config);
        return decodeResolver;
    }
}
