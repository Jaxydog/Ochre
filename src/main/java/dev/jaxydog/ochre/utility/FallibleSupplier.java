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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.function.Supplier;

/**
 * A {@link java.util.function.Supplier} that may throw when executed.
 *
 * @param <T> The result.
 * @param <E> The value thrown during execution.
 *
 * @author Jaxydog
 * @since 0.1.0
 */
@FunctionalInterface
public interface FallibleSupplier<T, E extends Throwable> {

    /**
     * Wraps the given infallible supplier in a function that is considered fallible type-wise.
     * <p>
     * The function will never actually throw unless the given function does.
     *
     * @param supplier The supplier.
     * @param <T> The result.
     * @param <E> The value thrown during execution.
     *
     * @return A new fallible function.
     *
     * @since 0.1.0
     */
    static <T, E extends Throwable> @NotNull FallibleSupplier<T, E> fromInfallible(final @NotNull Supplier<T> supplier) {
        return supplier::get;
    }

    /**
     * Returns the result.
     *
     * @return A result.
     *
     * @throws E If execution fails.
     * @since 0.1.0
     */
    T get()
        throws @UnknownNullability E;

}
