package com.uidroid.processor;

import static com.uidroid.processor.Utils.getCodeParams;
import static com.uidroid.processor.Utils.getCodeString;
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

public class ViewCompositeFactoryProcessor {

    private final Filer filer;
    private final Messager messager;

    public ViewCompositeFactoryProcessor(ProcessingEnvironment processingEnv) {
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
    }

    public void process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set.isEmpty()) {
            return;
        }

        final Map<String, List<ChildView>> map = new LinkedHashMap<>();

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
            String key = item.key();
            if (key.equals("")) {
                key = variable.getSimpleName().toString();
            }

            int fallback = item.fallback();
            if (map.containsKey(className)) {
                map.get(className).add(new ChildView(fieldName, fallback, key));
            }
        }

        try {
            write(map);
        } catch (IOException e) {
            error(e.getMessage());
        }
    }

    private void write(Map<String, List<ChildView>> map) throws IOException {
        String packageName;
        int lastDot = "com.uidroid.uidroid.factory.ViewCompositeFactory".lastIndexOf('.');
        packageName = "com.uidroid.uidroid.factory.ViewCompositeFactory".substring(0, lastDot);

        String simpleClassName = "com.uidroid.uidroid.factory.ViewCompositeFactory".substring(lastDot + 1);

        JavaFileObject builderFile = filer
                .createSourceFile("com.uidroid.uidroid.factory.ViewCompositeFactory");

        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            out.print("package ");
            out.print(packageName);
            out.println(";");
            out.println();
            out.println("import android.view.View;");
            out.println("import java.util.List;");
            out.println("import java.util.ArrayList;");
            out.println("import com.uidroid.uidroid.view.ViewComposite;");
            out.println("import com.uidroid.uidroid.factory.IViewCompositeFactory;");
            for (String clazz: map.keySet()) {
                out.println("import " + clazz + ";");
            }
            out.println();

            out.print("public final class " + simpleClassName + " implements IViewCompositeFactory { \n\n");
            out.print("  public ViewComposite build(View object) { \n");
            out.print("    switch(object.getClass().getCanonicalName()) { \n");
            map.forEach((viewType, value) -> {
                out.print("      case " + getCodeString(viewType) + ": \n");
                out.print("        return this.build((" + getSimpleName(viewType) + ") object); \n");
            });
            out.print("      default: \n");
            out.print("         throw new RuntimeException(\"Cannot create composite\"); \n");
            out.print("    } \n");
            out.print("  } \n\n");

            map.forEach((key, childElements) -> {
                String objectView = getSimpleName(key);

                out.print("  public ViewComposite build(" + objectView + " value) { \n");
                out.print("    ViewComposite object = new ViewComposite(); \n");
                for (ChildView childElement : childElements) {
                    out.print("    object.put("
                            + getCodeParams(getCodeString(childElement.key), "value." + childElement.fieldName, String.valueOf(childElement.fallback))
                            + "); \n");
                }
                out.print("    return object; \n");
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

    private void error(String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message);
    }

}
