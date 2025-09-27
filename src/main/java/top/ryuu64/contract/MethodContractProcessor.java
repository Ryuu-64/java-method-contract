package top.ryuu64.contract;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(Processor.class)
@SupportedAnnotationTypes("top.ryuu64.contract.MethodContract")
@SupportedSourceVersion(SourceVersion.RELEASE_25)
public class MethodContractProcessor extends AbstractProcessor {
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> metaAnnotatedAnnotations = roundEnv.getElementsAnnotatedWith(MethodContract.class);

        for (Element annotationElement : metaAnnotatedAnnotations) {
            if (annotationElement.getKind() != ElementKind.ANNOTATION_TYPE) {
                continue;
            }

            TypeElement annotationType = (TypeElement) annotationElement;
            // 2. 获取该注解类型上的 @MethodContract 元注解信息
            MethodContract contract = annotationType.getAnnotation(MethodContract.class);
            // 理论上不会发生，因为是通过getElementsAnnotatedWith找到的
            if (contract == null) {
                continue;
            }

            Set<? extends Element> elementsAnnotatedWithContract = roundEnv.getElementsAnnotatedWith(annotationType);
            for (Element element : elementsAnnotatedWithContract) {
                if (!isValidClass(element)) {
                    continue;
                }

                processContract(element, contract);
            }
        }
        // 表示这些注解已由此处理器处理，不会传递给其他处理器
        return true;
    }

    private boolean isValidClass(Element element) {
        if (element.getKind() == ElementKind.CLASS || element.getKind() == ElementKind.INTERFACE) {
            return true;
        }

        messager.printMessage(
                Diagnostic.Kind.ERROR,
                "@MethodContract can only be applied to a class or interface.",
                element
        );
        return false;
    }

    private void processContract(Element element, MethodContract contract) {
        TypeElement classElement = (TypeElement) element;
        List<Modifier> requiredModifiers = Arrays.stream(contract.modifiers()).collect(Collectors.toList());
        String requiredMethodName = contract.methodName();
        List<? extends TypeMirror> requiredParameterType;
        try {
            contract.parameterTypes(); // 这行会触发异常
            requiredParameterType = Collections.emptyList();
        } catch (MirroredTypesException exception) {
            requiredParameterType = exception.getTypeMirrors();
        }

        TypeMirror requiredReturnType;
        try {
            contract.returnType();
            requiredReturnType = element.asType();
        } catch (MirroredTypeException exception) {
            requiredReturnType = exception.getTypeMirror();
        }

        // 使用 TypeMirror 进行后续检查
        if (containsMatchingMethod(classElement, requiredModifiers, requiredMethodName, requiredParameterType, requiredReturnType)) {
            return;
        }

        printErrorMessage(requiredParameterType, classElement, requiredReturnType, requiredMethodName);
    }

    private void printErrorMessage(
            List<? extends TypeMirror> requiredParameterType, TypeElement classElement,
            TypeMirror requiredReturnType, String requiredMethodName
    ) {
        // 生成错误信息（使用 TypeMirror 的字符串表示）
        String paramTypesStr = requiredParameterType.stream()
                .map(TypeMirror::toString) // 或使用更友好的方式格式化
                .collect(Collectors.joining(", "));
        String errorMessage = String.format(
                "Class '%s' must implement a method: `%s %s(%s)`.",
                classElement.getSimpleName(),
                requiredReturnType.toString(), // 使用 TypeMirror 的字符串表示
                requiredMethodName,
                paramTypesStr
        );
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, errorMessage, classElement);
    }

    private boolean containsMatchingMethod(
            TypeElement classElement,
            List<Modifier> requiredModifiers,
            String requiredMethodName,
            List<? extends TypeMirror> requiredParamTypes,
            TypeMirror requiredReturnType
    ) {
        Types typeUtils = processingEnv.getTypeUtils();
        for (Element enclosedElement : classElement.getEnclosedElements()) {
            if (enclosedElement.getKind() != ElementKind.METHOD) {
                continue;
            }

            ExecutableElement element = (ExecutableElement) enclosedElement;
            if (!hasModifiers(element, requiredModifiers)) {
                continue;
            }

            if (!hasReturnType(element, requiredReturnType, typeUtils)) {
                continue;
            }

            if (!hasSimpleName(element, requiredMethodName)) {
                continue;
            }

            if (hasParameterTypes(element, requiredParamTypes, typeUtils)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasModifiers(ExecutableElement element, List<Modifier> requiredModifiers) {
        return element.getModifiers().containsAll(requiredModifiers);
    }

    private static boolean hasReturnType(ExecutableElement element, TypeMirror requiredReturnType, Types typeUtils) {
        return typeUtils.isSameType(element.getReturnType(), requiredReturnType);
    }

    private static boolean hasSimpleName(ExecutableElement element, String name) {
        return element.getSimpleName().toString().equals(name);
    }

    private static boolean hasParameterTypes(
            ExecutableElement element,
            List<? extends TypeMirror> requiredTypes,
            Types typeUtils
    ) {
        List<TypeMirror> types = element.getParameters().stream()
                .map(VariableElement::asType)
                .toList();
        if (types.size() != requiredTypes.size()) {
            return false;
        }

        for (int i = 0; i < requiredTypes.size(); i++) {
            TypeMirror methodParamType = types.get(i);
            TypeMirror requiredParamType = requiredTypes.get(i);

            if (!typeUtils.isSameType(methodParamType, requiredParamType)) {
                return false;
            }
        }
        return true;
    }
}