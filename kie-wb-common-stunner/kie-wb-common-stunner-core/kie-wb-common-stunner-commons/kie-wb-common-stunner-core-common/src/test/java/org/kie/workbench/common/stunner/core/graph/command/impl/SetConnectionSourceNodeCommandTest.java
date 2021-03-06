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

package org.kie.workbench.common.stunner.core.graph.command.impl;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.core.command.CommandResult;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.Magnet;
import org.kie.workbench.common.stunner.core.graph.content.view.MagnetImpl;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewConnector;
import org.kie.workbench.common.stunner.core.rule.RuleEvaluationContext;
import org.kie.workbench.common.stunner.core.rule.RuleViolation;
import org.kie.workbench.common.stunner.core.rule.context.CardinalityContext;
import org.kie.workbench.common.stunner.core.rule.context.ConnectorCardinalityContext;
import org.kie.workbench.common.stunner.core.rule.context.EdgeCardinalityContext;
import org.kie.workbench.common.stunner.core.rule.context.GraphConnectionContext;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.kie.workbench.common.stunner.core.TestingGraphUtils.verifyConnection;
import static org.kie.workbench.common.stunner.core.TestingGraphUtils.verifyConnectorCardinality;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SetConnectionSourceNodeCommandTest extends AbstractGraphCommandTest {

    private static final String NODE_UUID = "nodeUUID";
    private static final String LAST_SOURCE_NODE_UUID = "lastSourceNodeUUID";
    private static final String TARGET_UUID = "nodeTargetUUID";
    private static final String EDGE_UUID = "edgeUUID";
    private static final Double MAGNETX = 15d;
    private static final Double MAGNETY = 0d;

    private Node node;
    private Node lastSourceNode;
    private Node target;
    private Edge edge;
    private ViewConnector edgeContent;
    private Optional<Magnet> sourceMagnet;
    private SetConnectionSourceNodeCommand tested;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() throws Exception {
        super.init(500,
                   500);
        node = mockNode(NODE_UUID);
        lastSourceNode = mockNode(LAST_SOURCE_NODE_UUID);
        target = mockNode(TARGET_UUID);
        edge = mockEdge(EDGE_UUID);
        edgeContent = mock(ViewConnector.class);
        sourceMagnet = Optional.of(MagnetImpl.Builder.build(0d,
                                                            0d));
        graphNodes.add(node);
        when(graphIndex.getNode(eq(NODE_UUID))).thenReturn(node);
        when(graphIndex.getNode(eq(LAST_SOURCE_NODE_UUID))).thenReturn(lastSourceNode);
        when(graphIndex.getNode(eq(TARGET_UUID))).thenReturn(target);
        when(graphIndex.getEdge(eq(EDGE_UUID))).thenReturn(edge);
        when(edge.getContent()).thenReturn(edgeContent);
        when(edge.getSourceNode()).thenReturn(lastSourceNode);
        when(edge.getTargetNode()).thenReturn(target);
        when(edgeContent.getSourceMagnet()).thenReturn(sourceMagnet);
        this.tested = new SetConnectionSourceNodeCommand(node,
                                                         edge,
                                                         MagnetImpl.Builder.build(MAGNETX,
                                                                                  MAGNETY),
                                                         true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAllow() {
        CommandResult<RuleViolation> result = tested.allow(graphCommandExecutionContext);
        assertEquals(CommandResult.Type.INFO,
                     result.getType());
        final ArgumentCaptor<RuleEvaluationContext> contextCaptor = ArgumentCaptor.forClass(RuleEvaluationContext.class);
        verify(ruleManager,
               times(3)).evaluate(eq(ruleSet),
                                  contextCaptor.capture());
        final List<RuleEvaluationContext> contexts = contextCaptor.getAllValues();
        assertEquals(3,
                     contexts.size());
        verifyConnection((GraphConnectionContext) contexts.get(0),
                         edge,
                         node,
                         target);

        verifyConnectorCardinality((ConnectorCardinalityContext) contexts.get(1),
                                   graph,
                                   lastSourceNode,
                                   edge,
                                   EdgeCardinalityContext.Direction.OUTGOING,
                                   Optional.of(CardinalityContext.Operation.DELETE));
        verifyConnectorCardinality((ConnectorCardinalityContext) contexts.get(2),
                                   graph,
                                   node,
                                   edge,
                                   EdgeCardinalityContext.Direction.OUTGOING,
                                   Optional.of(CardinalityContext.Operation.ADD));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAllowNoRules() {
        when(graphCommandExecutionContext.getRuleManager()).thenReturn(null);
        CommandResult<RuleViolation> result = tested.allow(graphCommandExecutionContext);
        assertEquals(CommandResult.Type.INFO,
                     result.getType());
        verify(ruleManager,
               times(0)).evaluate(eq(ruleSet),
                                  any(RuleEvaluationContext.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAllowNoSourceConnection() {
        this.tested = new SetConnectionSourceNodeCommand(null,
                                                         edge,
                                                         MagnetImpl.Builder.build(MAGNETX,
                                                                                  MAGNETY),
                                                         true);
        CommandResult<RuleViolation> result = tested.allow(graphCommandExecutionContext);
        assertEquals(CommandResult.Type.INFO,
                     result.getType());
        final ArgumentCaptor<RuleEvaluationContext> contextCaptor = ArgumentCaptor.forClass(RuleEvaluationContext.class);
        verify(ruleManager,
               times(2)).evaluate(eq(ruleSet),
                                  contextCaptor.capture());
        final List<RuleEvaluationContext> contexts = contextCaptor.getAllValues();
        assertEquals(2,
                     contexts.size());
        verifyConnection((GraphConnectionContext) contexts.get(0),
                         edge,
                         null,
                         null);

        verifyConnectorCardinality((ConnectorCardinalityContext) contexts.get(1),
                                   graph,
                                   lastSourceNode,
                                   edge,
                                   EdgeCardinalityContext.Direction.OUTGOING,
                                   Optional.of(CardinalityContext.Operation.DELETE));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExecute() {
        final List lastSourceOutEdges = mock(List.class);
        final List sourceOutEdges = mock(List.class);
        final List targetInEdges = mock(List.class);
        when(node.getOutEdges()).thenReturn(sourceOutEdges);
        when(lastSourceNode.getOutEdges()).thenReturn(lastSourceOutEdges);
        when(target.getInEdges()).thenReturn(targetInEdges);
        CommandResult<RuleViolation> result = tested.execute(graphCommandExecutionContext);
        final ArgumentCaptor<RuleEvaluationContext> contextCaptor = ArgumentCaptor.forClass(RuleEvaluationContext.class);
        verify(ruleManager,
               times(3)).evaluate(eq(ruleSet),
                                  contextCaptor.capture());
        final List<RuleEvaluationContext> contexts = contextCaptor.getAllValues();
        assertEquals(3,
                     contexts.size());
        verifyConnection((GraphConnectionContext) contexts.get(0),
                         edge,
                         node,
                         target);

        verifyConnectorCardinality((ConnectorCardinalityContext) contexts.get(1),
                                   graph,
                                   lastSourceNode,
                                   edge,
                                   EdgeCardinalityContext.Direction.OUTGOING,
                                   Optional.of(CardinalityContext.Operation.DELETE));
        verifyConnectorCardinality((ConnectorCardinalityContext) contexts.get(2),
                                   graph,
                                   node,
                                   edge,
                                   EdgeCardinalityContext.Direction.OUTGOING,
                                   Optional.of(CardinalityContext.Operation.ADD));
        assertEquals(CommandResult.Type.INFO,
                     result.getType());
        verify(lastSourceOutEdges,
               times(1)).remove(eq(edge));
        verify(sourceOutEdges,
               times(1)).add(eq(edge));
        verify(edgeContent,
               times(1)).setSourceMagnet(any(Magnet.class));
        verify(edge,
               times(1)).setSourceNode(eq(node));
        verify(targetInEdges,
               times(0)).remove(any(Edge.class));
        verify(targetInEdges,
               times(0)).add(any(Edge.class));
        verify(graphIndex,
               times(0)).removeEdge(any(Edge.class));
        verify(graphIndex,
               times(0)).addEdge(any(Edge.class));
        verify(graphIndex,
               times(0)).addNode(any(Node.class));
        verify(graphIndex,
               times(0)).removeNode(any(Node.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteNoMagnets() {
        this.tested = new SetConnectionSourceNodeCommand(node,
                                                         edge);
        final List lastSourceOutEdges = mock(List.class);
        final List sourceOutEdges = mock(List.class);
        final List targetInEdges = mock(List.class);
        when(node.getOutEdges()).thenReturn(sourceOutEdges);
        when(lastSourceNode.getOutEdges()).thenReturn(lastSourceOutEdges);
        when(target.getInEdges()).thenReturn(targetInEdges);
        CommandResult<RuleViolation> result = tested.execute(graphCommandExecutionContext);
        final ArgumentCaptor<RuleEvaluationContext> contextCaptor = ArgumentCaptor.forClass(RuleEvaluationContext.class);
        verify(ruleManager,
               times(3)).evaluate(eq(ruleSet),
                                  contextCaptor.capture());
        final List<RuleEvaluationContext> contexts = contextCaptor.getAllValues();
        assertEquals(3,
                     contexts.size());
        verifyConnection((GraphConnectionContext) contexts.get(0),
                         edge,
                         node,
                         target);

        verifyConnectorCardinality((ConnectorCardinalityContext) contexts.get(1),
                                   graph,
                                   lastSourceNode,
                                   edge,
                                   EdgeCardinalityContext.Direction.OUTGOING,
                                   Optional.of(CardinalityContext.Operation.DELETE));
        verifyConnectorCardinality((ConnectorCardinalityContext) contexts.get(2),
                                   graph,
                                   node,
                                   edge,
                                   EdgeCardinalityContext.Direction.OUTGOING,
                                   Optional.of(CardinalityContext.Operation.ADD));
        assertEquals(CommandResult.Type.INFO,
                     result.getType());
        verify(lastSourceOutEdges,
               times(1)).remove(eq(edge));
        verify(sourceOutEdges,
               times(1)).add(eq(edge));
        verify(edgeContent,
               times(0)).setSourceMagnet(any(Magnet.class));
        verify(edge,
               times(1)).setSourceNode(eq(node));
        verify(targetInEdges,
               times(0)).remove(any(Edge.class));
        verify(targetInEdges,
               times(0)).add(any(Edge.class));
        verify(graphIndex,
               times(0)).removeEdge(any(Edge.class));
        verify(graphIndex,
               times(0)).addEdge(any(Edge.class));
        verify(graphIndex,
               times(0)).addNode(any(Node.class));
        verify(graphIndex,
               times(0)).removeNode(any(Node.class));
    }
}
