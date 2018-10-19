/*
 *  Copyright 2018 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.forge.content.pojo.binder;

import org.onehippo.forge.content.pojo.model.ContentNode;

/**
 * 
 * @param <D> target physical data node (e.g, {@link javax.jcr.Node}) to bind the input {@link ContentNode}.
 */
public interface ContentNodeBindingTargetSelector<D> {

    /**
     * Select a target physical data node (e.g, {@link javax.jcr.Node}) to bind the input {@link ContentNode}
     * from the given {@code base} physical data node.
     * <p>
     * If this method returns null, it means there's nothing to update and that's not a problem.
     * @param source {@link ContentNode} source to bind from.
     * @param base base physical data node from which the target physical data node should be found.
     *        e.g, a child data node with the same name.
     * @return a target physical data node (e.g, {@link javax.jcr.Node}) to bind the input {@link ContentNode}.
     *         Or null if there's nothing to update.
     * @throws ContentNodeBindingException
     */
    public D select(ContentNode contentNode, D base) throws ContentNodeBindingException;

}