package com.wcw.wojcodesandbox.utils;

import com.wcw.wojcodesandbox.model.ExecuteMessage;
import org.springframework.util.StopWatch;

import javax.xml.stream.events.StartDocument;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 进程工具类
 */
public class ProcessUtils {
    public static ExecuteMessage runProcessAndGetMessage(Process runProcess,String opName){
        ExecuteMessage executeMessage = new ExecuteMessage();
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            //等待程序执行获取错误码
            int exitValue = runProcess.waitFor();
            executeMessage.setExitCode(exitValue);
            //正常退出
            if(exitValue == 0){
                System.out.println(opName+"编译成功");
                BufferedReader bufferReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                StringBuilder compileOutputStringBuilder = new StringBuilder();
                String compileOutputLine;
                while ((compileOutputLine = bufferReader.readLine()) != null){
                    compileOutputStringBuilder.append(compileOutputLine);
                }
                System.out.println(compileOutputStringBuilder);
                executeMessage.setMessage(compileOutputStringBuilder.toString());
            }else{
                System.out.println(opName+"失败,错误码："+exitValue);
                BufferedReader bufferReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                StringBuilder compileOutputStringBuilder = new StringBuilder();
                String compileOutputLine;
                while ((compileOutputLine = bufferReader.readLine()) != null){
                    compileOutputStringBuilder.append(compileOutputLine);
                }
                executeMessage.setMessage(compileOutputStringBuilder.toString());
                BufferedReader errorBufferReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
                String errorCompileOutputLine;
                StringBuilder errorCompileOutputStringBuilder = new StringBuilder();
                while ((errorCompileOutputLine = errorBufferReader.readLine()) != null){
                    errorCompileOutputStringBuilder.append(errorCompileOutputLine);
                }
                executeMessage.setMessage(errorCompileOutputStringBuilder.toString());
            }
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getTotalTimeMillis());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return executeMessage;
    }
}
