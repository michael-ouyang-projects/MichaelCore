package tw.framework.michaelcore.aop.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import tw.framework.michaelcore.aop.MichaelCoreAopHandler;

@Retention(RUNTIME)
@Target({TYPE, METHOD })
public @interface AopHere {

    public Class<? extends MichaelCoreAopHandler> value();

}
