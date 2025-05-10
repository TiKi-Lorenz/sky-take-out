package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/*
自定义切面，实现公共字段自动填充处理逻辑
* */
@Aspect
@Component
//切面类，@Aspect注解表示这是一个切面类
//@Component注解把这个类交给spring容器管理
@Slf4j
public class AutoFillAspect {
    /*
    切入点，对哪些类的哪些方法进行拦截
    * */
    //mapper包下的所有类的所有方法&&自定义注解注解过的方法生效
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){
    }
    //前置通知，在通知中进行公共字段的赋值
    @Before("autoFillPointCut()")//在这个方法执行前执行
    public void autoFill(JoinPoint joinPoint){//JoinPoint是连接点
        log.info("开始进行公共字段自动填充");
        //1.获取到当前被拦截的方法上的数据库操作类型
        //1.1需要获取当前连接点（JoinPoint）的方法签名信息,
            // 1.2将返回值强制类型转换为 MethodSignature，这是 Signature 的一个子接口，表示“方法的签名”
        MethodSignature signature =(MethodSignature) joinPoint.getSignature();
        //1.3获得方法上的注解对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        //1.4获取到当前被拦截方法的操作类型
        OperationType operationType = autoFill.value();


        //2.获取到当前被拦截方法的参数实体
        Object[] args = joinPoint.getArgs();
        if( args==null || args.length == 0){
            return;
        }
        //2.1 获取当前的第一个参数，因为把实体放在第一位
        Object entity = args[0];//因为实体的类型是不确定的，所以用Object去接受

        //3.准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        //4.根据当前不同的操作类型，为对应的属性通过反射来赋值
        if(operationType==OperationType.INSERT){
            //如果是插入操作，需要为四个公共字段赋值
            try {
                //通过反射获取属性，公共字段已经存储在常量类（AutoFillConstant）当中，可以调用规范处理
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //通过反射为对象属性赋值
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        else if(operationType == OperationType.UPDATE){
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //通过反射为对象属性赋值
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }


        }
            //如果是更新操作，需要为两个公共字段赋值

    }
}
