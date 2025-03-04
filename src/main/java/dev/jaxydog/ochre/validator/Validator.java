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

import dev.jaxydog.ochre.utility.Scoped;
import dev.jaxydog.ochre.utility.ScopedException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
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
public abstract class Validator<T>
    extends Scoped<Validator.Context>
{

    /**
     * Creates a new {@link Validator}.
     *
     * @since 0.1.0
     */
    protected Validator() { }

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
                throws @NotNull ScopedException
            {
                return this.runScoped(new Context("custom implementation"), () -> predicate.test(value));
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
                throws @NotNull ScopedException
            {
                final @NotNull Scope scope = this.createScope(new Context("iterative testing"));

                return validators.stream()
                    .allMatch((final @NotNull Validator<T> validator) -> this.runScoped(
                        scope,
                        () -> validator.test(value)
                    ));
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
                throws @NotNull ScopedException
            {
                final @NotNull Scope scope = this.createScope(new Context("iterative testing"));

                return validators.stream()
                    .anyMatch((final @NotNull Validator<T> validator) -> this.runScoped(
                        scope,
                        () -> validator.test(value)
                    ));
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
     * @param value The received value.
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
     * @throws ScopedException If execution fails.
     * @since 0.1.0
     */
    public abstract boolean test(final @NotNull T value)
        throws @NotNull ScopedException;

    /**
     * Validates the given value, throwing a {@link InvalidValueException} if it fails.
     *
     * @param value The value to validate.
     *
     * @throws ScopedException If execution fails.
     * @throws InvalidValueException If the value is not considered valid.
     * @since 0.1.0
     */
    public void validate(final @NotNull T value)
        throws @NotNull ScopedException, @NotNull InvalidValueException
    {
        if (!this.test(value)) throw new InvalidValueException(this.expected(), this.received(value));
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
                throws @NotNull ScopedException
            {
                return this.runScoped(
                    new Context("chaining"),
                    () -> combine.apply(self.test(value), other.test(value))
                );
            }

        };
    }

    /**
     * A context used for a {@link Validator}'s inner {@link Scoped.Scope}.
     *
     * @param step The current validation step.
     *
     * @author Jaxydog
     * @since 0.1.0
     */
    public record Context(@NotNull String step) {

        @Override
        public String toString() {
            return "step '%s'".formatted(this.step());
        }

    }

}
