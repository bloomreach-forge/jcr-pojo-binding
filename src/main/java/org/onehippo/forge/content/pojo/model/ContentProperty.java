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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.jackrabbit.util.ISO8601;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Serializable POJO abstraction for a content property (e.g, {@link javax.jcr.Property}).
 */
@XmlRootElement(name = "property")
public class ContentProperty extends ContentItem {

    private static final long serialVersionUID = 1L;

    /**
     * Type of content property.
     */
    private ContentPropertyType type = ContentPropertyType.UNDEFINED;

    /**
     * Whether or not this property is for multiple values.
     */
    private boolean multiple;

    /**
     * List of stringified values.
     */
    private List<String> values;

    /**
     * Default constructor for deserialization.
     */
    public ContentProperty() {
        super();
    }

    /**
     * Constructor with content property name and content property type.
     * @param name content property name
     * @param type content property type
     */
    public ContentProperty(String name, ContentPropertyType type) {
        this(name, type, false);
    }

    /**
     * Constructor with content property name, content property type and flag for multiplicity of its value(s).
     * @param name content property name
     * @param type content property type
     * @param multiple whether or not this property is for multiple values
     */
    public ContentProperty(String name, ContentPropertyType type, boolean multiple) {
        super(name);
        this.type = type;
        this.multiple = multiple;
    }

    /**
     * Transient or ignore-able (in JSON marshaling) property, always returning false.
     */
    @JsonIgnore
    @XmlTransient
    public boolean isNode() {
        return false;
    }

    /**
     * Returns content property type.
     * @return content property type
     */
    @XmlElement(name = "type")
    public ContentPropertyType getType() {
        return type;
    }

    /**
     * Sets content property type.
     * @param type content property type
     */
    public void setType(ContentPropertyType type) {
        this.type = type;
    }

    /**
     * Gets multiplicity of its value(s).
     * @return true if this content property can have multiple values.
     */
    @XmlElement(name = "multiple")
    public boolean isMultiple() {
        return multiple;
    }

    /**
     * Sets multiplicity of its value(s).
     * @param multiple multiplicity of its value(s)
     */
    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    /**
     * Returns a non-null list of stringified values.
     * @return a non-null list of stringified values
     */
    @XmlElementWrapper(name = "values")
    @XmlElements(@XmlElement(name = "value"))
    public List<String> getValues() {
        if (values == null) {
            values = new LinkedList<>();
        }

        return values;
    }

    /**
     * Returns the first stringified value.
     * @return the first stringified value
     */
    @JsonIgnore
    @XmlTransient
    public String getValue() {
        if (values != null && !values.isEmpty()) {
            return values.get(0);
        }

        return null;
    }

    /**
     * Sets single stringified value to this content property.
     * @param value single stringified value
     */
    public void setValue(String value) {
        if (!getValues().isEmpty()) {
            getValues().clear();
        }

        if (value != null) {
            getValues().add(value);
        }
    }

    /**
     * Sets single {@link BinaryValue} to this content property.
     * @param binaryValue {@link BinaryValue} value
     */
    public void setValue(BinaryValue binaryValue) {
        try {
            setValue(binaryValue.toUriString());
        } catch (IOException e) {
            throw new IllegalArgumentException(e.toString(), e);
        }
    }

    /**
     * Adds a stringified value to this content property.
     * @param value a stringified value
     */
    public void addValue(String value) {
        getValues().add(value);
    }

    /**
     * Adds all the items of the given array of the stringified values to this content property.
     * @param values array of the stringified values
     */
    public void addValues(String ... values) {
        if (values != null && values.length > 0) {
            for (String value : values) {
                addValue(value);
            }
        }
    }

    /**
     * Adds a {@link BinaryValue} value to this content property.
     * @param binaryValue a {@link BinaryValue} value
     */
    public void addValue(BinaryValue binaryValue) {
        try {
            addValue(binaryValue.toUriString());
        } catch (IOException e) {
            throw new IllegalArgumentException(e.toString(), e);
        }
    }

    /**
     * Adds all the items of the given array of {@link BinaryValue} values to this content property.
     * @param binaryValues array of {@link BinaryValue} values
     */
    public void addValues(BinaryValue ... binaryValues) {
        if (binaryValues != null && binaryValues.length > 0) {
            for (BinaryValue binaryValue : binaryValues) {
                addValue(binaryValue);
            }
        }
    }

    /**
     * Remove all the values from this content property.
     */
    public void removeValues() {
        if (values != null) {
            values.clear();
        }
    }

