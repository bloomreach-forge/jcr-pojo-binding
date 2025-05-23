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
package org.onehippo.forge.content.pojo.mapper.jcr;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.lang3.StringUtils;
import org.onehippo.forge.content.pojo.common.ContentValueConverter;
import org.onehippo.forge.content.pojo.common.jcr.DefaultJcrContentValueConverter;
import org.onehippo.forge.content.pojo.mapper.ContentNodeMapper;
import org.onehippo.forge.content.pojo.mapper.ContentNodeMappingException;
import org.onehippo.forge.content.pojo.mapper.ContentNodeMappingItemFilter;
import org.onehippo.forge.content.pojo.mapper.jcr.hippo.DefaultHippoJcrItemMappingFilter;
import org.onehippo.forge.content.pojo.model.ContentNode;
import org.onehippo.forge.content.pojo.model.ContentProperty;
import org.onehippo.forge.content.pojo.model.ContentPropertyType;

/**
 * Default {@link ContentNodeMapper} implementation for JCR.
 */
public class DefaultJcrContentNodeMapper implements ContentNodeMapper<Node, Item, Value> {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public DefaultJcrContentNodeMapper() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContentNode map(Node jcrDataNode) throws ContentNodeMappingException {
        return map(jcrDataNode, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContentNode map(Node jcrDataNode, ContentNodeMappingItemFilter<Item> itemFilter)
            throws ContentNodeMappingException {
        return map(jcrDataNode, itemFilter, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContentNode map(Node jcrDataNode, ContentNodeMappingItemFilter<Item> itemFilter,
            ContentValueConverter<Value> valueConverter) throws ContentNodeMappingException {
        ContentNode contentNode = null;

        try {
            if (itemFilter == null) {
                itemFilter = new DefaultHippoJcrItemMappingFilter();
            }

            if (valueConverter == null) {
                valueConverter = new DefaultJcrContentValueConverter(jcrDataNode.getSession());
            }

            contentNode = createContentNodeByJcrNodeTypes(jcrDataNode);

            Property prop;
            ContentProperty contentProp;

            for (PropertyIterator propIt = jcrDataNode.getProperties(); propIt.hasNext();) {
                prop = propIt.nextProperty();

                if (itemFilter != null && !itemFilter.accept(prop)) {
                    continue;
                }

                contentProp = createContentPropertyFromJcrProperty(prop, valueConverter);
                contentNode.setProperty(contentProp);
            }

            Node childJcrNode;
            ContentNode childContentNode;

            for (NodeIterator nodeIt = jcrDataNode.getNodes(); nodeIt.hasNext();) {
                childJcrNode = nodeIt.nextNode();

                if (itemFilter != null && !itemFilter.accept(childJcrNode)) {
                    continue;
                }

                childContentNode = map(childJcrNode, itemFilter, valueConverter);
                contentNode.addNode(childContentNode);
            }
        } catch (RepositoryException e) {
            throw new ContentNodeMappingException(e.toString(), e);
        }

        return contentNode;
    }

    protected ContentProperty createContentPropertyFromJcrProperty(final Property jcrProp, final ContentValueConverter<Value> valueConverter) throws RepositoryException {
        ContentProperty contentProp = null;

        ContentPropertyType type = ContentPropertyType.UNDEFINED;
        final int jcrPropType = jcrProp.getType();
        final Node jcrNode = jcrProp.getParent();

        switch (jcrPropType) {
        case PropertyType.STRING: {
            type = ContentPropertyType.STRING;
            break;
        }
        case PropertyType.BINARY: {
            type = ContentPropertyType.BINARY;
            break;
        }
        case PropertyType.LONG: {
            type = ContentPropertyType.LONG;
            break;
        }
        case PropertyType.DOUBLE: {
            type = ContentPropertyType.DOUBLE;
            break;
        }
        case PropertyType.DATE: {
            type = ContentPropertyType.DATE;
            break;
        }
        case PropertyType.BOOLEAN: {
            type = ContentPropertyType.BOOLEAN;
            break;
        }
        case PropertyType.DECIMAL: {
            type = ContentPropertyType.STRING;
            break;
        }
        case PropertyType.NAME:
        case PropertyType.URI: {
            type = ContentPropertyType.STRING;
            break;
        }
        case PropertyType.PATH:
        case PropertyType.REFERENCE:
        case PropertyType.WEAKREFERENCE: {
            type = ContentPropertyType.PATH;
            break;
        }
        }

        contentProp = new ContentProperty(jcrProp.getName(), type, jcrProp.isMultiple());

        if (ContentPropertyType.PATH.equals(type)) {
            Node referenceNode = jcrProp.getNode();
            String referenceNodePath = referenceNode.getPath();
            contentProp.setValue(referenceNodePath);
        } else if (ContentPropertyType.BINARY.equals(type)) {
            String mimeType = null;

            if (jcrNode.hasProperty("jcr:mimeType")) {
                mimeType = StringUtils.trim(jcrNode.getProperty("jcr:mimeType").getString());
            }

            if (jcrProp.isMultiple()) {
                for (Value jcrValue : jcrProp.getValues()) {
                    contentProp.addValue(valueConverter.toBinaryValue(jcrValue, mimeType));
                }
            } else {
                contentProp.addValue(valueConverter.toBinaryValue(jcrProp.getValue(), mimeType));
            }
        } else {
            if (jcrProp.isMultiple()) {
                for (Value jcrValue : jcrProp.getValues()) {
                    contentProp.addValue(valueConverter.toString(jcrValue));
                }
            } else {
                contentProp.addValue(valueConverter.toString(jcrProp.getValue()));
            }
        }

        return contentProp;
    }

    private ContentNode createContentNodeByJcrNodeTypes(final Node jcrNode) throws RepositoryException {
        ContentNode contentNode = new ContentNode(jcrNode.getName(), jcrNode.getPrimaryNodeType().getName());

        for (NodeType mixinType : jcrNode.getMixinNodeTypes()) {
            contentNode.addMixinType(mixinType.getName());
        }

        return contentNode;
    }

}
