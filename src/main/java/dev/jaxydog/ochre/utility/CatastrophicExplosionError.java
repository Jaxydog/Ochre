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
import org.jetbrains.annotations.Nullable;

/**
 * An error thrown when something that <i>absolutely should never fail</i> does, in fact, fail.
 *
 * @author Jaxydog
 * @since 0.1.0
 */
public final class CatastrophicExplosionError
    extends Error
{

    /**
     * Creates a new {@link CatastrophicExplosionError} with the given message.
     *
     * @param message The error's message.
     *
     * @since 0.1.0
     */
    public CatastrophicExplosionError(final @NotNull String message) {
        super(message);
    }

    /**
     * Creates a new {@link CatastrophicExplosionError} with the given cause.
     *
     * @param cause The error's cause.
     *
     * @since 0.1.0
     */
    public CatastrophicExplosionError(final @Nullable Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new {@link CatastrophicExplosionError} with the given message and cause.
     *
     * @param message The error's message.
     * @param cause The error's cause.
     *
     * @since 0.1.0
     */
    public CatastrophicExplosionError(final @NotNull String message, final @Nullable Throwable cause) {
        super(message, cause);
    }

}
