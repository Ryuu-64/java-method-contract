package top.ryuu64.contract;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

class MethodContractProcessorTest {
    private final Compiler compiler = Compiler.javac().withProcessors(new MethodContractProcessor());
    private static final JavaFileObject METHOD_CONTRACT = JavaFileObjects.forSourceString(
            "top.ryuu64.contract.MethodContract",
            "package top.ryuu64.contract;\n" +
                    "\n" +
                    "import javax.lang.model.element.Modifier;\n" +
                    "import java.lang.annotation.ElementType;\n" +
                    "import java.lang.annotation.Retention;\n" +
                    "import java.lang.annotation.RetentionPolicy;\n" +
                    "import java.lang.annotation.Target;\n" +
                    "\n" +
                    "@Target(ElementType.ANNOTATION_TYPE)\n" +
                    "@Retention(RetentionPolicy.RUNTIME)\n" +
                    "public @interface MethodContract {\n" +
                    "    String methodName();\n" +
                    "    Modifier[] modifiers() default {};\n" +
                    "    Class<?>[] parameterTypes() default {};\n" +
                    "    Class<?> returnType() default void.class;\n" +
                    "}"
    );

    private static final JavaFileObject CREATE_CONTRACT = JavaFileObjects.forSourceString(
            "top.ryuu64.contract.CreateContract",
            "package top.ryuu64.contract;\n" +
                    "\n" +
                    "                    import javax.lang.model.element.Modifier;\n" +
                    "                    import java.lang.annotation.ElementType;\n" +
                    "                    import java.lang.annotation.Retention;\n" +
                    "                    import java.lang.annotation.RetentionPolicy;\n" +
                    "                    import java.lang.annotation.Target;\n" +
                    "\n" +
                    "                    @MethodContract(methodName = \"create\", modifiers = {Modifier.PRIVATE, Modifier.STATIC})\n" +
                    "                    @Retention(RetentionPolicy.CLASS)\n" +
                    "                    @Target(ElementType.TYPE)\n" +
                    "                    public @interface CreateContract {\n" +
                    "                    }"
    );

    @Test
    public void whenClassHasRequiredStaticMethod_ThenCompilationSucceeds() {
        JavaFileObject validSource = JavaFileObjects.forSourceString(
                "TestClass",
                "package test;\n" +
                        "                        import top.ryuu64.contract.CreateContract;\n" +
                        "\n" +
                        "                        @CreateContract\n" +
                        "                        public class TestClass {\n" +
                        "                            private static void create() {\n" +
                        "                            }\n" +
                        "                        }"
        );

        Compilation compilation = compiler.compile(METHOD_CONTRACT, CREATE_CONTRACT, validSource);
        CompilationSubject.assertThat(compilation).succeeded();
    }

    @Test
    public void whenClassMissingRequiredStaticMethod_ThenCompilationFails() {
        JavaFileObject invalidSource = JavaFileObjects.forSourceString(
                "InvalidClass",
                "package test;\n" +
                        "                        import top.ryuu64.contract.CreateContract;\n" +
                        "\n" +
                        "                        @CreateContract\n" +
                        "                        public class InvalidClass {\n" +
                        "                        }"
        );

        Compilation compilation = compiler.compile(METHOD_CONTRACT, CREATE_CONTRACT, invalidSource);
        CompilationSubject.assertThat(compilation).failed();
        CompilationSubject.assertThat(compilation).hadErrorContaining("Class 'InvalidClass' must implement a method: `private static void create()`.");
    }

    @Test
    public void whenClassHasWrongSignatureMethod_ThenCompilationFails() {
        JavaFileObject wrongClass = JavaFileObjects.forSourceString(
                "WrongClass",
                "                        package test;\n" +
                        "                        import top.ryuu64.contract.CreateContract;\n" +
                        "\n" +
                        "                        @CreateContract\n" +
                        "                        public class WrongClass {\n" +
                        "                            public static WrongClass create(String param) {\n" +
                        "                                return new WrongClass();\n" +
                        "                            }\n" +
                        "                        }"
        );

        Compilation compilation = compiler.compile(METHOD_CONTRACT, CREATE_CONTRACT, wrongClass);
        CompilationSubject.assertThat(compilation).failed();
        CompilationSubject.assertThat(compilation).hadErrorContaining("Class 'WrongClass' must implement a method: `private static void create()`.");
    }
}
