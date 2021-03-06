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

package org.kie.workbench.common.stunner.core.graph.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.core.TestingGraphInstanceBuilder;
import org.kie.workbench.common.stunner.core.TestingGraphMockHandler;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class GraphUtilsTest {

    private TestingGraphMockHandler graphTestHandler;
    private TestingGraphInstanceBuilder.TestGraph2 graphInstance;

    @Before
    public void setup() {
        this.graphTestHandler = new TestingGraphMockHandler();
        graphInstance = TestingGraphInstanceBuilder.newGraph2(graphTestHandler);
    }

    @Test
    public void hasChildrenTest() {
        boolean hasChildren = GraphUtils.hasChildren(graphInstance.parentNode);
        assertTrue(hasChildren);
    }

    @Test
    public void notHasChildrenTest() {
        boolean hasChildren = GraphUtils.hasChildren(graphInstance.startNode);
        assertFalse(hasChildren);
    }

    @Test
    public void countChildrenTest() {
        Long countChildren = GraphUtils.countChildren(graphInstance.parentNode);
        assertEquals(Long.valueOf(3),
                     countChildren);
    }
}
