package com.game.utils;

import cn.hutool.core.util.RandomUtil;
import org.springframework.util.DigestUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import static com.game.utils.SystemConstants.PADDING_LEN;

public class Encoder {

    public static String encode(String password) {
        // 生成盐
        String salt = RandomUtil.randomString(12);
        // 加密
        return encode(password, salt);
    }

    private static String encode(String password, String salt) {
        // 加密
        return salt + "@" + DigestUtils.md5DigestAsHex((password + salt).getBytes(StandardCharsets.UTF_8));
    }

    public static Boolean matches(String encodedPassword, String rawPassword) {
        if (encodedPassword == null || rawPassword == null) {
            return false;
        }
        if (!encodedPassword.contains("@")) {
            throw new RuntimeException("密码格式不正确！");
        }
        String[] arr = encodedPassword.split("@");
        // 获取盐
        String salt = arr[0];
        // 比较
        return encodedPassword.equals(encode(rawPassword, salt));
    }

    //AES加密
    public static String encryptAES(String plainText, String secretKey) throws Exception {
        secretKey = padKey(secretKey, PADDING_LEN);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    //AES解密
    public static String decryptAES(String encryptedText, String secretKey) throws Exception {
        secretKey = padKey(secretKey, PADDING_LEN);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private static String padKey(String key, int len) {
        // AES算法要求的密钥长度分别为16、24、32字节,不足部分用0填充，超出部分截取
        int keyLength = key.length();
        if (keyLength < len) {
            key = padRight(key, len);
        } else {
            key = key.substring(0, len);
        }
        return key;
    }

    private static String padRight(String str, int desiredLen) {
        char paddingChar = '\0'; // 使用空字符填充
        if (str.length() < desiredLen) {
            char[] padded = Arrays.copyOf(str.toCharArray(), desiredLen);
            Arrays.fill(padded, str.length(), desiredLen, paddingChar);
            return new String(padded);
        } else {
            return str.substring(0, desiredLen);
        }
    }

}
