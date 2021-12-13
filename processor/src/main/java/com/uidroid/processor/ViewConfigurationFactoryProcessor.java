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
import javax.lang.model.type.MirroredTypeException;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class ViewConfigurationFactoryProcessor {

    private final Filer filer;
    private final Messager messager;
    private final ProcessingEnvironment processingEnvironment;

    private enum FieldToConfigurationElementType {
        OBJECT,
        ELEMENT,
        ELEMENT_LIST,
        PARAMETER,
        ACTION
    }

    public ViewConfigurationFactoryProcessor(ProcessingEnvironment processingEnv) {
        this.processingEnvironment = processingEnv;
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
    }

    public void process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set.isEmpty()) {
            return;
        }

        final Map<String, Configuration> map = new LinkedHashMap<>();

        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(UI.ViewConfiguration.class)) {
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                error("Only class can be annotated with UI.ViewConfiguration", annotatedElement);
                continue;
            }

            UI.ViewConfiguration item = annotatedElement.getAnnotation(UI.ViewConfiguration.class);

            if (item != null) {
                String clazz = annotatedElement.asType().toString();

                if (!map.containsKey(clazz)) {
                    map.put(clazz, new Configuration());
                }
                map.get(clazz).viewType = getViewClass(item);

                UI.BindWith binder = annotatedElement.getAnnotation(UI.BindWith.class);
                if (binder != null) {
                    map.get(clazz).binder = getBinderClass(binder);
                }

                UI.StableParam[] params = item.params();
                for (UI.StableParam param: params) {
                    map.get(clazz).params.add(new StableParam(param.key(), param.value()));
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
                    map.get(className).childConfigurationElements.add(
                            new ChildConfigurationElement(fieldName, FieldToConfigurationElementType.ELEMENT, key));
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
                    map.get(className).childConfigurationElements.add(new ChildConfigurationElement(fieldName, FieldToConfigurationElementType.PARAMETER, key));
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
                    map.get(className).childConfigurationElements.add(
                            new ChildConfigurationElement(fieldName, FieldToConfigurationElementType.ELEMENT_LIST, key));
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
                    map.get(className).childConfigurationElements.add(
                            new ChildConfigurationElement(fieldName, FieldToConfigurationElementType.ACTION, null));
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
                    ChildConfigurationElement childConfigurationElement =
                            new ChildConfigurationElement(fieldName, FieldToConfigurationElementType.OBJECT, key, viewClass, id);

                    if (binder != null) {
                        childConfigurationElement.binder = getBinderClass(binder);
                    }

                    for (UI.StableParam param: params) {
                        childConfigurationElement.params.add(new StableParam(param.key(), param.value()));
                    }

                    map.get(className).childConfigurationElements.add(childConfigurationElement);
                }
            }
        }

        try {
            write(map);
        } catch (IOException e) {
            error(e.getMessage());
        }
    }

    private void write(Map<String, Configuration> map) throws IOException {
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
            out.print("  public ViewConfiguration build(Object object) { \n");
            out.print("    switch(object.getClass().getCanonicalName()) { \n");
            map.forEach((objectView, value) -> {
                out.print("      case " + getCodeString(objectView) + ": \n");
                out.print("        return this.build((" + getSimpleName(objectView) + ") object); \n");
            });
            out.print("      default: \n");
            out.print("         throw new RuntimeException(\"Cannot create configuration for \" + value); \n");
            out.print("    } \n");
            out.print("  } \n\n");

            // methods
            map.forEach((key, configuration) -> {
                String objectView = getSimpleName(key);

                out.print("  public ViewConfiguration build(" + objectView + " value) { \n");
                out.print("    if (value == null) { \n");
                out.print("      return null; \n");
                out.print("    } \n");

                // Construct the ViewConfiguration instance
                String idField = configuration.id;
                String viewType = configuration.viewType;
                if (configuration.binder == null) {
                    if (idField != null) {
                        out.print("    String id = value." + idField
                                + " != null ? value." + idField
                                + " : String.valueOf(value.hashCode()); \n");
                        out.print("    ViewConfiguration object = new ViewConfiguration("
                                + getCodeParams("id", getCodeString(viewType), getCodeString("emptyBinder"))
                                + "); \n");
                    } else {
                        out.print("    ViewConfiguration object = new ViewConfiguration("
                                + getCodeParams("String.valueOf(value.hashCode())", getCodeString(viewType), getCodeString("emptyBinder"))
                                + "); \n");
                    }
                } else {
                    String binderType = configuration.binder;
                    if (configuration.id != null) {
                        out.print("    String id = value." + idField + " != null ? value." + idField
                                + " : String.valueOf(value.hashCode()); \n");
                        out.print("    ViewConfiguration object = new ViewConfiguration("
                                + getCodeParams("id", getCodeString(viewType), getCodeString(binderType))
                                + "); \n");
                    } else {
                        out.print("    ViewConfiguration object = new ViewConfiguration("
                                + getCodeParams("String.valueOf(value.hashCode())", getCodeString(viewType), getCodeString(binderType))
                                + "); \n");
                    }
                }

                int i = 1; // for multiple simple objects method
                for (ChildConfigurationElement element : configuration.childConfigurationElements) {
                    if (element.type.equals(FieldToConfigurationElementType.ACTION)) {
                        out.println("    object.setAction(value." + element.fieldName + ");");
                        continue;
                    }

                    if (element.type.equals(FieldToConfigurationElementType.ELEMENT_LIST)) {
                        out.println("    this.bulk(" + getCodeParams("object",
                                getCodeString(element.key),
                                "new ArrayList<Object>(value." + element.fieldName)
                                + "));");
                        continue;
                    }

                    if (element.type.equals(FieldToConfigurationElementType.ELEMENT)) {
                        String child = "this.build(value." + element.fieldName + ")";
                        out.print("    object.addChildConfiguration("
                                + getCodeParams(getCodeString(element.key), child)
                                + "); \n");
                        continue;
                    }

                    if (element.type.equals(FieldToConfigurationElementType.PARAMETER)) {
                        String child = "value." + element.fieldName;
                        out.print("    object.putParam("
                                + getCodeParams(getCodeString(element.key), child)
                                + "); \n");
                        continue;
                    }
                    // Simple configurations
                    if (element.type.equals(FieldToConfigurationElementType.OBJECT)) {
                        String idCode = !element.id.equals("") ? getCodeString(element.id)
                                : "String.valueOf(value." + element.fieldName + ".hashCode())";
                        String binderCode = element.binder == null ? "null" : getCodeString(element.binder);

                        out.print("    ViewConfiguration config" + i + " = new ViewConfiguration("
                                + getCodeParams(idCode, getCodeString(element.viewClass), binderCode)
                                + "); \n");
                        out.print("    config" + i + ".putParam("
                                + getCodeString(element.key) + ", value." + element.fieldName
                                + "); \n");
                        for (StableParam param: element.params) {
                            out.print("    config" + i + ".putParam("
                                    + getCodeString(param.key) + ", " + getCodeString(param.value)
                                    + "); \n");
                        }
                        out.print("    object.addChildConfiguration("
                                + getCodeParams(getCodeString(element.key), "config" + i)
                                + "); \n");
                        i++;
                    }
                }

                // Stable params
                for (StableParam param: configuration.params) {
                    out.print("    object.putParam("
                            + getCodeParams(getCodeString(param.key), getCodeString(param.value))
                            + "); \n");
                }

                out.print("    return object; \n");
                out.print("  } \n\n");
            });

            out.println("  private void bulk(ViewConfiguration parent, String key, List<Object> objects) { \n" +
                    "    for (Object object: objects) { \n" +
                    "      parent.addChildConfiguration(key, this.build(object)); \n" +
                    "    } \n" +
                    "  }; \n");
            out.println("}");
        }
    }

    static class Configuration {
        public String binder;
        public String viewType;
        public String id;

        public List<ChildConfigurationElement> childConfigurationElements = new ArrayList<>();
        public List<StableParam> params = new ArrayList<>();
    }

    static class StableParam {

        public String value;
        public String key;

        public StableParam(String key, String value) {
            this.value = value;
            this.key = key;
        }
    }

    static class ChildConfigurationElement { // can also represents a param

        public FieldToConfigurationElementType type;

        public String fieldName;
        public String key;

        // for simple objects view elements
        public String viewClass;
        public String id;
        public String binder;
        public List<StableParam> params = new ArrayList<>();

        public ChildConfigurationElement(
                String fieldName,
                FieldToConfigurationElementType type,
                String key) {
            this.fieldName = fieldName;
            this.type = type;
            this.key = key;
        }

        public ChildConfigurationElement(
                String fieldName,
                FieldToConfigurationElementType type,
                String key,
                String viewClass,
                String id) {
            this.fieldName = fieldName;
            this.type = type;
            this.key = key;

            this.viewClass = viewClass;
            this.id = id;
        }

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
