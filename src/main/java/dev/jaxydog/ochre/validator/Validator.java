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

package dev.jaxydog.ochre.validator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Validates that a value of type {@code T} passes a predicate.
 *
 * @param <T> The type being validated.
 *
 * @author Jaxydog
 * @since 0.1.0
 */
public abstract class Validator<T> {

    /**
     * The current scope.
     *
     * @since 0.1.0
     */
    private volatile @Nullable Scope scope = null;

    /**
     * Creates a new {@link Validator} using the given predicate.
     *
     * @param expected A string describing what the validator is expecting.
     * @param received A function that returns a string describing what the validator received in case of error.
     * @param predicate A predicate that tests whether a given value is valid.
     * @param <T> The type being validated.
     *
     * @return A new {@link Validator}.
     *
     * @since 0.1.0
     */
    public static <T> Validator<T> custom(
        final @NotNull String expected,
        final @NotNull Function<@NotNull T, @NotNull String> received,
        final @NotNull Predicate<@NotNull T> predicate
    )
    {
        return new Validator<>() {

            @Override
            public @NotNull String expected() {
                return expected;
            }

            @Override
            public @NotNull String received(@NotNull T value) {
                return received.apply(value);
            }

            @Override
            public boolean test(@NotNull T value)
                throws @NotNull ValidatorException
            {
                try (final @NotNull Scope scope = this.scope(this.context("custom implementation"))) {
                    return scope.call(() -> predicate.test(value));
                }
            }

        };
    }

    /**
     * Returns a new {@link  Validator} that marks a value as valid if all input {@link Validator}s consider it to be
     * valid.
     *
     * @param validators A list of validators.
     * @param <T> The type being tested.
     *
     * @return A new {@link Validator}.
     *
     * @since 0.1.0
     */
    public static <T> Validator<T> all(final @NotNull List<@NotNull Validator<T>> validators) {
        return new Validator<>() {

            @Override
            public @NotNull String expected() {
                final @NotNull Stream<String> expectations = validators.stream().map(Validator::expected);

                return "all of: %s".formatted(expectations.collect(Collectors.joining(", ")));
            }

            @Override
            public @NotNull String received(@NotNull T value) {
                final @NotNull Stream<String> received =
                    validators.stream().map((final @NotNull Validator<T> validator) -> validator.received(value));

                return "any of: %s".formatted(received.collect(Collectors.joining(", ")));
            }

            @Override
            public boolean test(@NotNull T value)
                throws @NotNull ValidatorException
            {
                try (final @NotNull Scope scope = this.scope(this.context("iterative testing"))) {
                    return validators.stream()
                        .allMatch((final @NotNull Validator<T> validator) -> scope.call(() -> validator.test(value)));
                }
            }

        };
    }

    /**
     * Returns a new {@link  Validator} that marks a value as valid if any input {@link Validator}s consider it to be
     * valid.
     *
     * @param validators A list of validators.
     * @param <T> The type being tested.
     *
     * @return A new {@link Validator}.
     *
     * @since 0.1.0
     */
    public static <T> Validator<T> any(final @NotNull List<@NotNull Validator<T>> validators) {
        return new Validator<>() {

            @Override
            public @NotNull String expected() {
                final @NotNull Stream<String> expectations = validators.stream().map(Validator::expected);

                return "any of: %s".formatted(expectations.collect(Collectors.joining(", ")));
            }

            @Override
            public @NotNull String received(@NotNull T value) {
                final @NotNull Stream<String> received =
                    validators.stream().map((final @NotNull Validator<T> validator) -> validator.received(value));

                return "all of: %s".formatted(received.collect(Collectors.joining(", ")));
            }

            @Override
            public boolean test(@NotNull T value)
                throws @NotNull ValidatorException
            {
                try (final @NotNull Scope scope = this.scope(this.context("iterative testing"))) {
                    return validators.stream()
                        .anyMatch((final @NotNull Validator<T> validator) -> scope.call(() -> validator.test(value)));
                }
            }

        };
    }

    /**
     * Returns a string describing what this {@link Validator} was expecting.
     *
     * @return What this {@link Validator} was expecting.
     *
     * @since 0.1.0
     */
    public abstract @NotNull String expected();

    /**
     * Returns a string describing what this {@link Validator} received if the value failed validation.
     *
     * @return What this {@link Validator} received.
     *
     * @since 0.1.0
     */
    public abstract @NotNull String received(final @NotNull T value);

    /**
     * Returns {@code true} if the given value is considered valid.
     *
     * @param value The value to test.
     *
     * @return Whether the given value is valid.
     *
     * @throws ValidatorException If execution fails.
     * @since 0.1.0
     */
    public abstract boolean test(final @NotNull T value)
        throws @NotNull ValidatorException;

    /**
     * Validates the given value, throwing a {@link InvalidValueException} if it fails.
     *
     * @param value The value to validate.
     *
     * @throws ValidatorException If execution fails.
     * @throws InvalidValueException If the value is not considered valid.
     * @since 0.1.0
     */
    public void validate(final @NotNull T value)
        throws @NotNull ValidatorException, @NotNull InvalidValueException
    {
        if (!this.test(value)) throw new InvalidValueException(this.expected(), this.received(value));
    }

