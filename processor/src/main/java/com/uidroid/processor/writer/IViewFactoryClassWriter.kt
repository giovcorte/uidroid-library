package com.uidroid.processor.writer

import com.uidroid.processor.AbstractClassWriter
import com.uidroid.processor.Utils.getCodeString
import com.uidroid.processor.Utils.getSimpleName
import com.uidroid.processor.items.BindableObjectImpl
import com.uidroid.processor.items.BindableViewImpl
import java.io.IOException
import java.io.PrintWriter
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager

class IViewFactoryClassWriter(filer: Filer, messager: Messager) : AbstractClassWriter(
    filer, messager
) {
    @Throws(IOException::class)
    fun writeIViewFactoryClass(
        objects: Map<String, BindableObjectImpl>,
        views: Map<String, BindableViewImpl>
    ) {
        val packageName: String
        val lastDot = "com.uidroid.uidroid.factory.ViewFactory".lastIndexOf('.')
        packageName = "com.uidroid.uidroid.factory.ViewFactory".substring(0, lastDot)
        val builderFile = filer
            .createSourceFile("com.uidroid.uidroid.factory.ViewFactory")
        PrintWriter(builderFile.openWriter()).use { out ->
            out.print("package ")
            out.print(packageName)
            out.println(";")
            out.println()
            out.println("import android.view.View;")
            out.println("import android.content.Context;")
            out.println("import com.uidroid.uidroid.IViewFactory;")
            out.println("import com.uidroid.uidroid.IView;")
            out.println("import com.uidroid.uidroid.IData;")
            for (data in objects.values) {
                out.println("import " + data.viewClassName + ";")
            }
            out.println()
            out.print("public final class ViewFactory implements IViewFactory { \n\n")

            // instance variable
            out.print("  private final Context context; \n\n")

            // constructor
            out.print("  public ViewFactory(Context context) { \n")
            out.print("    this.context = context; \n")
            out.print("  } \n\n")
            out.print("  public IView build(IData data) { \n")
            out.print("    switch(data.name()) { \n")
            for (data in objects.values) {
                if (views.containsKey(data.viewClassName) && views[data.viewClassName]!!.implementIView) {
                    out.print(
                        """      case ${getCodeString(getSimpleName(data.className))}: 
"""
                    )
                    out.print(
                        """        return new ${getSimpleName(data.viewClassName)}(context); 
"""
                    )
                }
            }
            out.print("      default: \n")
            out.print("         throw new RuntimeException(\"Cannot create view for \" + data.name()); \n")
            out.print("    } \n")
            out.print("  } \n\n")
            out.println("}")
        }
    }
}