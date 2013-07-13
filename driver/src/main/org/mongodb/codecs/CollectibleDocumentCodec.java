/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mongodb.codecs;

import org.bson.BSONWriter;
import org.mongodb.CollectibleCodec;
import org.mongodb.Document;
import org.mongodb.IdGenerator;
import org.mongodb.codecs.validators.FieldNameValidator;

/**
 * Codec for documents that go in collections, and therefore have an _id.  Ensures that the _id field is written
 * first.
 */
public class CollectibleDocumentCodec extends DocumentCodec implements CollectibleCodec<Document> {
    public static final String ID_FIELD_NAME = "_id";
    private final IdGenerator idGenerator;
    private final EncoderRegistry encoderRegistry = new EncoderRegistry();

    public CollectibleDocumentCodec(final PrimitiveCodecs primitiveCodecs,
                                    final IdGenerator idGenerator) {
        super(primitiveCodecs, new FieldNameValidator());
        if (idGenerator == null) {
            throw new IllegalArgumentException("idGenerator is null");
        }
        this.idGenerator = idGenerator;
        getEncoderRegistry().register(Document.class, this);
    }

    @Override
    protected void beforeFields(final BSONWriter bsonWriter, final Document document) {
        if (document.get(ID_FIELD_NAME) == null) {
            document.put(ID_FIELD_NAME, idGenerator.generate());
        }
        bsonWriter.writeName(ID_FIELD_NAME);
        writeValue(bsonWriter, document.get(ID_FIELD_NAME));
    }

    @Override
    protected boolean skipField(final String key) {
        return key.equals(ID_FIELD_NAME);
    }

    @Override
    public Object getId(final Document document) {
        return document.get(ID_FIELD_NAME);
    }
}
