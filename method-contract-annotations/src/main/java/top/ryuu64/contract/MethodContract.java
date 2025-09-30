package top.ryuu64.contract;

import javax.lang.model.element.Modifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodContract {
    String methodName();

    Modifier[] modifiers() default {};

    Class<?>[] parameterTypes() default {};

    Class<?> returnType() default void.class;
}
