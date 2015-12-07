/*
 *  Copyright 2015-2015 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.forge.content.pojo.mapper.jcr.hippo;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;

import org.hippoecm.repository.HippoStdNodeType;
import org.hippoecm.repository.api.HippoNodeType;
import org.onehippo.forge.content.pojo.common.jcr.JcrContentUtils;
import org.onehippo.forge.content.pojo.common.jcr.hippo.HippoDocumentUtils;
import org.onehippo.forge.content.pojo.mapper.ContentNodeMapper;
import org.onehippo.forge.content.pojo.mapper.ContentNodeMappingException;
import org.onehippo.forge.content.pojo.mapper.ContentNodeMappingItemFilter;
import org.onehippo.forge.content.pojo.model.ContentNode;
import org.onehippo.forge.content.pojo.model.ContentProperty;
import org.onehippo.forge.content.pojo.model.ContentPropertyType;
import org.onehippo.forge.content.pojo.model.HandleContentNode;

public class DefaultHippoJcrContentNodeMapper implements ContentNodeMapper<Node, Item> {

    private static final long serialVersionUID = 1L;

    public DefaultHippoJcrContentNodeMapper() {
        super();
    }

    @Override
    public ContentNode map(Node jcrDataNode) throws ContentNodeMappingException {
        return map(jcrDataNode, new DefaultHippoJcrItemMappingFilter());
    }

    @Override
    public ContentNode map(Node jcrDataNode, ContentNodeMappingItemFilter<Item> itemFilter) throws ContentNodeMappingException {
        ContentNode contentNode = null;

        try {
            contentNode = createContentNodeByJcrNodeTypes(jcrDataNode);

            Property prop;
            ContentProperty contentProp;

            for (PropertyIterator propIt = jcrDataNode.getProperties(); propIt.hasNext(); ) {
                prop = propIt.nextProperty();

                if (itemFilter != null && !itemFilter.accept(prop)) {
                    continue;
                }

                contentProp = createContentPropertyFromJcrProperty(prop);
                contentNode.addProperty(contentProp);
            }

            Node childJcrNode;
            ContentNode childContentNode;

            if (HippoDocumentUtils.isDocumentHandleNode(jcrDataNode)) {
                String state;

                for (NodeIterator nodeIt = jcrDataNode.getNodes(jcrDataNode.getName()); nodeIt.hasNext(); ) {
                    childJcrNode = nodeIt.nextNode();

                    if (itemFilter != null && !itemFilter.accept(childJcrNode)) {
                        continue;
                    }

                    if (HippoDocumentUtils.isDocumentVariantNode(childJcrNode)) {
                        state = childJcrNode.getProperty(HippoStdNodeType.HIPPOSTD_STATE).getString();

                        if (HippoStdNodeType.PUBLISHED.equals(state) || HippoStdNodeType.UNPUBLISHED.equals(state)) {
                            childContentNode = map(childJcrNode, itemFilter);
                            ((HandleContentNode) contentNode).putRendition(state, childContentNode);
                        }
                    }
                }

                for (NodeIterator nodeIt = jcrDataNode.getNodes(HippoNodeType.HIPPO_TRANSLATION); nodeIt.hasNext();) {
                    childJcrNode = nodeIt.nextNode();

                    if (itemFilter != null && !itemFilter.accept(childJcrNode)) {
                        continue;
                    }

                    if (childJcrNode.isNodeType(HippoNodeType.NT_TRANSLATION)) {
                        childContentNode = map(childJcrNode, itemFilter);
                        contentNode.addNode(childContentNode);
                    }
                }
            } else {
                for (NodeIterator nodeIt = jcrDataNode.getNodes(); nodeIt.hasNext(); ) {
                    childJcrNode = nodeIt.nextNode();

                    if (itemFilter != null && !itemFilter.accept(childJcrNode)) {
                        continue;
                    }

                    childContentNode = map(childJcrNode, itemFilter);
                    contentNode.addNode(childContentNode);
                }
            }
        } catch (RepositoryException e) {
            throw new ContentNodeMappingException(e.toString(), e);
        }

        return contentNode;
    }

    protected ContentProperty createContentPropertyFromJcrProperty(final Property jcrProp) throws RepositoryException {
        final ContentProperty contentProp = new ContentProperty();

        contentProp.setName(jcrProp.getName());

        ContentPropertyType type = ContentPropertyType.UNDEFINED;

        switch (jcrProp.getType()) {
        case PropertyType.STRING:
        {
            type = ContentPropertyType.STRING;
            break;
        }
        case PropertyType.BINARY:
        {
            type = ContentPropertyType.BINARY;
            break;
        }
        case PropertyType.LONG:
        {
            type = ContentPropertyType.LONG;
            break;
        }
        case PropertyType.DOUBLE:
        {
            type = ContentPropertyType.DOUBLE;
            break;
        }
        case PropertyType.DATE:
        {
            type = ContentPropertyType.DATE;
            break;
        }
        case PropertyType.BOOLEAN:
        {
            type = ContentPropertyType.BOOLEAN;
            break;
        }
        case PropertyType.NAME:
        case PropertyType.PATH:
        case PropertyType.URI:
        {
            type = ContentPropertyType.STRING;
            break;
        }
        case PropertyType.DECIMAL:
        {
            type = ContentPropertyType.STRING;
            break;
        }
        }

        contentProp.setType(type);

        contentProp.setMultiple(jcrProp.isMultiple());

        if (jcrProp.isMultiple()) {
            for (Value jcrValue : jcrProp.getValues()) {
                contentProp.addValue(JcrContentUtils.jcrValueToString(jcrValue));
            }
        } else {
            contentProp.addValue(JcrContentUtils.jcrValueToString(jcrProp.getValue()));
        }

        return contentProp;
    }

    private ContentNode createContentNodeByJcrNodeTypes(final Node jcrNode) throws RepositoryException {
        ContentNode contentNode = null;

        if (HippoDocumentUtils.isDocumentHandleNode(jcrNode)) {
            contentNode = new HandleContentNode();
        } else {
            contentNode = new ContentNode();
        }

        contentNode.setPrimaryType(jcrNode.getPrimaryNodeType().getName());
        contentNode.setName(jcrNode.getName());

        for (NodeType mixinType: jcrNode.getMixinNodeTypes()) {
            contentNode.addMixinType(mixinType.getName());
        }

        return contentNode;
    }

}
