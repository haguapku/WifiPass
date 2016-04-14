package com.haguapku.wificlient.util;

import com.haguapku.library.common.util.DmLoader;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by MarkYoung on 15/11/2.
 */
public class AESUtil {

    private static byte[] iv = new byte[] {				//算法参数
            -12,35,-25,65,45,-87,95,-22,-15,45,55,-66,32,5-4,84,55
    };

//    private static String secret = DmLoader.getSecret();
    private static String secret = "abc";
    private static SecretKey key;    					//加密密钥
    private static AlgorithmParameterSpec paramSpec;    //算法参数
    private static Cipher ecipher;    					//加密算法

    static{
        try {
            key = new SecretKeySpec(asBin(secret), "AES");
            //使用iv中的字节作为IV来构造一个 算法参数。
            paramSpec = new IvParameterSpec(iv);
            //生成一个实现指定转换的 Cipher 对象
            ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException e) {
        } catch (NoSuchPaddingException e) {
        }
    }

    /**
     * 加密，使用指定数据源生成密钥，使用用户数据作为算法参数进行AES加密
     * @param msg 加密的数据
     * @return
     */
    public static synchronized String encrypt(String msg) {
        try {
            //用密钥和一组算法参数初始化此 cipher
            ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
            //加密并转换成16进制字符串
            return asHex(ecipher.doFinal(msg.getBytes()));
        } catch (BadPaddingException e) {
        } catch (InvalidKeyException e) {
        } catch (InvalidAlgorithmParameterException e) {
        } catch (IllegalBlockSizeException e) {
        }
        return msg;
    }

    /**
     * 解密，对生成的16进制的字符串进行解密
     * @param value 解密的数据
     * @return
     */
    public static synchronized String decrypt(String value) {
        try {
            ecipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
            return new String(ecipher.doFinal(asBin(value)));
        } catch (BadPaddingException e) {
        } catch (InvalidKeyException e) {
        } catch (InvalidAlgorithmParameterException e) {
        } catch (IllegalBlockSizeException e) {
        } catch (Exception e) {
        }
        return "";
    }

    /**
     * 将字节数组转换成16进制字符串
     * @param buf
     * @return
     */
    public static String asHex(byte buf[]) {
        StringBuffer strbuf = new StringBuffer(buf.length * 2);
        int i;
        for (i = 0; i < buf.length; i++) {
            if (((int) buf[i] & 0xff) < 0x10)//小于十前面补零
                strbuf.append("0");
            strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
        }
        return strbuf.toString();
    }

    /**
     * 将16进制字符串转换成字节数组
     * @param src
     * @return
     */
    private static byte[] asBin(String src) {
        if (src.length() < 1)
            return null;
        byte[] encrypted = new byte[src.length() / 2];
        for (int i = 0; i < src.length() / 2; i++) {
            int high = Integer.parseInt(src.substring(i * 2, i * 2 + 1), 16);//取高位字节
            int low = Integer.parseInt(src.substring(i * 2 + 1, i * 2 + 2), 16);//取低位字节
            encrypted[i] = (byte) (high * 16 + low);
        }
        return encrypted;
    }

    public static String md5(String content) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            byte[] code = digest.digest(content.getBytes());
            if (code != null) {
                return asHex(code);
            }
        } catch (NoSuchAlgorithmException e) {
        }
        return "";
    }

}
