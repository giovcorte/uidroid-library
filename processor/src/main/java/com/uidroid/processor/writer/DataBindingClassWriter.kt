package com.uidroid.processor.writer

import com.uidroid.processor.AbstractClassWriter
import com.uidroid.processor.Utils.getCombinedClassName
import com.uidroid.processor.Utils.getParams
import com.uidroid.processor.Utils.getSimpleName
import com.uidroid.processor.Utils.getTypedParams
import com.uidroid.processor.Utils.lower
import com.uidroid.processor.items.BindableViewImpl
import com.uidroid.processor.items.BindingMethodImpl
import java.io.IOException
import java.io.PrintWriter
import java.util.*
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager

class DataBindingClassWriter(filer: Filer, messager: Messager) : AbstractClassWriter(
    filer, messager
) {
    @Throws(IOException::class)
    fun writeDataBindingClass(
        views: Map<String, BindableViewImpl>,
        methods: Map<String, BindingMethodImpl>
    ) {
        val packageName: String
        val lastDot = "com.uidroid.uidroid.DataBinding".lastIndexOf('.')
        packageName = "com.uidroid.uidroid.DataBinding".substring(0, lastDot)
        val simpleClassName = "com.uidroid.uidroid.DataBinding".substring(lastDot + 1)
        val builderFile = filer
            .createSourceFile("com.uidroid.uidroid.DataBinding")
        PrintWriter(builderFile.openWriter()).use { out ->
            // basic imports
            out.print("package ")
            out.print(packageName)
            out.println(";")
            out.println()
            out.println("import com.uidroid.uidroid.DataBindingHelper;")
            out.println("import java.util.List;")
            out.println("import java.util.ArrayList;")
            out.println("import android.view.View;")

            // user imports
            val viewImported: MutableSet<String> = HashSet()
            val dependenciesImported: MutableSet<String> = HashSet()
            for (method in methods.values) {
                out.println("import " + method.enclosingClass + ";")
                out.println("import " + method.dataClass + ";")
                out.println("import " + method.viewClass + ";")
                for (dependency in method.dependencies) {
                    if (!dependenciesImported.contains(dependency)) {
                        out.println("import $dependency;")
                    }
                    dependenciesImported.add(dependency)
                }
                viewImported.add(method.viewClass)
            }
            for (view in views.values) {
                if (!viewImported.contains(view.className)) {
                    out.println("import " + view.className + ";")
                }
                for (fields in view.bindableViewFields.values) {
                    for (field in fields) {
                        if (!viewImported.contains(field.fieldViewClassName)) {
                            out.println("import " + field.fieldViewClassName + ";")
                        }
                    }
                }
            }
            out.println()

            // open class
            out.print("public final class $simpleClassName { \n\n")

            // instance variables
            for (dependency in dependenciesImported) {
                out.print(
                    "  private final " + getSimpleName(dependency) + " " + lower(
                        getSimpleName(
                            dependency
                        ) + "; \n"
                    )
                )
            }
            out.println()

            // constructor
            out.print("""  public DataBinding(${getTypedParams(ArrayList(dependenciesImported))}) { """)
            for (dependency in dependenciesImported) {
                out.print("    this." + lower(getSimpleName(dependency)) + " = " + lower("""${getSimpleName(dependency)};""".trimIndent()))
            }
            out.print("  } \n\n")

            // bind overloaded methods
            for (viewModelPair in methods.keys) {
                val method = methods[viewModelPair]
                val simpleViewClassName = getSimpleName(method!!.viewClass)
                val simpleModelClass = getSimpleName(method.dataClass)
                val enclosingClass = method.enclosingClass
                val methodName = method.methodName
                out.print("  public void bind($simpleViewClassName view, $simpleModelClass data) { \n")
                if (!method.dependencies.isEmpty()) { // method with dependencies
                    out.print(
                        """    ${getSimpleName(enclosingClass)}.$methodName(view, data, ${
                            getParams(
                                method.dependencies
                            )
                        }); 
"""
                    )
                } else { // method with only view and data
                    out.print(
                        """    ${getSimpleName(enclosingClass)}.$methodName(view, data); 
"""
                    )
                }

                // this is a custom view
                if (views.containsKey(method.viewClass)) {
                    // main view action for this data class
                    val actionsMap = views[method.viewClass]!!.actions
                    if (actionsMap.containsKey(simpleModelClass)) {
                        val action = actionsMap[simpleModelClass]
                        out.print(
                            """    DataBindingHelper.bindAction(view, data.${action!!.path}); 
"""
                        )
                    }

                    // view field binding
                    val viewFieldsMap = views[method.viewClass]!!.bindableViewFields
                    if (viewFieldsMap.containsKey(simpleModelClass)) {
                        val fields = viewFieldsMap[simpleModelClass]!!
                        for (field in fields) {
                            val simpleFieldViewClass = getSimpleName(field.fieldViewClassName)
                            val simpleFieldDataClass = field.fieldObjectClassName
                            val key =
                                getCombinedClassName(simpleFieldViewClass, simpleFieldDataClass)
                            if (methods.containsKey(key)) {
                                out.print(
                                    """    bind(view.${field.fieldName} , data.${field.objectPath}); 
"""
                                )
                            }
                        }
                    }

                    // view field actions
                    val actionFieldsMap = views[method.viewClass]!!.bindableActionFields
                    if (actionFieldsMap.containsKey(simpleModelClass)) {
                        val fields = actionFieldsMap[simpleModelClass]!!
                        for (field in fields) {
                            out.print(
                                """    DataBindingHelper.bindAction(view.${field.fieldName} , data.${field.objectPath}); 
"""
                            )
                        }
                    }
                }
                out.print("  } \n\n")
            }

            // close class
            out.print("}")
        }
    }
}