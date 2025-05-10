package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
自定义注解，用于标识某个方法需要进行功能字段自动填充
* */
//Target可用的值定义在ElementType枚举类中，常用值有：Type，类，接口，成员变量，成员方法
@Target(ElementType.METHOD)//指定注解添加位置,指定只能添加在方法上
@Retention(RetentionPolicy.RUNTIME)//指定注解的生命周期,运行时有效
public @interface AutoFill {
    //我们在common模块的enumeration包下定义了一个枚举类
    // 里面定义了我们需要的数据库操作类型 UPDATE和INSERT
    //指定操作类型
    OperationType value();
    //定义注解属性：value() 是 @AutoFill 注解的一个属性，
    // 类型为 OperationType（枚举类型）。
    //指定操作类型：通过这个属性，开发者可以在使用 @AutoFill 注解时，明确指定当前方法对应的数据库操作类型（如 UPDATE 或 INSERT）。
}