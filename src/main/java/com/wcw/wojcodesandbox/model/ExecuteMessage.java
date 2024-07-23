package com.wcw.wojcodesandbox.model;

import lombok.Data;
@Data
public class ExecuteMessage {
    private Integer exitCode;
    private String message;
    private String errorMessage;
    private Long time;
}
