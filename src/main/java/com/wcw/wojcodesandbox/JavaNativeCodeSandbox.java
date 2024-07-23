package com.wcw.wojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.wcw.wojcodesandbox.model.ExecuteCodeRequest;
import com.wcw.wojcodesandbox.model.ExecuteCodeResponse;
import com.wcw.wojcodesandbox.model.ExecuteMessage;
import com.wcw.wojcodesandbox.model.JudgeInfo;
import com.wcw.wojcodesandbox.security.DefaultSecurityManager;
import com.wcw.wojcodesandbox.security.DenySecurityManager;
import com.wcw.wojcodesandbox.security.MySecurityManager;
import com.wcw.wojcodesandbox.utils.ProcessUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class JavaNativeCodeSandbox implements CodeSandbox{
    private static final String GOBLE_CODE_DIR_NAME = "tmpCode";
    private static final String GOBLE_JAVA_CLASS_NAME = "Main.java";
    private static final long TIME_OUT = 5000L;
    private static final List<String> blockList = Arrays.asList("Files","exec");
    private static final String Security_MANAGER_PATH = "D:\\API\\woj-code-sandbox\\src\\main\\resources\\testCode\\simpleComputeArgs";
    private static final String SECURITY_MANAGER_CLASS = "MySecurityManager";
    private static final WordTree WORD_TREE;
    static{
        WORD_TREE = new WordTree();
        WORD_TREE.addWords(blockList);
    }

    public static void main(String[] args) {
        JavaNativeCodeSandbox javaNativeCodeSandbox = new JavaNativeCodeSandbox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("3 4"));
        String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        //System.setSecurityManager(new MySecurityManager());
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();
        FoundWord foundWord = WORD_TREE.matchWord(code);
        if(foundWord != null){
            System.out.println("包含禁止词："+foundWord.getFoundWord());
            return null;
        }
        String userDir = System.getProperty("user.dir");
        String gobleCodePathName = userDir + File.separator+ GOBLE_CODE_DIR_NAME;
        if(!FileUtil.exist(gobleCodePathName)){
            FileUtil.mkdir(gobleCodePathName);
        }
        // 把用户的代码隔离存放
        String userCodeParentPath = gobleCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + GOBLE_JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePath, "UTF-8");
        // 编译代码，得到class文件
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsoluteFile());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            System.out.println(executeMessage);
        }catch(Exception e){
            return getErrorResponse(e);
        }
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        // 取用时最大值，便于判断是否超时
        long maxTime = 0;
        for(String input : inputList){
            String runCmd = String.format("java -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=%s Main %s", userCodeParentPath,Security_MANAGER_PATH, SECURITY_MANAGER_CLASS, input);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                new Thread(() -> {
                    try {
                        Thread.sleep(TIME_OUT);
                        System.out.println("超时了，中断");
                        runProcess.destroy();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                }).start();
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
                executeMessageList.add(executeMessage);
                System.out.println(executeMessage);
            }catch(Exception e){
                return getErrorResponse(e);
            }
        }
        //收集输出结果
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        for(ExecuteMessage executeMessage : executeMessageList){
            String errorMessage = executeMessage.getErrorMessage();
            if(StrUtil.isNotBlank(errorMessage)){
                // 用户提交的代码执行中错误
                executeCodeResponse.setStatus(3);
                executeCodeResponse.setMessage(errorMessage);
                break;
            }
            outputList.add(executeMessage.getMessage());
            Long time = executeMessage.getTime();
            if( time != null ){
                maxTime = Math.max(maxTime, time);
            }
        }
        if(outputList.size() == executeMessageList.size()){
            executeCodeResponse.setStatus(1);
        }
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        //要借助第三方库来获取内存占用，非常麻烦，此处不做实现
        //judgeInfo.setMemory();
        judgeInfo.setTime(maxTime);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        //文件清理
        if(userCodeFile.getParentFile() != null){
            boolean del = FileUtil.del(userCodeParentPath);
            if(del){
                System.out.println("删除临时文件成功");
            }else{
                System.out.println("删除临时文件失败");
            }
        }
        return executeCodeResponse;
    }

    /**
     * 获取错误响应
     * @param e
     * @return
     */
    private ExecuteCodeResponse getErrorResponse( Throwable e ){
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        //表示代码沙箱错误
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }
}
