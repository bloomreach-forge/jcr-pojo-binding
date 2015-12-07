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
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class HandleContentNode extends ContentNode {

    private static final long serialVersionUID = 1L;

    private Map<String, ContentNode> renditions;

    public HandleContentNode() {
        super();
    }

    public Map<String, ContentNode> getRenditions() {
        if (renditions == null) {
            return Collections.emptyMap();
        }

        return renditions;
    }

    public void setRenditions(Map<String, ContentNode> renditions) {
        this.renditions = renditions;
    }

    public void putRendition(String renditionName, ContentNode rendition) {
        if (renditions == null) {
            renditions = new LinkedHashMap<>();
        }

        renditions.put(renditionName, rendition);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(renditions).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HandleContentNode)) {
            return false;
        }

        if (!super.equals(o)) {
            return false;
        }

        HandleContentNode that = (HandleContentNode) o;

        if (renditions == null && that.renditions != null) {
            return false;
        }

        return renditions.equals(that.renditions);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("primaryType", getPrimaryType()).append("mixinTypes", getMixinTypes())
                .append("properties", getProperties()).append("nodes", getNodes()).append("renditions", renditions)
                .toString();
    }
}
