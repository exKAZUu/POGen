package com.google.testing.pogen.pages;


import java.lang.reflect.Method;

import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

public aspect OracleGeneratorAspect {
  pointcut driverMethodPointcut(WebDriver driver):
		call(* WebDriver.*(..)) && target(driver) && !this(Selenium) && !this(OracleGeneratorAspect) && !this(VariableAnalyzer);


  before(WebDriver driver): driverMethodPointcut(driver) {
    if (thisEnclosingJoinPointStaticPart.getSignature() instanceof MethodSignature) {
      MethodSignature ms = (MethodSignature) thisEnclosingJoinPointStaticPart.getSignature();
      Method method = ms.getMethod();
      Test test = method.getAnnotation(Test.class);
      if (test != null) {
        OracleGenerator.instance.verifyAndSave(driver, method);
      }
    }
  }
}
