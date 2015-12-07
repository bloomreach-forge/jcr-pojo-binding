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
package org.onehippo.forge.content.pojo.common.jcr.hippo;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.hippoecm.repository.HippoStdNodeType;
import org.hippoecm.repository.api.HippoNodeType;

public class HippoDocumentUtils {

    private HippoDocumentUtils() {
    }

    public static boolean isDocumentVariantNode(final Node jcrNode) throws RepositoryException {
        if (jcrNode.isNodeType(HippoNodeType.NT_DOCUMENT)
                && jcrNode.isNodeType(HippoStdNodeType.NT_PUBLISHABLE)
                && jcrNode.getParent().isNodeType(HippoNodeType.NT_HANDLE)) {
            return true;
        }

        return false;
    }

    public static boolean isLiveDocumentVariantNode(final Node jcrNode) throws RepositoryException {
        if (isDocumentVariantNode(jcrNode) && jcrNode.hasProperty(HippoStdNodeType.HIPPOSTD_STATE)) {
            if (HippoStdNodeType.PUBLISHED.equals(jcrNode.getProperty(HippoStdNodeType.HIPPOSTD_STATE).getString())) {
                return true;
            }
        }

        return false;
    }

    public static boolean isPreviewDocumentVariantNode(final Node jcrNode) throws RepositoryException {
        if (isDocumentVariantNode(jcrNode) && jcrNode.hasProperty(HippoStdNodeType.HIPPOSTD_STATE)) {
            if (HippoStdNodeType.UNPUBLISHED.equals(jcrNode.getProperty(HippoStdNodeType.HIPPOSTD_STATE).getString())) {
                return true;
            }
        }

        return false;
    }

    public static boolean isDocumentHandleNode(final Node jcrNode) throws RepositoryException {
        if (jcrNode.isNodeType(HippoNodeType.NT_HANDLE)
                && jcrNode.hasNode(jcrNode.getName())) {
            final Node variantNode = jcrNode.getNode(jcrNode.getName());

            if (variantNode.isNodeType(HippoNodeType.NT_DOCUMENT)
                    && variantNode.isNodeType(HippoStdNodeType.NT_PUBLISHABLE)) {
                return true;
            }
        }

        return false;
    }
}
