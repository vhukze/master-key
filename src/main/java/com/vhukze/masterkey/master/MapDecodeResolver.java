package com.vhukze.masterkey.master;

import com.vhukze.masterkey.anno.ParamsDecode;
import com.vhukze.masterkey.entity.ParamsDecodeConfig;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.MapMethodProcessor;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Map;

/**
 * map参数类型的解析器 TODO map参数解析未实现
 *
 * @author vhukze
 * date 2023/3/10 - 20:02
 */
public class MapDecodeResolver extends MapMethodProcessor {

    private ParamsDecodeConfig config;

    public void setConfig(ParamsDecodeConfig config) {
        this.config = config;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Map.class.isAssignableFrom(parameter.getParameterType()) &&
                (parameter.hasMethodAnnotation(ParamsDecode.class) || parameter.hasParameterAnnotation(ParamsDecode.class));
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer, NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {
        DecodeResolver decodeResolver = new DecodeResolver();
        decodeResolver.setConfig(config);
        return decodeResolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
    }
}
