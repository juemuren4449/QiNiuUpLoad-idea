package com.juemuren;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * 加密辅助类
 *
 * @author cychen
 */
public class EncryptHelper {

    /**
     * 构造函数
     */
    private EncryptHelper() {
    }

    /**
     * 转化MD5编码
     *
     * @param value 字符串值
     * @return MD5编码
     */
    public static String toMD5(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(value.getBytes());
            return HexHelper.toHexFromBytes(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            System.out.println("转化MD5编码异常" + e);
        }
        return "";
    }

    /**
     * 返回七牛图片上传文件名
     * @return
     */
    public static String FileNameToMD5() {
        String value = UUID.randomUUID().toString() + System.currentTimeMillis();
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(value.getBytes());
            return HexHelper.toHexFromBytes(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            System.out.println("转化MD5编码异常" + e);
        }
        return "";
    }

}
