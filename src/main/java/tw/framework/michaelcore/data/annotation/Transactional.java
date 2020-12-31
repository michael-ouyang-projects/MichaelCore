package tw.framework.michaelcore.data.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import tw.framework.michaelcore.data.enumeration.TransactionalIsolation;
import tw.framework.michaelcore.data.enumeration.TransactionalPropagation;

@Retention(RUNTIME)
@Target({TYPE, METHOD })
public @interface Transactional {

    public Class<? extends Throwable> rollbackFor() default RuntimeException.class;

    public TransactionalPropagation propagation() default TransactionalPropagation.REQUIRED;

    public TransactionalIsolation isolation() default TransactionalIsolation.READ_COMMITTED;

}
