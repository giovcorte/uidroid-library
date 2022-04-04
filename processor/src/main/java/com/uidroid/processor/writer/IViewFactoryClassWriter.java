package com.uidroid.processor.writer;

import static com.uidroid.processor.Utils.getCodeString;
import static com.uidroid.processor.Utils.getSimpleName;

import com.uidroid.processor.AbstractClassWriter;
import com.uidroid.processor.items.BindableObjectImpl;
import com.uidroid.processor.items.BindableViewImpl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.tools.JavaFileObject;

public class IViewFactoryClassWriter extends AbstractClassWriter {

    public IViewFactoryClassWriter(Filer filer, Messager messager) {
        super(filer, messager);
    }

    public void writeIViewFactoryClass(Map<String, BindableObjectImpl> objects,
                                              Map<String, BindableViewImpl> views)
            throws IOException {
        String packageName;
        int lastDot = "com.uidroid.uidroid.factory.ViewFactory".lastIndexOf('.');
        packageName = "com.uidroid.uidroid.factory.ViewFactory".substring(0, lastDot);

        JavaFileObject builderFile = filer
                .createSourceFile("com.uidroid.uidroid.factory.ViewFactory");

        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            out.print("package ");
            out.print(packageName);
            out.println(";");
            out.println();
            out.println("import android.view.View;");
            out.println("import android.content.Context;");
            out.println("import com.uidroid.uidroid.IViewFactory;");
            out.println("import com.uidroid.uidroid.IView;");
            out.println("import com.uidroid.uidroid.IData;");
            for (BindableObjectImpl data: objects.values()) {
                out.println("import " + data.viewClassName + ";");
            }
            out.println();

            out.print("public final class ViewFactory implements IViewFactory { \n\n");

            // instance variable
            out.print("  private final Context context; \n\n");

            // constructor
            out.print("  public ViewFactory(Context context) { \n");
            out.print("    this.context = context; \n");
            out.print("  } \n\n");

            out.print("  public IView build(IData data) { \n");
            out.print("    switch(data.name()) { \n");
            for (BindableObjectImpl data: objects.values()) {
                if (views.containsKey(data.viewClassName) && views.get(data.viewClassName).implementIView) {
                    out.print("      case " + getCodeString(getSimpleName(data.className)) + ": \n");
                    out.print("        return new " + getSimpleName(data.viewClassName) + "(context); \n");
                }
            }
            out.print("      default: \n");
            out.print("         throw new RuntimeException(\"Cannot create view for \" + data.name()); \n");
            out.print("    } \n");
            out.print("  } \n\n");
            out.println("}");
        }
    }

}
