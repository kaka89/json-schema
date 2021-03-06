/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.json.schema;

import org.everit.json.schema.internal.JSONPrinter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

/**
 * Enum schema validator.
 */
public class EnumSchema extends Schema {

    private static Object toJavaValue(Object orig)  {
        if (orig instanceof JSONArray) {
            return ((JSONArray) orig).toList();
        } else if (orig instanceof JSONObject){
            return ((JSONObject) orig).toMap();
        } else {
            return orig;
        }
    }

    private static Set<Object> toJavaValues(Set<Object> orgJsons) {
        return orgJsons.stream().map(EnumSchema::toJavaValue).collect(toSet());
    }

    /**
     * Builder class for {@link EnumSchema}.
     */
    public static class Builder extends Schema.Builder<EnumSchema> {

        private Set<Object> possibleValues = new HashSet<>();

        @Override
        public EnumSchema build() {
            return new EnumSchema(this);
        }

        public Builder possibleValue(final Object possibleValue) {
            possibleValues.add(possibleValue);
            return this;
        }

        public Builder possibleValues(final Set<Object> possibleValues) {
            this.possibleValues = possibleValues;
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final Set<Object> possibleValues;

    public EnumSchema(final Builder builder) {
        super(builder);
        possibleValues = Collections.unmodifiableSet(toJavaValues(builder.possibleValues));
    }

    public Set<Object> getPossibleValues() {
        return possibleValues;
    }

    @Override
    public void validate(final Object subject) {
        Object effectiveSubject = toJavaValue(subject);
        possibleValues
                .stream()
                .filter(val -> ObjectComparator.deepEquals(val, effectiveSubject))
                .findAny()
                .orElseThrow(
                        () -> failure(format("%s is not a valid enum value", subject), "enum"));
    }

    @Override
    void describePropertiesTo(final JSONPrinter writer) {
        writer.key("type");
        writer.value("enum");
        writer.key("enum");
        writer.array();
        possibleValues.forEach(writer::value);
        writer.endArray();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof EnumSchema) {
            EnumSchema that = (EnumSchema) o;
            return that.canEqual(this) &&
                    Objects.equals(possibleValues, that.possibleValues) &&
                    super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), possibleValues);
    }

    @Override
    protected boolean canEqual(final Object other) {
        return other instanceof EnumSchema;
    }

}
