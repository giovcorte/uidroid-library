package com.uidroid.processor;

import static com.uidroid.processor.Utils.getLayoutIdFromClassName;
import static com.uidroid.processor.Utils.getSimpleName;

import com.uidroid.annotation.UI;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class ViewFieldsInjectorProcessor {

    private final Filer filer;
    private final Messager messager;

    public ViewFieldsInjectorProcessor(ProcessingEnvironment processingEnv) {
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
    }

    public void process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set.isEmpty()) {
            return;
        }

        final Map<String, List<ChildView>> map = new LinkedHashMap<>();
        String appPackage = null;

        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(UI.AppPackage.class)) {
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                error("Only class can be annotated with UI.CompositeView", annotatedElement);
                continue;
            }

            UI.AppPackage item = annotatedElement.getAnnotation(UI.AppPackage.class);

            if (item != null) {
                appPackage = item.packageName();
                break;
            }
        }

        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(UI.CustomView.class)) {
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                error("Only class can be annotated with UI.CompositeView", annotatedElement);
                continue;
            }

            UI.CustomView item = annotatedElement.getAnnotation(UI.CustomView.class);

            if (item != null) {
                String clazz = annotatedElement.asType().toString();

                if (!map.containsKey(clazz)) {
                    map.put(clazz, new ArrayList<>());
                }
            }
        }

        for (Element annotatedElement: roundEnvironment.getElementsAnnotatedWith(UI.View.class)) {
            if (annotatedElement.getKind() != ElementKind.FIELD) {
                error("Only fields can be annotated with UI.View", annotatedElement);
                continue;
            }

            UI.View item = annotatedElement.getAnnotation(UI.View.class);
            VariableElement variable = ((VariableElement) annotatedElement);

            String fieldName = variable.getSimpleName().toString();
            String className = ((TypeElement) annotatedElement.getEnclosingElement()).getQualifiedName().toString();
            String key = variable.getSimpleName().toString();

            int fallback = item.fallback();

            if (map.containsKey(className)) {
                map.get(className).add(new ChildView(fieldName, fallback, key));
            }
        }

        if (appPackage != null && !appPackage.equals("")) {
            try {
                write(appPackage, map);
            } catch (IOException e) {
                error(e.getMessage());
            }
        } else {
            message("You must annotate an object with @AppPackage(<rootPackage>) to generate this helper class.");
        }

    }

    private void write(String appPackage, Map<String, List<ChildView>> map) throws IOException {
        String packageName;
        int lastDot = "com.uidroid.uidroid.injector.ViewFieldsInjector".lastIndexOf('.');
        packageName = "com.uidroid.uidroid.injector.ViewFieldsInjector".substring(0, lastDot);

        String simpleClassName = "com.uidroid.uidroid.injector.ViewFieldsInjector".substring(lastDot + 1);

        JavaFileObject builderFile = filer
                .createSourceFile("com.uidroid.uidroid.injector.ViewFieldsInjector");

        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            out.print("package ");
            out.print(packageName);
            out.println(";");
            out.println();
            out.println("import android.view.View;");
            out.println("import " + appPackage + ".R;");
            out.println("import android.content.Context;");
            out.println("import android.view.LayoutInflater;");
            for (String clazz: map.keySet()) {
                out.println("import " + clazz + ";");
            }
            out.println();

            out.print("public final class " + simpleClassName + " { \n\n");
            map.forEach((key, childElements) -> {
                String objectView = getSimpleName(key);

                out.print("  public static void bindFields(" + objectView + " view, Context context) { \n");
                out.print("    LayoutInflater.from(context).inflate(R.layout." + getLayoutIdFromClassName(objectView) + ", view, true); \n");
                for (ChildView childElement : childElements) {
                    out.print("    view." + childElement.fieldName + " = view.findViewById(R.id." + childElement.key + "); \n");
                }
                out.print("  } \n\n");
            });
            out.println("}");
        }
    }

    static class ChildView {

        public int fallback;
        public String fieldName;
        public String key;

        public ChildView(String fieldName, int fallback, String key) {
            this.fallback = fallback;
            this.fieldName = fieldName;
            this.key = key;
        }
    }

    private void error(String message, Element element) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private void message(String message) {
        messager.printMessage(Diagnostic.Kind.WARNING, message);
    }

    private void error(String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message);
    }

}
