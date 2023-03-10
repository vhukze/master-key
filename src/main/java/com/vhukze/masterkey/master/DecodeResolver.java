package com.vhukze.masterkey.master;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.*;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.asymmetric.AbstractAsymmetricCrypto;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.symmetric.*;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.vhukze.masterkey.anno.ParamsDecode;
import com.vhukze.masterkey.entity.ParamsDecodeConfig;
import com.vhukze.masterkey.entity.ExEnum;
import com.vhukze.masterkey.exception.KeyException;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.MethodParameter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.*;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 参数解析器
 *
 * @author vhukze
 * date 2023/3/8 12:10
 */
@Log4j2
public class DecodeResolver implements HandlerMethodArgumentResolver {

    /**
     * 解密前的加密JSON中的key
     */
    private static String jsonKey;

    private ParamsDecodeConfig config;

    public void setConfig(ParamsDecodeConfig config) {
        this.config = config;
    }

    /**
     * 目前支持的对称加密算法
     */
    private static final List<String> symmetryList = CollUtil.toList("SM4", "AES", "DES", "DESede");

    /**
     * 目前支持的非对称加密算法
     */
    private static final List<String> asymmetricList = CollUtil.toList("RSA", "SM2");

    /**
     * 如果接口或者接口参数有解密注解，就解析
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasMethodAnnotation(ParamsDecode.class) || parameter.hasParameterAnnotation(ParamsDecode.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer modelAndViewContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory webDataBinderFactory) throws IOException, InstantiationException, IllegalAccessException {

        // 如果注解中指定了解密方式，重新读取配置文件中，获取指定配置
        ParamsDecode paramsDecode = parameter.getMethodAnnotation(ParamsDecode.class);
        if (StrUtil.isNotBlank(paramsDecode.value())) {
            this.resetConfig(paramsDecode.value());
        }

        // 获取解密前的加密JSON中的key，如果配置了就使用json键值对解析，没有配置就是单独的加密字符串
        if (StrUtil.isNotBlank(config.getJsonKey())) {
            jsonKey = config.getJsonKey();
        }

        if (StrUtil.isBlank(config.getEncode())) {
            throw new KeyException(ExEnum.E01);
        }

        SymmetricCrypto symmetric = null;
        AbstractAsymmetricCrypto<?> asymmetric = null;
        // 对称加密
        if (symmetryList.stream().anyMatch(i -> i.equalsIgnoreCase(config.getEncode()))) {
            symmetric = this.getSymmetry();
        }
        // 非对称加密
        else if (asymmetricList.stream().anyMatch(i -> i.equalsIgnoreCase(config.getEncode()))) {
            asymmetric = this.getAsymmetry();
        } else {
            throw new KeyException(ExEnum.E03);
        }

        // 获取post请求的json字符串
        String postStr = this.getPostStr(webRequest);

        // 接口参数的字节码对象
        Class<?> parameterType = parameter.getParameterType();

        //获取加密的请求数据并解密
        Object beforParam;
        String afterParam = null;
        if (StrUtil.isNotBlank(config.getJsonKey())) {
            // 配置了json-key，但参数为text加密字符串
            if (!JSONUtil.isJson(postStr)) {
                throw new KeyException(ExEnum.E07);
            }
            beforParam = JSONUtil.parseObj(postStr, true).get(jsonKey);
        } else {
            // 参数为json格式，配置文件未配置json-key
            if (JSONUtil.isJson(postStr)) {
                throw new KeyException(ExEnum.E06);
            }
            beforParam = postStr;
        }

        if (beforParam != null) {
            if (StrUtil.isBlank(beforParam.toString())) {
                afterParam = "";
            } else {
                if (symmetric != null) {
                    afterParam = symmetric.decryptStr(beforParam.toString(), CharsetUtil.CHARSET_UTF_8);
                } else if (asymmetric != null) {
                    afterParam = asymmetric.decryptStr(beforParam.toString(), KeyType.PrivateKey);
                }
            }
        }

        // 如果是自定义的实体类参数，把请求参数封装
        if (parameterType.getClassLoader() != null) {

            // 校验参数
            if (parameter.hasParameterAnnotation(Validated.class)) {
                Validated validated = parameter.getParameterAnnotation(Validated.class);
                this.verifyObjField(afterParam, parameterType, validated.value());
            }

            //json转对象  // 这里的return就会把转化过的参数赋给控制器的方法参数
            return JSONUtil.toBean(afterParam, parameterType);

            // 如果是非集合类(基础类型包装类)
        } else if (!Iterable.class.isAssignableFrom(parameterType)) {

            this.verifyOneField(parameter, afterParam);
            return Convert.convert(parameterType, afterParam);

            //如果是集合类
        } else if (Iterable.class.isAssignableFrom(parameterType)) {

            //转成对象数组
            JSONArray jsonArray = JSONUtil.parseArray(afterParam);

            this.verifyCollField(parameter, jsonArray);

            return jsonArray.toList(Object.class);

            // 如果是map
        } else if (Map.class.isAssignableFrom(parameterType)) {

            //转成对象数组
            JSONObject jsonObject = JSONUtil.parseObj(afterParam);

            return Convert.toMap(String.class, Object.class, jsonObject);
        }

        return null;

    }

    private AbstractAsymmetricCrypto<?> getAsymmetry() {

        if (StrUtil.isBlank(config.getPublicKey())) {
            throw new KeyException(ExEnum.E08);
        }
        if (StrUtil.isBlank(config.getPrivateKey())) {
            throw new KeyException(ExEnum.E09);
        }

        switch (config.getEncode().toUpperCase(Locale.ROOT)) {
            case "SM2":
                return new SM2(config.getPrivateKey(), config.getPublicKey());
            case "RSA":
                return new RSA(config.getPrivateKey(), config.getPublicKey());
            default:
                return null;
        }
    }

    /**
     * 重新获取指定的配置 TODO
     *
     * @param value 注解中指定的加密方式
     */
    private void resetConfig(String value) {

    }

