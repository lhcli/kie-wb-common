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

package org.kie.workbench.common.stunner.core.client.session.command.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.util.CanvasFileExport;
import org.kie.workbench.common.stunner.core.client.session.command.ClientSessionCommand;
import org.kie.workbench.common.stunner.core.client.session.impl.AbstractClientSession;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.backend.vfs.Path;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExportToJpgSessionCommandTest {

    private static final String FILE_NAME = "file-name1";

    @Mock
    private CanvasFileExport canvasFileExport;

    @Mock
    private AbstractClientSession session;

    @Mock
    private AbstractCanvasHandler canvasHandler;

    @Mock
    private Diagram diagram;

    @Mock
    private Metadata metadata;

    @Mock
    private Path path;

    @Mock
    private ClientSessionCommand.Callback callback;

    private ExportToJpgSessionCommand tested;

    @Before
    public void setup() throws Exception {
        when(session.getCanvasHandler()).thenReturn(canvasHandler);
        when(canvasHandler.getDiagram()).thenReturn(diagram);
        when(diagram.getMetadata()).thenReturn(metadata);
        when(metadata.getPath()).thenReturn(path);
        when(path.getFileName()).thenReturn(FILE_NAME);
        this.tested = new ExportToJpgSessionCommand(canvasFileExport);
        this.tested.bind(session);
    }

    @Test
    public void testExport() {
        this.tested.execute(callback);
        verify(canvasFileExport,
               times(1)).exportToJpg(eq(canvasHandler),
                                     eq(FILE_NAME));
        verify(canvasFileExport,
               never()).exportToPng(any(AbstractCanvasHandler.class),
                                    anyString());
        verify(canvasFileExport,
               never()).exportToPdf(any(AbstractCanvasHandler.class),
                                    anyString());
    }
}
