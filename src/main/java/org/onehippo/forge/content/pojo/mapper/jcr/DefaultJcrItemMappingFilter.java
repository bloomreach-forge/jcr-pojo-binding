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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.onehippo.forge.content.pojo.common.util.GlobPattern;
import org.onehippo.forge.content.pojo.mapper.ContentNodeMappingException;
import org.onehippo.forge.content.pojo.mapper.ContentNodeMappingItemFilter;

public class DefaultJcrItemMappingFilter implements ContentNodeMappingItemFilter<Item> {

    private boolean protectedPropertyExcluded;

    private Set<Integer> propertyTypeIncludes;
    private Set<Integer> propertyTypeExcludes;
    private List<String> propertyPathIncludes;
    private List<String> propertyPathExcludes;
    private List<String> nodePathIncludes;
    private List<String> nodePathExcludes;

    private List<Pattern> propertyPathIncludePatterns;
    private List<Pattern> propertyPathExcludePatterns;
    private List<Pattern> nodePathIncludePatterns;
    private List<Pattern> nodePathExcludePatterns;

    public DefaultJcrItemMappingFilter() {
    }

    public boolean isProtectedPropertyExcluded() {
        return protectedPropertyExcluded;
    }

    public void setProtectedPropertyExcluded(boolean protectedPropertyExcluded) {
        this.protectedPropertyExcluded = protectedPropertyExcluded;
    }

    public Set<Integer> getPropertyTypeIncludes() {
        return propertyTypeIncludes;
    }

    public void setPropertyTypeIncludes(Set<Integer> propertyTypeIncludes) {
        this.propertyTypeIncludes = propertyTypeIncludes;
    }

    public void addPropertyTypeInclude(Integer propertyType) {
        if (propertyTypeIncludes == null) {
            propertyTypeIncludes = new HashSet<>();
        }

        propertyTypeIncludes.add(propertyType);
    }

    public Set<Integer> getPropertyTypeExcludes() {
        return propertyTypeExcludes;
    }

    public void setPropertyTypeExcludes(Set<Integer> propertyTypeExcludes) {
        this.propertyTypeExcludes = propertyTypeExcludes;
    }

    public void addPropertyTypeExclude(Integer propertyType) {
        if (propertyTypeExcludes == null) {
            propertyTypeExcludes = new HashSet<>();
        }

        propertyTypeExcludes.add(propertyType);
    }

    public List<String> getPropertyPathIncludes() {
        return propertyPathIncludes;
    }

    public void setPropertyPathIncludes(List<String> propertyPathIncludes) {
        this.propertyPathIncludes = propertyPathIncludes;
    }

    public void addPropertyPathInclude(String propertyPathInclude) {
        if (propertyPathIncludes == null) {
            propertyPathIncludes = new ArrayList<>();
        }

        propertyPathIncludes.add(propertyPathInclude);
    }

    public List<String> getPropertyPathExcludes() {
        return propertyPathExcludes;
    }

    public void setPropertyPathExcludes(List<String> propertyPathExcludes) {
        this.propertyPathExcludes = propertyPathExcludes;
    }

    public void addPropertyPathExclude(String propertyPathExclude) {
        if (propertyPathExcludes == null) {
            propertyPathExcludes = new ArrayList<>();
        }

        propertyPathExcludes.add(propertyPathExclude);
    }

    public List<String> getNodePathIncludes() {
        return nodePathIncludes;
    }

    public void setNodePathIncludes(List<String> nodePathIncludes) {
        this.nodePathIncludes = nodePathIncludes;
    }

    public void addNodePathInclude(String nodePathInclude) {
        if (nodePathIncludes == null) {
            nodePathIncludes = new ArrayList<>();
        }

        nodePathIncludes.add(nodePathInclude);
    }

    public List<String> getNodePathExcludes() {
        return nodePathExcludes;
    }

    public void setNodePathExcludes(List<String> nodePathExcludes) {
        this.nodePathExcludes = nodePathExcludes;
    }

    public void addNodePathExclude(String nodePathExclude) {
        if (nodePathExcludes == null) {
            nodePathExcludes = new ArrayList<>();
        }

        nodePathExcludes.add(nodePathExclude);
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
            if (!isPropertyIncludableByType(property)) {
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

    private boolean isPropertyIncludableByType(final Property property) {
        try {
            final int type = property.getType();

            if (propertyTypeExcludes != null && propertyTypeExcludes.contains(type)) {
                return false;
            }

            if (propertyTypeIncludes != null && !propertyTypeIncludes.isEmpty()) {
                return propertyTypeIncludes.contains(type);
            }

            return true;
        } catch (RepositoryException e) {
            throw new ContentNodeMappingException(e.toString(), e);
        }
    }

    private boolean isPathIncludable(final String path, final Collection<Pattern> includePatterns,
            final Collection<Pattern> excludePatterns) {

        Matcher m;

        if (excludePatterns != null && !excludePatterns.isEmpty()) {
            for (Pattern pattern : excludePatterns) {
                m = pattern.matcher(path);

                if (m.matches()) {
                    return false;
                }
            }
        }

        if (includePatterns != null && !includePatterns.isEmpty()) {
            for (Pattern pattern : includePatterns) {
                m = pattern.matcher(path);

                if (m.matches()) {
                    return true;
                }
            }

            return false;
        }

        return true;
    }

    private List<Pattern> getPropertyIncludePatterns() {
        if (mismatchPatternsFromSources(propertyPathIncludePatterns, propertyPathIncludes)) {
            propertyPathIncludePatterns = new ArrayList<>();
            GlobPattern glob = new GlobPattern();

            for (String include : propertyPathIncludes) {
                propertyPathIncludePatterns.add(glob.compile(include));
            }
        }

        return propertyPathIncludePatterns;
    }

    private List<Pattern> getPropertyExcludePatterns() {
        if (mismatchPatternsFromSources(propertyPathExcludePatterns, propertyPathExcludes)) {
            propertyPathExcludePatterns = new ArrayList<>();
            GlobPattern glob = new GlobPattern();

            for (String include : propertyPathExcludes) {
                propertyPathExcludePatterns.add(glob.compile(include));
            }
        }

        return propertyPathExcludePatterns;
    }

    private List<Pattern> getNodeIncludePatterns() {
        if (mismatchPatternsFromSources(nodePathIncludePatterns, nodePathIncludes)) {
            nodePathIncludePatterns = new ArrayList<>();
            GlobPattern glob = new GlobPattern();

            for (String include : nodePathIncludes) {
                nodePathIncludePatterns.add(glob.compile(include));
            }
        }

        return nodePathIncludePatterns;
    }

    private List<Pattern> getNodeExcludePatterns() {
        if (mismatchPatternsFromSources(nodePathExcludePatterns, nodePathExcludes)) {
            nodePathExcludePatterns = new ArrayList<>();
            GlobPattern glob = new GlobPattern();

            for (String include : nodePathExcludes) {
                nodePathExcludePatterns.add(glob.compile(include));
            }
        }

        return nodePathExcludePatterns;
    }

    private boolean mismatchPatternsFromSources(List<Pattern> patterns, List<String> patternSources) {
        if (patterns == null) {
            if (patternSources != null && !patternSources.isEmpty()) {
                return true;
            }
        } else if (patternSources != null) {
            if (patterns.size() != patternSources.size()) {
                return true;
            }
        }

        return false;
    }

    private boolean isProtectedProperty(final Property property) throws RepositoryException {
        try {
            return property.getDefinition().isProtected();
        } catch (UnsupportedOperationException ignore) {
        }

        return false;
    }
}
