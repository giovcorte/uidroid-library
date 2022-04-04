package com.uidroid.processor.writer;

import static com.uidroid.processor.Utils.getCombinedClassName;
import static com.uidroid.processor.Utils.getParams;
import static com.uidroid.processor.Utils.getSimpleName;
import static com.uidroid.processor.Utils.getTypedParams;
import static com.uidroid.processor.Utils.lower;

import com.uidroid.processor.AbstractClassWriter;
import com.uidroid.processor.items.BindableActionFieldImpl;
import com.uidroid.processor.items.BindableActionImpl;
import com.uidroid.processor.items.BindableViewImpl;
import com.uidroid.processor.items.BindableViewFieldImpl;
import com.uidroid.processor.items.BindingMethodImpl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.tools.JavaFileObject;

public class DataBindingClassWriter extends AbstractClassWriter {

    public DataBindingClassWriter(Filer filer, Messager messager) {
        super(filer, messager);
    }

    public void writeDataBindingClass(Map<String, BindableViewImpl> views,
                                      Map<String, BindingMethodImpl> methods) throws IOException {
        String packageName;
        int lastDot = "com.uidroid.uidroid.DataBinding".lastIndexOf('.');
        packageName = "com.uidroid.uidroid.DataBinding".substring(0, lastDot);

        String simpleClassName = "com.uidroid.uidroid.DataBinding".substring(lastDot + 1);

        JavaFileObject builderFile = filer
                .createSourceFile("com.uidroid.uidroid.DataBinding");

        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            // basic imports
            out.print("package ");
            out.print(packageName);
            out.println(";");
            out.println();
            out.println("import com.uidroid.uidroid.DataBindingHelper;");
            out.println("import java.util.List;");
            out.println("import java.util.ArrayList;");
            out.println("import android.view.View;");

            // user imports
            Set<String> viewImported = new HashSet<>();
            Set<String> dependenciesImported = new HashSet<>();

            for (BindingMethodImpl method : methods.values()) {
                out.println("import " + method.enclosingClass + ";");
                out.println("import " + method.dataClass + ";");
                out.println("import " + method.viewClass + ";");

                for (String dependency: method.dependencies) {
                    if (!dependenciesImported.contains(dependency)) {
                        out.println("import " + dependency + ";");
                    }
                    dependenciesImported.add(dependency);
                }
                viewImported.add(method.viewClass);
            }

            for (BindableViewImpl view : views.values()) {
                if (!viewImported.contains(view.className)) {
                    out.println("import " + view.className + ";");
                }

                for (List<BindableViewFieldImpl> fields: view.bindableViewFields.values()) {
                    for (BindableViewFieldImpl field : fields) {
                        if (!viewImported.contains(field.fieldViewClassName)) {
                            out.println("import " + field.fieldViewClassName + ";");
                        }
                    }
                }
            }
            out.println();

            // open class
            out.print("public final class " + simpleClassName + " { \n\n");

            // instance variables
            for (String dependency: dependenciesImported) {
                out.print("  private final " + getSimpleName(dependency) + " " + lower(getSimpleName(dependency) + "; \n"));
            }
            out.println();

            // constructor
            out.print("  public DataBinding("
                    + getTypedParams(new ArrayList<>(dependenciesImported))
                    + ") { \n");
            for (String dependency: dependenciesImported) {
                out.print("    this." + lower(getSimpleName(dependency)) + " = "
                        + lower(getSimpleName(dependency) + "; \n"));
            }
            out.print("  } \n\n");

            // bind overloaded methods
            for (String viewModelPair: methods.keySet()) {
                BindingMethodImpl method = methods.get(viewModelPair);

                String simpleViewClassName = getSimpleName(method.viewClass);
                String simpleModelClass = getSimpleName(method.dataClass);

                String enclosingClass = method.enclosingClass;
                String methodName = method.methodName;

                out.print("  public void bind(" + simpleViewClassName + " view, " + simpleModelClass + " data) { \n");

                if (!method.dependencies.isEmpty()) { // method with dependencies
                    out.print("    " + getSimpleName(enclosingClass) + "." + methodName + "(view, data, "
                            + getParams(method.dependencies) + "); \n");
                } else { // method with only view and data
                    out.print("    " + getSimpleName(enclosingClass) + "." + methodName + "(view, data); \n");
                }

                // this is a custom view
                if (views.containsKey(method.viewClass)) {
                    // main view action for this data class
                    Map<String, BindableActionImpl> actionsMap =
                            views.get(method.viewClass).actions;

                    if (actionsMap.containsKey(simpleModelClass)) {
                        BindableActionImpl action = actionsMap.get(simpleModelClass);

                        out.print("    DataBindingHelper.bindAction(view, data."
                                + action.path + "); \n");
                    }

                    // view field binding
                    Map<String, List<BindableViewFieldImpl>> viewFieldsMap =
                            views.get(method.viewClass).bindableViewFields;

                    if (viewFieldsMap.containsKey(simpleModelClass)) {
                        List<BindableViewFieldImpl> fields = viewFieldsMap.get(simpleModelClass);

                        for (BindableViewFieldImpl field: fields) {
                            String simpleFieldViewClass = getSimpleName(field.fieldViewClassName);
                            String simpleFieldDataClass = field.fieldObjectClassName;
                            String key = getCombinedClassName(simpleFieldViewClass, simpleFieldDataClass);

                            if (methods.containsKey(key)) {
                                out.print("    bind(view." + field.fieldName
                                        + " , data." + field.objectPath + "); \n");
                            }
                        }
                    }

                    // view field actions
                    Map<String, List<BindableActionFieldImpl>> actionFieldsMap =
                            views.get(method.viewClass).bindableActionFields;

                    if (actionFieldsMap.containsKey(simpleModelClass)) {
                        List<BindableActionFieldImpl> fields = actionFieldsMap.get(simpleModelClass);

                        for (BindableActionFieldImpl field: fields) {
                            out.print("    DataBindingHelper.bindAction(view." + field.fieldName
                                    + " , data." + field.objectPath + "); \n");
                        }
                    }
                }

                out.print("  } \n\n");
            }

            // close class
            out.print("}");
        }
    }

}
