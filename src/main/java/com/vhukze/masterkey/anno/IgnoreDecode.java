package com.vhukze.masterkey.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标明当前接口无需参数解密，配合全局加密配置（global-decode）使用
 *
 * @author vhukze
 * date 2023/3/13 - 17:09
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreDecode {

}
