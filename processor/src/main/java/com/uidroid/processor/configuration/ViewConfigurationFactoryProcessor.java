package com.uidroid.processor.configuration;

import static com.uidroid.processor.Utils.getCodeParams;
import static com.uidroid.processor.Utils.getCodeString;
import static com.uidroid.processor.Utils.getSimpleName;

import com.uidroid.annotation.UI;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
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
import javax.lang.model.type.MirroredTypeException;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class ViewConfigurationFactoryProcessor {

    private final Filer filer;
    private final Messager messager;
    private final ProcessingEnvironment processingEnvironment;

    public ViewConfigurationFactoryProcessor(ProcessingEnvironment processingEnv) {
        this.processingEnvironment = processingEnv;
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
    }

    public void process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set.isEmpty()) {
            return;
        }

        final Map<String, ConfigurationObject> map = new LinkedHashMap<>();

        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(UI.ViewConfiguration.class)) {
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                error("Only class can be annotated with UI.ViewConfiguration", annotatedElement);
                continue;
            }

            UI.ViewConfiguration item = annotatedElement.getAnnotation(UI.ViewConfiguration.class);

            if (item != null) {
                String clazz = annotatedElement.asType().toString();

                if (!map.containsKey(clazz)) {
                    map.put(clazz, new ConfigurationObject());
                }
                map.get(clazz).viewType = getViewClass(item);

                UI.BindWith binder = annotatedElement.getAnnotation(UI.BindWith.class);
                if (binder != null) {
                    map.get(clazz).binder = getBinderClass(binder);
                }

                UI.StableParam[] params = item.params();
                for (UI.StableParam param: params) {
                    map.get(clazz).uiFields.add(new StableParameter(param.key(), param.value()));
                }
            }
        }

        for (Element annotatedElement: roundEnvironment.getElementsAnnotatedWith(UI.Configuration.class)) {
            if (annotatedElement.getKind() != ElementKind.FIELD) {
                error("Only fields can be annotated with UI.Configuration", annotatedElement);
                continue;
            }

            UI.Configuration item = annotatedElement.getAnnotation(UI.Configuration.class);

            if (item != null) {
                VariableElement variable = ((VariableElement) annotatedElement);

                String fieldName = variable.getSimpleName().toString();
                String className = ((TypeElement) annotatedElement.getEnclosingElement()).getQualifiedName().toString();
                String key = item.key();

                if (key.equals("")) {
                    key = fieldName;
                }

                if (map.containsKey(className)) {
                    map.get(className).uiFields.add(new Configuration(fieldName, key));
                }
            }
        }

        for (Element annotatedElement: roundEnvironment.getElementsAnnotatedWith(UI.Param.class)) {
            if (annotatedElement.getKind() != ElementKind.FIELD) {
                error("Only fields can be annotated with UI.Param", annotatedElement);
                continue;
            }

            UI.Param item = annotatedElement.getAnnotation(UI.Param.class);

            if (item != null) {
                VariableElement variable = ((VariableElement) annotatedElement);

                String fieldName = variable.getSimpleName().toString();
                String className = ((TypeElement) annotatedElement.getEnclosingElement()).getQualifiedName().toString();
                String key = item.key();

                if (key.equals("")) {
                    key = fieldName;
                }

                if (map.containsKey(className)) {
                    map.get(className).uiFields.add(new Parameter(fieldName, key));
                }
            }
        }

        for (Element annotatedElement: roundEnvironment.getElementsAnnotatedWith(UI.ConfigurationsList.class)) {
            if (annotatedElement.getKind() != ElementKind.FIELD) {
                error("Only fields can be annotated with UI.ConfigurationsList", annotatedElement);
                continue;
            }

            UI.ConfigurationsList item = annotatedElement.getAnnotation(UI.ConfigurationsList.class);

            if (item != null) {
                VariableElement variable = ((VariableElement) annotatedElement);

                String fieldName = variable.getSimpleName().toString();
                String className = ((TypeElement) annotatedElement.getEnclosingElement())
                        .getQualifiedName().toString();
                String key = item.key();

                if (key.equals("")) {
                    key = fieldName;
                }

                if (map.containsKey(className)) {
                    map.get(className).uiFields.add(new ConfigurationList(key, fieldName));
                }
            }
        }

        for (Element annotatedElement: roundEnvironment.getElementsAnnotatedWith(UI.Action.class)) {
            if (annotatedElement.getKind() != ElementKind.FIELD) {
                error("Only fields can be annotated with UI.ConfigurationsList", annotatedElement);
                continue;
            }

            UI.Action item = annotatedElement.getAnnotation(UI.Action.class);

            if (item != null) {
                VariableElement variable = ((VariableElement) annotatedElement);

                String fieldName = variable.getSimpleName().toString();
                String className = ((TypeElement) annotatedElement.getEnclosingElement()).getQualifiedName().toString();

                if (map.containsKey(className)) {
                    map.get(className).uiFields.add(new Action(fieldName));
                }
            }
        }

        for (Element annotatedElement: roundEnvironment.getElementsAnnotatedWith(UI.Id.class)) {
            if (annotatedElement.getKind() != ElementKind.FIELD) {
                error("Only fields can be annotated with UI.Param", annotatedElement);
                continue;
            }

            UI.Id item = annotatedElement.getAnnotation(UI.Id.class);

            if (item != null) {
                VariableElement variable = ((VariableElement) annotatedElement);

                String fieldName = variable.getSimpleName().toString();
                String className = ((TypeElement) annotatedElement.getEnclosingElement())
                        .getQualifiedName().toString();

                if (map.containsKey(className)) {
                    map.get(className).id = fieldName;
                }
            }
        }

        for (Element annotatedElement: roundEnvironment.getElementsAnnotatedWith(UI.FieldConfiguration.class)) {
            if (annotatedElement.getKind() != ElementKind.FIELD) {
                error("Only fields can be annotated with UI.Param", annotatedElement);
                continue;
            }

            UI.FieldConfiguration item = annotatedElement.getAnnotation(UI.FieldConfiguration.class);

            if (item != null) {
                VariableElement variable = ((VariableElement) annotatedElement);

                String id = item.id();
                String key = item.key();
                if (key.equals("")) {
                    key = variable.getSimpleName().toString();
                }

                String viewClass = getViewClass(item);
                String fieldName = variable.getSimpleName().toString();
                String className = ((TypeElement) annotatedElement.getEnclosingElement()).getQualifiedName().toString();
                UI.StableParam[] params = item.params();

                UI.BindWith binder = variable.getAnnotation(UI.BindWith.class);

                if (map.containsKey(className)) {
                    ConfigurationObject configuration =
                            new ConfigurationObject(viewClass, null, id, fieldName, key);

                    if (binder != null) {
                        configuration.binder = getBinderClass(binder);
                    }

                    for (UI.StableParam param: params) {
                        configuration.uiFields.add(new StableParameter(param.key(), param.value()));
                    }

                    map.get(className).uiFields.add(configuration);
                }
            }
        }

        try {
            write(map);
        } catch (IOException e) {
            error(e.getMessage());
        }
    }

    private void write(Map<String, ConfigurationObject> map) throws IOException {
        String packageName;
        int lastDot = "com.uidroid.uidroid.factory.ViewConfigurationFactory".lastIndexOf('.');
        packageName = "com.uidroid.uidroid.factory.ViewConfigurationFactory".substring(0, lastDot);

        String simpleClassName = "com.uidroid.uidroid.factory.ViewConfigurationFactory".substring(lastDot + 1);

        JavaFileObject builderFile = filer
                .createSourceFile("com.uidroid.uidroid.factory.ViewConfigurationFactory");

        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            // imports
            out.print("package ");
            out.print(packageName);
            out.println(";");
            out.println();
            out.println("import com.uidroid.uidroid.model.ViewConfiguration;");
            out.println("import com.uidroid.uidroid.factory.IViewConfigurationFactory;");
            out.println("import java.util.List;");
            out.println("import java.util.ArrayList;");
            for (String clazz: map.keySet()) {
                out.println("import " + clazz + ";");
            }
            out.println();

            // class
            out.print("public final class " + simpleClassName + " implements IViewConfigurationFactory { \n\n");

            // main method
            out.print("  public ViewConfiguration build(Object object) { \n");
            out.print("    switch(object.getClass().getCanonicalName()) { \n");
            map.forEach((objectView, value) -> {
                out.print("      case " + getCodeString(objectView) + ": \n");
                out.print("        return this.build((" + getSimpleName(objectView) + ") object); \n");
            });
            out.print("      default: \n");
            out.print("         return null; \n");
            out.print("    } \n");
            out.print("  } \n\n");

            // methods
            map.forEach((key, configurationObject) -> {
                out.print("  public ViewConfiguration build(" + getSimpleName(key) + " value) { \n");
                out.print("    if (value == null) { \n");
                out.print("      return null; \n");
                out.print("    } \n");
                out.print("    " + getIdCodeString(configurationObject.id) + " \n");
                out.print("    ViewConfiguration object = new ViewConfiguration("
                        + getCodeParams("id", getCodeString(configurationObject.viewType), getBinderCodeString(configurationObject.binder))
                        + "); \n");

                for (UIField uiField : configurationObject.uiFields) {
                    uiField.printCode(out);
                }

                out.print("    return object; \n");
                out.print("  } \n\n");
            });
            out.print("}");
        }
    }

    private static String getIdCodeString(String idField) {
        if (idField != null) {
            return "final String id = value."
                    + idField + " != null ? value."
                    + idField + " : String.valueOf(value.hashCode());";
        } else {
            return "final String id = String.valueOf(value.hashCode());";
        }
    }

    private static String getBinderCodeString(String binderType) {
        return binderType != null ? getCodeString(binderType) : "null";
    }

    private String getBinderClass(UI.BindWith annotation) {
        try {
            Class<?> value = annotation.binder();
            return value.getCanonicalName();
        } catch (MirroredTypeException mte) {
            return mte.getTypeMirror().toString();
        }
    }

    private String getViewClass(UI.ViewConfiguration annotation) {
        try {
            Class<?> value = annotation.view();
            return value.getCanonicalName();
        } catch (MirroredTypeException mte) {
            return processingEnvironment.getTypeUtils().asElement(mte.getTypeMirror()).asType().toString();
        }
    }

    private String getViewClass(UI.FieldConfiguration annotation) {
        try {
            Class<?> value = annotation.view();
            return value.getCanonicalName();
        } catch (MirroredTypeException mte) {
            return processingEnvironment.getTypeUtils().asElement(mte.getTypeMirror()).asType().toString();
        }
    }

    private void error(String message, Element element) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private void error(String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message);
    }

}
