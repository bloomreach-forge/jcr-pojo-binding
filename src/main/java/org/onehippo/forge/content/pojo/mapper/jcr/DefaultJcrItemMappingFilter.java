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
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.onehippo.forge.content.pojo.common.BasePathBasedContentNodeItemFilter;
import org.onehippo.forge.content.pojo.mapper.ContentNodeMappingException;
import org.onehippo.forge.content.pojo.mapper.ContentNodeMappingItemFilter;

/**
 * Default {@link ContentNodeMappingItemFilter} implementation for JCR {@link javax.jcr.Item},
 * based on basic name/path based includes/excludes filtering.
 */
public class DefaultJcrItemMappingFilter extends BasePathBasedContentNodeItemFilter
        implements ContentNodeMappingItemFilter<Item> {

    private boolean protectedPropertyExcluded;

    public DefaultJcrItemMappingFilter() {
    }

    public boolean isProtectedPropertyExcluded() {
        return protectedPropertyExcluded;
    }

    public void setProtectedPropertyExcluded(boolean protectedPropertyExcluded) {
        this.protectedPropertyExcluded = protectedPropertyExcluded;
    }

    @Override
    public boolean accept(Item item) throws ContentNodeMappingException {
        if (item.isNode()) {
            return acceptNode((Node) item);
        } else {
            return acceptProperty((Property) item);
        }
    }

    protected boolean acceptNode(Node node) throws ContentNodeMappingException {
        try {
            if (!isPathIncludable(node.getName(), getNodeIncludePatterns(), getNodeExcludePatterns())) {
                return false;
            }

            return true;
        } catch (RepositoryException e) {
            throw new ContentNodeMappingException(e.toString(), e);
        }
    }

    protected boolean acceptProperty(Property property) throws ContentNodeMappingException {
        try {
            if (!isPropertyIncludableByType(PropertyType.nameFromValue(property.getType()))) {
                return false;
            }

            if (!isPathIncludable(property.getName(), getPropertyIncludePatterns(), getPropertyExcludePatterns())) {
                return false;
            }

            if (isProtectedPropertyExcluded() && isProtectedProperty(property)) {
                return false;
            }

            return true;
        } catch (RepositoryException e) {
            throw new ContentNodeMappingException(e.toString(), e);
        }
    }

    private boolean isProtectedProperty(final Property property) throws RepositoryException {
        try {
            return property.getDefinition().isProtected();
        } catch (UnsupportedOperationException ignore) {
        }

        return false;
    }
}
