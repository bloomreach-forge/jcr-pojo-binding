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
package org.onehippo.forge.content.pojo.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Base64;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class BinaryValueTest {

    private static final String RED_DOT_IMG_DATA_IN_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==";
    private static final String RED_DOT_IMG_DATA_URI = "data:image/png;base64," + RED_DOT_IMG_DATA_IN_BASE64;

    @Test
    public void testBinaryValues() throws Exception {
        BinaryValue bv = BinaryValue.fromDataURI(RED_DOT_IMG_DATA_URI);
        assertEquals("image/png", bv.getMediaType());
        assertNull(bv.getCharset());
        assertEquals(RED_DOT_IMG_DATA_IN_BASE64,
                Base64.getEncoder().encodeToString(IOUtils.toByteArray(bv.getStream())));
        bv.dispose();
    }

}
