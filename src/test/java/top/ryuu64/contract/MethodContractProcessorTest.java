package top.ryuu64.contract;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

class MethodContractProcessorTest {
    private final Compiler compiler = Compiler.javac().withProcessors(new MethodContractProcessor());

    // 1. 定义注解的源码作为测试资源
    private final JavaFileObject methodContractSource = JavaFileObjects.forSourceString(
            "top.ryuu64.contract.MethodContract",
            """
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
                    """
    );

    private final JavaFileObject createContractSource = JavaFileObjects.forSourceString(
            "top.ryuu64.contract.CreateContract",
            """
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
                    """
    );

    @Test
    public void test_WhenClassHasRequiredStaticMethod_ThenCompilationSucceeds() {
        // 2. 准备正确实现了静态方法的测试源码
        JavaFileObject validSource = JavaFileObjects.forSourceString(
                "TestClass",
                """
                        package test;
                        import top.ryuu64.contract.CreateContract;
                        @CreateContract
                        public class TestClass {
                            public static void create() {
                            }
                        }
                        """
        );

        // 3. 编译时传入所有必要的源文件：注解定义 + 测试类
        Compilation compilation = compiler.compile(methodContractSource, createContractSource, validSource);

        // 断言编译成功
        CompilationSubject.assertThat(compilation).succeeded();
    }

    @Test
    public void test_WhenClassMissingRequiredStaticMethod_ThenCompilationFails() {
        JavaFileObject invalidSource = JavaFileObjects.forSourceString(
                "InvalidClass",
                """
                        package test;
                        import top.ryuu64.contract.CreateContract;
                        @CreateContract
                        public class InvalidClass {
                            // 缺少 create() 方法
                        }
                        """
        );

        Compilation compilation = compiler.compile(methodContractSource, createContractSource, invalidSource);
        CompilationSubject.assertThat(compilation).failed();
    }

    @Test
    public void test_WhenClassHasWrongSignatureMethod_ThenCompilationFails() {
        JavaFileObject wrongSignatureSource = JavaFileObjects.forSourceString(
                "WrongClass",
                """
                        package test;
                        import top.ryuu64.contract.CreateContract;
                        @CreateContract
                        public class WrongClass {
                            public static WrongClass create(String param) {
                                return new WrongClass();
                            }
                        }
                        """
        );

        Compilation compilation = compiler.compile(methodContractSource, createContractSource, wrongSignatureSource);
        CompilationSubject.assertThat(compilation).failed();
    }
}