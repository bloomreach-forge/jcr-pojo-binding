/*
 *  Copyright 2015-2025 Bloomreach (http://www.bloomreach.com)
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
package org.onehippo.forge.content.pojo.binder.jcr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Index structure for efficient O(1) lookups by name and type.
 * Used internally by {@link DefaultJcrContentNodeBinder} for optimized node binding.
 *
 * @param <T> the type of nodes being indexed
 */
class NodeIndex<T> {

    private final Map<String, Map<String, List<T>>> index = new LinkedHashMap<>();
    private final List<T> compounds = new ArrayList<>();

    void add(String name, String type, T node) {
        index.computeIfAbsent(name, k -> new LinkedHashMap<>())
             .computeIfAbsent(type, k -> new ArrayList<>())
             .add(node);
    }

    void addCompound(T node) {
        compounds.add(node);
    }

    List<T> get(String name, String type) {
        Map<String, List<T>> byType = index.get(name);
        if (byType == null) {
            return Collections.emptyList();
        }
        List<T> result = byType.get(type);
        return (result != null) ? result : Collections.emptyList();
    }

    Map<String, List<T>> getByName(String name) {
        return index.getOrDefault(name, Collections.emptyMap());
    }

    Set<String> getNames() {
        return index.keySet();
    }

    List<T> getCompounds() {
        return compounds;
    }
}
