package com.uidroid.processor;

import static com.uidroid.processor.Utils.getCleanPath;
import static com.uidroid.processor.Utils.getCombinedClassName;
import static com.uidroid.processor.Utils.getDataClassFromPath;
import static com.uidroid.processor.Utils.getSimpleName;
import static com.uidroid.processor.Utils.getTargetDataClassFromPath;

import com.uidroid.annotation.BindAction;
import com.uidroid.annotation.BindWith;
import com.uidroid.annotation.BindableObject;
import com.uidroid.annotation.BindableView;
import com.uidroid.annotation.BindingMethod;
import com.uidroid.annotation.Data;
import com.uidroid.annotation.Inject;
import com.uidroid.annotation.View;
import com.uidroid.processor.items.BindableActionFieldImpl;
import com.uidroid.processor.items.BindableActionImpl;
import com.uidroid.processor.items.BindableObjectImpl;
import com.uidroid.processor.items.BindableViewFieldImpl;
import com.uidroid.processor.items.BindableViewImpl;
import com.uidroid.processor.items.BindingMethodImpl;
import com.uidroid.processor.writer.AdapterDataBindingClassWriter;
import com.uidroid.processor.writer.DataBindingClassWriter;
import com.uidroid.processor.writer.IViewFactoryClassWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

public class UIAnnotationProcessor extends AbstractProcessor {

    private final static String DATA_PATH_REGEX = "^([A-Z][\\w]+\\.)([\\w]+\\.)?+([\\w]+:)([A-Z][\\w]+)";
    private final static String ACTION_PATH_REGEX = "^([A-Z][\\w]+\\.)([\\w]+\\.)?+([\\w]+)";

