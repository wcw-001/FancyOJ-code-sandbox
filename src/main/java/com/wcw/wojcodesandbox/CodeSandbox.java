package com.wcw.wojcodesandbox;

import com.wcw.wojcodesandbox.model.ExecuteCodeRequest;
import com.wcw.wojcodesandbox.model.ExecuteCodeResponse;

public interface CodeSandbox {
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
