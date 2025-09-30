package top.ryuu64.contract;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class ClassToJavaFileObject {
    public static JavaFileObject fromClass(Class<?> clazz) {
        try {
            String className = clazz.getName();
            String classResource = "/" + className.replace('.', '/') + ".class";
            try (InputStream is = clazz.getResourceAsStream(classResource)) {
                if (is == null) {
                    throw new IOException("Cannot find class file for " + className);
                }

                try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        stream.write(buffer, 0, len);
                    }
                }
            }

            // 创建 JavaFileObject
            return new SimpleJavaFileObject(
                    URI.create("string:///" + className.replace('.', '/') + JavaFileObject.Kind.CLASS.extension),
                    JavaFileObject.Kind.CLASS
            ) {
                @Override
                public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                    throw new UnsupportedOperationException("Class file does not have char content");
                }
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

