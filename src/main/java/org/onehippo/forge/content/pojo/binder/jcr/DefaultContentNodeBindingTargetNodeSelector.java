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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.onehippo.forge.content.pojo.binder.ContentNodeBindingException;
import org.onehippo.forge.content.pojo.binder.ContentNodeBindingTargetSelector;
import org.onehippo.forge.content.pojo.model.ContentNode;

/**
 * Default {@link ContentNodeBindingTargetSelector} for JCR data nodes.
 */
public class DefaultContentNodeBindingTargetNodeSelector implements ContentNodeBindingTargetSelector<Node> {

    @Override
    public Node select(ContentNode contentNode, Node base) throws ContentNodeBindingException {
        final String contentNodeName = contentNode.getName();

        try {
            if (!base.hasNode(contentNodeName)) {
                throw new ContentNodeBindingException(
                        "Selecting call must be made only when having any target node(s) by the content node's name.");
            }

            // Must be >= 1.
            int contentNodeIndex = contentNode.getIndex();

            Node targetNode = base.getNode(contentNodeName);
            // targetNode's index must be 1 always as we used #getNode(name) initially.

            if (contentNodeIndex == 1) {
                return targetNode;
            } else {
                final List<Node> siblings = collectSiblingNodes(base, contentNode.getName());

                if (contentNodeIndex > siblings.size()) {
                    return null;
                }

                return siblings.get(contentNodeIndex - 1);
            }
        } catch (RepositoryException e) {
            throw new ContentNodeBindingException("Failed to select a target node due to JCR exception.", e);
        }
    }

    private List<Node> collectSiblingNodes(final Node base, final String name) throws RepositoryException {
        final List<Node> list = new ArrayList<>();

        for (NodeIterator nodeIt = base.getNodes(name); nodeIt.hasNext(); ) {
            Node node = nodeIt.nextNode();

            if (node != null) {
                list.add(node);
            }
        }

        return list;
    }
}
