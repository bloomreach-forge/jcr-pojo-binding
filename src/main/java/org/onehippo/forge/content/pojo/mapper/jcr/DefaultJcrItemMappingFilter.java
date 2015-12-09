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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.onehippo.forge.content.pojo.common.jcr.JcrContentUtils;
import org.onehippo.forge.content.pojo.common.util.GlobPattern;
import org.onehippo.forge.content.pojo.mapper.ContentNodeMappingException;
import org.onehippo.forge.content.pojo.mapper.ContentNodeMappingItemFilter;

public class DefaultJcrItemMappingFilter implements ContentNodeMappingItemFilter<Item> {

    private boolean protectedPropertyExcluded;

    private List<String> propertyIncludes;
    private List<String> propertyExcludes;
    private List<String> nodeIncludes;
    private List<String> nodeExcludes;

    private List<Pattern> propertyIncludePatterns;
    private List<Pattern> propertyExcludePatterns;
    private List<Pattern> nodeIncludePatterns;
    private List<Pattern> nodeExcludePatterns;

    public DefaultJcrItemMappingFilter() {
    }

    public boolean isProtectedPropertyExcluded() {
        return protectedPropertyExcluded;
    }

    public void setProtectedPropertyExcluded(boolean protectedPropertyExcluded) {
        this.protectedPropertyExcluded = protectedPropertyExcluded;
    }

    public List<String> getPropertyIncludes() {
        return propertyIncludes;
    }

    public void setPropertyIncludes(List<String> propertyIncludes) {
        this.propertyIncludes = propertyIncludes;
    }

    public void addPropertyInclude(String propertyInclude) {
        if (propertyIncludes == null) {
            propertyIncludes = new ArrayList<>();
        }

        propertyIncludes.add(propertyInclude);
    }

    public List<String> getPropertyExcludes() {
        return propertyExcludes;
    }

    public void setPropertyExcludes(List<String> propertyExcludes) {
        this.propertyExcludes = propertyExcludes;
    }

    public void addPropertyExclude(String propertyExclude) {
        if (propertyExcludes == null) {
            propertyExcludes = new ArrayList<>();
        }

        propertyExcludes.add(propertyExclude);
    }

    public List<String> getNodeIncludes() {
        return nodeIncludes;
    }

    public void setNodeIncludes(List<String> nodeIncludes) {
        this.nodeIncludes = nodeIncludes;
    }

    public void addNodeInclude(String nodeInclude) {
        if (nodeIncludes == null) {
            nodeIncludes = new ArrayList<>();
        }

        nodeIncludes.add(nodeInclude);
    }

    public List<String> getNodeExcludes() {
        return nodeExcludes;
    }

    public void setNodeExcludes(List<String> nodeExcludes) {
        this.nodeExcludes = nodeExcludes;
    }

    public void addNodeExclude(String nodeExclude) {
        if (nodeExcludes == null) {
            nodeExcludes = new ArrayList<>();
        }

        nodeExcludes.add(nodeExclude);
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
            if (!isPathIncludable(property.getName(), getPropertyIncludePatterns(), getPropertyExcludePatterns())) {
                return false;
            }

            if (isProtectedPropertyExcluded() && JcrContentUtils.isProtected(property)) {
                return false;
            }
        } catch (RepositoryException e) {
            throw new ContentNodeMappingException(e.toString(), e);
        }

        return true;
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
        if (mismatchPatternsFromSources(propertyIncludePatterns, propertyIncludes)) {
            propertyIncludePatterns = new ArrayList<>();
            GlobPattern glob = new GlobPattern();

            for (String include : propertyIncludes) {
                propertyIncludePatterns.add(glob.compile(include));
            }
        }

        return propertyIncludePatterns;
    }

    private List<Pattern> getPropertyExcludePatterns() {
        if (mismatchPatternsFromSources(propertyExcludePatterns, propertyExcludes)) {
            propertyExcludePatterns = new ArrayList<>();
            GlobPattern glob = new GlobPattern();

            for (String include : propertyExcludes) {
                propertyExcludePatterns.add(glob.compile(include));
            }
        }

        return propertyExcludePatterns;
    }

    private List<Pattern> getNodeIncludePatterns() {
        if (mismatchPatternsFromSources(nodeIncludePatterns, nodeIncludes)) {
            nodeIncludePatterns = new ArrayList<>();
            GlobPattern glob = new GlobPattern();

            for (String include : nodeIncludes) {
                nodeIncludePatterns.add(glob.compile(include));
            }
        }

        return nodeIncludePatterns;
    }

    private List<Pattern> getNodeExcludePatterns() {
        if (mismatchPatternsFromSources(nodeExcludePatterns, nodeExcludes)) {
            nodeExcludePatterns = new ArrayList<>();
            GlobPattern glob = new GlobPattern();

            for (String include : nodeExcludes) {
                nodeExcludePatterns.add(glob.compile(include));
            }
        }

        return nodeExcludePatterns;
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
}
