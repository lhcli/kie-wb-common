/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.forms.editor.backend.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.kie.workbench.common.forms.editor.model.FormModelSynchronizationResult;
import org.kie.workbench.common.forms.editor.model.impl.FormModelSynchronizationResultImpl;
import org.kie.workbench.common.forms.editor.model.impl.TypeConflictImpl;
import org.kie.workbench.common.forms.editor.service.backend.FormModelHandler;
import org.kie.workbench.common.forms.model.FieldDefinition;
import org.kie.workbench.common.forms.model.HasFormModelProperties;
import org.kie.workbench.common.forms.model.ModelProperty;
import org.kie.workbench.common.forms.service.backend.util.ModelPropertiesGenerator;
import org.kie.workbench.common.services.backend.project.ProjectClassLoaderHelper;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.uberfire.backend.vfs.Path;

public abstract class AbstractFormModelHandler<F extends HasFormModelProperties> implements FormModelHandler<F> {

    protected F formModel;
    protected Path path;
    protected ClassLoader projectClassLoader;

    protected KieProjectService projectService;

    protected ProjectClassLoaderHelper classLoaderHelper;

    public AbstractFormModelHandler(KieProjectService projectService,
                                    ProjectClassLoaderHelper classLoaderHelper) {
        this.projectService = projectService;
        this.classLoaderHelper = classLoaderHelper;
    }

    @Override
    public void init(F formModel,
                     Path path) {
        this.formModel = formModel;
        this.path = path;
        initClassLoader();
        initialize();
    }

    protected void initClassLoader() {
        this.projectClassLoader = classLoaderHelper.getProjectClassLoader(projectService.resolveProject(path));
    }

    @Override
    public List<FieldDefinition> getAllFormModelFields() {
        checkInitialized();
        return doGenerateModelFields();
    }

    @Override
    public FieldDefinition createFieldDefinition(ModelProperty property) {
        checkInitialized();
        return doCreateFieldDefinition(property);
    }

    protected abstract void initialize();

    @Override
    public FormModelSynchronizationResult synchronizeFormModel() {
        checkInitialized();

        return synchronizeFormModelProperties(formModel,
                                              getCurrentModelProperties());
    }

    @Override
    public FormModelSynchronizationResult synchronizeFormModelProperties(final F formModel,
                                                                         final List<ModelProperty> currentProperties) {
        final FormModelSynchronizationResultImpl result = new FormModelSynchronizationResultImpl();

        List<ModelProperty> modelProperties = Optional.ofNullable(formModel.getProperties()).orElse(new ArrayList<>());

        result.setPreviousProperties(modelProperties);

        currentProperties.forEach((currentProperty) -> {
            Optional<ModelProperty> optional = result.getPreviousProperties().stream().filter(oldProperty -> oldProperty.getName().equals(currentProperty.getName())).findFirst();
            if (optional.isPresent()) {
                ModelProperty oldProperty = optional.get();
                if (!oldProperty.equals(currentProperty)) {
                    // currentProperty exists on the Model oldProperties but type doesn't match -> adding it to conlfict
                    result.getConflicts().put(oldProperty.getName(),
                                              new TypeConflictImpl(oldProperty.getName(),
                                                                   oldProperty.getTypeInfo(),
                                                                   currentProperty.getTypeInfo()));
                }
            } else {
                // currentPproperty doesn't exist on the previous properties -> adding to new properties
                result.getNewProperties().add(currentProperty);
            }
        });

        modelProperties.forEach(oldProperty -> {
            Optional<ModelProperty> optional = currentProperties.stream().filter(currentProperty -> currentProperty.getName().equals(oldProperty.getName())).findFirst();
            if (!optional.isPresent()) {
                result.getRemovedProperties().add(oldProperty);
            }
        });

        formModel.getProperties().clear();
        formModel.getProperties().addAll(currentProperties);

        return result;
    }

    protected Optional<ModelProperty> createModelProperty(String name,
                                                          String className,
                                                          boolean isMultiple) {

        ModelProperty property = ModelPropertiesGenerator.createModelProperty(name,
                                                                              className,
                                                                              isMultiple,
                                                                              projectClassLoader);

        return Optional.ofNullable(property);
    }

    protected abstract void log(String s,
                                Exception e);

    protected abstract List<ModelProperty> getCurrentModelProperties();

    protected abstract List<FieldDefinition> doGenerateModelFields();

    protected abstract FieldDefinition doCreateFieldDefinition(ModelProperty property);

    public void checkInitialized() {
        if (path == null || formModel == null || projectClassLoader == null) {
            throw new IllegalArgumentException("Handler isn't initialized");
        }
    }
}
