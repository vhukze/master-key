package com.vhukze.masterkey.utils;

import cn.hutool.core.util.StrUtil;
import com.vhukze.masterkey.entity.ExEnum;
import com.vhukze.masterkey.exception.KeyException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.Objects;
import java.util.Properties;

/**
 * 配置文件工具类
 *
 * @author vhukze
 * date 2023/3/15 - 17:23
 */
@Log4j2
public class ConfigUtil {

    /**
     * 获取项目当前使用的配置文件对象
     */
    public static Properties getAppYml() {
        // 1 加载配置文件
        Resource app = new ClassPathResource("application.yaml");
        if (!app.exists()) {
            app = new ClassPathResource("application.yaml");
            if (!app.exists()) {
                app = new ClassPathResource("application.properties");
            } else {
                throw new KeyException(ExEnum.E10);
            }
        }

        // 2 将加载的配置文件交给 YamlPropertiesFactoryBean
        YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
        yamlPropertiesFactoryBean.setResources(app);

        // 3 将yml转换成 key：val
        Properties properties = yamlPropertiesFactoryBean.getObject();
        String active = null;
        if (properties != null) {
            active = properties.getProperty("spring.profiles.active");
        }
        if (StrUtil.isBlank(active)) {
            log.error("未找到spring.profiles.active配置");
        } else {
            // 添加环境配置文件内容
            Resource app1 = new ClassPathResource("application-" + active + "." +
                    (Objects.requireNonNull(app.getFilename()).split("\\.")[1]));
            yamlPropertiesFactoryBean.setResources(app, app1);
        }
        return yamlPropertiesFactoryBean.getObject();
    }

}
