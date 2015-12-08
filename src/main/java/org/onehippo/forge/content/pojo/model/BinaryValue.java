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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;

public class BinaryValue {

    private String mediaType;
    private String charset;
    private byte [] data;
    private FileObject fileObject;
    private InputStream inputStream;

    public BinaryValue() {
    }

    public BinaryValue(byte [] data, String mediaType, String charset) {
        this.data = data;
        this.mediaType = mediaType;
        this.charset = charset;
    }

    public BinaryValue(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getCharset() {
        return charset;
    }

    public InputStream getStream() throws IOException {
        dispose();

        if (data != null) {
            inputStream = new ByteArrayInputStream(data);
        } else if (fileObject != null) {
            inputStream = fileObject.getContent().getInputStream();
        }

        if (inputStream != null) {
            return inputStream;
        }

        throw new IOException("No data nor fileObject set.");
    }

    public void dispose() throws IOException {
        if (inputStream != null) {
            inputStream.close();
            inputStream = null;
        }

        if (fileObject != null) {
            fileObject.close();
            fileObject = null;
        }
    }

    public static BinaryValue fromDataURI(String dataUri) {
        if (!StringUtils.startsWith(dataUri, "data:")) {
            throw new IllegalArgumentException("Invalid data uri.");
        }

        int offset = StringUtils.indexOf(dataUri, ',');

        if (offset == -1) {
            throw new IllegalArgumentException("Invalid data uri.");
        }

        String mediaType = null;
        String charset = null;
        String metadata = dataUri.substring(5, offset);
        String [] tokens = StringUtils.split(metadata, ";");

        if (tokens != null) {
            for (String token : tokens) {
                if (token.startsWith("charset=")) {
                    charset = token.substring(8);
                } else if (!token.equals("base64")) {
                    mediaType = token;
                }
            }
        }

        String dataString = dataUri.substring(offset + 1);

        byte [] data = Base64.getDecoder().decode(dataString);
        BinaryValue binaryValue = new BinaryValue(data, mediaType, charset);

        return binaryValue;
    }

    public static String toDataURI(byte [] data, final String mediaType, final String charset) {
        int size = (data == null ? 0 : data.length);
        StringBuilder sbTemp = new StringBuilder(size + 20);

        sbTemp.append("data:");

        if (StringUtils.isNotBlank(mediaType)) {
            sbTemp.append(mediaType);
        }

        if (StringUtils.isNotBlank(charset)) {
            sbTemp.append(';').append(charset);
        }

        sbTemp.append(";base64");
        sbTemp.append(',');
        sbTemp.append(Base64.getEncoder().encodeToString(data));

        return sbTemp.toString();
    }

}
