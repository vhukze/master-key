package com.vhukze.masterkey.abs;

/**
 * 自定义解密，实现此接口即可集成自定义解密功能
 *
 * @author vhukze
 * date 2023/3/13 - 18:20
 */
public interface MkDecodeInterface {

    String decode(String before);

}
