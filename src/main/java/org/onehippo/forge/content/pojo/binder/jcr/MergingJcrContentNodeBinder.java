/*
 *  Copyright 2015-2018 Hippo B.V. (http://www.onehippo.com)
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

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;
import org.onehippo.forge.content.pojo.binder.ContentNodeBinder;
import org.onehippo.forge.content.pojo.binder.ContentNodeBindingException;
import org.onehippo.forge.content.pojo.binder.ContentNodeBindingItemFilter;
import org.onehippo.forge.content.pojo.binder.jcr.DefaultContentNodeJcrBindingItemFilter;
import org.onehippo.forge.content.pojo.binder.jcr.DefaultJcrContentNodeBinder;
import org.onehippo.forge.content.pojo.common.ContentValueConverter;
import org.onehippo.forge.content.pojo.common.jcr.DefaultJcrContentValueConverter;
import org.onehippo.forge.content.pojo.model.ContentItem;
import org.onehippo.forge.content.pojo.model.ContentNode;

/**
 * Default {@link ContentNodeBinder} implementation for JCR.
 */
public class MergingJcrContentNodeBinder extends DefaultJcrContentNodeBinder {

	private static final long serialVersionUID = 64835589348934L;
	
    /**
     * Default constructor.
     */
    public MergingJcrContentNodeBinder() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bind(Node jcrDataNode, ContentNode contentNode, ContentNodeBindingItemFilter<ContentItem> itemFilter,
                     ContentValueConverter<Value> valueConverter) throws ContentNodeBindingException {
    	
        try {
            if (itemFilter == null) {
                itemFilter = new DefaultContentNodeJcrBindingItemFilter();
            }

            if (valueConverter == null) {
                valueConverter = new DefaultJcrContentValueConverter(jcrDataNode.getSession());
            }

            if (StringUtils.isNotBlank(contentNode.getPrimaryType())
                    && !jcrDataNode.getPrimaryNodeType().getName().equals(contentNode.getPrimaryType())) {
                jcrDataNode.setPrimaryType(contentNode.getPrimaryType());
            }

            for (String mixinType : contentNode.getMixinTypes()) {
                if (!jcrDataNode.isNodeType(mixinType)) {
                    jcrDataNode.addMixin(mixinType);
                }
            }

            bindProperties(jcrDataNode, contentNode, itemFilter, valueConverter);

            updateJcrNode(jcrDataNode, contentNode, itemFilter);

        } catch (RepositoryException e) {
            throw new ContentNodeBindingException(e.toString(), e);
        }
    }

    /**
     * Traverse incoming ContentNode recursively and update the JCR node 
     */
    protected void updateJcrNode(final Node jcrDataNode, final ContentNode contentNode, final ContentNodeBindingItemFilter<ContentItem> itemFilter) throws RepositoryException {

            Node childJcrNode = null;
            
            List<ContentNode> childContentNodes = contentNode.getNodes();
            
            if(childContentNodes!=null & !childContentNodes.isEmpty()) {
	            for(ContentNode childContentNode : childContentNodes) {
	            	
	            	if (itemFilter != null && !itemFilter.accept(childContentNode)) {
	                    continue;
	                }
	            	
	            	if(!jcrDataNode.hasNode(childContentNode.getName())){
	            		childJcrNode = jcrDataNode.addNode(childContentNode.getName(), childContentNode.getPrimaryType());
	                } else {
	                	childJcrNode = jcrDataNode.getNode(childContentNode.getName());
	                }
	            	bind(childJcrNode, childContentNode, itemFilter, null);
	            }
            }
    }
}
