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
package org.onehippo.forge.content.pojo.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.onehippo.forge.content.pojo.common.util.GlobPattern;

public class BasePathBasedContentNodeItemFilter {

    private Set<String> propertyTypeNameIncludes;
    private Set<String> propertyTypeNameExcludes;
    private List<String> propertyPathIncludes;
    private List<String> propertyPathExcludes;
    private List<String> nodePathIncludes;
    private List<String> nodePathExcludes;

    private List<Pattern> propertyPathIncludePatterns;
    private List<Pattern> propertyPathExcludePatterns;
    private List<Pattern> nodePathIncludePatterns;
    private List<Pattern> nodePathExcludePatterns;

    public Set<String> getPropertyTypeNameIncludes() {
        return propertyTypeNameIncludes;
    }

    public void setPropertyTypeNameIncludes(Set<String> propertyTypeNameIncludes) {
        this.propertyTypeNameIncludes = propertyTypeNameIncludes;
    }

    public void addPropertyTypeNameInclude(String propertyTypeName) {
        if (propertyTypeNameIncludes == null) {
            propertyTypeNameIncludes = new HashSet<>();
        }

        propertyTypeNameIncludes.add(propertyTypeName);
    }

    public Set<String> getPropertyTypeNameExcludes() {
        return propertyTypeNameExcludes;
    }

    public void setPropertyTypeNameExcludes(Set<String> propertyTypeNameExcludes) {
        this.propertyTypeNameExcludes = propertyTypeNameExcludes;
    }

    public void addPropertyTypeNameExclude(String propertyTypeName) {
        if (propertyTypeNameExcludes == null) {
            propertyTypeNameExcludes = new HashSet<>();
        }

        propertyTypeNameExcludes.add(propertyTypeName);
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

    protected boolean isPropertyIncludableByType(final String typeName) {
        if (propertyTypeNameExcludes != null && propertyTypeNameExcludes.contains(typeName)) {
            return false;
        }

        if (propertyTypeNameIncludes != null && !propertyTypeNameIncludes.isEmpty()) {
            return propertyTypeNameIncludes.contains(typeName);
        }

        return true;
    }

    protected boolean isPathIncludable(final String path, final Collection<Pattern> includePatterns,
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

    protected List<Pattern> getPropertyIncludePatterns() {
        if (mismatchPatternsFromSources(propertyPathIncludePatterns, propertyPathIncludes)) {
            propertyPathIncludePatterns = new ArrayList<>();
            GlobPattern glob = new GlobPattern();

            for (String include : propertyPathIncludes) {
                propertyPathIncludePatterns.add(glob.compile(include));
            }
        }

        return propertyPathIncludePatterns;
    }

    protected List<Pattern> getPropertyExcludePatterns() {
        if (mismatchPatternsFromSources(propertyPathExcludePatterns, propertyPathExcludes)) {
            propertyPathExcludePatterns = new ArrayList<>();
            GlobPattern glob = new GlobPattern();

            for (String include : propertyPathExcludes) {
                propertyPathExcludePatterns.add(glob.compile(include));
            }
        }

        return propertyPathExcludePatterns;
    }

    protected List<Pattern> getNodeIncludePatterns() {
        if (mismatchPatternsFromSources(nodePathIncludePatterns, nodePathIncludes)) {
            nodePathIncludePatterns = new ArrayList<>();
            GlobPattern glob = new GlobPattern();

            for (String include : nodePathIncludes) {
                nodePathIncludePatterns.add(glob.compile(include));
            }
        }

        return nodePathIncludePatterns;
    }

    protected List<Pattern> getNodeExcludePatterns() {
        if (mismatchPatternsFromSources(nodePathExcludePatterns, nodePathExcludes)) {
            nodePathExcludePatterns = new ArrayList<>();
            GlobPattern glob = new GlobPattern();

            for (String include : nodePathExcludes) {
                nodePathExcludePatterns.add(glob.compile(include));
            }
        }

        return nodePathExcludePatterns;
    }

    protected boolean mismatchPatternsFromSources(List<Pattern> patterns, List<String> patternSources) {
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
