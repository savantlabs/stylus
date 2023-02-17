package io.savantlabs.stylus.test.util;

import jakarta.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.springframework.util.ReflectionUtils;

public final class TestReflectionUtils {

  protected TestReflectionUtils() {
    throw new UnsupportedOperationException();
  }

  public static <T> T invokeStaticMethod(
      Class<?> targetClz,
      String methodName,
      @Nullable List<Class<?>> paramTypes,
      @Nullable List<Object> arguments,
      Class<T> returnClz) {
    try {
      Method method;
      if (paramTypes == null) {
        method = ReflectionUtils.findMethod(targetClz, methodName);
      } else {
        method =
            ReflectionUtils.findMethod(targetClz, methodName, paramTypes.toArray(Class[]::new));
      }
      Assertions.assertThat(method).isNotNull();
      method.setAccessible(true);
      if (arguments == null) {
        return returnClz.cast(method.invoke(null));
      } else {
        return returnClz.cast(method.invoke(null, arguments.toArray(Object[]::new)));
      }
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T invokeMethod(
      Object targetObj,
      String methodName,
      @Nullable List<Class<?>> paramTypes,
      @Nullable List<Object> arguments,
      Class<T> returnClz) {
    try {
      Method method;
      if (paramTypes == null) {
        method = ReflectionUtils.findMethod(targetObj.getClass(), methodName);
      } else {
        method =
            ReflectionUtils.findMethod(
                targetObj.getClass(), methodName, paramTypes.toArray(Class[]::new));
      }
      Assertions.assertThat(method).isNotNull();
      method.setAccessible(true);
      if (arguments == null) {
        return returnClz.cast(method.invoke(targetObj));
      } else {
        return returnClz.cast(method.invoke(targetObj, arguments.toArray(Object[]::new)));
      }
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }
}
