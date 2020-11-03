package tw.framework.michaelcore.ioc.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import tw.framework.michaelcore.ioc.BeanScope;

@Retention(RUNTIME)
@Target(METHOD)
public @interface Bean {

    public String value() default "";

    public BeanScope scope() default BeanScope.SINGLETON;

}
