/*
 *  Copyright 2015-2018 Hippo B.V. (http://www.onehippo.com)
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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.apache.commons.lang.StringUtils;
import org.hippoecm.repository.HippoStdNodeType;
import org.hippoecm.repository.api.HippoNodeType;
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

/**
 * Default {@link ContentNodeBinder} implementation for JCR.
 */
public class DefaultJcrContentNodeBinder implements ContentNodeBinder<Node, ContentItem, Value> {

    private static final long serialVersionUID = 1L;

    // No constant in the product until 11.1
    private static final String NT_COMPOUND = "hippo:compound";

    // No constant in the product until 12.3
    private static final String NT_IMAGE_LINK = "hippogallerypicker:imagelink";

    /**
     * Flag whether it should merge subnodes without removing any other existing subnodes.
     * <p>
     * The default value is {@code false}, meaning the default behavior is to remove all the existing subnodes
     * beforehand and add all the subnodes from the {@link ContentNode} afterward, resulting any other subnodes
     * that do not exist in the input {@link ContentNode} to be removed by default.
     */
    private boolean subNodesMergingOnly;

    /**
     * Default constructor.
     */
    public DefaultJcrContentNodeBinder() {
        super();
    }

    /**
     * Return true if it should merge subnodes without removing any other existing subnodes.
     * <p>
     * The default value is {@code false}, meaning the default behavior is to remove all the existing subnodes
     * beforehand and add all the subnodes from the {@link ContentNode} afterward, resulting any other subnodes
     * that do not exist in the input {@link ContentNode} to be removed by default.
     * @return true if it should merge subnodes without removing any other existing subnodes
     */
    public boolean isSubNodesMergingOnly() {
        return subNodesMergingOnly;
    }

