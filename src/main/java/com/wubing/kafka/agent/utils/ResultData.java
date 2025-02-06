package com.wubing.kafka.agent.utils;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResultData<T> implements Serializable {
    private int code;
    private String msg;
    private T data;


    public ResultData() {

    }

    public static <T> ResultData<T> success(T data) {
        ResultData<T> resultData = new ResultData<>();
        resultData.setCode(ReturnCode.RC200.getCode());
        resultData.setMsg(ReturnCode.RC200.getMessage());
        resultData.setData(data);
        return resultData;
    }

    public static <T> ResultData<T> success2(String message, T data) {
        ResultData<T> resultData = new ResultData<>();
        resultData.setCode(ReturnCode.RC200.getCode());
        resultData.setMsg(message);
        resultData.setData(data);
        return resultData;
    }


    public static <T> ResultData<T> fail(int code, String message) {
        ResultData<T> resultData = new ResultData<>();
        resultData.setCode(code);
        resultData.setMsg(message);
        return resultData;
    }
}
