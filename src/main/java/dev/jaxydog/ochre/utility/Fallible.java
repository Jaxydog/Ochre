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

package dev.jaxydog.ochre.utility;

import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

/**
 * Provides common methods for handling errors in an ever-so-slightly nicer way.
 *
 * @author Jaxydog
 * @since 0.1.0
 */
@NonExtendable
public interface Fallible {

    /**
     * Catches a value thrown by the given {@link FallibleRunnable} using the given {@link FallibleConsumer}.
     *
     * @param runnable The {@link FallibleRunnable} that is being run.
     * @param consumer The {@link FallibleConsumer} that catches any thrown value.
     * @param <E> The type thrown by the {@link FallibleRunnable}.
     * @param <F> The type thrown by the {@link FallibleConsumer}.
     *
     * @throws ClassCastException If the value thrown by the {@link FallibleRunnable} is not of type {@code E}.
     * @throws F If the {@link FallibleConsumer} throws.
     * @since 0.1.0
     */
    @SuppressWarnings("unchecked")
    static <E extends Throwable, F extends Throwable> void catchThrown(
        final @NotNull FallibleRunnable<@UnknownNullability E> runnable,
        final @NotNull FallibleConsumer<@UnknownNullability E, @UnknownNullability F> consumer
    )
        throws @NotNull ClassCastException, @UnknownNullability F
    {
        try {
            runnable.run();
        } catch (final @UnknownNullability Throwable throwable) {
            consumer.accept((E) throwable);
        }
    }

    /**
     * Catches a value thrown by the given {@link FallibleSupplier} using the given {@link FallibleFunction}.
     *
     * @param supplier The {@link FallibleSupplier} that is being run.
     * @param function The {@link FallibleFunction} that catches any thrown value.
     * @param <T> The type being returned.
     * @param <E> The type thrown by the {@link FallibleSupplier}.
     * @param <F> The type thrown by the {@link FallibleFunction}.
     *
     * @return The {@link FallibleSupplier}'s return value, or the {@link FallibleFunction}'s return value if a value
     * was thrown.
     *
     * @throws ClassCastException If the value thrown by the {@link FallibleSupplier} is not of type {@code E}.
     * @throws F If the {@link FallibleFunction} throws.
     * @since 0.1.0
     */
    @SuppressWarnings("unchecked")
    static <T, E extends Throwable, F extends Throwable> @UnknownNullability T catchThrown(
        final @NotNull FallibleSupplier<@UnknownNullability T, @UnknownNullability E> supplier,
        final @NotNull FallibleFunction<@UnknownNullability E, @UnknownNullability T, @UnknownNullability F> function
    )
        throws @NotNull ClassCastException, @UnknownNullability F
    {
        try {
            return supplier.get();
        } catch (final @UnknownNullability Throwable throwable) {
            return function.apply((E) throwable);
        }
    }

    /**
     * Wraps a value thrown by the given {@link FallibleRunnable} using the given {@link FallibleFunction}.
     *
     * @param runnable The {@link FallibleRunnable} that is being run.
     * @param function The {@link FallibleFunction} that wraps any thrown value.
     * @param <E> The type thrown by the {@link FallibleRunnable}.
     * @param <F> The type returned by the {@link FallibleFunction}.
     * @param <G> The type thrown by the {@link FallibleFunction}.
     *
     * @throws ClassCastException If the value thrown by the {@link FallibleRunnable} is not of type {@code E}.
     * @throws F If the {@link FallibleRunnable} throws.
     * @throws G If the {@link FallibleFunction} throws.
     * @since 0.1.0
     */
    @SuppressWarnings("unchecked")
    static <E extends Throwable, F extends Throwable, G extends Throwable> void wrapThrown(
        final @NotNull FallibleRunnable<@UnknownNullability E> runnable,
        final @NotNull FallibleFunction<@UnknownNullability E, @UnknownNullability F, @UnknownNullability G> function
    )
        throws @NotNull ClassCastException, @UnknownNullability F, @UnknownNullability G
    {
        try {
            runnable.run();
        } catch (final @UnknownNullability Throwable throwable) {
            throw function.apply((E) throwable);
        }
    }

    /**
     * Wraps a value thrown by the given {@link FallibleSupplier} using the given {@link FallibleFunction}.
     *
     * @param supplier The {@link FallibleSupplier} that is being run.
     * @param function The {@link FallibleFunction} that wraps any thrown value.
     * @param <T> The type returned by the {@link FallibleSupplier}.
     * @param <E> The type thrown by the {@link FallibleSupplier}.
     * @param <F> The type returned by the {@link FallibleFunction}.
     * @param <G> The type thrown by the {@link FallibleFunction}.
     *
     * @return The {@link FallibleSupplier}'s return value.
     *
     * @throws ClassCastException If the value thrown by the {@link FallibleSupplier} is not of type {@code E}.
     * @throws F If the {@link FallibleSupplier} throws.
     * @throws G If the {@link FallibleFunction} throws.
     * @since 0.1.0
     */
    @SuppressWarnings("unchecked")
    static <T, E extends Throwable, F extends Throwable, G extends Throwable> @UnknownNullability T wrapThrown(
        final @NotNull FallibleSupplier<@UnknownNullability T, @UnknownNullability E> supplier,
        final @NotNull FallibleFunction<@UnknownNullability E, @UnknownNullability F, @UnknownNullability G> function
    )
        throws @NotNull ClassCastException, @UnknownNullability F, @UnknownNullability G
    {
        try {
            return supplier.get();
        } catch (final @UnknownNullability Throwable throwable) {
            throw function.apply((E) throwable);
        }
    }

    /**
     * Throws a {@link CatastrophicExplosionError} if the given {@link FallibleRunnable} throws.
     *
     * @param runnable The {@link FallibleRunnable} that is being run.
     * @param <E> The type thrown by the {@link FallibleRunnable}.
     *
     * @throws ClassCastException If the value thrown by the {@link FallibleSupplier} is not of type {@code E}.
     * @throws CatastrophicExplosionError If the {@link FallibleRunnable} throws.
     * @since 0.1.0
     */
    static <E extends Throwable> void explodeOnThrown(final @NotNull FallibleRunnable<@UnknownNullability E> runnable)
        throws @NotNull ClassCastException, @NotNull CatastrophicExplosionError
    {
        Fallible.wrapThrown(runnable, CatastrophicExplosionError::new);
    }

    /**
     * Throws a {@link CatastrophicExplosionError} if the given {@link FallibleSupplier} throws.
     *
     * @param supplier The {@link FallibleSupplier} that is being run.
     * @param <T> The type returned by the {@link FallibleSupplier}.
     * @param <E> The type thrown by the {@link FallibleSupplier}.
     *
     * @return The {@link FallibleSupplier}'s return value.
     *
     * @throws ClassCastException If the value thrown by the {@link FallibleSupplier} is not of type {@code E}.
     * @throws CatastrophicExplosionError If the {@link FallibleSupplier} throws.
     * @since 0.1.0
     */
    static <T, E extends Throwable> @UnknownNullability T explodeOnThrown(
        final @NotNull FallibleSupplier<T, @UnknownNullability E> supplier
    )
        throws @NotNull ClassCastException, @NotNull CatastrophicExplosionError
    {
        return Fallible.wrapThrown(supplier, CatastrophicExplosionError::new);
    }

}
