/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * Copyright © 2025 Jaxydog
 *
 * This file is part of Ochre.
 *
 * Ochre is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Ochre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with Ochre. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.jaxydog.ochre.converter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.jaxydog.ochre.utility.ScopedException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

/**
 * Converts values of type {@code T} to and from {@link JsonElement} values.
 *
 * @param <T> The type being converted.
 *
 * @author Jaxydog
 * @since 0.1.0
 */
public abstract class JsonConverter<T>
    extends Converter<T, JsonElement>
{

    /**
     * Creates a new {@link JsonConverter}.
     *
     * @since 0.1.0
     */
    protected JsonConverter() { }

    /**
     * A {@link Converter} for boolean values.
     *
     * @since 0.1.0
     */
    public static final JsonConverter<Boolean> BOOLEAN = new JsonConverter<>() {

        @Override
        public @NotNull JsonElement into(final @NotNull Boolean value)
            throws @NotNull ScopedException
        {
            return new JsonPrimitive(value);
        }

        @Override
        public @NotNull Boolean from(final @NotNull JsonElement value)
            throws @NotNull ScopedException
        {
            return this.runScoped(Method.FROM.context(), value::getAsBoolean);
        }

    };

    /**
     * A {@link Converter} for numeric values.
     *
     * @since 0.1.0
     */
    public static final JsonConverter<Number> NUMBER = new JsonConverter<>() {

        @Override
        public @NotNull JsonElement into(@NotNull Number value)
            throws @NotNull ScopedException
        {
            return new JsonPrimitive(value);
        }

        @Override
        public @NotNull Number from(@NotNull JsonElement value)
            throws @NotNull ScopedException
        {
            return this.runScoped(Method.FROM.context(), value::getAsNumber);
        }

    };

    /**
     * A {@link Converter} for byte values.
     *
     * @since 0.1.0
     */
    public static final JsonConverter<Byte> BYTE =
        (JsonConverter<Byte>) JsonConverter.NUMBER.mapInput(Number::byteValue, v -> v);

    /**
     * A {@link Converter} for short values.
     *
     * @since 0.1.0
     */
    public static final JsonConverter<Short> SHORT =
        (JsonConverter<Short>) JsonConverter.NUMBER.mapInput(Number::shortValue, v -> v);

    /**
     * A {@link Converter} for integer values.
     *
     * @since 0.1.0
     */
    public static final JsonConverter<Integer> INTEGER =
        (JsonConverter<Integer>) JsonConverter.NUMBER.mapInput(Number::intValue, v -> v);

    /**
     * A {@link Converter} for long values.
     *
     * @since 0.1.0
     */
    public static final JsonConverter<Long> LONG =
        (JsonConverter<Long>) JsonConverter.NUMBER.mapInput(Number::longValue, v -> v);

    /**
     * A {@link Converter} for float values.
     *
     * @since 0.1.0
     */
    public static final JsonConverter<Float> FLOAT =
        (JsonConverter<Float>) JsonConverter.NUMBER.mapInput(Number::floatValue, v -> v);

    /**
     * A {@link Converter} for double values.
     *
     * @since 0.1.0
     */
    public static final JsonConverter<Double> DOUBLE =
        (JsonConverter<Double>) JsonConverter.NUMBER.mapInput(Number::doubleValue, v -> v);

    /**
     * A {@link Converter} for string values.
     *
     * @since 0.1.0
     */
    public static final JsonConverter<String> STRING = new JsonConverter<>() {

        @Override
        public @NotNull JsonElement into(@NotNull String value)
            throws @NotNull ScopedException
        {
            return new JsonPrimitive(value);
        }

        @Override
        public @NotNull String from(@NotNull JsonElement value)
            throws @NotNull ScopedException
        {
            return this.runScoped(Method.FROM.context(), value::getAsString);
        }

    };

    /**
     * A {@link Converter} for {@link Identifier} values.
     *
     * @since 0.1.0
     */
    public static final JsonConverter<Identifier> IDENTIFIER =
        (JsonConverter<Identifier>) JsonConverter.STRING.mapInput(Identifier::of, Identifier::toString);

    /**
     * Creates a new {@link JsonConverter} that converts to and from a list of type {@link T}.
     *
     * @return A new {@link JsonConverter}.
     *
     * @since 0.1.0
     */
    public final @NotNull JsonConverter<List<@NotNull T>> list() {
        final @NotNull Function<@NotNull T, @NotNull JsonElement> thisInto = this::into;
        final @NotNull Function<@NotNull JsonElement, @NotNull T> thisFrom = this::from;

        return new JsonConverter<>() {

            @Override
            public @NotNull JsonElement into(@NotNull List<@NotNull T> value)
                throws @NotNull ScopedException
            {
                final @NotNull JsonArray array = new JsonArray(value.size());
                final @NotNull Scope scope = this.createScope(Method.INTO.context("array construction"));

                for (final @NotNull T entry : value) {
                    array.add(this.runScoped(scope, () -> thisInto.apply(entry)));
                }

                return array;
            }

            @Override
            public @NotNull List<@NotNull T> from(@NotNull JsonElement value)
                throws @NotNull ScopedException
            {
                final @NotNull JsonArray array =
                    this.runScoped(Method.FROM.context("array resolution"), value::getAsJsonArray);

                final @NotNull List<@NotNull T> list = new ObjectArrayList<>(array.size());
                final @NotNull Scope scope = this.createScope(Method.INTO.context("list construction"));

                for (final @NotNull JsonElement element : array.asList()) {
                    list.add(this.runScoped(scope, () -> thisFrom.apply(element)));
                }

                return list;
            }

        };
    }

    /**
     * Creates a new {@link JsonConverter} that converts to and from a map of type {@link T}.
     *
     * @return A new {@link JsonConverter}.
     *
     * @since 0.1.0
     */
    public final @NotNull JsonConverter<Map<@NotNull String, @NotNull T>> map() {
        final @NotNull Function<@NotNull T, @NotNull JsonElement> thisInto = this::into;
        final @NotNull Function<@NotNull JsonElement, @NotNull T> thisFrom = this::from;

        return new JsonConverter<>() {

            @Override
            public @NotNull JsonElement into(@NotNull Map<@NotNull String, @NotNull T> value)
                throws @NotNull ScopedException
            {
                final @NotNull JsonObject object = new JsonObject();
                final @NotNull Scope scope = this.createScope(Method.INTO.context("object construction"));

                for (final @NotNull Entry<@NotNull String, @NotNull T> entry : value.entrySet()) {
                    object.add(entry.getKey(), this.runScoped(scope, () -> thisInto.apply(entry.getValue())));
                }

                return object;
            }

            @Override
            public @NotNull Map<String, T> from(@NotNull JsonElement value)
                throws @NotNull ScopedException
            {
                final @NotNull JsonObject object =
                    this.runScoped(Method.FROM.context("object resolution"), value::getAsJsonObject);

                final @NotNull Map<String, T> map = new Object2ObjectOpenHashMap<>(object.size());
                final @NotNull Scope scope = this.createScope(Method.FROM.context("map construction"));

                for (final @NotNull Entry<@NotNull String, @NotNull JsonElement> entry : object.entrySet()) {
                    map.put(entry.getKey(), this.runScoped(scope, () -> thisFrom.apply(entry.getValue())));
                }

                return map;
            }

        };
    }

}
