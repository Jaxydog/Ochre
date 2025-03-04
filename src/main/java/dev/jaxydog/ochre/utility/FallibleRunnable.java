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

/**
 * A {@link Runnable} that may throw when executed.
 *
 * @param <E> The value thrown during execution.
 *
 * @author Jaxydog
 * @since 0.1.0
 */
@FunctionalInterface
public interface FallibleRunnable<E extends Throwable> {

    /**
     * Wraps the given infallible runnable in a function that is considered fallible type-wise.
     * <p>
     * The function will never actually throw unless the given function does.
     *
     * @param runnable The runnable.
     * @param <E> The value thrown during execution.
     *
     * @return A new fallible function.
     *
     * @since 0.1.0
     */
    static <E extends Throwable> @NotNull FallibleRunnable<E> fromInfallible(final @NotNull Runnable runnable) {
        return runnable::run;
    }

    /**
     * Runs the {@link  FallibleRunnable}.
     *
     * @throws E If execution fails.
     * @since 0.1.0
     */
    void run()
        throws @UnknownNullability E;

}
