package com.uidroid.processor;

import static com.uidroid.processor.Utils.firstLowerCased;
import static com.uidroid.processor.Utils.getParams;
import static com.uidroid.processor.Utils.getSimpleName;

import com.uidroid.annotation.UI;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class DatabindingProvidersProcessor {

    private final Filer filer;
    private final Messager messager;

    public DatabindingProvidersProcessor(ProcessingEnvironment processingEnv) {
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    public void process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set.isEmpty()) {
            return;
        }

        final List<String> databindingClasses = new ArrayList<>();
        final List<String> resultFields = new ArrayList<>();

        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(UI.class)) {
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                continue;
            }

            UI item = annotatedElement.getAnnotation(UI.class);

            if (item != null) {
                databindingClasses.add(annotatedElement.asType().toString());
            }
        }

        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(UI.BinderFor.class)) {
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                continue;
            }

            UI.BinderFor autoElement = annotatedElement.getAnnotation(UI.BinderFor.class);

            if (autoElement != null) {
                TypeElement typeElement = (TypeElement) annotatedElement;
                List<? extends Element> elements = typeElement.getEnclosedElements();

                for (Element e: elements) {
                    if (e.getKind().equals(ElementKind.CONSTRUCTOR)) {
                        ExecutableElement executableElement = (ExecutableElement) e;
                        List<? extends VariableElement>  list = executableElement.getParameters();

                        for (VariableElement variableElement: list) {
                            if (!resultFields.contains(variableElement.asType().toString())) {
                                resultFields.add(variableElement.asType().toString());
                            }
                        }

                        break;
                    }
                }
            }
        }

        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(UI.Binder.class)) {
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                continue;
            }

            UI.Binder autoElement = annotatedElement.getAnnotation(UI.Binder.class);

            if (autoElement != null) {
                TypeElement typeElement = (TypeElement) annotatedElement;
                List<? extends Element> elements = typeElement.getEnclosedElements();

                for (Element e: elements) {
                    if (e.getKind().equals(ElementKind.CONSTRUCTOR)) {
                        ExecutableElement executableElement = (ExecutableElement) e;
                        List<? extends VariableElement>  list = executableElement.getParameters();

                        for (VariableElement variableElement: list) {
                            if (!resultFields.contains(variableElement.asType().toString())) {
                                resultFields.add(variableElement.asType().toString());
                            }
                        }

                        break;
                    }
                }
            }
        }

        if (!databindingClasses.isEmpty()) {
            for (String clazz: databindingClasses) {
                try {
                    write(clazz, resultFields);
                } catch (IOException e) {
                    error(e.getMessage());
                }
            }
        } else {
            message();
        }
    }

    private void write(String databindingClass, List<String> resultFields) throws IOException {
        String className = databindingClass + "Provider";
        String packageName;
        int lastDot = className.lastIndexOf('.');
        packageName = className.substring(0, lastDot);

        JavaFileObject builderFile = filer
                .createSourceFile(className);

        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            out.print("package ");
            out.print(packageName);
            out.println(";");
            out.println();
            out.println("import android.view.View;");
            out.println("import java.util.List;");
            out.println("import java.util.ArrayList;");
            out.println("import com.uidroid.uidroid.factory.ViewBinderFactory;");
            out.println("import com.uidroid.uidroid.factory.ViewCompositeFactory;");
            out.println("import com.uidroid.uidroid.factory.ViewConfigurationFactory;");
            out.println("import com.uidroid.uidroid.loader.ImageLoader;");
            out.println("import com.uidroid.uidroid.loader.IImageLoader;");
            out.println("import com.uidroid.uidroid.handler.ClickHandler;");
            out.println("import com.uidroid.uidroid.factory.IViewFactory;");
            out.println("import com.uidroid.uidroid.factory.ViewFactory;");
            out.println("import android.content.Context;");
            out.println("import " + databindingClass + ";");
            for (String s: resultFields) {
                out.println("import " + s + ";");
            }
            out.print("\n");

            out.print("public final class " + getSimpleName(className) + " { \n\n");
            for (String s: resultFields) {
                out.print("  private " + getSimpleName(s) + " " + firstLowerCased(getSimpleName(s)) + "; \n");
            }
            out.print("  private IImageLoader imageLoader; \n\n");
            out.print("  public " + getSimpleName(getSimpleName(className)) + "(Context context) { \n");
            out.print("    this.imageLoader = new ImageLoader(context); \n");
            out.print("  } \n");
            out.print("\n");
            for (String s: resultFields) {
                out.print("  public " + getSimpleName(className) + " " + firstLowerCased(getSimpleName(s))
                        + "(" + getSimpleName(s) + " " + firstLowerCased(getSimpleName(s)) + ") { \n");
                out.print("    this." + firstLowerCased(getSimpleName(s)) + " = " + firstLowerCased(getSimpleName(s)) + "; \n");
                out.print("    return this; \n");
                out.print("  } \n");
            }

            out.print("  public " + getSimpleName(className) + " imageLoader(IImageLoader imageLoader) { \n");
            out.print("    this.imageLoader = imageLoader; \n");
            out.print("    return this; \n");
            out.print("  } \n");

            out.print("  public " + getSimpleName(databindingClass) + " build() { \n");
            out.print("    return new " + getSimpleName(databindingClass) + "( \n");
            out.print("        imageLoader, \n");
            out.print("        new ClickHandler(), \n");
            out.print("        new ViewBinderFactory(" + getParams(resultFields) + "), \n");
            out.print("        new ViewConfigurationFactory(), \n");
            out.print("        new ViewCompositeFactory(), \n");
            out.print("        new ViewFactory()); \n } \n");
            out.println("}");
        }
    }

    private void error(String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message);
    }

    private void message() {
        messager.printMessage(Diagnostic.Kind.WARNING,
                "You must implement and @UI annotate DatabindingContext class.");
    }

}

