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
 */package org.onehippo.forge.content.pojo.mapper.jcr.hippo;

import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.hippoecm.repository.HippoStdNodeType;
import org.hippoecm.repository.api.HippoNodeType;
import org.onehippo.forge.content.pojo.mapper.ContentNodeMappingException;

public class DocumentVariantNodeMappingFilter extends DefaultHippoJcrItemMappingFilter {

    private Set<String> expectedStateSet;

    public DocumentVariantNodeMappingFilter(String... expectedStates) {
        super();

        expectedStateSet = new HashSet<>();

        if (expectedStates != null && expectedStates.length != 0) {
            for (String expectedState : expectedStates) {
                expectedStateSet.add(expectedState);
            }
        }
    }

    @Override
    protected boolean acceptNode(Node node) throws ContentNodeMappingException {
        if (!super.acceptNode(node)) {
            return false;
        }

        try {
            if (isDocumentVariantNode(node)) {
                if (node.hasProperty(HippoStdNodeType.HIPPOSTD_STATE)
                        && expectedStateSet.contains(node.getProperty(HippoStdNodeType.HIPPOSTD_STATE).getString())) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        } catch (RepositoryException e) {
            throw new ContentNodeMappingException(e.toString(), e);
        }
    }

    protected boolean isDocumentVariantNode(final Node jcrNode) throws RepositoryException {
        if (jcrNode.isNodeType(HippoNodeType.NT_DOCUMENT)
                && jcrNode.isNodeType(HippoStdNodeType.NT_PUBLISHABLE)
                && jcrNode.getParent().isNodeType(HippoNodeType.NT_HANDLE)) {
            return true;
        }

        return false;
    }

}