    /**
     * Set flag whether it should merge subnodes without removing any other existing subnodes.
     * @param subNodesMergingOnly
     */
    public void setSubNodesMergingOnly(boolean subNodesMergingOnly) {
        this.subNodesMergingOnly = subNodesMergingOnly;
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

            bindProperties(jcrDataNode, contentNode, itemFilter, valueConverter);

            if (!isSubNodesMergingOnly()) {
                removeSubNodes(jcrDataNode, contentNode, itemFilter);
                addSubNodes(jcrDataNode, contentNode, itemFilter, valueConverter);
            } else {
                mergeSubNodes(jcrDataNode, contentNode, itemFilter, valueConverter);
            }

        } catch (RepositoryException e) {
            throw new ContentNodeBindingException(e.toString(), e);
        }
    }

    /**
     * Set the properties on the JCR node based on the POJO.
     */
    protected void bindProperties(final Node jcrDataNode, final ContentNode contentNode, final ContentNodeBindingItemFilter<ContentItem> itemFilter, final ContentValueConverter<Value> valueConverter) throws RepositoryException {
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

                if (jcrValues != null) {
                    if (contentProp.isMultiple()) {
                        try {
                            jcrDataNode.setProperty(propName, jcrValues);
                        } catch (ArrayIndexOutOfBoundsException ignore) {
                            // Due to REPO-1428, let's ignore this kind of exception for now...
                        }
                    } else if (jcrValues.length > 0) {
                        try {
                            jcrDataNode.setProperty(propName, jcrValues[0]);
                        } catch (ValueFormatException e) {
                            // In this case, the content node (from a file) has a property as single value.
                            // However, if the relaxed document type has changed from single value to multiple values for a property,
                            // and so if the prototype has changed to multiple values, while the exported content property
                            // is still single property,
                            // then this ValueFormatException may happen because the single value cannot be set
                            // for a new multi-value property generated from the new prototype.
                            // Therefore, try to set property with array again in this case.
                            jcrDataNode.setProperty(propName, jcrValues);
                        }
                    }
                }
            }
        }
    }

    /**
     * Remove subnodes from the JCR node, when binding.
     */
    protected void removeSubNodes(final Node jcrDataNode, final ContentNode contentNode, final ContentNodeBindingItemFilter<ContentItem> itemFilter) throws RepositoryException {

        if (jcrDataNode.isNodeType(HippoNodeType.NT_DOCUMENT) && jcrDataNode.getParent().isNodeType(HippoNodeType.NT_HANDLE)) {
            // remove compounds of a document
            final Set<String> subNodeNames = getCompoundNodeNames(jcrDataNode);
            final String[] nameGlobs = subNodeNames.toArray(new String[subNodeNames.size()]);
            for (NodeIterator nodeIt = jcrDataNode.getNodes(nameGlobs); nodeIt.hasNext(); ) {
                nodeIt.nextNode().remove();
            }
        }
        else {
            // remove subnodes based on the POJO's subnodes
            for (ContentNode childContentNode : contentNode.getNodes()) {
                if (itemFilter != null && !itemFilter.accept(childContentNode)) {
                    continue;
                }

                for (Node sameNameTypeChildNode : findChildNodesByNameAndType(jcrDataNode, childContentNode)) {
                    sameNameTypeChildNode.remove();
                }
            }
        }
    }

    /**
     * (Re)add subnodes to the JCR node, based on POJO.
     */
    protected void addSubNodes(final Node jcrDataNode, final ContentNode contentNode, final ContentNodeBindingItemFilter<ContentItem> itemFilter, final ContentValueConverter<Value> valueConverter) throws RepositoryException {

        for (ContentNode childContentNode : contentNode.getNodes()) {
            if (itemFilter != null && !itemFilter.accept(childContentNode)) {
                continue;
            }

            final Node childJcrNode = jcrDataNode.addNode(childContentNode.getName(), childContentNode.getPrimaryType());

            bind(childJcrNode, childContentNode, itemFilter, valueConverter);
        }
    }

    /**
     * Merge subnodes in the JCR node, based on POJO.
     */
    protected void mergeSubNodes(final Node jcrDataNode, final ContentNode contentNode,
            final ContentNodeBindingItemFilter<ContentItem> itemFilter,
            final ContentValueConverter<Value> valueConverter) throws RepositoryException {

        Node childJcrNode;

        for (ContentNode childContentNode : contentNode.getNodes()) {

            if (itemFilter != null && !itemFilter.accept(childContentNode)) {
                continue;
            }

            if (!jcrDataNode.hasNode(childContentNode.getName())) {
                childJcrNode = jcrDataNode.addNode(childContentNode.getName(), childContentNode.getPrimaryType());
            } else {
                childJcrNode = jcrDataNode.getNode(childContentNode.getName());
            }

            bind(childJcrNode, childContentNode, itemFilter, valueConverter);
        }
    }

    protected Value[] createJcrValuesFromContentProperty(final Node jcrNode, final ContentProperty contentProp,
                                                         final ContentValueConverter<Value> valueConverter)
            throws RepositoryException {
        List<Value> jcrValues = new LinkedList<>();

        Value jcrValue;

        if (ContentPropertyType.BINARY.equals(contentProp.getType())) {
            for (Object binaryValue : contentProp.getValuesAsObject()) {
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

    protected Set<String> getCompoundNodeNames(final Node jcrDataNode) throws RepositoryException {

        final Set<String> compoundNodeNames = new HashSet<>();
        final NodeIterator nodeIter = jcrDataNode.getNodes();
        while (nodeIter.hasNext()) {
            final Node subNode = nodeIter.nextNode();
            if (isCompoundType(subNode)) {
                compoundNodeNames.add(subNode.getName());
            }
        }

        return compoundNodeNames;
    }

    /**
     * Is a node a hippo:compound, or of some product type that is used as compound but does not extend from that.
     */
    protected boolean isCompoundType(final Node node) throws RepositoryException {
        return node.isNodeType(NT_COMPOUND) ||
                node.isNodeType(HippoNodeType.NT_MIRROR) ||
                node.isNodeType(HippoStdNodeType.NT_HTML) ||
                node.isNodeType(NT_IMAGE_LINK);

    }

    protected List<Node> findChildNodesByNameAndType(final Node base, final ContentNode contentNode)
            throws RepositoryException {
        final List<Node> childNodes = new LinkedList<>();

        for (NodeIterator nodeIt = base.getNodes(contentNode.getName()); nodeIt.hasNext();) {
            final Node childNode = nodeIt.nextNode();

            if (childNode.getPrimaryNodeType().getName().equals(contentNode.getPrimaryType())) {
                childNodes.add(childNode);
            }
        }

        return childNodes;
    }

    protected boolean isProtectedProperty(final Property property) throws RepositoryException {
        try {
            return property.getDefinition().isProtected();
        } catch (UnsupportedOperationException ignore) {
        }

        return false;
    }
}
