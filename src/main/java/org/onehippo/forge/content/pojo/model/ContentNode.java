/*
 *  Copyright 2015-2024 Hippo B.V. (http://www.onehippo.com)
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

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Serializable POJO abstraction for content node (e.g, {@link javax.jcr.Node}).
 */
@XmlRootElement(name = "node")
@XmlType(propOrder={"primaryType", "mixinTypes", "properties", "nodes"})
public class ContentNode extends ContentItem {

    private static final long serialVersionUID = 1L;

    /**
     * Primary content node type.
     */
    private String primaryType;

    /**
     * Mixin content node types.
     */
    private Set<String> mixinTypes;

    /**
     * Content properties embedded in this content node.
     */
    private List<ContentProperty> properties;

    /**
     * Parent content node embdding this content node.
     */
    private ContentNode parent;

    /**
     * Child content nodes embedded in this content node.
     */
    private List<ContentNode> nodes;

    /**
     * Default constructor for deserialization.
     */
    public ContentNode() {
        super();
    }

    /**
     * Constructor with content node name and content node primary type.
     * @param name content node name
     * @param primaryType content node primary type name
     */
    public ContentNode(String name, String primaryType) {
        super(name);
        this.primaryType = primaryType;
    }

    /**
     * Transient or ignore-able (in JSON marshaling) property, always returning true.
     */
    @JsonIgnore
    @XmlTransient
    public boolean isNode() {
        return true;
    }

    /**
     * Returns content node primary type name.
     * @return content node primary type name
     */
    @XmlElement(name = "primaryType")
    public String getPrimaryType() {
        return primaryType;
    }

    /**
     * Sets content node primary type name.
     * @param primaryType content node primary type name
     */
    public void setPrimaryType(String primaryType) {
        this.primaryType = primaryType;
    }

    /**
     * Returns a non-null set of content node mixin type names.
     * @return a non-null set of content node mixin type names
     */
    @XmlElementWrapper(name = "mixinTypes")
    @XmlElements(@XmlElement(name = "mixinType"))
    public Set<String> getMixinTypes() {
        if (mixinTypes == null) {
            mixinTypes = new LinkedHashSet<>();
        }

        return mixinTypes;
    }

    /**
     * Adds a mixin content node type to this content node.
     * @param mixinType mixin content node type name
     */
    public void addMixinType(String mixinType) {
        getMixinTypes().add(mixinType);
    }

    /**
     * Removes a mixin content node type name from this content node.
     * @param mixinType a mixin content node type name
     */
    public void removeMixinType(String mixinType) {
        if (mixinTypes != null && !mixinTypes.isEmpty()) {
            mixinTypes.remove(mixinType);
        }
    }

    /**
     * Returns a non-null list of embedded content properties in this content node.
     * @return a non-null list of embedded content properties in this content node
     */
    @XmlElementWrapper(name = "properties")
    @XmlElements(@XmlElement(name = "property"))
    public List<ContentProperty> getProperties() {
        if (properties == null) {
            properties = new LinkedList<>();
        }

        return properties;
    }

    /**
     * Returns true if this content node has a content property named by the {@code name}.
     * @param name content property name
     * @return true if this content node has a content property named by the {@code name}
     */
    public boolean hasProperty(String name) {
        return getProperty(name) != null;
    }

    /**
     * Finds and return the content property by the {@code name}.
     * @param name content property name
     * @return the content property by the {@code name}
     */
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

    /**
     * Sets a content property by {@link ContentProperty} instance.
     * @param property content property
     */
    public void setProperty(ContentProperty property) {
        if (!getProperties().isEmpty()) {
            int index = 0;

            for (ContentProperty contentProp : getProperties()) {
                if (contentProp.getName().equals(property.getName())) {
                    properties.set(index, property);
                    return;
                }
                ++index;
            }
        }

        getProperties().add(property);
    }

