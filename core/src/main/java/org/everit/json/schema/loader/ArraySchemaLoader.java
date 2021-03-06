package org.everit.json.schema.loader;

import org.everit.json.schema.ArraySchema;

import static java.util.Objects.requireNonNull;

/**
 * @author erosb
 */
class ArraySchemaLoader {

    private final LoadingState ls;

    private final SchemaLoader defaultLoader;

    public ArraySchemaLoader(LoadingState ls, SchemaLoader defaultLoader) {
        this.ls = requireNonNull(ls, "ls cannot be null");
        this.defaultLoader = requireNonNull(defaultLoader, "defaultLoader cannot be null");
    }

    ArraySchema.Builder load() {
        ArraySchema.Builder builder = ArraySchema.builder();
        ls.schemaJson.maybe("minItems").map(JsonValue::requireInteger).ifPresent(builder::minItems);
        ls.schemaJson.maybe("maxItems").map(JsonValue::requireInteger).ifPresent(builder::maxItems);
        ls.schemaJson.maybe("uniqueItems").map(JsonValue::requireBoolean).ifPresent(builder::uniqueItems);
        ls.schemaJson.maybe("additionalItems").ifPresent(maybe -> {
            maybe.canBe(Boolean.class, builder::additionalItems)
                .or(JsonObject.class, obj -> builder.schemaOfAdditionalItems(defaultLoader.loadChild(obj).build()))
                .requireAny();
        });
        ls.schemaJson.maybe("items").ifPresent(items -> {
            items.canBe(JsonObject.class, itemSchema -> builder.allItemSchema(defaultLoader.loadChild(itemSchema).build()))
                .or(JsonArray.class, arr -> buildTupleSchema(builder, arr))
                .requireAny();
        });
        return builder;
    }

    private void buildTupleSchema(ArraySchema.Builder builder, JsonArray itemSchema) {
        itemSchema.forEach((i, subschema) -> {
            builder.addItemSchema(defaultLoader.loadChild(subschema.requireObject()).build());
        });
    }

}
