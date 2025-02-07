package com.wubing.kafka.agent.exception;

import com.wubing.kafka.agent.utils.ResultData;
import com.wubing.kafka.agent.utils.ReturnCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.NetworkException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.concurrent.ExecutionException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NetworkException.class)
    public ResultData<Object> expHandler(NetworkException exp) {
        log.error("Internal network exception", exp);
        return ResultData.fail(ReturnCode.RC401.getCode(), ReturnCode.RC401.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultData<Object> expHandler(MethodArgumentNotValidException exp) {
        FieldError error = (FieldError) exp.getAllErrors().get(0);
        String errMsg = error.getField() + " " + error.getDefaultMessage();
        log.error("MethodArgumentNotValidException: " + errMsg);
        return ResultData.fail(ReturnCode.RC402.getCode(), errMsg);
    }

    @ExceptionHandler(KafkaClientException.class)
    public ResultData<Object> expHandler(KafkaClientException exp) {
        log.error("Kafka Client exception: ", exp);
        return ResultData.fail(ReturnCode.RC403.getCode(), exp.getMessage());
    }

    @ExceptionHandler(ExecutionException.class)
    public ResultData<Object> expHandler(ExecutionException exp) {
        log.error("ExecutionException", exp);
        return ResultData.fail(ReturnCode.RC404.getCode(), exp.getCause().getMessage());
    }

    @ExceptionHandler(InterruptedException.class)
    public ResultData<Object> expHandler(InterruptedException exp) {
        log.error("InterruptedException", exp);
        return ResultData.fail(ReturnCode.RC405.getCode(), exp.getCause().getMessage());
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResultData<Object> expHandler(HttpMessageNotReadableException exp) {
        log.error("HttpMessageNotReadableException", exp);
        return ResultData.fail(ReturnCode.RC405.getCode(), "请求参数有误");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResultData<Object> expHandler(IllegalArgumentException exp) {
        log.error("IllegalArgumentException", exp);
        return ResultData.fail(ReturnCode.RC406.getCode(), "存在非法参数");
    }
//
//    @ExceptionHandler(RemotingSendRequestException.class)
//    public Response<Void> expHandler(RemotingSendRequestException exp) {
//        log.error("RemotingSendRequestException", exp);
//        return Response.fail("MQAdmin请求异常");
//    }
//
//    @ExceptionHandler(RemotingTimeoutException.class)
//    public Response<Void> expHandler(RemotingTimeoutException exp) {
//        log.error("RemotingTimeoutException", exp);
//        return Response.fail("MQAdmin超时异常");
//    }
//
//    @ExceptionHandler(MQBrokerException.class)
//    public Response<Void> expHandler(MQBrokerException exp) {
//        log.error("MQBrokerException", exp);
//        return Response.fail("MQBroker异常");
//    }
//
//    @ExceptionHandler(MQClientException.class)
//    public Response<Void> expHandler(MQClientException exp) {
//        log.error("MQClientException", exp);
//        return Response.fail("MQClient异常");
//    }
//
//    @ExceptionHandler(RemotingException.class)
//    public Response<Void> expHandler(RemotingException exp) {
//        log.error("RemotingException", exp);
//        return Response.fail("MQ远程异常");
//    }
//
//    @ExceptionHandler(UserNotFoundException.class)
//    public Response<Void> expHandler(UserNotFoundException exp) {
//        log.error("UserNotFoundException", exp);
//        return Response.fail("未获取到用户信息");
//    }
//
//    @ExceptionHandler(UnknownHostException.class)
//    public Response<Void> expHandler(UnknownHostException exp) {
//        log.error("UnknownHostException", exp);
//        return Response.fail("未知服务地址异常");
//    }
//

//
//    @ExceptionHandler(AuthRuntimeException.class)
//    public Response<Void> expHandler(AuthRuntimeException exp) {
//        log.error("AuthRuntimeException", exp);
//        return Response.fail("IAM SDK: " + exp.getMessage());
//    }
//
//    @ExceptionHandler(Exception.class)
//    public Response<Void> expHandler(Exception exp) {
//        log.error("Exception", exp);
//        return Response.fail("未知异常");
//    }
}
