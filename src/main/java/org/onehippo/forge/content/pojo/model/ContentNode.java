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

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

@XmlRootElement(name="node")
public class ContentNode extends ContentItem {

    private static final long serialVersionUID = 1L;

    private String primaryType;

    private Set<String> mixinTypes;

    private List<ContentProperty> properties;

    private List<ContentNode> nodes;

    public ContentNode() {
        super();
    }

    public ContentNode(String name, String primaryType) {
        super(name);
        this.primaryType = primaryType;
    }

    @JsonIgnore
    @XmlTransient
    public boolean isNode() {
        return true;
    }

    @XmlElement(name="primaryType")
    public String getPrimaryType() {
        return primaryType;
    }

    @XmlElementWrapper(name="mixinTypes")
    @XmlElements(@XmlElement(name="mixinType"))
    public Set<String> getMixinTypes() {
        if (mixinTypes == null) {
            mixinTypes = new LinkedHashSet<>();
        }

        return mixinTypes;
    }

    public void addMixinType(String mixinType) {
        if (mixinTypes == null) {
            mixinTypes = new LinkedHashSet<>();
        }

        mixinTypes.add(mixinType);
    }

    public void removeMixinType(String mixinType) {
        if (mixinTypes != null && !mixinTypes.isEmpty()) {
            mixinTypes.remove(mixinType);
        }
    }

    @XmlElementWrapper(name="properties")
    @XmlElements(@XmlElement(name="property"))
    public List<ContentProperty> getProperties() {
        if (properties == null) {
            properties = new LinkedList<>();
        }

        return properties;
    }

    public boolean hasProperty(String name) {
        return getProperty(name) != null;
    }

    public ContentProperty getProperty(String name) {
        if (properties != null) {
            for (ContentProperty contentProp : properties) {
                if (contentProp.getName().equals(name)) {
                    return contentProp;
                }
            }
        }

        return null;
    }

    public void setProperty(ContentProperty property) {
        if (properties == null) {
            properties = new LinkedList<>();
        }

        if (!properties.isEmpty()) {
            int size = properties.size();
            ContentProperty contentProp;

            for (int i = 0; i < size; i++) {
                contentProp = properties.get(i);

                if (contentProp.getName().equals(property.getName())) {
                    properties.set(i, property);
                    return;
                }
            }
        }

        properties.add(property);
    }

    public void setProperty(String name, String value) {
        setProperty(name, ContentPropertyType.STRING, value);
    }

    public void setProperty(String name, ContentPropertyType type, String value) {
        ContentProperty prop = new ContentProperty(name, type);
        prop.setValue(value);
        setProperty(prop);
    }

    public void setProperty(String name, String [] values) {
        setProperty(name, ContentPropertyType.STRING, values);
    }

    public void setProperty(String name, ContentPropertyType type, String [] values) {
        ContentProperty prop = new ContentProperty(name, type, true);

        if (values != null) {
            for (String value : values) {
                prop.addValue(value);
            }
        }

        setProperty(prop);
    }

    public void setProperty(String name, BinaryValue binaryValue) {
        ContentProperty prop = new ContentProperty(name, ContentPropertyType.BINARY);
        prop.setValue(binaryValue);
        setProperty(prop);
    }

    public void setProperty(String name, BinaryValue [] binaryValues) {
        ContentProperty prop = new ContentProperty(name, ContentPropertyType.BINARY, true);

        if (binaryValues != null) {
            for (BinaryValue binaryValue : binaryValues) {
                prop.addValue(binaryValue);
            }
        }

        setProperty(prop);
    }

    @XmlElementWrapper(name="nodes")
    @XmlElements(@XmlElement(name="node"))
    public List<ContentNode> getNodes() {
        if (nodes == null) {
            nodes = new LinkedList<>();
        }

        return nodes;
    }

    public boolean hasNode(String name) {
        return getNode(name) != null;
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

    public void addNode(ContentNode node) {
        if (nodes == null) {
            nodes = new LinkedList<>();
        }

        nodes.add(node);
    }

    public ContentNode queryNodeByXPath(String xpath) {
        return (ContentNode) queryObjectByXPath(xpath);
    }

    public ContentProperty queryPropertyByXPath(String xpath) {
        return (ContentProperty) queryObjectByXPath(xpath);
    }

    public List<ContentNode> queryNodesByXPath(String xpath) {
        return (List<ContentNode>) queryObjectsByXPath(xpath);
    }

    public List<ContentProperty> queryPropertiesByXPath(String xpath) {
        return (List<ContentProperty>) queryObjectsByXPath(xpath);
    }

    public Object queryObjectByXPath(String xpath) {
        return createJXPathContext().getValue(xpath);
    }

    public List<?> queryObjectsByXPath(String xpath) {
        return createJXPathContext().selectNodes(xpath);
    }

    private JXPathContext createJXPathContext() {
        JXPathContext jxpathCtx = JXPathContext.newContext(this);
        jxpathCtx.setLenient(true);
        return jxpathCtx;
    }

    @Override
    public Object clone() {
        ContentNode clone = new ContentNode(getName(), primaryType);

        if (mixinTypes != null) {
            for (String mixinType : mixinTypes) {
                clone.addMixinType(mixinType);
            }
        }

        if (properties != null) {
            for (ContentProperty contentProp : properties) {
                clone.setProperty((ContentProperty) contentProp.clone());
            }
        }

        if (nodes != null) {
            for (ContentNode node : nodes) {
                clone.addNode((ContentNode) node.clone());
            }
        }

        return clone;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(primaryType).append(mixinTypes).append(properties).append(nodes)
                .toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ContentNode)) {
            return false;
        }

        ContentNode that = (ContentNode) o;

        if (!StringUtils.equals(primaryType, that.primaryType)) {
            return false;
        }

        if (!SetUtils.isEqualSet(mixinTypes, that.mixinTypes)) {
            return false;
        }

        if (properties == null) {
            if (that.properties != null) {
                return false;
            }
        } else {
            if (!properties.equals(that.properties)) {
                return false;
            }
        }

        if (!ListUtils.isEqualList(nodes, that.nodes)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("primaryType", primaryType).append("mixinTypes", mixinTypes)
                .append("properties", properties).append("nodes", nodes).toString();
    }
}
