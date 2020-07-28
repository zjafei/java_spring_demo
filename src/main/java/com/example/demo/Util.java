package com.example.demo;

import java.io.File;

public class Util {
  static String getRootPath() {
    File file = new File(Thread.currentThread().getContextClassLoader().getResource("").getPath()).getParentFile();
    return file.getParent();
  }
}