    /**
     * Sets a {@link ContentPropertyType#STRING} typed content property having the {@code name} to the {@code value}. 
     * @param name content property name
     * @param value string content property value
     */
    public void setProperty(String name, String value) {
        setProperty(name, ContentPropertyType.STRING, value);
    }

    /**
     * Sets a content property of {@code type} to the stringified {@code value}.
     * @param name content property name
     * @param type content property type
     * @param value stringified content property value
     */
    public void setProperty(String name, ContentPropertyType type, String value) {
        ContentProperty prop = new ContentProperty(name, type);
        prop.setValue(value);
        setProperty(prop);
    }

    /**
     * Sets a content property of a string array type to {@code values}.
     * @param name content property name
     * @param values string array of property values
     */
    public void setProperty(String name, String[] values) {
        setProperty(name, ContentPropertyType.STRING, values);
    }

    /**
     * Sets a content property having {@code name} of {@code type} to the stringified {@code values}.
     * @param name content property name
     * @param type content property type
     * @param values stringified property value array
     */
    public void setProperty(String name, ContentPropertyType type, String[] values) {
        ContentProperty prop = new ContentProperty(name, type, true);

        if (values != null) {
            for (String value : values) {
                prop.addValue(value);
            }
        }

        setProperty(prop);
    }

    /**
     * Sets a property having {@code name} to the given {@link BinaryValue} object.
     * @param name content property name
     * @param binaryValue {@link BinaryValue} object
     */
    public void setProperty(String name, BinaryValue binaryValue) {
        ContentProperty prop = new ContentProperty(name, ContentPropertyType.BINARY);
        prop.setValue(binaryValue);
        setProperty(prop);
    }

    /**
     * Sets a property having {@code name} to the given array of {@link BinaryValue} objects.
     * @param name content property name
     * @param binaryValues array of {@link BinaryValue} objects
     */
    public void setProperty(String name, BinaryValue[] binaryValues) {
        ContentProperty prop = new ContentProperty(name, ContentPropertyType.BINARY, true);

        if (binaryValues != null) {
            for (BinaryValue binaryValue : binaryValues) {
                prop.addValue(binaryValue);
            }
        }

        setProperty(prop);
    }

    @XmlTransient
    @JsonIgnore
    public ContentNode getParent() {
        return parent;
    }

    /**
     * Returns a non-null child content nodes.
     * @return a non-null child content nodes
     */
    @XmlElementWrapper(name = "nodes")
    @XmlElements(@XmlElement(name = "node"))
    public List<ContentNode> getNodes() {
        if (nodes == null) {
            nodes = new LinkedList<>();
        }

        return nodes;
    }

    public boolean hasAnyNode() {
        return (nodes != null && !nodes.isEmpty());
    }

    /**
     * Returns true if this content node has any child content node having the {@code name}.
     * @param name child content node name
     * @return true if this content node has any child content node having the {@code name}
     */
    public boolean hasNode(String name) {
        return getNode(name) != null;
    }

    /**
     * Returns the child content node having the {@code name} if existing. Null otherwise.
     * @param name child content node name
     * @return the child content node having the {@code name} if existing. Null otherwise.
     */
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

    /**
     * Adds a child content node.
     * @param node child content node
     */
    public void addNode(ContentNode node) {
        if (nodes == null) {
            nodes = new LinkedList<>();
        }

        nodes.add(node);
        node.parent = this;
    }

    /**
     * Return the index of this content node within the ordered set of its
     * same-name sibling content nodes.Note that the index always starts at 1 (not 0).
     * As a result, for content nodes that do not have same-name-siblings, this method will always return 1.
     * @return The index of this content node within the ordered set of its same-name
     *         sibling content nodes.
     */
    @XmlTransient
    @JsonIgnore
    public int getIndex() {
        if (parent != null && parent.hasAnyNode()) {
            int index = 0;

            for (ContentNode sibling : parent.getNodes()) {
                if (sibling == this) {
                    return ++index;
                } else if (getName().equals(sibling.getName())) {
                    ++index;
                }
            }
        }

        return 1;
    }

