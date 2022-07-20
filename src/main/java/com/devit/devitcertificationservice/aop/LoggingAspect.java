package com.devit.devitcertificationservice.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("@within(LoggingClientInfo) || @annotation(LoggingClientInfo)")
    public Object logPerformance(ProceedingJoinPoint pjp) throws Throwable {
        Object proceed = null;
        try{
            // 처리 시간 출력 (ms) 을 위한 start
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            proceed = pjp.proceed();

            log.info("====================client information=====================");

            // 클라이언트 ip 출력
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String ip = request.getHeader("X-FORWARDED-FOR");
            if (ip == null) {
                ip = request.getRemoteAddr();
            }
            log.info("client ip : {} ", ip);

            // 호출 시간
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            log.info("current time : {} ", simpleDateFormat.format(new Date()));

           // request header 출력
            ObjectMapper mapper = new ObjectMapper();
            log.info("Request Headers : \n{}", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(getRequestHeaders(request)));

            // request body 출력
            if("POST".equalsIgnoreCase(request.getMethod())) {
                if(!request.getContentType().equals("multipart/form-data")) {
                    log.info("Request Body : \n{}", request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
                }
            } else {
                log.info("Request Query Params : {} ", paramMapToString(request.getParameterMap()));
            }

            // 처리 시간 출력 (ms) 을 위한 stop
            stopWatch.stop();
            log.info("처리 시간 = {}ms", stopWatch.getTotalTimeMillis());

            log.info("response = {}", mapper.writeValueAsString(proceed));

        } catch (Exception e) {
            log.error("client information logging error : {} ", e.getMessage());
        }

        return proceed;
    }


    private String paramMapToString(Map<String, String[]> paraStringMap) {
        return paraStringMap.entrySet().stream()
                .map(entry -> String.format("%s = %s", entry.getKey(),
                        Arrays.toString(entry.getValue()))).collect(Collectors.joining(", "));

    }

    private Map<String,String> getRequestHeaders(HttpServletRequest request) {
        Map<String,String> headerMap = new HashMap<>();

        Enumeration<String> headerArray = request.getHeaderNames();
        while (headerArray.hasMoreElements()) {
            String headerName = (String) headerArray.nextElement();
            headerMap.put(headerName, request.getHeader(headerName));
        }
        return headerMap;
    }

    private void getResponseHeaders(HttpServletResponse response) {
        log.info(response.getContentType());
        log.info(String.valueOf(response.getHeaderNames()));
        response.getHeaderNames().forEach(headerName -> log.info(headerName + ":" + response.getHeader(headerName)));
    }

    private String getResponseBody(final HttpServletResponse response) throws IOException {
        String payload = null;
        ContentCachingResponseWrapper wrapper =
                WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                payload = new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
                wrapper.copyBodyToResponse();
            }
        }
        return null == payload ? " - " : payload;
    }
}
