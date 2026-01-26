/*
 *  Copyright 2015-2025 Bloomreach (https://www.bloomreach.com)
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.apache.commons.lang3.StringUtils;
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

    private static final String NT_COMPOUND = "hippo:compound";
    private static final String NT_IMAGE_LINK = "hippogallerypicker:imagelink";

    private boolean subNodesMergingOnly;
    private boolean fullOverwriteMode;

    public boolean isSubNodesMergingOnly() {
        return subNodesMergingOnly;
    }

    public void setSubNodesMergingOnly(boolean subNodesMergingOnly) {
        this.subNodesMergingOnly = subNodesMergingOnly;
    }

    public boolean isFullOverwriteMode() {
        return fullOverwriteMode;
    }

    public void setFullOverwriteMode(boolean fullOverwriteMode) {
        this.fullOverwriteMode = fullOverwriteMode;
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
            ContentNodeBindingItemFilter<ContentItem> filter = resolveFilter(itemFilter);
            ContentValueConverter<Value> converter = resolveConverter(jcrDataNode, valueConverter);

            syncPrimaryType(jcrDataNode, contentNode);
            syncMixinTypes(jcrDataNode, contentNode);
            bindProperties(jcrDataNode, contentNode, filter, converter);
            bindSubNodes(jcrDataNode, contentNode, filter, converter);

        } catch (RepositoryException e) {
            throw new ContentNodeBindingException(e.toString(), e);
        }
    }


    protected void bindProperties(Node jcrDataNode, ContentNode contentNode,
                                  ContentNodeBindingItemFilter<ContentItem> itemFilter,
                                  ContentValueConverter<Value> valueConverter) throws RepositoryException {

        for (ContentProperty contentProp : contentNode.getProperties()) {
            if (!itemFilter.accept(contentProp)) {
                continue;
            }
            if (isProtectedProperty(jcrDataNode, contentProp.getName())) {
                continue;
            }
            bindProperty(jcrDataNode, contentProp, valueConverter);
        }
    }

    private void bindProperty(Node jcrDataNode, ContentProperty contentProp,
                              ContentValueConverter<Value> valueConverter) throws RepositoryException {

        if (ContentPropertyType.PATH.equals(contentProp.getType())) {
            bindPathProperty(jcrDataNode, contentProp);
        } else {
            bindValueProperty(jcrDataNode, contentProp, valueConverter);
        }
    }

    private void bindPathProperty(Node jcrDataNode, ContentProperty contentProp) throws RepositoryException {
        String pathValue = contentProp.getValue();
        if (StringUtils.isBlank(pathValue)) {
            return;
        }
        if (!jcrDataNode.getSession().nodeExists(pathValue)) {
            return;
        }
        jcrDataNode.setProperty(contentProp.getName(), jcrDataNode.getSession().getNode(pathValue));
    }

    private void bindValueProperty(Node jcrDataNode, ContentProperty contentProp,
                                   ContentValueConverter<Value> valueConverter) throws RepositoryException {

        Value[] jcrValues = createJcrValues(contentProp, valueConverter);
        if (jcrValues == null || jcrValues.length == 0) {
            return;
        }

        String propName = contentProp.getName();
        if (contentProp.isMultiple()) {
            setMultipleProperty(jcrDataNode, propName, jcrValues);
        } else {
            setSingleProperty(jcrDataNode, propName, jcrValues);
        }
    }

    private void setMultipleProperty(Node jcrDataNode, String propName, Value[] jcrValues) throws RepositoryException {
        try {
            jcrDataNode.setProperty(propName, jcrValues);
        } catch (ArrayIndexOutOfBoundsException ignore) {
            // REPO-1428 workaround
        }
    }

    private void setSingleProperty(Node jcrDataNode, String propName, Value[] jcrValues) throws RepositoryException {
        try {
            jcrDataNode.setProperty(propName, jcrValues[0]);
        } catch (ValueFormatException e) {
            // Property type changed from single to multiple - retry with array
            jcrDataNode.setProperty(propName, jcrValues);
        }
    }


    protected void bindSubNodes(Node jcrDataNode, ContentNode contentNode,
                                ContentNodeBindingItemFilter<ContentItem> itemFilter,
                                ContentValueConverter<Value> valueConverter) throws RepositoryException {

        if (isFullOverwriteMode()) {
            removeAllSubNodes(jcrDataNode);
            addSubNodes(jcrDataNode, contentNode, itemFilter, valueConverter);
        } else if (isSubNodesMergingOnly()) {
            mergeSubNodes(jcrDataNode, contentNode, itemFilter, valueConverter);
        } else {
            removeSubNodes(jcrDataNode, contentNode, itemFilter);
            addSubNodes(jcrDataNode, contentNode, itemFilter, valueConverter);
        }
    }

    protected void removeAllSubNodes(Node jcrDataNode) throws RepositoryException {
        NodeIterator children = jcrDataNode.getNodes();
        while (children.hasNext()) {
            children.nextNode().remove();
        }
    }

    protected void removeSubNodes(Node jcrDataNode, ContentNode contentNode,
                                  ContentNodeBindingItemFilter<ContentItem> itemFilter) throws RepositoryException {

        NodeIndex<Node> index = indexJcrChildren(jcrDataNode);

        for (Node node : index.getCompounds()) {
            node.remove();
        }

        for (ContentNode child : contentNode.getNodes()) {
            if (!itemFilter.accept(child)) {
                continue;
            }
            for (Node node : index.get(child.getName(), child.getPrimaryType())) {
                node.remove();
            }
        }
    }

    protected void addSubNodes(Node jcrDataNode, ContentNode contentNode,
                               ContentNodeBindingItemFilter<ContentItem> itemFilter,
                               ContentValueConverter<Value> valueConverter) throws RepositoryException {

        for (ContentNode child : contentNode.getNodes()) {
            if (!itemFilter.accept(child)) {
                continue;
            }
            Node childJcrNode = jcrDataNode.addNode(child.getName(), child.getPrimaryType());
            bind(childJcrNode, child, itemFilter, valueConverter);
        }
    }

    protected void mergeSubNodes(Node jcrDataNode, ContentNode contentNode,
                                 ContentNodeBindingItemFilter<ContentItem> itemFilter,
                                 ContentValueConverter<Value> valueConverter) throws RepositoryException {

        Map<String, Map<String, List<ContentNode>>> contentIndex = indexContentChildren(contentNode, itemFilter);
        Set<String> contentNames = contentIndex.keySet();

        NodeIndex<Node> jcrIndex = indexMergeableJcrChildren(jcrDataNode, contentNames);

        Set<String> mergeableNames = new LinkedHashSet<>(jcrIndex.getNames());
        mergeableNames.addAll(contentNames);

        for (String name : mergeableNames) {
            Map<String, List<Node>> jcrByType = jcrIndex.getByName(name);
            Map<String, List<ContentNode>> contentByType = contentIndex.getOrDefault(name, Collections.emptyMap());
            bindMatchingNodesByType(jcrDataNode, jcrByType, contentByType, itemFilter, valueConverter);
        }
    }

    private void bindMatchingNodesByType(Node jcrParent,
                                         Map<String, List<Node>> jcrNodesByType,
                                         Map<String, List<ContentNode>> contentNodesByType,
                                         ContentNodeBindingItemFilter<ContentItem> itemFilter,
                                         ContentValueConverter<Value> valueConverter) throws RepositoryException {

        for (Map.Entry<String, List<ContentNode>> entry : contentNodesByType.entrySet()) {
            String nodeType = entry.getKey();
            List<ContentNode> sourceNodes = entry.getValue();
            List<Node> targetNodes = jcrNodesByType.getOrDefault(nodeType, Collections.emptyList());

            for (int i = 0; i < sourceNodes.size(); i++) {
                ContentNode source = sourceNodes.get(i);
                Node target = (i < targetNodes.size())
                        ? targetNodes.get(i)
                        : jcrParent.addNode(source.getName(), source.getPrimaryType());
                bind(target, source, itemFilter, valueConverter);
            }
        }
    }

    private NodeIndex<Node> indexJcrChildren(Node jcrDataNode) throws RepositoryException {
        NodeIndex<Node> index = new NodeIndex<>();

        for (NodeIterator it = jcrDataNode.getNodes(); it.hasNext(); ) {
            Node child = it.nextNode();
            String name = child.getName();
            String type = child.getPrimaryNodeType().getName();

            if (isCompoundType(child)) {
                index.addCompound(child);
            } else {
                index.add(name, type, child);
            }
        }
        return index;
    }

    private NodeIndex<Node> indexMergeableJcrChildren(Node jcrDataNode, Set<String> contentNames)
            throws RepositoryException {

        NodeIndex<Node> index = new NodeIndex<>();

        for (NodeIterator it = jcrDataNode.getNodes(); it.hasNext(); ) {
            Node child = it.nextNode();
            String name = child.getName();

            if (isCompoundType(child) || contentNames.contains(name)) {
                String type = child.getPrimaryNodeType().getName();
                index.add(name, type, child);
            }
        }
        return index;
    }

    private Map<String, Map<String, List<ContentNode>>> indexContentChildren(
            ContentNode contentNode, ContentNodeBindingItemFilter<ContentItem> itemFilter) {

        Map<String, Map<String, List<ContentNode>>> index = new LinkedHashMap<>();

        for (ContentNode child : contentNode.getNodes()) {
            if (!itemFilter.accept(child)) {
                continue;
            }
            index.computeIfAbsent(child.getName(), k -> new LinkedHashMap<>())
                 .computeIfAbsent(child.getPrimaryType(), k -> new ArrayList<>())
                 .add(child);
        }
        return index;
    }


    private Value[] createJcrValues(ContentProperty contentProp,
                                    ContentValueConverter<Value> valueConverter) throws RepositoryException {

        List<Value> jcrValues = new ArrayList<>();

        if (ContentPropertyType.BINARY.equals(contentProp.getType())) {
            for (Object binaryValue : contentProp.getValuesAsObject()) {
                Value jcrValue = valueConverter.toJcrValue((BinaryValue) binaryValue);
                if (jcrValue != null) {
                    jcrValues.add(jcrValue);
                }
            }
        } else {
            for (String stringValue : contentProp.getValues()) {
                Value jcrValue = valueConverter.toJcrValue(contentProp.getType().toString(), stringValue);
                if (jcrValue != null) {
                    jcrValues.add(jcrValue);
                }
            }
        }

        return jcrValues.toArray(new Value[0]);
    }

    protected boolean isCompoundType(Node node) throws RepositoryException {
        return node.isNodeType(NT_COMPOUND)
                || node.isNodeType(HippoNodeType.NT_MIRROR)
                || node.isNodeType(HippoStdNodeType.NT_HTML)
                || node.isNodeType(NT_IMAGE_LINK);
    }

    protected boolean isProtectedProperty(Property property) throws RepositoryException {
        try {
            return property.getDefinition().isProtected();
        } catch (UnsupportedOperationException ignore) {
            return false;
        }
    }

    private boolean isProtectedProperty(Node jcrDataNode, String propName) throws RepositoryException {
        if (!jcrDataNode.hasProperty(propName)) {
            return false;
        }
        return isProtectedProperty(jcrDataNode.getProperty(propName));
    }

    private ContentNodeBindingItemFilter<ContentItem> resolveFilter(ContentNodeBindingItemFilter<ContentItem> filter) {
        return (filter != null) ? filter : new DefaultContentNodeJcrBindingItemFilter();
    }

    private ContentValueConverter<Value> resolveConverter(Node jcrDataNode, ContentValueConverter<Value> converter)
            throws RepositoryException {
        return (converter != null) ? converter : new DefaultJcrContentValueConverter(jcrDataNode.getSession());
    }

    private void syncPrimaryType(Node jcrDataNode, ContentNode contentNode) throws RepositoryException {
        String targetType = contentNode.getPrimaryType();
        if (StringUtils.isBlank(targetType)) {
            return;
        }
        if (jcrDataNode.getPrimaryNodeType().getName().equals(targetType)) {
            return;
        }
        jcrDataNode.setPrimaryType(targetType);
    }

    private void syncMixinTypes(Node jcrDataNode, ContentNode contentNode) throws RepositoryException {
        for (String mixinType : contentNode.getMixinTypes()) {
            if (!jcrDataNode.isNodeType(mixinType)) {
                jcrDataNode.addMixin(mixinType);
            }
        }
    }

}
