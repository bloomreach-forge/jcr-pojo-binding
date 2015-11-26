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
package org.onehippo.forge.content.pojo.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ContentNode extends ContentItem {

    private static final long serialVersionUID = 1L;

    private String primaryType;

    private Set<String> mixinTypes;

    private Set<ContentProperty> properties;

    private List<ContentNode> nodes;

    public ContentNode() {
        super();
    }

    public String getPrimaryType() {
        return primaryType;
    }

    public void setPrimaryType(String primaryType) {
        this.primaryType = primaryType;
    }

    public Set<String> getMixinTypes() {
        if (mixinTypes == null) {
            return Collections.emptySet();
        }

        return mixinTypes;
    }

    public void setMixinTypes(Set<String> mixinTypes) {
        this.mixinTypes = mixinTypes;
    }

    public void addMixinType(String mixinType) {
        if (mixinTypes == null) {
            mixinTypes = new LinkedHashSet<>();
        }

        mixinTypes.add(mixinType);
    }

    public Set<ContentProperty> getProperties() {
        if (properties == null) {
            return Collections.emptySet();
        }

        return properties;
    }

    public ContentProperty getProperty(String name) {
        if (properties == null) {
            return null;
        }

        for (ContentProperty prop : properties) {
            if (prop.getName().equals(name)) {
                return prop;
            }
        }

        return null;
    }

    public void setProperties(Set<ContentProperty> properties) {
        this.properties = properties;
    }

    public void addProperty(ContentProperty property) {
        if (properties == null) {
            properties = new LinkedHashSet<>();
        }

        properties.add(property);
    }

    public List<ContentNode> getNodes() {
        if (nodes == null) {
            return Collections.emptyList();
        }

        return nodes;
    }

    public ContentNode getNode(String name) {
        if (nodes != null) {
            for (ContentNode node : nodes) {
                if (node.getName().equals(name)) {
                    return node;
                }
            }
        }

        return null;
    }

    public List<ContentNode> getNodes(String name) {
        List<ContentNode> filteredNodes = null;

        if (nodes != null) {
            for (ContentNode node : nodes) {
                if (node.getName().equals(name)) {
                    if (filteredNodes == null) {
                        filteredNodes = new LinkedList<>();
                    }

                    filteredNodes.add(node);
                }
            }
        }

        if (filteredNodes == null) {
            return Collections.emptyList();
        }

        return filteredNodes;
    }

    public void setNodes(List<ContentNode> nodes) {
        this.nodes = nodes;
    }

    public void addNode(ContentNode node) {
        if (nodes == null) {
            nodes = new LinkedList<>();
        }

        nodes.add(node);
    }
}