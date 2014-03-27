/*
 * Copyright 2010 International Health Terminology Standards Development Organisation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ihtsdo.otf.tcc.model.cc.refex2.data.dataTypes;

import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.ihtsdo.otf.tcc.api.refex2.data.RefexDataType;
import org.ihtsdo.otf.tcc.api.refex2.data.dataTypes.RefexCompressedStringBI;
import org.ihtsdo.otf.tcc.model.cc.refex2.data.RefexData;

/**
 * An extension of String which utilizes compression and stores the compressed
 * data as a byte[], rather than a string.
 * 
 * This type is only appropriate when the string to be stored is very large.
 * 
 * It will actually increase the amount of space required for storage for small
 * strings.
 * 
 * {@link RefexCompressedString}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexCompressedString extends RefexData implements RefexCompressedStringBI {

    public RefexCompressedString(String string) throws PropertyVetoException {
        super(RefexDataType.COMPRESSED_STRING);
        setDataString(string);
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refex2.data.dataTypes.RefexCompressedStringBI#setDataString(java.lang.String)
     */
    @Override
    public void setDataString(String string) throws PropertyVetoException {
        data_ = compress(string);
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refex2.data.dataTypes.RefexCompressedStringBI#getDataString()
     */
    @Override
    public String getDataString() {
        return decompress(data_);
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refex2.data.RefexDataBI#getDataObject()
     */
    @Override
    public Object getDataObject() {
        return getDataString();
    }

    private static byte[] compress(String text) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            OutputStream out = new DeflaterOutputStream(baos);
            out.write(text.getBytes("UTF-8"));
            out.close();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return baos.toByteArray();
    }

    private static String decompress(byte[] bytes) {
        InputStream in = new InflaterInputStream(new ByteArrayInputStream(bytes));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            return new String(baos.toByteArray(), "UTF-8");
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

}
