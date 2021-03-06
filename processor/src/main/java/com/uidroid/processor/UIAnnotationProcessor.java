package com.uidroid.processor;

import com.uidroid.annotation.UI;
import com.uidroid.processor.binder.ViewBinderFactoryProcessor;
import com.uidroid.processor.composite.ViewCompositeFactoryProcessor;
import com.uidroid.processor.composite.ViewFieldsInjectorProcessor;
import com.uidroid.processor.configuration.ViewConfigurationBuilderProcessor;
import com.uidroid.processor.configuration.ViewConfigurationFactoryProcessor;
import com.uidroid.processor.view.ViewFactoryProcessor;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

public class UIAnnotationProcessor extends AbstractProcessor {

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set.isEmpty()) {
            return false;
        }

        ViewBinderFactoryProcessor binderFactoryProcessor = new ViewBinderFactoryProcessor(processingEnv);
        ViewConfigurationFactoryProcessor configurationFactoryProcessor = new ViewConfigurationFactoryProcessor(processingEnv);
        ViewFactoryProcessor viewFactoryProcessor = new ViewFactoryProcessor(processingEnv);
        ViewCompositeFactoryProcessor compositeFactoryProcessor = new ViewCompositeFactoryProcessor(processingEnv);
        DatabindingProvidersProcessor databindingProvidersProcessor = new DatabindingProvidersProcessor(processingEnv);
        ViewFieldsInjectorProcessor viewFieldsInjectorProcessor = new ViewFieldsInjectorProcessor(processingEnv);
        ViewConfigurationBuilderProcessor viewConfigurationBuilderProcessor = new ViewConfigurationBuilderProcessor(processingEnv);

        binderFactoryProcessor.process(set, roundEnvironment);
        configurationFactoryProcessor.process(set, roundEnvironment);
        viewFactoryProcessor.process(set, roundEnvironment);
        compositeFactoryProcessor.process(set, roundEnvironment);
        databindingProvidersProcessor.process(set, roundEnvironment);
        viewFieldsInjectorProcessor.process(set, roundEnvironment);
        viewConfigurationBuilderProcessor.process(set, roundEnvironment);

        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(UI.ViewConfiguration.class.getCanonicalName());
        annotations.add(UI.Configuration.class.getCanonicalName());
        annotations.add(UI.Param.class.getCanonicalName());
        annotations.add(UI.ConfigurationsList.class.getCanonicalName());
        annotations.add(UI.Id.class.getCanonicalName());
        annotations.add(UI.FieldConfiguration.class.getCanonicalName());
        annotations.add(UI.Action.class.getCanonicalName());
        annotations.add(UI.CustomView.class.getCanonicalName());
        annotations.add(UI.AppPackage.class.getCanonicalName());
        annotations.add(UI.View.class.getCanonicalName());
        annotations.add(UI.BinderFor.class.getCanonicalName());
        annotations.add(UI.Binder.class.getCanonicalName());
        annotations.add(UI.BindWith.class.getCanonicalName());
        annotations.add(UI.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}
