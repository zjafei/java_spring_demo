package com.example.demo;

import java.net.URL;
import java.net.URLConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.util.Calendar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.IOUtils;
import com.alibaba.fastjson.JSON;

@SpringBootApplication
@RestController
public class DemoApplication {

  // private final int spd = 5; // 语速，取值0-15，默认为5中语速
  // private final int ctp = 1; // 客户端类型选择，web端填写固定值1
  // private final String lan = "zh"; // 固定值zh。语言选择,目前只有中英文混合模式，填写固定值zh
  private final String documentRoot = new File(Thread.currentThread().getContextClassLoader().getResource("").getPath())
      .getParentFile().getParent();

  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }

  static class Oauth {
    private String token;
    private int expires;

    public Oauth() {
      this.token = "";
      this.expires = 0;
    }

    public int getExpires() {
      return expires;
    }

    public String getToken() {
      return token;
    }

    public void setExpires(int expires) {
      this.expires = expires;
    }

    public void setToken(String token) {
      this.token = token;
    }
  }

  class Content {
    private String url;

    public Content() {
      this.url = "";
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getUrl() {
      return this.url;
    }
  }

  class ApiResponse {
    private int code;
    private String msg;
    private Content content;

    public ApiResponse() {
      this.code = 5;
      this.msg = null;
      this.content = new Content();
    }

    public void set(int code, String msg) {
      this.code = code;
      this.msg = msg;
    }

    public int getCode() {
      return this.code;
    }

    public String getMsg() {
      return this.msg;
    }

    public Content getContent() {
      return this.content;
    }
  }

  private String getTokenFilePath() {
    return this.documentRoot + File.separator + "public" + File.separator + "token";
  }

  private Oauth getToken() {
    Oauth oa = new Oauth();
    String appKey = "jF3lMAKtYtgtQxL71Ek1yq4X";
    String appSecret = "S1X01TBdFedFZFM7PKb6Q7jgvqDq6AAg";
    String oauthUrl = "https://openapi.baidu.com/oauth/2.0/token?grant_type=client_credentials&client_id=" + appKey
        + "&client_secret=" + appSecret;
    try {
      URL url = new URL(oauthUrl);
      InputStreamReader isr = new InputStreamReader(url.openStream(), "utf-8");
      BufferedReader br = new BufferedReader(isr);
      String strRead = br.readLine();

      if (strRead.length() > 0) {
        Object tokenJson = JSON.parse(strRead);
        if (tokenJson instanceof JSONObject) {
          JSONObject tokenJSONObject = (JSONObject) tokenJson;
          oa.setToken((String) tokenJSONObject.get("access_token"));
          oa.setExpires(
              (int) tokenJSONObject.get("expires_in") + (int) (Calendar.getInstance().getTimeInMillis() / 1000l));
          FileWriter writer = new FileWriter(this.getTokenFilePath());
          writer.write(JSONObject.toJSONString(oa));
          writer.close();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return oa;
  }

  // 读取token
  private Oauth getOauth() {
    Oauth oa = new Oauth();
    int c = 0;
    try {
      FileReader reader = new FileReader(this.getTokenFilePath());
      String cl = "";
      c = reader.read();
      while (c != -1) {
        cl = cl + (char) c;
        c = reader.read();
      }
      reader.close();
      if (cl.length() > 0) {
        Object clJson = JSON.parse(cl);
        if (clJson instanceof JSONObject) {
          Oauth oaTemp = JSONObject.toJavaObject((JSONObject) clJson, Oauth.class);
          // 时间过期
          if (oaTemp.getExpires() < (int) (Calendar.getInstance().getTimeInMillis() / 1000l)) {
            oa = this.getToken();
          } else {
            oa = oaTemp;
          }
        }
      } else {
        oa = this.getToken();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return oa;
  }

  private ApiResponse validation(String tex, String cuId, String tok) {
    ApiResponse ar = new ApiResponse();
    if (tex.length() == 0) {// 合成的文本无效
      ar.set(1, "tex is undefined");
    } else if (tok.length() == 0) {
      ar.set(2, "tok is undefined");
    } else {
      ar.set(0, "success");
    }
    // TODO 数据库的有效性
    // else {
    // $tokenRes = $users->db()->field(['endTime', 'domain'])->where('token',
    // $tok)->find();
    // if (null === $tokenRes) {// token 不存在
    // $response['code'] = 3;
    // $response['msg'] = 'tok is invalid';
    // } elseif ($tokenRes['domain'] !== $cuid) {// 域名不符
    // $response['code'] = 2;
    // $response['msg'] = 'cuid is invalid';
    // } elseif (0 !== $tokenRes['endTime'] && $tokenRes['endTime'] <= time()) {//
    // 时间到期
    // $response['code'] = 4;
    // $response['msg'] = 'be expired';
    // } else {
    // $response['code'] = 0;
    // $response['msg'] = 'success';
    // }
    // }
    return ar;
  }

  @GetMapping("/audio-url")
  public String audioUrl(HttpServletRequest request, @RequestParam(value = "tex", defaultValue = "") String tex, // 要转语音的文字
      @RequestParam(value = "tok", defaultValue = "") String tok, // 用户的 tok
      @RequestParam(value = "cuId", defaultValue = "") String cuId, // 用户的 id
      @RequestParam(value = "callback", defaultValue = "") String callback // jsonp 的 callback ,
  ) {
    if (cuId.length() == 0) {
      String remoteScheme = request.getScheme();
      String remoteHost = request.getServerName();
      int remotePort = request.getServerPort();
      cuId = remoteScheme + "://" + remoteHost + (remotePort == 80 ? "" : (":" + remotePort));
    }

    ApiResponse ar = this.validation(tex, cuId, tok);
    if (ar.getCode() == 0) {
      Oauth ao = this.getOauth();
      String text2audioUrl = "https://tsn.baidu.com/text2audio?tex=" + tex + "&cuid=" + cuId + "&tok=" + ao.getToken()
          + "&lan=zh&ctp=1";
      try {
        ar.getContent().setUrl(Util.encryptAES(text2audioUrl));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    String arJson = JSONObject.toJSONString(ar);

    return callback.length() == 0 ? arJson : callback + "(" + arJson + ")";
  }

  @GetMapping("/audio")
  public void audio(HttpServletRequest request, HttpServletResponse response,
      @RequestParam(value = "url", defaultValue = "") String url) {
    if (url.length() > 0) {
      try {
        String aUrl = Util.decryptAES(url);
        System.out.println(aUrl);
        URL audioUrl = new URL(aUrl);
        InputStream inputStream = audioUrl.openStream();
        response.setContentType("audio/mp3;charset=UTF-8");
        IOUtils.copy(audioUrl.openStream(), response.getOutputStream());
        response.flushBuffer();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

}
