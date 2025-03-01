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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Converts values of type {@code T} to and from values of type {@code U}.
 *
 * @param <T> The first conversion type.
 * @param <U> The second conversion type.
 *
 * @author Jaxydog
 * @since 0.1.0
 */
public abstract class Converter<T, U> {

    /**
     * The current scope.
     *
     * @since 0.1.0
     */
    private volatile @Nullable Scope scope = null;

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
        final @NotNull Function<@NotNull T, @NotNull U> into,
        final @NotNull Function<@NotNull U, @NotNull T> from
    )
    {
        return new Converter<>() {

            @Override
            public @NotNull U into(@NotNull T value)
                throws @NotNull ConverterException
            {
                try (final @NotNull Scope scope = this.scope(this.context(Method.INTO, "custom implementation"))) {
                    return scope.call(() -> into.apply(value));
                }
            }

            @Override
            public @NotNull T from(@NotNull U value)
                throws @NotNull ConverterException
            {
                try (final @NotNull Scope scope = this.scope(this.context(Method.FROM, "custom implementation"))) {
                    return scope.call(() -> from.apply(value));
                }
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
     * @throws ConverterException If the conversion fails.
     * @since 0.1.0
     */
    public abstract @NotNull U into(final @NotNull T value)
        throws @NotNull ConverterException;

    /**
     * Converts from a value of type {@code U} into one of type {@code T}.
     *
     * @param value The value to convert.
     *
     * @return The converted value.
     *
     * @throws ConverterException If the conversion fails.
     * @since 0.1.0
     */
    public abstract @NotNull T from(final @NotNull U value)
        throws @NotNull ConverterException;

    /**
     * Creates a new {@link Context}.
     *
     * @param method The current method.
     * @param action The current action within the method, or {@code null} if unknown.
     *
     * @return A new {@link Context}.
     *
     * @since 0.1.0
     */
    protected final @NotNull Context context(final @NotNull Method method, final @Nullable String action) {
        return this.new Context(method, action);
    }

    /**
     * Creates a new {@link Scope}.
     *
     * @param context The scope's inner context.
     *
     * @return A new {@link Scope}.
     *
     * @throws IllegalStateException If this is called from within a {@link Scope}.
     * @since 0.1.0
     */
    protected final @NotNull Scope scope(final @NotNull Context context)
        throws @NotNull IllegalStateException
    {
        return this.new Scope(context);
    }

    /**
     * Creates a new {@link ConverterException} using the given extended message and source exception.
     *
     * @param extended The extended message, or {@code null} if not applicable.
     * @param cause The source exception, or {@code null} if not applicable.
     *
     * @return A new {@link ConverterException}.
     *
     * @throws NoSuchElementException If the serializer's context is unset.
     * @since 0.1.0
     */
    protected final @NotNull ConverterException exception(
        final @Nullable String extended,
        final @Nullable Exception cause
    )
        throws @UnknownNullability NoSuchElementException
    {
        final @NotNull Context context = this.getContext().orElseThrow();

        if (cause instanceof final @NotNull ConverterException actual && actual.getContext().equals(context)) {
            return actual;
        } else if (Objects.isNull(extended)) {
            return new ConverterException(context, cause);
        } else {
            return new ConverterException(context, extended, cause);
        }
    }

    /**
     * Gets this {@link Converter}'s current {@link Context}.
     * <p>
     * If there is not an active {@link Scope}, this will return {@link Optional#empty()}.
     *
     * @since 0.1.0
     */
    public final @NotNull Optional<Context> getContext() {
        return Optional.ofNullable(this.scope).map(Scope::getContext);
    }

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
                throws @NotNull ConverterException
            {
                final @NotNull T mapped;

                try (final @NotNull Scope scope = this.scope(this.context(Method.INTO, "mapping"))) {
                    mapped = scope.call(() -> from.apply(value));
                }
                try (final @NotNull Scope scope = this.scope(this.context(Method.INTO, "conversion"))) {
                    return scope.call(() -> thisInto.apply(mapped));
                }
            }

            @Override
            public @NotNull V from(@NotNull U value)
                throws @NotNull ConverterException
            {
                final @NotNull T converted;

                try (final @NotNull Scope scope = this.scope(this.context(Method.INTO, "conversion"))) {
                    converted = scope.call(() -> thisFrom.apply(value));
                }
                try (final @NotNull Scope scope = this.scope(this.context(Method.FROM, "mapping"))) {
                    return scope.call(() -> into.apply(converted));
                }
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
                throws @NotNull ConverterException
            {
                final @NotNull U converted;

                try (final @NotNull Scope scope = this.scope(this.context(Method.INTO, "conversion"))) {
                    converted = scope.call(() -> thisInto.apply(value));
                }
                try (final @NotNull Scope scope = this.scope(this.context(Method.INTO, "mapping"))) {
                    return scope.call(() -> into.apply(converted));
                }
            }

            @Override
            public @NotNull T from(@NotNull V value)
                throws @NotNull ConverterException
            {
                final @NotNull U mapped;

                try (final @NotNull Scope scope = this.scope(this.context(Method.FROM, "mapping"))) {
                    mapped = scope.call(() -> from.apply(value));
                }
                try (final @NotNull Scope scope = this.scope(this.context(Method.FROM, "conversion"))) {
                    return scope.call(() -> thisFrom.apply(mapped));
                }
            }

        };
    }

    /**
     * A method being executed, for usage within a {@link Context}.
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

    }

    /**
     * An execution context for a {@link Converter}.
     *
     * @author Jaxydog
     * @since 0.1.0
     */
    public final class Context {

        /**
         * The type of the outer {@link Converter}.
         *
         * @since 0.1.0
         */
        @SuppressWarnings("unchecked")
        private final @NotNull Class<? extends Converter<T, U>> type =
            (Class<? extends Converter<T, U>>) Converter.this.getClass();

        /**
         * The {@link Converter}'s current method.
         *
         * @since 0.1.0
         */
        private final @NotNull Method method;
        /**
         * The {@link Converter}'s current action within the method.
         *
         * @since 0.1.0
         */
        private final @Nullable String action;

        /**
         * Creates a new {@link Context}.
         *
         * @param method The current method.
         * @param action The current action within the method.
         *
         * @since 0.1.0
         */
        private Context(final @NotNull Method method, final @Nullable String action) {
            this.method = method;
            this.action = action;
        }

        /**
         * Returns the {@link Converter}'s current method.
         *
         * @return The current method.
         *
         * @since 0.1.0
         */
        public @NotNull Method getMethod() {
            return this.method;
        }

        /**
         * Returns the {@link Converter}'s current action within the method.
         *
         * @return The current action.
         *
         * @since 0.1.0
         */
        public @NotNull Optional<String> getAction() {
            return Optional.ofNullable(this.action);
        }

        /**
         * Returns a description of the context for usage in exception messages.
         *
         * @return A description of the context.
         *
         * @since 0.1.0
         */
        public @NotNull String getDescription() {
            if (Objects.isNull(this.action)) {
                return "'%s'".formatted(this.getMethod().name());
            } else {
                return "'%s' (%s)".formatted(this.getMethod().name(), this.action);
            }
        }

    }

    /**
     * A temporary scope with a unique {@link Context} for usage within the current {@link Converter}.
     *
     * @author Jaxydog
     * @since 0.1.0
     */
    public final class Scope implements AutoCloseable {

        /**
         * The scope's context.
         *
         * @since 0.1.0
         */
        private final @NotNull Context context;

        /**
         * Creates a new {@link Scope}.
         *
         * @param context The scope's context.
         *
         * @throws IllegalStateException If this is called when a scope is already active.
         * @since 0.1.0
         */
        private Scope(final @NotNull Context context)
            throws @NotNull IllegalStateException
        {
            this.context = context;

            synchronized (this) {
                if (Objects.isNull(Converter.this.scope)) {
                    Converter.this.scope = this;
                } else {
                    throw new IllegalStateException("Cannot create a scope from within a scope");
                }
            }
        }

        /**
         * Returns this scope's context.
         *
         * @return The context.
         *
         * @since 0.1.0
         */
        public @NotNull Context getContext() {
            return this.context;
        }

        /**
         * Runs the given {@link Runnable} within this scope.
         *
         * @param runnable The function to invoke.
         *
         * @throws ConverterException If the function throws an exception.
         * @throws IllegalStateException If this scope is not active.
         * @since 0.1.0
         */
        public void call(final @NotNull Runnable runnable)
            throws @NotNull ConverterException, @NotNull IllegalStateException
        {
            if (Objects.isNull(Converter.this.scope)) {
                throw new IllegalStateException("The scope is not active");
            }

            try {
                runnable.run();
            } catch (final @UnknownNullability Exception exception) {
                throw Converter.this.exception(null, exception);
            }
        }

        /**
         * Runs the given {@link Supplier} within this scope.
         *
         * @param supplier The function to invoke.
         * @param <V> The return type of the function.
         *
         * @return The return value of the given function.
         *
         * @throws ConverterException If the function throws an exception.
         * @throws IllegalStateException If this scope is not active.
         * @since 0.1.0
         */
        public <V> @UnknownNullability V call(final @NotNull Supplier<@UnknownNullability V> supplier)
            throws @NotNull ConverterException, @NotNull IllegalStateException
        {
            if (Objects.isNull(Converter.this.scope)) {
                throw new IllegalStateException("The scope is not active");
            }

            try {
                return supplier.get();
            } catch (final @UnknownNullability Exception exception) {
                throw Converter.this.exception(null, exception);
            }
        }

        @Override
        public synchronized void close()
            throws @NotNull IllegalStateException
        {
            if (Objects.equals(this, Converter.this.scope)) {
                Converter.this.scope = null;
            } else {
                throw new IllegalStateException("The scope being closed is not the current scope");
            }
        }

    }

}