    /**
     * Queries and returns single content node from this base content node by
     * the <a href="https://commons.apache.org/proper/commons-jxpath/">JXPath</a> expression ({@code jxpath}).
     * @param jxpath <a href="https://commons.apache.org/proper/commons-jxpath/">JXPath</a> expression
     * @return single content node from this base content node from the JXPath query execution
     */
    public ContentNode queryNodeByXPath(String jxpath) {
        return (ContentNode) queryObjectByXPath(jxpath);
    }

    /**
     * Queries and returns single content property from this base content node by
     * the <a href="https://commons.apache.org/proper/commons-jxpath/">JXPath</a> expression ({@code jxpath}).
     * @param jxpath <a href="https://commons.apache.org/proper/commons-jxpath/">JXPath</a> expression
     * @return single content property from this base content node from the JXPath query execution
     */
    public ContentProperty queryPropertyByXPath(String jxpath) {
        return (ContentProperty) queryObjectByXPath(jxpath);
    }

    /**
     * Queries and returns a list of content nodes from this base content node by
     * the <a href="https://commons.apache.org/proper/commons-jxpath/">JXPath</a> expression ({@code jxpath}).
     * @param jxpath <a href="https://commons.apache.org/proper/commons-jxpath/">JXPath</a> expression
     * @return list of content nodes from this base content node from the JXPath query execution
     */
    public List<ContentNode> queryNodesByXPath(String jxpath) {
        return (List<ContentNode>) queryObjectsByXPath(jxpath);
    }

    /**
     * Queries and returns a list of content properties from this base content node by
     * the <a href="https://commons.apache.org/proper/commons-jxpath/">JXPath</a> expression ({@code jxpath}).
     * @param jxpath <a href="https://commons.apache.org/proper/commons-jxpath/">JXPath</a> expression
     * @return list of content properties from this base content node from the JXPath query execution
     */
    public List<ContentProperty> queryPropertiesByXPath(String jxpath) {
        return (List<ContentProperty>) queryObjectsByXPath(jxpath);
    }

    /**
     * Queries and returns an object from this base content node by
     * the <a href="https://commons.apache.org/proper/commons-jxpath/">JXPath</a> expression ({@code jxpath}).
     * @param jxpath <a href="https://commons.apache.org/proper/commons-jxpath/">JXPath</a> expression
     * @return an object from this base content node from the JXPath query execution
     */
    public Object queryObjectByXPath(String jxpath) {
        return createJXPathContext().getValue(jxpath);
    }

    /**
     * Queries and returns a list of objects from this base content node by
     * the <a href="https://commons.apache.org/proper/commons-jxpath/">JXPath</a> expression ({@code jxpath}).
     * @param jxpath <a href="https://commons.apache.org/proper/commons-jxpath/">JXPath</a> expression
     * @return a list of objects from this base content node from the JXPath query execution
     */
    public List<?> queryObjectsByXPath(String jxpath) {
        return createJXPathContext().selectNodes(jxpath);
    }

    /**
     * Creates and returns a {@link JXPathContext} instance used by default.
     * @return a {@link JXPathContext} instance used by default
     */
    private JXPathContext createJXPathContext() {
        JXPathContext jxpathCtx = JXPathContext.newContext(this);
        jxpathCtx.setLenient(true);
        return jxpathCtx;
    }

    /**
     * Deep-clone this content node object.
     * @return deep-cloned content node object
     */
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
        return new HashCodeBuilder().append(getName()).append(primaryType).append(mixinTypes).append(properties)
                .append(nodes).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ContentNode)) {
            return false;
        }

        ContentNode that = (ContentNode) o;

        if (!StringUtils.equals(getName(), that.getName())) {
            return false;
        }

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
            if (!ListUtils.isEqualList(properties, that.properties)) {
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
        return new ToStringBuilder(this).append("name", getName()).append("primaryType", primaryType)
                .append("mixinTypes", mixinTypes).append("properties", properties).append("nodes", nodes).toString();
    }
}
