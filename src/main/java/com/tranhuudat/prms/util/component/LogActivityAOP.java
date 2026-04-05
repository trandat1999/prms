package com.tranhuudat.prms.util.component;

import com.tranhuudat.prms.util.ConstUtil;
import com.tranhuudat.prms.util.anotation.LogActivity;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author DatNuclear 22/01/2024
 * @project store-movie
 */
@Aspect
@Component
@Slf4j
public class LogActivityAOP {
    @Around("@annotation(com.tranhuudat.prms.util.anotation.LogActivity)")
    public Object logUsername(ProceedingJoinPoint call) throws Throwable {
        Object[] args = call.getArgs();
            MethodSignature signature = (MethodSignature) call.getSignature();
        Method method = signature.getMethod();
        String methodName = getHttpMethod(method);
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        StringBuilder params = new StringBuilder();
        assert args.length == parameterAnnotations.length;
        for(int i = 0; i < args.length; i++){
            for (Annotation annotation : parameterAnnotations[i]) {
                if (!(annotation instanceof PathVariable)){
                    continue;
                }
                PathVariable pathVariable = (PathVariable) annotation;
                params.append(pathVariable.value()).append(ConstUtil.EQUAL).append(ConstUtil.SPACE).append(args[i]);
            }
        }
        LogActivity logUsername = method.getAnnotation(LogActivity.class);
        String username;
        try{
            username = SecurityContextHolder.getContext().getAuthentication().getName();
        }catch(Exception e){
            username = "anonymous";
        }
        log.info("{} {} {} {} by username: {}",logUsername.m(),methodName, getMethodPath(method), params, username);
        return call.proceed();
    }

    public static String getHttpMethod(Method method) {
        if (method.isAnnotationPresent(GetMapping.class)) {
            return "GET";
        }
        if (method.isAnnotationPresent(PostMapping.class)) {
            return "POST";
        }
        if (method.isAnnotationPresent(DeleteMapping.class)) {
            return "DELETE";
        }
        if (method.isAnnotationPresent(PutMapping.class)) {
            return "PUT";
        }
        if (method.isAnnotationPresent(PatchMapping.class)) {
            return "PATCH";
        }
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        if (requestMapping != null) {
            RequestMethod[] methods = requestMapping.method();
            if (methods.length > 0) {
                return methods[0].name();
            }
        }
        return "GET";
    }
    private static String getMethodPath(Method method) {
        String path = "";
        RequestMapping classMapping = method.getDeclaringClass().getAnnotation(RequestMapping.class);
        if (classMapping != null) {
            path += classMapping.value().length > 0 ? classMapping.value()[0] : "";
        }
        // Kiểm tra các chú thích cụ thể hơn
        if (method.isAnnotationPresent(GetMapping.class)) {
            GetMapping getMapping = method.getAnnotation(GetMapping.class);
            path+= getMapping.value().length > 0 ? getMapping.value()[0] : "";
        }
        if (method.isAnnotationPresent(PostMapping.class)) {
            PostMapping postMapping = method.getAnnotation(PostMapping.class);
            path+= postMapping.value().length > 0 ? postMapping.value()[0] : "";
        }
        if (method.isAnnotationPresent(DeleteMapping.class)) {
            DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
            path+= deleteMapping.value().length > 0 ? deleteMapping.value()[0] : "";
        }
        if (method.isAnnotationPresent(PutMapping.class)) {
            PutMapping putMapping = method.getAnnotation(PutMapping.class);
            path+= putMapping.value().length > 0 ? putMapping.value()[0] : "";
        }
        if (method.isAnnotationPresent(PatchMapping.class)) {
            PatchMapping mapping = method.getAnnotation(PatchMapping.class);
            path+= mapping.value().length > 0 ? mapping.value()[0] : "";
        }
        // Kiểm tra @RequestMapping
        RequestMapping methodMapping = method.getAnnotation(RequestMapping.class);
        if (methodMapping != null) {
            path+= methodMapping.value().length > 0 ? methodMapping.value()[0] : "";
        }
        return path;
    }
}
