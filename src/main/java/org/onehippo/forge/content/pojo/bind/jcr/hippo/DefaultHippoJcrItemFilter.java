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
package org.onehippo.forge.content.pojo.bind.jcr.hippo;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.hippoecm.repository.api.HippoNodeType;
import org.onehippo.forge.content.pojo.bind.ContentNodeHandlingException;
import org.onehippo.forge.content.pojo.bind.ItemFilter;
import org.onehippo.forge.content.pojo.bind.jcr.JcrContentUtils;

public class DefaultHippoJcrItemFilter implements ItemFilter<Item> {

    public DefaultHippoJcrItemFilter() {
    }

    @Override
    public boolean accept(Item item) throws ContentNodeHandlingException {
        if (item.isNode()) {
            return acceptNode((Node) item);
        } else {
            return acceptProperty((Property) item);
        }
    }

    protected boolean acceptNode(Node node) throws ContentNodeHandlingException {
        return true;
    }

    protected boolean acceptProperty(Property property) throws ContentNodeHandlingException {
        try {
            if (HippoNodeType.HIPPO_PATH.equals(property.getName())) {
                return false;
            }

            if (JcrContentUtils.isProtected(property)) {
                return false;
            }
        } catch (RepositoryException e) {
            throw new ContentNodeHandlingException(e.toString(), e);
        }

        return true;
    }
}
