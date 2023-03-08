package com.vhukze.masterkey.exception;

import com.vhukze.masterkey.entity.ExEnum;

/**
 * 异常类
 *
 * @author vhukze
 * @date 2023/3/8 - 12:27
 */
public class KeyException extends RuntimeException {

    public KeyException(ExEnum exEnum) {
        super(exEnum.getMsg());
    }

}
