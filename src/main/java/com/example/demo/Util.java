package com.example.demo;

import java.io.File;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

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

  /**
     * UrlEncode， UTF-8 编码
     *
     * @param str 原始字符串
     * @return
     */
    public static String urlEncode(String str) {
      String result = null;
      try {
          result = URLEncoder.encode(str, "UTF-8");
      } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
      }
      return result;
  }

  /**
   * 从HttpURLConnection 获取返回的字符串
   *
   * @param conn
   * @return
   * @throws IOException
   * @throws DemoException
   */
  public static String getResponseString(HttpURLConnection conn) throws IOException {
      return new String(getResponseBytes(conn));
  }

  /**
   * 从HttpURLConnection 获取返回的bytes
   * 注意 HttpURLConnection自身问题， 400类错误，会直接抛出异常。不能获取conn.getInputStream();
   *
   * @param conn
   * @return
   * @throws IOException   http请求错误
   * @throws DemoException http 的状态码不是 200
   */
  public static byte[] getResponseBytes(HttpURLConnection conn) throws IOException {
      int responseCode = conn.getResponseCode();
      InputStream inputStream = conn.getInputStream();
      if (responseCode != 200) {
          System.err.println("http 请求返回的状态码错误，期望200， 当前是 " + responseCode);
          if (responseCode == 401) {
              System.err.println("可能是appkey appSecret 填错");
          }
          System.err.println("response headers" + conn.getHeaderFields());
          if (inputStream == null) {
              inputStream = conn.getErrorStream();
          }
          byte[] result = getInputStreamContent(inputStream);
          System.err.println(new String(result));

      }

      byte[] result = getInputStreamContent(inputStream);
      return result;
  }

  /**
   * 将InputStream内的内容全部读取，作为bytes返回
   *
   * @param is
   * @return
   * @throws IOException @see InputStream.read()
   */
  public static byte[] getInputStreamContent(InputStream is) throws IOException {
      byte[] b = new byte[1024];
      // 定义一个输出流存储接收到的数据
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      // 开始接收数据
      int len = 0;
      while (true) {
          len = is.read(b);
          if (len == -1) {
              // 数据读完
              break;
          }
          byteArrayOutputStream.write(b, 0, len);
      }
      return byteArrayOutputStream.toByteArray();
  }

}