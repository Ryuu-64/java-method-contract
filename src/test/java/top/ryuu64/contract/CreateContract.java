package top.ryuu64.contract;

import javax.lang.model.element.Modifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@MethodContract(methodName = "create", modifiers = {Modifier.STATIC})
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface CreateContract {
}