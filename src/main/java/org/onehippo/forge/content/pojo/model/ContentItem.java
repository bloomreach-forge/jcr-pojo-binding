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

import java.io.Serializable;



import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 * Serializable POJO abstraction for content item (e.g, {@link javax.jcr.Item}),
 * which should be either {@link ContentNode} or {@link ContentProperty}.
 */
abstract public class ContentItem implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    /**
     * Content item (either {@link ContentNode} or {@link ContentProperty}) name.
     */
    private String name;

    /**
     * Default constructor for deserialization.
     */
    public ContentItem() {
    }

    /**
     * Constructor with item (either {@link ContentNode} or {@link ContentProperty}) name.
     * @param name item name
     */
    public ContentItem(String name) {
        this.name = name;
    }

    /**
     * Returns true if this is a {@link ContentNode}, or false otherwise.
     * @return true if this is a {@link ContentNode}, or false otherwise
     */
    abstract public boolean isNode();

    /**
     * Returns item (either {@link ContentNode} or {@link ContentProperty}) name.
     * @return item (either {@link ContentNode} or {@link ContentProperty}) name
     */
    @XmlElement(name="name")
    public String getName() {
        return name;
    }

    /**
     * Sets item (either {@link ContentNode} or {@link ContentProperty}) name.
     * @param name item (either {@link ContentNode} or {@link ContentProperty}) name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Transient or ignore-able property in JSON marshaling, but this internal property
     * can be used in <a href="https://commons.apache.org/proper/commons-jxpath/">JXPath</a> expressions.
     * @return item (either {@link ContentNode} or {@link ContentProperty}) name
     */
    @JsonIgnore
    @XmlTransient
    public String getItemName() {
        return getName();
    }

}
