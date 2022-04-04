package com.uidroid.processor.writer

import com.uidroid.processor.AbstractClassWriter
import com.uidroid.processor.Utils.getCodeString
import com.uidroid.processor.Utils.getCombinedClassName
import com.uidroid.processor.Utils.getSimpleName
import com.uidroid.processor.items.BindableObjectImpl
import com.uidroid.processor.items.BindingMethodImpl
import java.io.IOException
import java.io.PrintWriter
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager

class AdapterDataBindingClassWriter(filer: Filer, messager: Messager) : AbstractClassWriter(
    filer, messager
) {
    @Throws(IOException::class)
    fun writeAdapterDataBindingClass(
        bindableObjects: Map<String, BindableObjectImpl>,
        methods: Map<String, BindingMethodImpl?>
    ) {
        val packageName: String
        val lastDot = "com.uidroid.uidroid.AdapterDataBinding".lastIndexOf('.')
        packageName = "com.uidroid.uidroid.AdapterDataBinding".substring(0, lastDot)
        val builderFile = filer
            .createSourceFile("com.uidroid.uidroid.AdapterDataBinding")
        PrintWriter(builderFile.openWriter()).use { out ->
            out.print("package ")
            out.print(packageName)
            out.println(";")
            out.println()
            out.println("import android.content.Context;")
            out.println("import com.uidroid.uidroid.IAdapterDataBinding;")
            out.println("import com.uidroid.uidroid.DataBinding;")
            for (`object` in bindableObjects.values) {
                out.println("import " + `object`.viewClassName + ";")
                out.println("import " + `object`.className + ";")
            }
            out.println()
            out.print("public final class AdapterDataBinding implements IAdapterDataBinding { \n\n")

            // instance variable
            out.print("  final DataBinding dataBinding; \n\n")

            // constructor
            out.print("  public AdapterDataBinding(DataBinding dataBinding) { \n")
            out.print("    this.dataBinding = dataBinding; \n")
            out.print("  } \n\n")
            out.print("  public void bind(IView view, IData data) { \n")
            out.print("    switch(pair(view, data)) { \n")
            for (`object` in bindableObjects.values) {
                val simpleViewName = getSimpleName(`object`.viewClassName)
                val simpleDataName = getSimpleName(`object`.className)
                val key = getCombinedClassName(simpleViewName, simpleDataName)
                if (methods.containsKey(key)) {
                    out.print(
                        """      case${getCodeString(simpleViewName + simpleDataName)}: 
"""
                    )
                    out.print("        dataBinding.bind(($simpleViewName) view, ($simpleDataName) data); \n")
                }
            }
            out.print("      default: \n")
            out.print("         throw new RuntimeException(\"Cannot bind view for \" + pair(view, data)); \n")
            out.print("    } \n")
            out.print("  } \n\n")

            // helper method
            out.print("  private String pair(IView view, IData data) { \n")
            out.print("    return view.name() + data.name(); \n")
            out.print("  } \n\n")
            out.println("}")
        }
    }
}