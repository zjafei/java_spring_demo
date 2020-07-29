package com.example.demo;

import java.io.File;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class Util {

  static String getRootPath() {
    File file = new File(Thread.currentThread().getContextClassLoader().getResource("").getPath()).getParentFile();
    return file.getParent();
  }

  private static final String key="ERIC$19801102&ma";
  private static final String iv ="NIfb&95GUY86Gfgh";
  private static Base64.Decoder decoder = Base64.getDecoder();
  private static Base64.Encoder encoder = Base64.getEncoder();

  /**
   * @author miracle.qu
   * @Description AES算法加密明文
   * @param data 明文
   * @param key  密钥，长度16
   * @param iv   偏移量，长度16
   * @return 密文
   */
  public static String encryptAES(String data) throws Exception {
    try {
      Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
      int blockSize = cipher.getBlockSize();
      byte[] dataBytes = data.getBytes();
      int plaintextLength = dataBytes.length;

      if (plaintextLength % blockSize != 0) {
        plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
      }

      byte[] plaintext = new byte[plaintextLength];
      System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);

      SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
      IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes()); // CBC模式，需要一个向量iv，可增加加密算法的强度

      cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
      byte[] encrypted = cipher.doFinal(plaintext);

      return Util.encoder.encodeToString(encrypted);

    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * @author miracle.qu
   * @Description AES算法解密密文
   * @param data 密文
   * @param key  密钥，长度16
   * @param iv   偏移量，长度16
   * @return 明文
   */
  public static String decryptAES(String data) throws Exception {
    try {
      byte[] encrypted1 = Util.decoder.decode(data);// 先用base64解密

      Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
      SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
      IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());

      cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

      byte[] original = cipher.doFinal(encrypted1);
      String originalString = new String(original);
      return originalString.trim();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

}