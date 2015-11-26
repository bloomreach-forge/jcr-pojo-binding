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
package org.onehippo.forge.content.pojo.bind.jcr.hippo;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.apache.commons.lang.StringUtils;
import org.onehippo.forge.content.pojo.bind.ContentNodeBinder;
import org.onehippo.forge.content.pojo.bind.ContentNodeBindingException;
import org.onehippo.forge.content.pojo.bind.ItemFilter;
import org.onehippo.forge.content.pojo.bind.jcr.JcrContentUtils;
import org.onehippo.forge.content.pojo.model.ContentItem;
import org.onehippo.forge.content.pojo.model.ContentNode;
import org.onehippo.forge.content.pojo.model.ContentProperty;

public class DefaultHippoJcrContentNodeBinder extends BaseHippoJcrContentNodeHandler implements ContentNodeBinder<Node, ContentItem> {

    private static final long serialVersionUID = 1L;

    public DefaultHippoJcrContentNodeBinder() {
        super();
    }

    @Override
    public void bind(Node jcrDataNode, ContentNode contentNode) throws ContentNodeBindingException {
    }

    @Override
    public void bind(Node jcrDataNode, ContentNode contentNode, ItemFilter<ContentItem> itemFilter) throws ContentNodeBindingException {
        try {
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

            for (ContentProperty contentProp : contentNode.getProperties()) {
                if (itemFilter != null && !itemFilter.accept(contentProp)) {
                    continue;
                }

                existingJcrProp = jcrDataNode.hasProperty(contentProp.getName())
                        ? jcrDataNode.getProperty(contentProp.getName()) : null;

                if (existingJcrProp != null && JcrContentUtils.isProtected(existingJcrProp)) {
                    continue;
                }

                jcrValues = createJcrValuesFromContentProperty(jcrDataNode, contentProp);

                if (jcrValues != null && jcrValues.length > 0) {
                    if (!contentProp.isMultiple()) {
                        jcrDataNode.setProperty(contentProp.getName(), jcrValues[0]);
                    } else {
                        jcrDataNode.setProperty(contentProp.getName(), jcrValues);
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

                bind(childJcrNode, childContentNode);
            }
        } catch (RepositoryException e) {
            throw new ContentNodeBindingException(e.toString(), e);
        }
    }

    private Value[] createJcrValuesFromContentProperty(final Node jcrNode, final ContentProperty contentProp)
            throws RepositoryException {
        List<Value> jcrValues = new LinkedList<>();

        final ValueFactory valueFactory = jcrNode.getSession().getValueFactory();

        for (Object objectValue : contentProp.getObjectValues()) {
            switch (contentProp.getType()) {
            case STRING: {
                jcrValues.add(valueFactory.createValue((String) objectValue));
                break;
            }
            case DATE: {
                jcrValues.add(valueFactory.createValue((Calendar) objectValue));
                break;
            }
            case BOOLEAN: {
                jcrValues.add(valueFactory.createValue((Boolean) objectValue));
                break;
            }
            case LONG: {
                jcrValues.add(valueFactory.createValue((Long) objectValue));
                break;
            }
            case DOUBLE: {
                jcrValues.add(valueFactory.createValue((Double) objectValue));
                break;
            }
            case DECIMAL: {
                jcrValues.add(valueFactory.createValue((BigDecimal) objectValue));
                break;
            }
            default:
                break;
            }
        }

        return jcrValues.toArray(new Value[jcrValues.size()]);
    }

    private List<Node> findChildNodesByNameAndType(final Node base, final ContentNode contentNode) throws RepositoryException {
        List<Node> childNodes = new LinkedList<>();

        Node childNode;

        for (NodeIterator nodeIt = base.getNodes(contentNode.getName()); nodeIt.hasNext(); ) {
            childNode = nodeIt.nextNode();

            if (childNode.getPrimaryNodeType().getName().equals(contentNode.getPrimaryType())) {
                childNodes.add(childNode);
            }
        }

        return childNodes;
    }
}