    /**
     * 获取对称加密对象
     */
    private SymmetricCrypto getSymmetry() {

        // 获取解密秘钥
        String key = config.getKey();
        if (StrUtil.isBlank(key)) {
            throw new KeyException(ExEnum.E02);
        }

        // 获取该加密方式的加密模式
        String mode = config.getMode();
        Mode modeEnum;
        if (StrUtil.isBlank(mode)) {
            modeEnum = Mode.NONE;
        } else {
            modeEnum = EnumUtil.getEnumMap(Mode.class).get(mode);
            if (modeEnum == null) {
                throw new KeyException(ExEnum.E05);
            }
        }

        // 获取填充方式
        String padding = config.getPadding();
        Padding paddingEnum;
        if (StrUtil.isBlank(padding)) {
            paddingEnum = Padding.NoPadding;
        } else {
            paddingEnum = EnumUtil.getEnumMap(Padding.class).get(padding);
            if (paddingEnum == null) {
                throw new KeyException(ExEnum.E04);
            }
        }

        switch (config.getEncode().toUpperCase(Locale.ROOT)) {
            case "AES":
                return new AES(modeEnum, paddingEnum, key.getBytes(), config.getSalt().getBytes());
            case "SM4":
                return new SM4(modeEnum, paddingEnum, key.getBytes(), config.getSalt().getBytes());
            case "DES":
                return new DES(modeEnum, paddingEnum, key.getBytes(), config.getSalt().getBytes());
            case "DESEDE":
                return new DESede(modeEnum, paddingEnum, key.getBytes(), config.getSalt().getBytes());
            default:
                return null;
        }
    }

    /**
     * 校验单个参数
     */
    private void verifyOneField(MethodParameter parameter, Object value) {
        for (Annotation annotation : parameter.getParameterAnnotations()) {
            if (annotation instanceof NotBlank) {
                if (value == null || StrUtil.isBlank(value.toString())) {
                    throw new ConstraintViolationException("参数值不能为空白字符串", null);
                }
            }
            if (annotation instanceof NotNull) {
                if (value == null) {
                    throw new ConstraintViolationException("参数值不能为null", null);
                }
            }
            // 只能是字符串类型
            if (annotation instanceof Size) {
                Size size = (Size) annotation;
                if (value != null && (value.toString().length() < size.min() || value.toString().length() > size.max())) {
                    throw new ConstraintViolationException("参数长度不符合要求", null);
                }
            }

            if (annotation instanceof Max) {
                Max max = (Max) annotation;
                if (value != null && Convert.toLong(value) > max.value()) {
                    throw new ConstraintViolationException("参数值太大", null);
                }
            }

            if (annotation instanceof Min) {
                Min min = (Min) annotation;
                if (value != null && Convert.toLong(value) < min.value()) {
                    throw new ConstraintViolationException("参数值太小", null);
                }
            }

            if (annotation instanceof Pattern) {
                Pattern pattern = (Pattern) annotation;
                if (value != null && !ReUtil.isMatch(pattern.regexp(), value.toString())) {
                    throw new ConstraintViolationException("参数值正则校验失败", null);
                }
            }

        }

    }

    /**
     * 校验集合类型
     */
    private void verifyCollField(MethodParameter parameter, JSONArray jsonArray) {

        for (Annotation annotation : parameter.getParameterAnnotations()) {
            if (annotation instanceof NotEmpty) {
                if (jsonArray == null || jsonArray.size() == 0) {
                    throw new ConstraintViolationException("集合不能为空", null);
                }
            }
            if (annotation instanceof NotNull) {
                if (jsonArray == null) {
                    throw new ConstraintViolationException("集合不能为null", null);
                }
            }
            if (annotation instanceof Size) {
                Size size = (Size) annotation;
                if (jsonArray.size() < size.min() || jsonArray.size() > size.max()) {
                    throw new ConstraintViolationException("集合长度不符合要求", null);
                }
            }
        }
    }