    private final static String I_VIEW_INTERFACE_CLASS = "com.uidroid.uidroid.IView";

    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set.isEmpty()) {
            return false;
        }

        final Map<String, BindingMethodImpl> methods = new LinkedHashMap<>(); // <"TextView:TextModel" - BindingMethod obj>
        final Map<String, BindableViewImpl> views = new LinkedHashMap<>(); // <"android.view.TextView" - BindableView obj>
        final Map<String, BindableObjectImpl> objects = new LinkedHashMap<>(); // <"com.myapp.MyModel" - BindableObject obj>

        // @BindingMethod annotated methods
        for (Element element: roundEnvironment.getElementsAnnotatedWith(BindingMethod.class)) {
            BindingMethod item = element.getAnnotation(BindingMethod.class);

            ExecutableElement method = (ExecutableElement) element;

            if (!method.getModifiers().contains(Modifier.PUBLIC) || !method.getModifiers().contains(Modifier.STATIC)) {
                error(method.getSimpleName().toString() + " must be public and static");
                continue;
            }

            String methodName = method.getSimpleName().toString();
            String enclosingClass = element.getEnclosingElement().asType().toString();
            String viewClass = null;
            String dataClass = null;
            List<String> dependencies = new ArrayList<>();

            List<? extends VariableElement> parameters = method.getParameters();
            int currentParameterIndex = 0;
            boolean isValidMethod = true;

            for (VariableElement param : parameters) {
                View viewAnnotated = param.getAnnotation(View.class);
                Data dataAnnotated = param.getAnnotation(Data.class);
                Inject injectAnnotated = param.getAnnotation(Inject.class);

                if (viewAnnotated != null) {
                    if (viewClass == null && currentParameterIndex == 0) {
                        viewClass = param.asType().toString();
                    } else {
                        isValidMethod = false;
                        error("@BindingMethod " + methodName + " -> only first parameter can be annotated with @View");
                    }
                } else if (dataAnnotated != null) {
                    if (dataClass == null && currentParameterIndex == 1) {
                        dataClass = param.asType().toString();
                    } else {
                        isValidMethod = false;
                        error("@BindingMethod " + methodName + " -> only first parameter can be annotated with @View");
                    }
                } else if (injectAnnotated != null && currentParameterIndex >= 2) {
                    dependencies.add(param.asType().toString());
                } else {
                    isValidMethod = false;
                    error("@BindingMethod " + methodName + " -> all parameter must be annotated");
                }

                currentParameterIndex++;
            }

            if (viewClass == null || dataClass == null) {
                error("@BindingMethod " + methodName + " -> must have a parameter @View annotated and one @Data annotated");
                isValidMethod = false;
            }

            if (isValidMethod) {
                BindingMethodImpl bindingMethod = new BindingMethodImpl(enclosingClass, methodName, viewClass, dataClass, dependencies);
                methods.put(getCombinedClassName(getSimpleName(viewClass), getSimpleName(dataClass)), bindingMethod);
            }
        }

        // @BindableView annotated android custom views
        for (Element element: roundEnvironment.getElementsAnnotatedWith(BindableView.class)) {
            BindableView item = element.getAnnotation(BindableView.class);

            String className = element.asType().toString();

            List<? extends TypeMirror> interfaces = ((TypeElement) element).getInterfaces();
            boolean implementIView = isImplementingInterface(interfaces, I_VIEW_INTERFACE_CLASS);

            if (!views.containsKey(className)) {
                views.put(className, new BindableViewImpl(className, implementIView));
            }
        }

        // @BindTo annotated fields of @BindableView annotated android custom views
        for (Element element: roundEnvironment.getElementsAnnotatedWith(BindWith.class)) {
            BindWith item = element.getAnnotation(BindWith.class);

            String enclosingClassName = element.getEnclosingElement().asType().toString();
            String viewClassName = element.asType().toString();
            String fieldName = element.getSimpleName().toString();
            String[] paths = item.paths();

            if (duplicatePathExist(paths, DATA_PATH_REGEX)) {
                error(enclosingClassName + " " + fieldName + " -> cannot exist a duplicate path for the same data model");
                continue;
            }

            if (!views.containsKey(enclosingClassName)) {
                views.put(enclosingClassName, new BindableViewImpl(enclosingClassName));
            }

            BindableViewImpl enclosingView = views.get(enclosingClassName);

            for (String path: paths) {
                if (!isValidPath(path, DATA_PATH_REGEX)) {
                    error(enclosingView.className + " view -> " + path + " is not a valid path");
                    continue;
                }

                String simpleDataClassForField = getDataClassFromPath(path);
                String fieldViewSimpleClassName = getSimpleName(viewClassName);
                String fieldObjectSimpleClassName = getTargetDataClassFromPath(path);

                String viewDataPairForBindingField = getCombinedClassName(fieldViewSimpleClassName, fieldObjectSimpleClassName);

                if (!enclosingView.bindableViewFields.containsKey(simpleDataClassForField)) {
                    enclosingView.bindableViewFields.put(simpleDataClassForField, new ArrayList<>());
                }

                List<BindableViewFieldImpl> fields = enclosingView.bindableViewFields.get(simpleDataClassForField);

                if (methods.containsKey(viewDataPairForBindingField)) {
                    fields.add(new BindableViewFieldImpl(
                            fieldName,
                            getCleanPath(path),
                            viewClassName,
                            fieldObjectSimpleClassName));
                } else {
                    error("The view:data " + viewDataPairForBindingField + " pair -> has not a binding method");
                }
            }
        }

        // @BindAction annotated fields on custom views
        for (Element element: roundEnvironment.getElementsAnnotatedWith(BindAction.class)) {
            BindAction item = element.getAnnotation(BindAction.class);

            if (element.getKind().equals(ElementKind.FIELD)) {

                String enclosingClassName = element.getEnclosingElement().asType().toString();
                String viewClassName = element.asType().toString();
                String fieldName = element.getSimpleName().toString();
                String[] paths = item.paths();

                if (duplicatePathExist(paths, ACTION_PATH_REGEX)) {
                    error(enclosingClassName + " " + fieldName + " -> cannot exist a duplicate path for the same data model");
                    continue;
                }

                if (!views.containsKey(enclosingClassName)) {
                    views.put(enclosingClassName, new BindableViewImpl(enclosingClassName));
                    error(enclosingClassName); // TODO remove
                }

                BindableViewImpl enclosingView = views.get(enclosingClassName);

                for (String path : paths) {
                    if (!isValidPath(path, ACTION_PATH_REGEX)) {
                        error(enclosingView.className + " view -> " + path + " is not a valid path");
                        continue;
                    }

                    String simpleDataClassForField = getDataClassFromPath(path);
                    String cleanPath = getCleanPath(path);

                    if (!enclosingView.bindableActionFields.containsKey(simpleDataClassForField)) {
                        enclosingView.bindableActionFields.put(simpleDataClassForField, new ArrayList<>());
                    }

                    List<BindableActionFieldImpl> fields = enclosingView.bindableActionFields.get(simpleDataClassForField);
                    fields.add(new BindableActionFieldImpl(fieldName, cleanPath, viewClassName));

                }
            } else if (element.getKind().equals(ElementKind.CLASS)) {
                String className = element.asType().toString();

                for (String path: item.paths()) {
                    String objectSimpleClassName = getDataClassFromPath(path);
                    String cleanPath = getCleanPath(path);

                    views.get(className).actions.put(objectSimpleClassName,
                            new BindableActionImpl(className, objectSimpleClassName, cleanPath));
                }
            }
        }

        // @BindableObject annotated data models
        for (Element element: roundEnvironment.getElementsAnnotatedWith(BindableObject.class)) {
            BindableObject item = element.getAnnotation(BindableObject.class);

            String viewClassName = getClassFromAnnotation(item);
            String objectClassName = element.asType().toString();

            objects.put(objectClassName, new BindableObjectImpl(objectClassName, viewClassName));
        }

        try {
            DataBindingClassWriter dataBindingClassWriter = new DataBindingClassWriter(filer, messager);
            dataBindingClassWriter.writeDataBindingClass(views, methods);

            AdapterDataBindingClassWriter adapterDataBindingClassWriter = new AdapterDataBindingClassWriter(filer, messager);
            adapterDataBindingClassWriter.writeAdapterDataBindingClass(objects, methods);

            IViewFactoryClassWriter viewFactoryClassWriter = new IViewFactoryClassWriter(filer, messager);
            viewFactoryClassWriter.writeIViewFactoryClass(objects, views);
        } catch (IOException e) {
            error(e.getMessage());
        }

        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(BindAction.class.getCanonicalName());
        annotations.add(View.class.getCanonicalName());
        annotations.add(BindWith.class.getCanonicalName());
        annotations.add(BindableView.class.getCanonicalName());
        annotations.add(BindableObject.class.getCanonicalName());
        annotations.add(Data.class.getCanonicalName());
        annotations.add(Inject.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * Utility methods
     */

    private boolean duplicatePathExist(String[] paths, String regex) {
        boolean duplicates = false;
        for(int i = 0; i < paths.length; i++) {
            for(int k = i + 1; k < paths.length; k++) {
                String iPath = paths[i];
                String kPath = paths[k];
                if (isValidPath(iPath, regex) && isValidPath(kPath, regex)) {
                    if (getDataClassFromPath(iPath).equals(getDataClassFromPath(kPath))) {
                        duplicates = true;
                    }
                }
            }
        }
        return duplicates;
    }

    private boolean isValidPath(String path, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(path);
        return matcher.matches();
    }

    public boolean isImplementingInterface(List<? extends TypeMirror> interfaces, String interfaceClass) {
        boolean found = false;
        for (TypeMirror current: interfaces) {
            if (current.toString().equals(interfaceClass)) {
                found = true;
                break;
            }
        }
        return found;
    }

    public String getClassFromAnnotation(BindableObject annotation) {
        try {
            Class<?> value = annotation.view();
            return value.getCanonicalName();
        } catch (MirroredTypeException mte) {
            return processingEnv.getTypeUtils().asElement(mte.getTypeMirror()).asType().toString();
        }
    }

    /**
     * Error methods
     */

    private void error(String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message);
    }

}
