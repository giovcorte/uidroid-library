package com.uidroid.processor;

import static com.uidroid.processor.Utils.getCodeString;
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
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class ViewFactoryProcessor {

    private final Filer filer;
    private final Messager messager;

    public ViewFactoryProcessor(ProcessingEnvironment processingEnv) {
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    public void process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set.isEmpty()) {
            return;
        }

        final List<String> result = new ArrayList<>();

        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(UI.CustomView.class)) {
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                error(annotatedElement);
                continue;
            }

            UI.CustomView autoElement = annotatedElement.getAnnotation(UI.CustomView.class);

            if (autoElement != null) {
                String clazz = annotatedElement.asType().toString();
                if (!result.contains(clazz)) {
                    result.add(clazz);
                }
            }
        }

        try {
            write(result);
        } catch (IOException e) {
            error(e.getMessage());
        }
    }

    private void write(List<String> result) throws IOException {
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
            out.println("import android.view.View;");
            out.println("import android.widget.TextView;");
            out.println("import android.widget.ImageView;");
            out.println("import androidx.recyclerview.widget.RecyclerView;");
            out.println("import com.uidroid.uidroid.factory.IViewFactory;");
            out.println("import com.uidroid.uidroid.DatabindingException;");
            for (String clazz: result) {
                out.println("import " + clazz + ";");
            }
            out.println();

            out.print("public final class ViewFactory implements IViewFactory { \n\n");
            out.print("  public View build(Context context, String value) { \n");
            out.print("    switch(value) { \n");
            out.print("      case " + getCodeString("android.widget.TextView") + ": \n");
            out.print("        return new TextView(context); \n");
            out.print("      case " + getCodeString("android.widget.ImageView") + ": \n");
            out.print("        return new ImageView(context); \n");
            out.print("      case " + getCodeString("androidx.recyclerview.widget.RecyclerView") + ": \n");
            out.print("        return new RecyclerView(context); \n");
            result.forEach(entry -> {
                out.print("      case" + getCodeString(entry) + ": \n");
                out.print("        return new " + getSimpleName(entry) + "(context); \n");
            });
            out.print("      default: \n");
            out.print("         throw new DatabindingException(\"Cannot create View for value\"); \n");
            out.print("    } \n");
            out.print("  } \n\n");
            out.println("}");
        }
    }

    private void error(Element element) {
        messager.printMessage(Diagnostic.Kind.ERROR, "Only class can be annotated with UI.CompositeView", element);
    }

    private void error(String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message);
    }

}
