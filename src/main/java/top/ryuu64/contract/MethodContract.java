package top.ryuu64.contract;

import javax.lang.model.element.Modifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodContract {
    /**
     * 需要检查的静态方法名称
     */
    String methodName();

    Modifier[] modifiers() default {};

    /**
     * 该方法的参数类型（可选，根据你的需求决定是否严格检查参数）
     * 例如：{ String.class, int.class }
     */
    Class<?>[] parameterTypes() default {};

    /**
     * 该方法的返回类型（可选，用于更精确的检查）
     */
    Class<?> returnType() default void.class;
}