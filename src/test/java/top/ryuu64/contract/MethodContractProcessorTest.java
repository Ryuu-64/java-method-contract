package top.ryuu64.contract;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

class MethodContractProcessorTest {
    private final Compiler compiler = Compiler.javac().withProcessors(new MethodContractProcessor());
    private final JavaFileObject methodContract = JavaFileObjects.forSourceString(
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

    private final JavaFileObject createContract = JavaFileObjects.forSourceString(
            "top.ryuu64.contract.CreateContract",
            """
                    package top.ryuu64.contract;
                    
                    import javax.lang.model.element.Modifier;
                    import java.lang.annotation.ElementType;
                    import java.lang.annotation.Retention;
                    import java.lang.annotation.RetentionPolicy;
                    import java.lang.annotation.Target;
                    
                    @MethodContract(methodName = "create", modifiers = {Modifier.PRIVATE, Modifier.STATIC})
                    @Retention(RetentionPolicy.CLASS)
                    @Target(ElementType.TYPE)
                    public @interface CreateContract {
                    }
                    """
    );

    @Test
    public void whenClassHasRequiredStaticMethod_ThenCompilationSucceeds() {
        JavaFileObject validSource = JavaFileObjects.forSourceString(
                "TestClass",
                """
                        package test;
                        import top.ryuu64.contract.CreateContract;
                        
                        @CreateContract
                        public class TestClass {
                            private static void create() {
                            }
                        }
                        """
        );

        Compilation compilation = compiler.compile(methodContract, createContract, validSource);
        CompilationSubject.assertThat(compilation).succeeded();
    }

    @Test
    public void whenClassMissingRequiredStaticMethod_ThenCompilationFails() {
        JavaFileObject invalidSource = JavaFileObjects.forSourceString(
                "InvalidClass",
                """
                        package test;
                        import top.ryuu64.contract.CreateContract;
                        
                        @CreateContract
                        public class InvalidClass {
                        }
                        """
        );

        Compilation compilation = compiler.compile(methodContract, createContract, invalidSource);
        CompilationSubject.assertThat(compilation).failed();
        CompilationSubject.assertThat(compilation).hadErrorContaining("Class 'InvalidClass' must implement a method: `private static void create()`.");
    }

    @Test
    public void whenClassHasWrongSignatureMethod_ThenCompilationFails() {
        JavaFileObject wrongClass = JavaFileObjects.forSourceString(
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

        Compilation compilation = compiler.compile(methodContract, createContract, wrongClass);
        CompilationSubject.assertThat(compilation).failed();
        CompilationSubject.assertThat(compilation).hadErrorContaining("Class 'WrongClass' must implement a method: `private static void create()`.");
    }
}