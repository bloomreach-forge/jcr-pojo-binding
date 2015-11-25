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
package org.onehippo.forge.content.pojo.bind.hippo;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;

import org.apache.jackrabbit.util.ISO8601;
import org.hippoecm.repository.HippoStdNodeType;
import org.hippoecm.repository.api.HippoNodeType;
import org.onehippo.forge.content.pojo.bind.ContentNodeBindingException;
import org.onehippo.forge.content.pojo.bind.ContentNodeMapper;
import org.onehippo.forge.content.pojo.bind.ContentNodeMappingException;
import org.onehippo.forge.content.pojo.model.ContentNode;
import org.onehippo.forge.content.pojo.model.ContentProperty;
import org.onehippo.forge.content.pojo.model.ContentPropertyType;
import org.onehippo.forge.content.pojo.model.DocumentContent;
import org.onehippo.forge.content.pojo.model.DocumentContentHandle;

public class DefaultHippoJcrContentNodeMapper extends BaseHippoJcrContentNodeHandler implements ContentNodeMapper<Node> {

    private static final long serialVersionUID = 1L;

    public DefaultHippoJcrContentNodeMapper() {
        super();
    }

    @Override
    public ContentNode map(Node jcrDataNode) throws ContentNodeMappingException {
        ContentNode contentNode = null;

        try {
            contentNode = createContentNodeByJcrNodeTypes(jcrDataNode);

            Property prop;
            ContentProperty contentProp;

            for (PropertyIterator propIt = jcrDataNode.getProperties(); propIt.hasNext(); ) {
                prop = propIt.nextProperty();

                if (!isMappableProperty(prop)) {
                    continue;
                }

                contentProp = createContentPropertyFromJcrProperty(prop);
                contentNode.addProperty(contentProp);
            }

            Node childJcrNode;
            ContentNode childContentNode;

            if (isDocumentHandleNode(jcrDataNode)) {
                String state;

                for (NodeIterator nodeIt = jcrDataNode.getNodes(jcrDataNode.getName()); nodeIt.hasNext(); ) {
                    childJcrNode = nodeIt.nextNode();

                    if (!isMappableNode(childJcrNode)) {
                        continue;
                    }

                    if (isDocumentVariantNode(childJcrNode)) {
                        state = childJcrNode.getProperty(HippoStdNodeType.HIPPOSTD_STATE).getString();

                        if (HippoStdNodeType.PUBLISHED.equals(state) || HippoStdNodeType.UNPUBLISHED.equals(state)) {
                            childContentNode = map(childJcrNode);
                            ((DocumentContentHandle) contentNode).putDocument(state, (DocumentContent) childContentNode);
                        }
                    }
                }

                for (NodeIterator nodeIt = jcrDataNode.getNodes(HippoNodeType.HIPPO_TRANSLATION); nodeIt.hasNext();) {
                    childJcrNode = nodeIt.nextNode();

                    if (!isMappableNode(childJcrNode)) {
                        continue;
                    }

                    if (childJcrNode.isNodeType(HippoNodeType.NT_TRANSLATION)) {
                        childContentNode = map(childJcrNode);
                        contentNode.addNode(childContentNode);
                    }
                }
            } else {
                for (NodeIterator nodeIt = jcrDataNode.getNodes(); nodeIt.hasNext(); ) {
                    childJcrNode = nodeIt.nextNode();

                    if (!isMappableNode(childJcrNode)) {
                        continue;
                    }

                    childContentNode = map(childJcrNode);
                    contentNode.addNode(childContentNode);
                }
            }
        } catch (RepositoryException e) {
            throw new ContentNodeBindingException(e.toString(), e);
        }

        return contentNode;
    }

    protected boolean isMappableNode(final Node node) throws RepositoryException {
        return true;
    }

    protected boolean isMappableProperty(final Property property) throws RepositoryException {
        if (HippoNodeType.HIPPO_PATH.equals(property.getName())) {
            return false;
        }

        if (JcrContentUtils.isProtected(property)) {
            return false;
        }

        return true;
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
                contentProp.addValue(jcrValueToString(jcrValue));
            }
        } else {
            contentProp.addValue(jcrValueToString(jcrProp.getValue()));
        }

        return contentProp;
    }

    private String jcrValueToString(final Value value) throws RepositoryException {
        String stringifiedValue = null;

        switch (value.getType()) {
        case PropertyType.STRING:
        {
            stringifiedValue = value.getString();
            break;
        }
        case PropertyType.BINARY:
        {
            // TODO: NOT SUPPORTED YET
            // Just ignore for now...
            break;
        }
        case PropertyType.LONG:
        {
            stringifiedValue = Long.toString(value.getLong());
            break;
        }
        case PropertyType.DOUBLE:
        {
            stringifiedValue = Double.toString(value.getDouble());
            break;
        }
        case PropertyType.DATE:
        {
            stringifiedValue = ISO8601.format(value.getDate());
            break;
        }
        case PropertyType.BOOLEAN:
        {
            stringifiedValue = Boolean.toString(value.getBoolean());
            break;
        }
        case PropertyType.NAME:
        case PropertyType.PATH:
        case PropertyType.URI:
        {
            stringifiedValue = value.getString();
            break;
        }
        case PropertyType.DECIMAL:
            stringifiedValue = value.getDecimal().toString();
            break;
        }

        return stringifiedValue;
    }

    private ContentNode createContentNodeByJcrNodeTypes(final Node jcrNode) throws RepositoryException {
        ContentNode contentNode = null;

        if (isDocumentVariantNode(jcrNode)) {
            contentNode = new DocumentContent();
        } else if (isDocumentHandleNode(jcrNode)) {
            contentNode = new DocumentContentHandle();
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

    protected boolean isDocumentVariantNode(final Node jcrNode) throws RepositoryException {
        if (jcrNode.isNodeType(HippoNodeType.NT_DOCUMENT)
                && jcrNode.isNodeType(HippoStdNodeType.NT_PUBLISHABLE)
                && jcrNode.getParent().isNodeType(HippoNodeType.NT_HANDLE)) {
            return true;
        }

        return false;
    }

    protected boolean isLiveDocumentVariantNode(final Node jcrNode) throws RepositoryException {
        if (isDocumentVariantNode(jcrNode) && jcrNode.hasProperty(HippoStdNodeType.HIPPOSTD_STATE)) {
            if (HippoStdNodeType.PUBLISHED.equals(jcrNode.getProperty(HippoStdNodeType.HIPPOSTD_STATE).getString())) {
                return true;
            }
        }

        return false;
    }

    protected boolean isPreviewDocumentVariantNode(final Node jcrNode) throws RepositoryException {
        if (isDocumentVariantNode(jcrNode) && jcrNode.hasProperty(HippoStdNodeType.HIPPOSTD_STATE)) {
            if (HippoStdNodeType.UNPUBLISHED.equals(jcrNode.getProperty(HippoStdNodeType.HIPPOSTD_STATE).getString())) {
                return true;
            }
        }

        return false;
    }

    protected boolean isDocumentHandleNode(final Node jcrNode) throws RepositoryException {
        if (jcrNode.isNodeType(HippoNodeType.NT_HANDLE)
                && jcrNode.hasNode(jcrNode.getName())) {
            final Node variantNode = jcrNode.getNode(jcrNode.getName());

            if (variantNode.isNodeType(HippoNodeType.NT_DOCUMENT)
                    && variantNode.isNodeType(HippoStdNodeType.NT_PUBLISHABLE)) {
                return true;
            }
        }

        return false;
    }
}
