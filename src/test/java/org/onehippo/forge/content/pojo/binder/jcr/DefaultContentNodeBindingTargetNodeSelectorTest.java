/*
 *  Copyright 2018 Hippo B.V. (http://www.onehippo.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.onehippo.forge.content.pojo.binder.jcr;

import javax.jcr.Node;

import org.junit.Before;
import org.junit.Test;
import org.onehippo.forge.content.pojo.binder.ContentNodeBindingTargetSelector;
import org.onehippo.forge.content.pojo.model.ContentNode;
import org.onehippo.repository.mock.MockNode;

import static org.junit.Assert.assertSame;

public class DefaultContentNodeBindingTargetNodeSelectorTest {

    private ContentNodeBindingTargetSelector<Node> contentNodeBindingTargetSelector = new DefaultContentNodeBindingTargetNodeSelector();

    private ContentNode baseContentNode;
    private ContentNode subContentNode1;
    private ContentNode subContentNode2;
    private ContentNode subContentNode3_1;
    private ContentNode subContentNode3_2;
    private ContentNode subContentNode3_3;
    private ContentNode subContentNode6;
    private ContentNode subContentNode7_1;
    private ContentNode subContentNode7_2;
    private ContentNode subContentNode9;

    private MockNode rootNode = MockNode.root();
    private Node baseNode;
    private Node subNode1;
    private Node subNode2;
    private Node subNode3_1;
    private Node subNode3_2;
    private Node subNode3_3;
    private Node subNode6;
    private Node subNode7_1;
    private Node subNode7_2;
    private Node subNode9;

    @Before
    public void setUp() throws Exception {
        baseContentNode = new ContentNode("document", "nt:unstructured");
        baseContentNode.addNode(subContentNode1 = new ContentNode("subNode1", "nt:unstructured"));
        baseContentNode.addNode(subContentNode2 = new ContentNode("subNode2", "nt:unstructured"));
        baseContentNode.addNode(subContentNode3_1 = new ContentNode("subNode3", "nt:unstructured"));
        baseContentNode.addNode(subContentNode3_2 = new ContentNode("subNode3", "nt:unstructured"));
        baseContentNode.addNode(subContentNode3_3 = new ContentNode("subNode3", "nt:unstructured"));
        baseContentNode.addNode(subContentNode6 = new ContentNode("subNode6", "nt:unstructured"));
        baseContentNode.addNode(subContentNode7_1 = new ContentNode("subNode7", "nt:unstructured"));
        baseContentNode.addNode(subContentNode7_2 = new ContentNode("subNode7", "nt:unstructured"));
        baseContentNode.addNode(subContentNode9 = new ContentNode("subNode9", "nt:unstructured"));

        baseNode = rootNode.addNode("document", "nt:unstructured");
        subNode1 = baseNode.addNode("subNode1", "nt:unstructured");
        subNode2 = baseNode.addNode("subNode2", "nt:unstructured");
        subNode3_1 = baseNode.addNode("subNode3", "nt:unstructured");
        subNode3_2 = baseNode.addNode("subNode3", "nt:unstructured");
        subNode3_3 = baseNode.addNode("subNode3", "nt:unstructured");
        subNode6 = baseNode.addNode("subNode6", "nt:unstructured");
        subNode7_1 = baseNode.addNode("subNode7", "nt:unstructured");
        subNode7_2 = baseNode.addNode("subNode7", "nt:unstructured");
        subNode9 = baseNode.addNode("subNode9", "nt:unstructured");
    }

    @Test
    public void testHappyCases() throws Exception {
        assertSame(subNode1, contentNodeBindingTargetSelector.select(subContentNode1, baseNode));
        assertSame(subNode2, contentNodeBindingTargetSelector.select(subContentNode2, baseNode));
        assertSame(subNode3_1, contentNodeBindingTargetSelector.select(subContentNode3_1, baseNode));
        assertSame(subNode3_2, contentNodeBindingTargetSelector.select(subContentNode3_2, baseNode));
        assertSame(subNode3_3, contentNodeBindingTargetSelector.select(subContentNode3_3, baseNode));
        assertSame(subNode6, contentNodeBindingTargetSelector.select(subContentNode6, baseNode));
        assertSame(subNode7_1, contentNodeBindingTargetSelector.select(subContentNode7_1, baseNode));
        assertSame(subNode7_2, contentNodeBindingTargetSelector.select(subContentNode7_2, baseNode));
        assertSame(subNode9, contentNodeBindingTargetSelector.select(subContentNode9, baseNode));
    }

    @Test
    public void testFallbackCases() throws Exception {
        Node base = rootNode.addNode("document-fallback", "nt:unstructured");
        Node sub1 = base.addNode("subNode1", "nt:unstructured");
        Node sub2 = base.addNode("subNode2", "nt:unstructured");
        Node sub3 = base.addNode("subNode3", "nt:unstructured");

        assertSame(sub1, contentNodeBindingTargetSelector.select(subContentNode1, base));
        assertSame(sub2, contentNodeBindingTargetSelector.select(subContentNode2, base));
        assertSame(sub3, contentNodeBindingTargetSelector.select(subContentNode3_1, base));
        assertSame(sub3, contentNodeBindingTargetSelector.select(subContentNode3_2, base));
        assertSame(sub3, contentNodeBindingTargetSelector.select(subContentNode3_3, base));
    }
}
