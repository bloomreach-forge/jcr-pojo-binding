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
package org.onehippo.forge.content.pojo.binder.jcr;

import java.util.LinkedList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;
import org.onehippo.forge.content.pojo.binder.ContentNodeBinder;
import org.onehippo.forge.content.pojo.binder.ContentNodeBindingException;
import org.onehippo.forge.content.pojo.binder.ContentNodeBindingItemFilter;
import org.onehippo.forge.content.pojo.common.ContentValueConverter;
import org.onehippo.forge.content.pojo.common.jcr.DefaultJcrContentValueConverter;
import org.onehippo.forge.content.pojo.model.BinaryValue;
import org.onehippo.forge.content.pojo.model.ContentItem;
import org.onehippo.forge.content.pojo.model.ContentNode;
import org.onehippo.forge.content.pojo.model.ContentProperty;
import org.onehippo.forge.content.pojo.model.ContentPropertyType;

public class DefaultJcrContentNodeBinder implements ContentNodeBinder<Node, ContentItem, Value> {

    private static final long serialVersionUID = 1L;

    public DefaultJcrContentNodeBinder() {
        super();
    }

    @Override
    public void bind(Node jcrDataNode, ContentNode contentNode) throws ContentNodeBindingException {
        bind(jcrDataNode, contentNode, null);
    }

    @Override
    public void bind(Node jcrDataNode, ContentNode contentNode, ContentNodeBindingItemFilter<ContentItem> itemFilter)
            throws ContentNodeBindingException {
        bind(jcrDataNode, contentNode, itemFilter, null);
    }

    @Override
    public void bind(Node jcrDataNode, ContentNode contentNode, ContentNodeBindingItemFilter<ContentItem> itemFilter,
            ContentValueConverter<Value> valueConverter) throws ContentNodeBindingException {
        try {
            if (itemFilter == null) {
                itemFilter = new DefaultContentNodeJcrBindingItemFilter();
            }

            if (valueConverter == null) {
                valueConverter = new DefaultJcrContentValueConverter(jcrDataNode.getSession());
            }

            if (StringUtils.isNotBlank(contentNode.getPrimaryType())
                    && !jcrDataNode.getPrimaryNodeType().getName().equals(contentNode.getPrimaryType())) {
                jcrDataNode.setPrimaryType(contentNode.getPrimaryType());
            }

            for (String mixinType : contentNode.getMixinTypes()) {
                if (!jcrDataNode.isNodeType(mixinType)) {
                    jcrDataNode.addMixin(mixinType);
                }
            }

            Value[] jcrValues;
            Property existingJcrProp;

            String propName;
            String pathValue;

            for (ContentProperty contentProp : contentNode.getProperties()) {
                propName = contentProp.getName();

                if (itemFilter != null && !itemFilter.accept(contentProp)) {
                    continue;
                }

                existingJcrProp = jcrDataNode.hasProperty(propName) ? jcrDataNode.getProperty(propName) : null;

                if (existingJcrProp != null && isProtectedProperty(existingJcrProp)) {
                    continue;
                }

                if (ContentPropertyType.PATH.equals(contentProp.getType())) {
                    pathValue = contentProp.getValue();

                    if (StringUtils.isNotBlank(pathValue) && jcrDataNode.getSession().nodeExists(pathValue)) {
                        jcrDataNode.setProperty(propName, jcrDataNode.getSession().getNode(pathValue));
                    }
                } else {
                    jcrValues = createJcrValuesFromContentProperty(jcrDataNode, contentProp, valueConverter);

                    if (jcrValues != null && jcrValues.length > 0) {
                        if (contentProp.isMultiple()) {
                            jcrDataNode.setProperty(propName, jcrValues);
                        } else {
                            jcrDataNode.setProperty(propName, jcrValues[0]);
                        }
                    }
                }
            }

            Node childJcrNode;

            for (ContentNode childContentNode : contentNode.getNodes()) {
                if (itemFilter != null && !itemFilter.accept(childContentNode)) {
                    continue;
                }

                for (Node sameNameTypeChildNode : findChildNodesByNameAndType(jcrDataNode, childContentNode)) {
                    sameNameTypeChildNode.remove();
                }

                childJcrNode = jcrDataNode.addNode(childContentNode.getName(), childContentNode.getPrimaryType());

                bind(childJcrNode, childContentNode, itemFilter, valueConverter);
            }
        } catch (RepositoryException e) {
            throw new ContentNodeBindingException(e.toString(), e);
        }
    }

    private Value[] createJcrValuesFromContentProperty(final Node jcrNode, final ContentProperty contentProp,
            final ContentValueConverter<Value> valueConverter)
            throws RepositoryException {
        List<Value> jcrValues = new LinkedList<>();

        Value jcrValue;

        if (ContentPropertyType.BINARY.equals(contentProp.getType())) {
            for (Object binaryValue : contentProp.getObjectValues()) {
                jcrValue = valueConverter.toJcrValue((BinaryValue) binaryValue);

                if (jcrValue != null) {
                    jcrValues.add(jcrValue);
                }
            }
        } else {
            for (String stringValue : contentProp.getValues()) {
                jcrValue = valueConverter.toJcrValue(contentProp.getType().toString(), stringValue);

                if (jcrValue != null) {
                    jcrValues.add(jcrValue);
                }
            }
        }

        return jcrValues.toArray(new Value[jcrValues.size()]);
    }

    private List<Node> findChildNodesByNameAndType(final Node base, final ContentNode contentNode)
            throws RepositoryException {
        List<Node> childNodes = new LinkedList<>();

        Node childNode;

        for (NodeIterator nodeIt = base.getNodes(contentNode.getName()); nodeIt.hasNext();) {
            childNode = nodeIt.nextNode();

            if (childNode.getPrimaryNodeType().getName().equals(contentNode.getPrimaryType())) {
                childNodes.add(childNode);
            }
        }

        return childNodes;
    }

    private boolean isProtectedProperty(final Property property) throws RepositoryException {
        try {
            return property.getDefinition().isProtected();
        } catch (UnsupportedOperationException ignore) {
        }

        return false;
    }
}
