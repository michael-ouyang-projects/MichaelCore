package tw.framework.michaelcore.data.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import tw.framework.michaelcore.data.enumeration.TransactionIsolation;
import tw.framework.michaelcore.data.enumeration.TransactionPropagation;

@Retention(RUNTIME)
@Target({TYPE, METHOD })
public @interface Transactional {

    public Class<? extends Throwable> rollbackFor() default RuntimeException.class;

    public TransactionPropagation propagation() default TransactionPropagation.REQUIRED;

    public TransactionIsolation isolation() default TransactionIsolation.READ_COMMITTED;

}
