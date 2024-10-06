package com.xtest.exception;

public class XTestException extends RuntimeException {
    private String errMessage;
    private int code;

    public XTestException(String errMessage, int code){
        super(errMessage);
        this.errMessage = errMessage;
        this.code = code;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
