package org.onehippo.forge.content.pojo.binder.jcr;

import org.onehippo.forge.content.pojo.binder.ContentNodeBindingException;
import org.onehippo.forge.content.pojo.binder.ContentNodeBindingItemFilter;
import org.onehippo.forge.content.pojo.common.BasePathBasedContentNodeItemFilter;
import org.onehippo.forge.content.pojo.mapper.ContentNodeMappingException;
import org.onehippo.forge.content.pojo.model.ContentItem;
import org.onehippo.forge.content.pojo.model.ContentNode;
import org.onehippo.forge.content.pojo.model.ContentProperty;

/**
 * Default {@link ContentNodeBindingItemFilter} implementation for JCR,
 * based on name and path based inclusion/exclusion filtering.
 */
public class DefaultContentNodeJcrBindingItemFilter extends BasePathBasedContentNodeItemFilter
        implements ContentNodeBindingItemFilter<ContentItem> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(ContentItem item) throws ContentNodeBindingException {
        if (item.isNode()) {
            return acceptNode((ContentNode) item);
        } else {
            return acceptProperty((ContentProperty) item);
        }
    }

    protected boolean acceptNode(ContentNode node) throws ContentNodeMappingException {
        if (!isPathIncludable(node.getName(), getNodeIncludePatterns(), getNodeExcludePatterns())) {
            return false;
        }

        return true;
    }

    protected boolean acceptProperty(ContentProperty property) throws ContentNodeMappingException {
        if (!isPropertyIncludableByType(property.getType().toString())) {
            return false;
        }

        if (!isPathIncludable(property.getName(), getPropertyIncludePatterns(), getPropertyExcludePatterns())) {
            return false;
        }

        return true;
    }

}
