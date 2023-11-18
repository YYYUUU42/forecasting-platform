package org.example.exception;


import org.example.model.enums.AppHttpCodeEnum;

public class CustomException extends RuntimeException {

    private int code;

    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public CustomException(AppHttpCodeEnum httpCodeEnum) {
        super(httpCodeEnum.getMessage());
        this.code = httpCodeEnum.getCode();
        this.message = httpCodeEnum.getMessage();
    }
}
