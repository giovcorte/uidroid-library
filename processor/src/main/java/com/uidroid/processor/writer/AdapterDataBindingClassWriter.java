package com.uidroid.processor.writer;

import static com.uidroid.processor.Utils.codeString;
import static com.uidroid.processor.Utils.combineClassName;
import static com.uidroid.processor.Utils.simpleName;

import com.uidroid.processor.AbstractClassWriter;
import com.uidroid.processor.items.BindableObjectImpl;
import com.uidroid.processor.items.BindingMethodImpl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.tools.JavaFileObject;

public class AdapterDataBindingClassWriter extends AbstractClassWriter {

    public AdapterDataBindingClassWriter(Filer filer, Messager messager) {
        super(filer, messager);
    }

    public void writeAdapterDataBindingClass(Map<String, BindableObjectImpl> bindableObjects,
                                             Map<String, BindingMethodImpl> methods)
            throws IOException {
        String packageName;
        int lastDot = "com.uidroid.uidroid.AdapterDataBinding".lastIndexOf('.');
        packageName = "com.uidroid.uidroid.AdapterDataBinding".substring(0, lastDot);

        JavaFileObject builderFile = filer
                .createSourceFile("com.uidroid.uidroid.AdapterDataBinding");

        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            out.print("package ");
            out.print(packageName);
            out.println(";");
            out.println();
            out.println("import android.content.Context;");
            out.println("import com.uidroid.uidroid.IAdapterDataBinding;");
            out.println("import com.uidroid.uidroid.DataBinding;");
            for (BindableObjectImpl object: bindableObjects.values()) {
                out.println("import " + object.viewClassName + ";");
                out.println("import " + object.className + ";");
            }

            out.println();

            out.print("public final class AdapterDataBinding implements IAdapterDataBinding { \n\n");

            // instance variable
            out.print("  final DataBinding dataBinding; \n\n");

            // constructor
            out.print("  public AdapterDataBinding(DataBinding dataBinding) { \n");
            out.print("    this.dataBinding = dataBinding; \n");
            out.print("  } \n\n");

            out.print("  public void bind(IView view, IData data) { \n");
            out.print("    switch(pair(view, data)) { \n");

            for (BindableObjectImpl object: bindableObjects.values()) {
                String simpleViewName = simpleName(object.viewClassName);
                String simpleDataName = simpleName(object.className);

                String key = combineClassName(simpleViewName, simpleDataName);

                if (methods.containsKey(key)) {
                    out.print("      case" + codeString(simpleViewName + simpleDataName) + ": \n");
                    out.print("        dataBinding.bind((" + simpleViewName + ") view, (" + simpleDataName + ") data); \n");
                    out.print("        break;");
                }
            }
            out.print("      default: \n");
            out.print("         throw new RuntimeException(\"Cannot bind view for \" + pair(view, data)); \n");
            out.print("    } \n");
            out.print("  } \n\n");

            // helper method
            out.print("  private String pair(IView view, IData data) { \n");
            out.print("    return view.name() + data.name(); \n");
            out.print("  } \n\n");

            out.println("}");
        }
    }

}
