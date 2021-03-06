/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.screens.library.client.widgets.library;

import javax.inject.Inject;

import org.jboss.errai.common.client.dom.Button;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.mvp.Command;

@Templated
public class ImportExampleButtonWidget implements IsElement {

    @Inject
    @DataField("import-example")
    Button importExample;

    public void init(final String name,
                     final String description,
                     final Command onClick) {
        importExample.setTextContent(name);
        importExample.setAttribute("title",
                                   description);
        importExample.setOnclick(event -> onClick.execute());
    }
}
