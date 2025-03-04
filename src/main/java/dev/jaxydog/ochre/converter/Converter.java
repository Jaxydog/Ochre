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

import dev.jaxydog.ochre.utility.FallibleFunction;
import dev.jaxydog.ochre.utility.Scoped;
import dev.jaxydog.ochre.utility.ScopedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

/**
 * Converts values of type {@code T} to and from values of type {@code U}.
 *
 * @param <T> The first conversion type.
 * @param <U> The second conversion type.
 *
 * @author Jaxydog
 * @since 0.1.0
 */
public abstract class Converter<T, U>
    extends Scoped<Converter.Context>
{

    /**
     * Creates a new {@link Converter}.
     *
     * @since 0.1.0
     */
    protected Converter() { }

    /**
     * Creates a new {@link Converter} using the given methods as conversion functions.
     *
     * @param into Converts from a value of type {@code T} to a value of type {@code U}.
     * @param from Converts from a value of type {@code U} to a value of type {@code T}.
     * @param <T> The first conversion type.
     * @param <U> The second conversion type.
     *
     * @return A new {@link Converter}
     *
     * @since 0.1.0
     */
    public static <T, U> Converter<T, U> custom(
        final @NotNull FallibleFunction<@NotNull T, @NotNull U, ? extends Exception> into,
        final @NotNull FallibleFunction<@NotNull U, @NotNull T, ? extends Exception> from
    )
    {
        return new Converter<>() {

            @Override
            public @NotNull U into(@NotNull T value)
                throws @NotNull ScopedException
            {
                return this.runScoped(Method.INTO.context("custom implementation"), () -> into.apply(value));
            }

            @Override
            public @NotNull T from(@NotNull U value)
                throws @NotNull ScopedException
            {
                return this.runScoped(Method.FROM.context("custom implementation"), () -> from.apply(value));
            }

        };
    }

    /**
     * Converts from a value of type {@code T} into one of type {@code U}.
     *
     * @param value The value to convert.
     *
     * @return The converted value.
     *
     * @throws ScopedException If the conversion fails.
     * @since 0.1.0
     */
    public abstract @NotNull U into(final @NotNull T value)
        throws @NotNull ScopedException;

    /**
     * Converts from a value of type {@code U} into one of type {@code T}.
     *
     * @param value The value to convert.
     *
     * @return The converted value.
     *
     * @throws ScopedException If the conversion fails.
     * @since 0.1.0
     */
    public abstract @NotNull T from(final @NotNull U value)
        throws @NotNull ScopedException;

    /**
     * Returns a new {@link Converter} that wraps this value, mapping its input to another type.
     *
     * @param into Converts a value of type {@code T} to a value of type {@code V}.
     * @param from Converts a value of type {@code V} to a value of type {@code T}.
     * @param <V> The new input type.
     *
     * @return A new {@link Converter}.
     *
     * @since 0.1.0
     */
    public final <V> @NotNull Converter<V, U> mapInput(
        final @NotNull Function<@NotNull T, @NotNull V> into,
        final @NotNull Function<@NotNull V, @NotNull T> from
    )
    {
        final @NotNull Function<@NotNull T, @NotNull U> thisInto = this::into;
        final @NotNull Function<@NotNull U, @NotNull T> thisFrom = this::from;

        return new Converter<>() {

            @Override
            public @NotNull U into(@NotNull V value)
                throws @NotNull ScopedException
            {
                final @NotNull T mapped = this.runScoped(Method.INTO.context("mapping"), () -> from.apply(value));

                return this.runScoped(Method.INTO.context("conversion"), () -> thisInto.apply(mapped));
            }

            @Override
            public @NotNull V from(@NotNull U value)
                throws @NotNull ScopedException
            {
                final @NotNull T converted =
                    this.runScoped(Method.FROM.context("conversion"), () -> thisFrom.apply(value));

                return this.runScoped(Method.FROM.context("mapping"), () -> into.apply(converted));
            }

        };
    }

    /**
     * Returns a new {@link Converter} that wraps this value, mapping its output to another type.
     *
     * @param into Converts a value of type {@code U} to a value of type {@code V}.
     * @param from Converts a value of type {@code V} to a value of type {@code U}.
     * @param <V> The new output type.
     *
     * @return A new {@link Converter}.
     *
     * @since 0.1.0
     */
    public final <V> @NotNull Converter<T, V> mapOutput(
        final @NotNull Function<@NotNull U, @NotNull V> into,
        final @NotNull Function<@NotNull V, @NotNull U> from
    )
    {
        final @NotNull Function<@NotNull T, @NotNull U> thisInto = this::into;
        final @NotNull Function<@NotNull U, @NotNull T> thisFrom = this::from;

        return new Converter<>() {

            @Override
            public @NotNull V into(@NotNull T value)
                throws @NotNull ScopedException
            {
                final @NotNull U converted =
                    this.runScoped(Method.INTO.context("conversion"), () -> thisInto.apply(value));

                return this.runScoped(Method.INTO.context("mapping"), () -> into.apply(converted));
            }

            @Override
            public @NotNull T from(@NotNull V value)
                throws @NotNull ScopedException
            {
                final @NotNull U mapped = this.runScoped(Method.FROM.context("mapping"), () -> from.apply(value));

                return this.runScoped(Method.FROM.context("conversion"), () -> thisFrom.apply(mapped));
            }

        };
    }

    /**
     * A method being executed by a {@link Converter}.
     *
     * @param name The method's name.
     *
     * @since 0.1.0
     */
    public record Method(@NotNull String name) {

        /**
         * The method used for {@link Converter#into(Object)}.
         *
         * @since 0.1.0
         */
        public static final Method INTO = new Method("into");
        /**
         * The method used for {@link Converter#from(Object)}.
         *
         * @since 0.1.0
         */
        public static final Method FROM = new Method("from");

        /**
         * Creates a new {@link Context} using this {@link Method}.
         *
         * @return A new context.
         *
         * @since 0.1.0
         */
        public @NotNull Context context() {
            return new Context(this, null);
        }

        /**
         * Creates a new {@link Context} using this {@link Method}.
         *
         * @param action This method's current action.
         *
         * @return A new context.
         *
         * @since 0.1.0
         */
        public @NotNull Context context(final @Nullable String action) {
            return new Context(this, action);
        }

    }

    /**
     * A context used for a {@link Converter}'s inner {@link Scoped.Scope}.
     *
     * @param method The current {@link Method}.
     * @param action The current {@link Method}'s action.
     *
     * @author Jaxydog
     * @since 0.1.0
     */
    public record Context(@NotNull Method method, @Nullable String action) {

        @Override
        public String toString() {
            if (Objects.isNull(this.action())) {
                return "method '%s'".formatted(this.method().name());
            } else {
                return "method '%s' while '%s'".formatted(this.method().name(), this.action());
            }
        }

    }

}
