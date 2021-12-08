package com.uidroid.processor;

import com.uidroid.annotation.UI;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;

public class ConfigurationBuilderProcessor {

    private final Filer filer;

    public ConfigurationBuilderProcessor(ProcessingEnvironment processingEnv) {
        filer = processingEnv.getFiler();
    }

    public void process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set.isEmpty()) {
            return;
        }

        final Map<String, Map<String, String>> result = new HashMap<>();

        processClasses(UI.ViewConfiguration.class, result, roundEnvironment);

        processFields(UI.Action.class, result, roundEnvironment);
        processFields(UI.Configuration.class, result, roundEnvironment);
        processFields(UI.ConfigurationsList.class, result, roundEnvironment);
        processFields(UI.Id.class, result, roundEnvironment);
        processFields(UI.FieldConfiguration.class, result, roundEnvironment);
        processFields(UI.Param.class, result, roundEnvironment);

        try {
            for (String className: result.keySet()) {
                writeBuilderFile(className, result.get(className));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processClasses(Class<? extends Annotation> annotation, Map<String, Map<String, String>> result, RoundEnvironment roundEnvironment) {
        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(annotation)) {
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                continue;
            }

            String clazz = annotatedElement.asType().toString();

            if (!result.containsKey(clazz)) {
                result.put(clazz, new HashMap<>());
            }
        }
    }

    private void processFields(Class<? extends Annotation> annotation, Map<String, Map<String, String>> result, RoundEnvironment roundEnvironment) {
        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(annotation)) {
            if (annotatedElement.getKind() != ElementKind.FIELD) {
                continue;
            }

            String className = ((TypeElement) annotatedElement.getEnclosingElement()).getQualifiedName().toString();
            VariableElement variable = ((VariableElement) annotatedElement);

            result.get(className).put(variable.getSimpleName().toString(), variable.asType().toString());
        }
    }

    private void writeBuilderFile(String className, Map<String, String> setterMap) throws IOException {
        String packageName = null;
        int lastDot = className.lastIndexOf('.');
        if (lastDot > 0) {
            packageName = className.substring(0, lastDot);
        }

        String simpleClassName = className.substring(lastDot + 1);
        String builderClassName = className + "Builder";
        String builderSimpleClassName = builderClassName
                .substring(lastDot + 1);

        JavaFileObject builderFile = filer
                .createSourceFile(builderClassName);

        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {

            if (packageName != null) {
                out.print("package ");
                out.print(packageName);
                out.println(";");
                out.println();
            }

            out.print("public class ");
            out.print(builderSimpleClassName);
            out.println(" {");
            out.println();

            out.print("    private ");
            out.print(simpleClassName);
            out.print(" object = new ");
            out.print(simpleClassName);
            out.println("();");
            out.println();

            out.print("    public ");
            out.print(simpleClassName);
            out.println(" build() {");
            out.println("        return object;");
            out.println("    }");
            out.println();

            setterMap.forEach((variableName, argumentType) -> {

                out.print("    public ");
                out.print(builderSimpleClassName);
                out.print(" ");
                out.print(variableName);

                out.print("(");

                out.print(argumentType);
                out.println(" value) {");
                out.print("        object." + variableName + " = value; \n");
                out.println("        return this;");
                out.println("    }");
                out.println();
            });

            out.println("}");
        }
    }

}