    /**
     * Creates a new {@link Context}.
     *
     * @param step The current validation step.
     *
     * @return A new {@link Context}.
     *
     * @since 0.1.0
     */
    protected final @NotNull Context context(final @NotNull String step) {
        return this.new Context(step);
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
     * Creates a new {@link ValidatorException} using the given extended message and source exception.
     *
     * @param extended The extended message, or {@code null} if not applicable.
     * @param cause The source exception, or {@code null} if not applicable.
     *
     * @return A new {@link ValidatorException}.
     *
     * @throws NoSuchElementException If the serializer's context is unset.
     * @since 0.1.0
     */
    protected final @NotNull ValidatorException exception(
        final @Nullable String extended,
        final @Nullable Exception cause
    )
        throws @UnknownNullability NoSuchElementException
    {
        final @NotNull Context context = this.getContext().orElseThrow();

        if (cause instanceof final @NotNull ValidatorException actual && actual.getContext().equals(context)) {
            return actual;
        } else if (Objects.isNull(extended)) {
            return new ValidatorException(context, cause);
        } else {
            return new ValidatorException(context, extended, cause);
        }
    }

    /**
     * Gets this {@link Validator}'s current {@link Context}.
     * <p>
     * If there is not an active {@link Scope}, this will return {@link Optional#empty()}.
     *
     * @since 0.1.0
     */
    public final @NotNull Optional<Context> getContext() {
        return Optional.ofNullable(this.scope).map(Scope::getContext);
    }

    /**
     * Returns a new {@link Validator} that chains this and the given {@link Validator} together.
     *
     * @param other The other {@link Validator}.
     * @param combine A function that combines the results of both tests.
     *
     * @return A new {@link Validator}.
     *
     * @since 0.1.0
     */
    public final @NotNull Validator<T> chain(
        final @NotNull Validator<T> other,
        final @NotNull BiFunction<@NotNull Boolean, @NotNull Boolean, @NotNull Boolean> combine
    )
    {
        final @NotNull Validator<T> self = this;

        return new Validator<>() {

            @Override
            public @NotNull String expected() {
                return "%s and %s".formatted(self.expected(), other.expected());
            }

            @Override
            public @NotNull String received(@NotNull T value) {
                return "%s or %s".formatted(self.received(value), other.received(value));
            }

            @Override
            public boolean test(@NotNull T value)
                throws @NotNull ValidatorException
            {
                try (final @NotNull Scope scope = this.scope(this.context("chaining"))) {
                    return scope.call(() -> combine.apply(self.test(value), other.test(value)));
                }
            }

        };
    }

    /**
     * An execution context for a {@link Validator}.
     *
     * @author Jaxydog
     * @since 0.1.0
     */
    public final class Context {

        /**
         * The type of the outer {@link Validator}.
         *
         * @since 0.1.0
         */
        @SuppressWarnings("unchecked")
        private final @NotNull Class<? extends Validator<T>> type =
            (Class<? extends Validator<T>>) Validator.this.getClass();

        /**
         * The current validation step.
         *
         * @since 0.1.0
         */
        private final @NotNull String step;

        /**
         * Creates a new {@link Context}.
         *
         * @param step The current validation step.
         *
         * @since 0.1.0
         */
        private Context(final @NotNull String step) {
            this.step = step;
        }

        /**
         * Returns the current validation step.
         *
         * @return The validation step.
         *
         * @since 0.1.0
         */
        public @NotNull String getStep() {
            return this.step;
        }

        /**
         * Returns a description of the context for usage in exception messages.
         *
         * @return A description of the context.
         *
         * @since 0.1.0
         */
        public @NotNull String getDescription() {
            return "'%s'".formatted(this.getStep());
        }

    }

    /**
     * A temporary scope with a unique {@link Context} for usage within the current {@link Validator}.
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
        public Scope(final @NotNull Context context)
            throws @NotNull IllegalStateException
        {
            this.context = context;

            synchronized (this) {
                if (Objects.isNull(Validator.this.scope)) {
                    Validator.this.scope = this;
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
         * @throws ValidatorException If the function throws an exception.
         * @throws IllegalStateException If this scope is not active.
         * @since 0.1.0
         */
        public void call(final @NotNull Runnable runnable)
            throws @NotNull ValidatorException, @NotNull IllegalStateException
        {
            if (Objects.isNull(Validator.this.scope)) {
                throw new IllegalStateException("The scope is not active");
            }

            try {
                runnable.run();
            } catch (final @UnknownNullability Exception exception) {
                throw Validator.this.exception(null, exception);
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
         * @throws ValidatorException If the function throws an exception.
         * @throws IllegalStateException If this scope is not active.
         * @since 0.1.0
         */
        public <V> @UnknownNullability V call(final @NotNull Supplier<@UnknownNullability V> supplier)
            throws @NotNull ValidatorException, @NotNull IllegalStateException
        {
            if (Objects.isNull(Validator.this.scope)) {
                throw new IllegalStateException("The scope is not active");
            }

            try {
                return supplier.get();
            } catch (final @UnknownNullability Exception exception) {
                throw Validator.this.exception(null, exception);
            }
        }

        @Override
        public synchronized void close()
            throws @NotNull IllegalStateException
        {
            if (Objects.equals(this, Validator.this.scope)) {
                Validator.this.scope = null;
            } else {
                throw new IllegalStateException("The scope being closed is not the current scope");
            }
        }

    }

}
