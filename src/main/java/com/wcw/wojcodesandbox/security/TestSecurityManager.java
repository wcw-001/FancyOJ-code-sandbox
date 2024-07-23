package com.wcw.wojcodesandbox.security;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TestSecurityManager {
    public static void main(String[] args) {
        //System.setSecurityManager(new MySecurityManager());
        //List<String> stringList = FileUtil.readLines("D:\\API接口项目\\woj-code-sandbox\\src\\main\\resources\\application.yml", StandardCharsets.UTF_8);
        FileUtil.writeString("aaa", "aaa", Charset.defaultCharset());
    }
}
