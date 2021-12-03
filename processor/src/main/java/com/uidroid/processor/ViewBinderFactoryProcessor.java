package com.uidroid.processor;

import static com.uidroid.processor.Utils.firstLowerCased;
import static com.uidroid.processor.Utils.getClearClassName;
import static com.uidroid.processor.Utils.getCodeString;
import static com.uidroid.processor.Utils.getParams;
import static com.uidroid.processor.Utils.getSimpleName;
import static com.uidroid.processor.Utils.getTypedParams;

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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class ViewBinderFactoryProcessor {

    private final Filer filer;
    private final Messager messager;

    public ViewBinderFactoryProcessor(ProcessingEnvironment processingEnv) {
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    public void process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set.isEmpty()) {
            return;
        }

        final Map<String, BinderInfo> result = getDefaultBinders(); // binder class mapped to his info
        final List<String> resultFields = new ArrayList<>(); // constructor parameters

        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(UI.BinderFor.class)) {
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                continue;
            }

            UI.BinderFor element = annotatedElement.getAnnotation(UI.BinderFor.class);

            if (element != null) {
                String clazz = getClearClassName(annotatedElement.asType().toString());

                if (!result.containsKey(clazz)) {
                    result.put(clazz, new BinderInfo());
                }

                result.get(clazz).addClass(clazz);
                result.get(clazz).addClass(getClassFromAnnotation(element));

                TypeElement typeElement = (TypeElement) annotatedElement;
                List<? extends Element> elements = typeElement.getEnclosedElements();

                for (Element e: elements) {
                    if (e.getKind().equals(ElementKind.CONSTRUCTOR)) {
                        ExecutableElement executableElement = (ExecutableElement) e;
                        List<? extends VariableElement>  list = executableElement.getParameters();
                        BinderInfo itemInfo = result.get(clazz);

                        if (itemInfo != null) {
                            for (VariableElement variableElement: list) {
                                itemInfo.constructorParameters.add(variableElement.asType().toString());
                                if (!resultFields.contains(variableElement.asType().toString())) {
                                    resultFields.add(variableElement.asType().toString());
                                }
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

            UI.Binder element = annotatedElement.getAnnotation(UI.Binder.class);

            if (element != null) {
                String clazz = getClearClassName(annotatedElement.asType().toString());

                if (!result.containsKey(clazz)) {
                    result.put(clazz, new BinderInfo());
                }
                result.get(clazz).addClass(clazz);

                TypeElement typeElement = (TypeElement) annotatedElement;
                List<? extends Element> elements = typeElement.getEnclosedElements();

                for (Element e: elements) {
                    if (e.getKind().equals(ElementKind.CONSTRUCTOR)) {
                        ExecutableElement executableElement = (ExecutableElement) e;
                        List<? extends VariableElement>  list = executableElement.getParameters();
                        BinderInfo itemInfo = result.get(clazz);

                        if (itemInfo != null) {
                            for (VariableElement variableElement: list) {
                                itemInfo.constructorParameters.add(variableElement.asType().toString());
                                if (!resultFields.contains(variableElement.asType().toString())) {
                                    resultFields.add(variableElement.asType().toString());
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }

        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(UI.BindWith.class)) {
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                continue;
            }

            UI.BindWith element = annotatedElement.getAnnotation(UI.BindWith.class);

            if (element != null) {
                String clazz = getClearClassName(getClassFromAnnotation(element));

                if (!result.containsKey(clazz)) {
                    result.put(clazz, new BinderInfo());
                }
                result.get(clazz).addClass(annotatedElement.asType().toString());

            }
        }

        try {
            write(result, resultFields);
        } catch (IOException e) {
            error(e.getMessage());
        }
    }

    private Map<String, BinderInfo> getDefaultBinders() {
        final String UIDroidBindersPackage = "com.uidroid.uidroid.binder";
        final String androidWidgetPackage = "android.widget";

        final Map<String, BinderInfo> binderInfoMap = new LinkedHashMap<>();

        binderInfoMap.put(UIDroidBindersPackage + ".CheckBoxBinder",
                new BinderInfo().putClass(UIDroidBindersPackage + ".CheckBoxBinder")
                        .putClass(androidWidgetPackage + ".CheckBox"));

        binderInfoMap.put(UIDroidBindersPackage + ".EditTextViewBinder",
                new BinderInfo().putClass(UIDroidBindersPackage + ".EditTextViewBinder")
                        .putClass(androidWidgetPackage + ".EditText"));

        binderInfoMap.put(UIDroidBindersPackage + ".TextViewBinder",
                new BinderInfo().putClass(UIDroidBindersPackage + ".TextViewBinder")
                        .putClass(androidWidgetPackage + ".TextView"));

        binderInfoMap.put(UIDroidBindersPackage + ".ImageViewBinder",
                new BinderInfo().putClass(UIDroidBindersPackage + ".ImageViewBinder")
                        .putClass(androidWidgetPackage + ".ImageView"));

        binderInfoMap.put(UIDroidBindersPackage + ".ViewPagerBinder",
                new BinderInfo().putClass(UIDroidBindersPackage + ".ViewPagerBinder")
                        .putClass("androidx.viewpager.widget.ViewPager"));

        binderInfoMap.put(UIDroidBindersPackage + ".RecyclerViewBinder",
                new BinderInfo().putClass(UIDroidBindersPackage + ".RecyclerViewBinder")
                        .putClass("androidx.recyclerview.widget.RecyclerView"));

        return binderInfoMap;
    }

    private void write(Map<String, BinderInfo> result, List<String> resultFields) throws IOException {
        String packageName;
        int lastDot = "com.uidroid.uidroid.factory.ViewBinderFactory".lastIndexOf('.');
        packageName = "com.uidroid.uidroid.factory.ViewBinderFactory".substring(0, lastDot);

        JavaFileObject builderFile = filer
                .createSourceFile("com.uidroid.uidroid.factory.ViewBinderFactory");

        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            out.print("package ");
            out.print(packageName);
            out.println(";");
            out.println();
            out.println("import android.view.View;");
            out.println("import java.util.List;");
            out.println("import java.util.ArrayList;");
            out.println("import com.uidroid.uidroid.factory.IViewBinderFactory;");
            out.println("import com.uidroid.uidroid.binder.IViewBinder;");
            for (String clazz : result.keySet()) {
                out.println("import " + getClearClassName(clazz) + ";");
            }
            for (String s : resultFields) {
                out.println("import " + s + ";");
            }
            out.println();

            out.print("public final class ViewBinderFactory implements IViewBinderFactory { \n\n");
            for (String s : resultFields) {
                out.print("  private " + getSimpleName(s) + " " + firstLowerCased(getSimpleName(s)) + "; \n");
            }
            out.print("\n");
            out.print("  public ViewBinderFactory(" + getTypedParams(resultFields) + ") { \n");
            for (String s : resultFields) {
                out.print("    this." + firstLowerCased(getSimpleName(s)) + " = " + firstLowerCased(getSimpleName(s)) + "; \n");
            }
            out.print("  } \n\n");

            out.print("  public IViewBinder build(String value) { \n");
            out.print("    switch(value) { \n");
            result.forEach((type, value) -> {
                for (String clazz: value.viewClassNames) {
                    out.print("      case " + getCodeString(getClearClassName(clazz)) + ": \n");
                }
                out.print("        return new " + getSimpleName(type) + "(" + getParams(value.constructorParameters) + "); \n");
            });
            out.print("      default: \n");
            out.print("         throw new RuntimeException(\"Cannot create binder for \" + value); \n");
            out.print("    } \n");
            out.print("  } \n\n");
            out.println("}");
        }
    }

    private String getClassFromAnnotation(UI.BinderFor annotation) {
        try {
            Class<?> value = annotation.view();
            return value.getCanonicalName();
        } catch (MirroredTypeException mte) {
            return mte.getTypeMirror().toString();
        }
    }

    private String getClassFromAnnotation(UI.BindWith annotation) {
        try {
            Class<?> value = annotation.binder();
            return value.getCanonicalName();
        } catch (MirroredTypeException mte) {
            return mte.getTypeMirror().toString();
        }
    }

    private void error(String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message);
    }

    public static class BinderInfo {

        public List<String> viewClassNames = new ArrayList<>();
        public List<String> constructorParameters = new ArrayList<>();

        public BinderInfo() {

        }

        public void addClass(String clazz) {
            if (!viewClassNames.contains(clazz)) {
                viewClassNames.add(clazz);
            }
        }

        public BinderInfo putClass(String clazz) {
            if (!viewClassNames.contains(clazz)) {
                viewClassNames.add(clazz);
            }
            return this;
        }

    }
}
