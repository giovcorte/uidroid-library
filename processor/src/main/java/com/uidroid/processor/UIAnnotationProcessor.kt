package com.uidroid.processor

import com.uidroid.annotation.*
import com.uidroid.processor.Utils.getCleanPath
import com.uidroid.processor.Utils.getCombinedClassName
import com.uidroid.processor.Utils.getDataClassFromPath
import com.uidroid.processor.Utils.getSimpleName
import com.uidroid.processor.Utils.getTargetDataClassFromPath
import com.uidroid.processor.items.*
import com.uidroid.processor.writer.AdapterDataBindingClassWriter
import com.uidroid.processor.writer.DataBindingClassWriter
import com.uidroid.processor.writer.IViewFactoryClassWriter
import java.io.IOException
import java.util.*
import java.util.regex.Pattern
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic
import kotlin.reflect.KClass

class UIAnnotationProcessor : AbstractProcessor() {

    private lateinit var filer: Filer
    private lateinit var messager: Messager

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        filer = processingEnv.filer
        messager = processingEnv.messager
    }

    override fun process(set: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
        if (set.isEmpty()) {
            return false
        }
        val methods: MutableMap<String, BindingMethodImpl> = LinkedHashMap() // <"TextView:TextModel" - BindingMethod obj>
        val views: MutableMap<String, BindableViewImpl> = LinkedHashMap() // <"android.view.TextView" - BindableView obj>
        val objects: MutableMap<String, BindableObjectImpl> = LinkedHashMap() // <"com.myapp.MyModel" - BindableObject obj>

        // @BindingMethod annotated methods
        for (element in roundEnvironment.getElementsAnnotatedWith(
            BindingMethod::class.java
        )) {
            val item = element.getAnnotation(BindingMethod::class.java)
            val method = element as ExecutableElement
            if (!method.modifiers.contains(Modifier.PUBLIC) || !method.modifiers.contains(Modifier.STATIC)) {
                error(method.simpleName.toString() + " must be public and static")
                continue
            }
            val methodName = method.simpleName.toString()
            val enclosingClass = element.getEnclosingElement().asType().toString()
            var viewClass: String? = null
            var dataClass: String? = null
            val dependencies: MutableList<String> = ArrayList()
            val parameters = method.parameters
            var isValidMethod = true
            for ((currentParameterIndex, param) in parameters.withIndex()) {
                val viewAnnotated = param.getAnnotation(View::class.java)
                val dataAnnotated = param.getAnnotation(Data::class.java)
                val injectAnnotated = param.getAnnotation(Inject::class.java)
                if (viewAnnotated != null) {
                    if (viewClass == null && currentParameterIndex == 0) {
                        viewClass = param.asType().toString()
                    } else {
                        isValidMethod = false
                        error("@BindingMethod $methodName -> only first parameter can be annotated with @View")
                    }
                } else if (dataAnnotated != null) {
                    if (dataClass == null && currentParameterIndex == 1) {
                        dataClass = param.asType().toString()
                    } else {
                        isValidMethod = false
                        error("@BindingMethod $methodName -> only first parameter can be annotated with @View")
                    }
                } else if (injectAnnotated != null && currentParameterIndex >= 2) {
                    dependencies.add(param.asType().toString())
                } else {
                    isValidMethod = false
                    error("@BindingMethod $methodName -> all parameter must be annotated")
                }
            }
            if (viewClass == null || dataClass == null) {
                error("@BindingMethod $methodName -> must have a parameter @View annotated and one @Data annotated")
                isValidMethod = false
            }
            if (isValidMethod) {
                val bindingMethod = BindingMethodImpl(
                    enclosingClass,
                    methodName,
                    viewClass!!,
                    dataClass!!,
                    dependencies
                )
                methods[getCombinedClassName(
                    getSimpleName(viewClass),
                    getSimpleName(dataClass)
                )] = bindingMethod
            }
        }

        // @BindableView annotated android custom views
        for (element in roundEnvironment.getElementsAnnotatedWith(
            BindableView::class.java
        )) {
            val item = element.getAnnotation(BindableView::class.java)
            val className = element.asType().toString()
            val interfaces = (element as TypeElement).interfaces
            val implementIView = isImplementingInterface(interfaces, I_VIEW_INTERFACE_CLASS)
            if (!views.containsKey(className)) {
                views[className] = BindableViewImpl(className, implementIView)
            }
        }

        // @BindTo annotated fields of @BindableView annotated android custom views
        for (element in roundEnvironment.getElementsAnnotatedWith(
            BindWith::class.java
        )) {
            val item = element.getAnnotation(BindWith::class.java)
            val enclosingClassName = element.enclosingElement.asType().toString()
            val viewClassName = element.asType().toString()
            val fieldName = element.simpleName.toString()
            val paths: Array<String> = item.paths
            if (duplicatePathExist(paths, DATA_PATH_REGEX)) {
                error("$enclosingClassName $fieldName -> cannot exist a duplicate path for the same data model")
                continue
            }
            if (!views.containsKey(enclosingClassName)) {
                views[enclosingClassName] = BindableViewImpl(enclosingClassName)
            }
            val enclosingView = views[enclosingClassName]
            for (path in paths) {
                if (!isValidPath(path, DATA_PATH_REGEX)) {
                    error(enclosingView!!.className + " view -> " + path + " is not a valid path")
                    continue
                }
                val simpleDataClassForField = getDataClassFromPath(path)
                val fieldViewSimpleClassName = getSimpleName(viewClassName)
                val fieldObjectSimpleClassName = getTargetDataClassFromPath(path)
                val viewDataPairForBindingField = getCombinedClassName(fieldViewSimpleClassName, fieldObjectSimpleClassName)

                if (!enclosingView!!.bindableViewFields.containsKey(simpleDataClassForField)) {
                    enclosingView.bindableViewFields[simpleDataClassForField] = ArrayList()
                }

                val fields = enclosingView.bindableViewFields[simpleDataClassForField]!!

                if (methods.containsKey(viewDataPairForBindingField)) {
                    fields.add(
                        BindableViewFieldImpl(
                            fieldName,
                            getCleanPath(path),
                            viewClassName,
                            fieldObjectSimpleClassName
                        ))
                } else {
                    error("The view:data $viewDataPairForBindingField pair -> has not a binding method")
                }
            }
        }

        // @BindAction annotated fields on custom views
        for (element in roundEnvironment.getElementsAnnotatedWith(
            BindAction::class.java
        )) {
            val item = element.getAnnotation(BindAction::class.java)
            if (element.kind == ElementKind.FIELD) {
                val enclosingClassName = element.enclosingElement.asType().toString()
                val viewClassName = element.asType().toString()
                val fieldName = element.simpleName.toString()
                val paths: Array<String> = item.paths
                if (duplicatePathExist(paths, ACTION_PATH_REGEX)) {
                    error("$enclosingClassName $fieldName -> cannot exist a duplicate path for the same data model")
                    continue
                }
                if (!views.containsKey(enclosingClassName)) {
                    views[enclosingClassName] = BindableViewImpl(enclosingClassName)
                    error(enclosingClassName) // TODO remove
                }
                val enclosingView = views[enclosingClassName]
                for (path in paths) {
                    if (!isValidPath(path, ACTION_PATH_REGEX)) {
                        error(enclosingView!!.className + " view -> " + path + " is not a valid path")
                        continue
                    }
                    val simpleDataClassForField = getDataClassFromPath(path)
                    val cleanPath = getCleanPath(path)
                    if (!enclosingView!!.bindableActionFields.containsKey(simpleDataClassForField)) {
                        enclosingView.bindableActionFields[simpleDataClassForField] = ArrayList()
                    }
                    val fields = enclosingView.bindableActionFields[simpleDataClassForField]!!
                    fields.add(BindableActionFieldImpl(fieldName, cleanPath, viewClassName))
                }
            } else if (element.kind == ElementKind.CLASS) {
                val className = element.asType().toString()
                for (path in item.paths) {
                    val objectSimpleClassName = getDataClassFromPath(path)
                    val cleanPath = getCleanPath(path)
                    views[className]!!.actions[objectSimpleClassName] =
                        BindableActionImpl(className, objectSimpleClassName, cleanPath)
                }
            }
        }

        // @BindableObject annotated data models
        for (element in roundEnvironment.getElementsAnnotatedWith(
            BindableObject::class.java
        )) {
            val item = element.getAnnotation(BindableObject::class.java)
            val viewClassName = getClassFromAnnotation(item)
            val objectClassName = element.asType().toString()
            objects[objectClassName] = BindableObjectImpl(objectClassName, viewClassName)
        }
        try {
            val dataBindingClassWriter = DataBindingClassWriter(filer, messager)
            dataBindingClassWriter.writeDataBindingClass(views, methods)
            val adapterDataBindingClassWriter = AdapterDataBindingClassWriter(filer, messager)
            adapterDataBindingClassWriter.writeAdapterDataBindingClass(objects, methods)
            val viewFactoryClassWriter = IViewFactoryClassWriter(filer, messager)
            viewFactoryClassWriter.writeIViewFactoryClass(objects, views)
        } catch (e: IOException) {
            error(e.message)
        }
        return true
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        val annotations: MutableSet<String> = LinkedHashSet()
        annotations.add(BindAction::class.java.canonicalName)
        annotations.add(View::class.java.canonicalName)
        annotations.add(BindWith::class.java.canonicalName)
        annotations.add(BindableView::class.java.canonicalName)
        annotations.add(BindableObject::class.java.canonicalName)
        annotations.add(Data::class.java.canonicalName)
        annotations.add(Inject::class.java.canonicalName)
        return annotations
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    /**
     * Utility methods
     */
    private fun duplicatePathExist(paths: Array<String>, regex: String): Boolean {
        var duplicates = false
        for (i in paths.indices) {
            for (k in i + 1 until paths.size) {
                val iPath = paths[i]
                val kPath = paths[k]
                if (isValidPath(iPath, regex) && isValidPath(kPath, regex)) {
                    if (getDataClassFromPath(iPath) == getDataClassFromPath(kPath)) {
                        duplicates = true
                    }
                }
            }
        }
        return duplicates
    }

    private fun isValidPath(path: String, regex: String): Boolean {
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(path)
        return matcher.matches()
    }

    fun isImplementingInterface(interfaces: List<TypeMirror>, interfaceClass: String): Boolean {
        var found = false
        for (current in interfaces) {
            if (current.toString() == interfaceClass) {
                found = true
                break
            }
        }
        return found
    }

    fun getClassFromAnnotation(annotation: BindableObject): String {
        return try {
            val value: KClass<*> = annotation.view
            value.java.canonicalName
        } catch (mte: MirroredTypeException) {
            processingEnv.typeUtils.asElement(mte.typeMirror).asType().toString()
        }
    }

    /**
     * Error methods
     */
    private fun error(message: String?) {
        messager.printMessage(Diagnostic.Kind.ERROR, message)
    }

    companion object {
        private const val DATA_PATH_REGEX = "^([A-Z][\\w]+\\.)([\\w]+\\.)?+([\\w]+:)([A-Z][\\w]+)"
        private const val ACTION_PATH_REGEX = "^([A-Z][\\w]+\\.)([\\w]+\\.)?+([\\w]+)"
        private const val I_VIEW_INTERFACE_CLASS = "com.uidroid.uidroid.IView"
    }
}