    /**
     * 校验实体类参数
     *
     * @param param  前端传的参数（json字符串）
     * @param clazz  接口实体类参数的字节码对象
     * @param groups 校验那些组
     */
    private void verifyObjField(String param, Class<?> clazz, Class<?>[] groups) {

        // 前端传的参数
        JSONObject jsonObj = StrUtil.isBlank(param) ? new JSONObject() : JSONUtil.parseObj(param);

        // 实体类所有字段
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            // 字段如果不可访问，设置可访问
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }

            Annotation[] annotations = field.getDeclaredAnnotations();

            for (Annotation annotation : annotations) {

                if (annotation instanceof NotNull) {
                    NotNull notNull = (NotNull) annotation;
                    if ((ArrayUtil.isEmpty(groups) && ArrayUtil.isEmpty(notNull.groups()))
                            || ArrayUtil.containsAny(groups, notNull.groups())) {
                        if (jsonObj.get(field.getName()) == null) {
                            throw new ConstraintViolationException("字段\"" + field.getName() + "\"值不能为null", null);
                        }
                    }
                }

                if (annotation instanceof NotBlank) {
                    NotBlank notBlank = (NotBlank) annotation;
                    if ((ArrayUtil.isEmpty(groups) && ArrayUtil.isEmpty(notBlank.groups()))
                            || ArrayUtil.containsAny(groups, notBlank.groups())) {
                        Object val = jsonObj.get(field.getName());
                        if (val == null || StrUtil.isBlank(val.toString())) {
                            throw new ConstraintViolationException("字段\"" + field.getName() + "\"值不能为空白字符串", null);
                        }
                    }
                }

                if (annotation instanceof Size) {
                    Size size = (Size) annotation;
                    if ((ArrayUtil.isEmpty(groups) && ArrayUtil.isEmpty(size.groups()))
                            || ArrayUtil.containsAny(groups, size.groups())) {
                        Object val = jsonObj.get(field.getName());
                        if (val instanceof String) {
                            if (val.toString().length() < size.min() || val.toString().length() > size.max()) {
                                throw new ConstraintViolationException("字段\"" + field.getName() + "\"值长度或大小不符合要求", null);
                            }
                        }
                        if (val instanceof Collection && Convert.toList(val).size() == 0) {
                            throw new ConstraintViolationException("字段\"" + field.getName() + "\"值长度或大小不符合要求", null);
                        }
                    }
                }

                if (annotation instanceof Pattern) {
                    Pattern pattern = (Pattern) annotation;
                    if ((ArrayUtil.isEmpty(groups) && ArrayUtil.isEmpty(pattern.groups()))
                            || ArrayUtil.containsAny(groups, pattern.groups())) {
                        Object val = jsonObj.get(field.getName());
                        if (val != null && !ReUtil.isMatch(pattern.regexp(), val.toString())) {
                            throw new ConstraintViolationException("字段\"" + field.getName() + "\"值正则校验失败", null);
                        }
                    }
                }

                if (annotation instanceof Max) {
                    Max max = (Max) annotation;
                    if ((ArrayUtil.isEmpty(groups) && ArrayUtil.isEmpty(max.groups()))
                            || ArrayUtil.containsAny(groups, max.groups())) {
                        Object val = jsonObj.get(field.getName());
                        if (val != null && Convert.toInt(val) > max.value()) {
                            throw new ConstraintViolationException("字段\"" + field.getName() + "\"值太大", null);
                        }
                    }
                }

                if (annotation instanceof Min) {
                    Min min = (Min) annotation;
                    if ((ArrayUtil.isEmpty(groups) && ArrayUtil.isEmpty(min.groups()))
                            || ArrayUtil.containsAny(groups, min.groups())) {
                        Object val = jsonObj.get(field.getName());
                        if (val != null && Convert.toInt(val) < min.value()) {
                            throw new ConstraintViolationException("字段\"" + field.getName() + "\"值太小", null);
                        }
                    }
                }

                if (annotation instanceof Null) {
                    Null n = (Null) annotation;
                    if ((ArrayUtil.isEmpty(groups) && ArrayUtil.isEmpty(n.groups()))
                            || ArrayUtil.containsAny(groups, n.groups())) {
                        Object val = jsonObj.get(field.getName());
                        if (val != null) {
                            throw new ConstraintViolationException("字段\"" + field.getName() + "\"值不为null", null);
                        }
                    }
                }

            }


        }

    }

    private String getPostStr(NativeWebRequest webRequest) throws IOException {
        //获取post请求的json数据
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        int contentLength = request.getContentLength();
        if (contentLength < 0) {
            return null;
        }
        byte[] buffer = new byte[contentLength];
        for (int i = 0; i < contentLength; ) {

            int readlen = request.getInputStream().read(buffer, i,
                    contentLength - i);
            if (readlen == -1) {
                break;
            }
            i += readlen;
        }
        String str = new String(buffer, CharsetUtil.CHARSET_UTF_8);
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            //去掉json中的空格 换行符 制表符
            if (c != 32 && c != 13 && c != 10) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
