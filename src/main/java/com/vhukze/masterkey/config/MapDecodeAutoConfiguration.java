package com.vhukze.masterkey.config;

import com.vhukze.masterkey.entity.ParamsDecodeConfig;
import com.vhukze.masterkey.master.DecodeResolver;
import com.vhukze.masterkey.master.MapDecodeResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * 针对Map参数解析器的自动配置类
 *
 * @author vhukze
 * date 2023/3/10 - 16:04
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({MapDecodeResolver.class})
@EnableConfigurationProperties({ParamsDecodeConfig.class})
public class MapDecodeAutoConfiguration {

    @Resource
    private ParamsDecodeConfig config;

    @Bean
    @ConditionalOnMissingBean
    public MapDecodeResolver mapDecodeResolver() {
        MapDecodeResolver mapDecodeResolver = new MapDecodeResolver();
        mapDecodeResolver.setConfig(config);
        return mapDecodeResolver;
    }
}
