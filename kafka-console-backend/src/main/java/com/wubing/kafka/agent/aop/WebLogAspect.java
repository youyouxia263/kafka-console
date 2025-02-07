package com.wubing.kafka.agent.aop;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author wubing
 */
@Aspect
@Component
@Order(1)
@Slf4j
public class WebLogAspect {

    @Around("@within(org.springframework.web.bind.annotation.RestController)" +
            "||@within(org.springframework.stereotype.Controller)")
    public Object after(ProceedingJoinPoint joinPoint) throws Throwable{
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        log.info("请求方式:{},请求地址:{}", request.getMethod(),Optional.ofNullable(request.getRequestURI()).orElse(null));
        log.info("请求类方法:{}",joinPoint.getSignature());
        log.info("请求类方法参数:{}", JSON.toJSONString(filterArgs(joinPoint.getArgs())));
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed(joinPoint.getArgs());
        long end = System.currentTimeMillis();
        log.info("返回结果：{}", JSON.toJSONString(result));
        log.info("执行耗时:{}", end - start);
        return result;
    }

    private List<Object> filterArgs(Object[] objects) {
        return Arrays.stream(objects).filter(obj -> !(obj instanceof MultipartFile)
                && !(obj instanceof HttpServletResponse)
                && !(obj instanceof HttpServletRequest)).collect(Collectors.toList());
    }


}