    /**
     * Returns the count of the values in this content property.
     * @return the count of the values in this content property
     */
    @JsonIgnore
    @XmlTransient
    public int getValueCount() {
        return values == null ? 0 : values.size();
    }

    /**
     * Converts the internally stored stringified values to a list of native Java objects
     * such as {@link String}, {@link Calendar}, {@link Boolean}, {@link Long}, {@link Double}, {@link BigDecimal} and {@link BinaryValue}
     * based on the {@link #getType()} value of this content property.
     * @return list of converted native Java objects from the internal stringified values based on content property type
     */
    @JsonIgnore
    @XmlTransient
    public List<Object> getValuesAsObject() {
        List<Object> objectValues = new LinkedList<>();
        int valueCount = getValueCount();

        for (int i = 0; i < valueCount; i++) {
            objectValues.add(getValueAsObjectAt(i));
        }

        return objectValues;
    }

    /**
     * Converts the first internally stored stringified value to a native Java object
     * such as {@link String}, {@link Calendar}, {@link Boolean}, {@link Long}, {@link Double}, {@link BigDecimal} and {@link BinaryValue}
     * based on the {@link #getType()} value of this content property.
     * Or returns null if there's no value.
     * @return converted native Java object from the first internal stringified value based on content property type. Null if there's no value.
     */
    @JsonIgnore
    @XmlTransient
    public Object getValueAsObject() {
        if (values != null && !values.isEmpty()) {
            return getValueAsObjectAt(0);
        }

        return null;
    }

    /**
     * Converts the internally stored stringified value at the value {@code index} to a native Java object
     * such as {@link String}, {@link Calendar}, {@link Boolean}, {@link Long}, {@link Double}, {@link BigDecimal} and {@link BinaryValue}
     * based on the {@link #getType()} value of this content property.
     * @param index value index
     * @return converted native Java object from the internal stringified value based on content property type
     */
    private Object getValueAsObjectAt(final int index) {
        Object objectValue = null;

        final String stringifiedValue = values.get(index);

        switch (type) {
        case STRING: {
            objectValue = stringifiedValue;
            break;
        }
        case DATE: {
            objectValue = ISO8601.parse(stringifiedValue);
            break;
        }
        case BOOLEAN: {
            objectValue = Boolean.parseBoolean(stringifiedValue);
            break;
        }
        case LONG: {
            objectValue = Long.parseLong(stringifiedValue);
            break;
        }
        case DOUBLE: {
            objectValue = Double.parseDouble(stringifiedValue);
            break;
        }
        case DECIMAL: {
            objectValue = new BigDecimal(stringifiedValue);
            break;
        }
        case BINARY: {
            objectValue = createBinaryValue(stringifiedValue);
            break;
        }
        default: {
            throw new UnsupportedOperationException(
                    "Unsupported type, " + type + ". Only primitive number/string values are currently supported.");
        }
        }

        return objectValue;
    }

    /**
     * Creates a {@link BinaryValue} instance from the given stringified value
     * which can be either a <code>data:</code> URL or any other external URL to be read.
     * @param stringifiedValue stringfieid binary value, either a <code>data:</code> URL or any other external URL to be read
     * @return a {@link BinaryValue} instance
     */
    private BinaryValue createBinaryValue(String stringifiedValue) {
        BinaryValue binaryValue = null;

        if (StringUtils.startsWith(stringifiedValue, "data:")) {
            binaryValue = BinaryValue.fromDataURI(stringifiedValue);
        } else {
            try {
                FileSystemManager fsManager = VFS.getManager();
                FileObject fileObject = fsManager.resolveFile(stringifiedValue);
                binaryValue = new BinaryValue(fileObject);
            } catch (FileSystemException e) {
                throw new IllegalArgumentException("Unresolvable VFS url.");
            }
        }

        return binaryValue;
    }

    /**
     * Deep-clone this content property.
     * @return deep-cloned content property instance
     */
    @Override
    public Object clone() {
        ContentProperty clone = new ContentProperty(getName(), type, multiple);

        if (values != null) {
            for (String value : values) {
                clone.addValue(value);
            }
        }

        return clone;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getName()).append(type).append(multiple).append(values).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ContentProperty)) {
            return false;
        }

        ContentProperty that = (ContentProperty) o;

        if (!StringUtils.equals(getName(), that.getName())) {
            return false;
        }

        if (!type.equals(that.type)) {
            return false;
        }

        if (multiple != that.multiple) {
            return false;
        }

        return ListUtils.isEqualList(values, that.values);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("type", type).append("multiple", multiple).append("values", values)
                .toString();
    }

}
