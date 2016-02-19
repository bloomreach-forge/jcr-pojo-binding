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

/**
 * Non-serializable, transient Binary data value abstraction.
 */
public class BinaryValue {

    /**
     * Content media type.
     */
    private String mediaType;

    /**
     * Character set information of this binary value.
     */
    private String charset;

    /**
     * Binary data in byte array, which is used when marshaling/unmarshaling to/from a <code>data:</code> URL string.
     */
    private byte [] data;

    /**
     * External {@link FileObject} instance having the binary data.
     */
    private FileObject fileObject;

    /**
     * Transient input stream from the binary data.
     */
    private InputStream inputStream;

    /**
     * Default constructor.
     */
    public BinaryValue() {
    }

    /**
     * Constructor with binary data in byte array, which is used to build a <code>data:</code> URL string.
     * @param data binary data in byte array, which is used to build a <code>data:</code> URL string
     */
    public BinaryValue(byte [] data) {
        this(data, null, null);
    }

    /**
     * Constructor with binary data in byte array, which is used to build a <code>data:</code> URL string,
     * {@code mediaType} and {@code charset}, when the {@code data} represents character based data.
     * @param data binary data in byte array, which is used to build a <code>data:</code> URL string
     * @param mediaType media type
     * @param charset character set
     */
    public BinaryValue(byte [] data, String mediaType, String charset) {
        this.data = data;
        this.mediaType = mediaType;
        this.charset = charset;
    }

    /**
     * Constructor with a {@link FileObject} instance representing an external data file.
     * @param fileObject a {@link FileObject} instance representing an external data file
     */
    public BinaryValue(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    /**
     * Returns the media type.
     * @return media type
     */
    public String getMediaType() {
        return mediaType;
    }

    /**
     * Sets the media type
     * @param mediaType media type
     */
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * Returns the character set
     * @return character set
     */
    public String getCharset() {
        return charset;
    }

    /**
     * Sets the character set
     * @param charset character set
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * Creates and returns a transient input stream from the underlying data.
     * @return  a transient input stream from the underlying data
     * @throws IOException if any IO exception occurs
     */
    public InputStream getStream() throws IOException {
        if (inputStream != null) {
            inputStream.close();
            inputStream = null;
        }

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

    /**
     * Returns a URI representation of the underlying data.
     * Either a <code>data:</code> URL or an external URL based on an internal {@link FileObject}.
     * @return a <code>data:</code> URL or an external URL based on an internal {@link FileObject}
     * @throws IOException if any IO exception occurs
     */
    public String toUriString() throws IOException {
        if (data != null) {
            StringBuilder sb = new StringBuilder(data.length + 20);
            sb.append("data:");

            if (StringUtils.isNotBlank(mediaType)) {
                sb.append(mediaType);
            }

            if (StringUtils.isNotBlank(charset)) {
                sb.append(';').append(charset);
            }

            sb.append(";base64,");

            sb.append(Base64.getEncoder().encodeToString(data));

            return sb.toString();
        } else if (fileObject != null) {
            return fileObject.getURL().toString();
        }

        throw new IOException("No data nor fileObject set.");
    }

    /**
     * Disposes the transient input stream and the internal file object if any.
     * @throws IOException if any IO exception occurs
     */
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

    /**
     * Converts the given {@code dataUri} which is supposed to be a <code>data:</code> URL
     * to a {@link BinaryValue} object.
     * @param dataUri a <code>data:</code> URL
     * @return a {@link BinaryValue} object converted from the given {@code dataUri}
     */
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

    /**
     * Converts the {@code data} in {@code mediaType} and {@code charset} to a <code>data:</code> URL string.
     * @param data binary data in byte array
     * @param mediaType media type
     * @param charset character set
     * @return a <code>data:</code> URL string converted from the given {@code data}.
     */
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
