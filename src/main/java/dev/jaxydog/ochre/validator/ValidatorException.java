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

import java.util.Objects;

/**
 * An exception thrown by a {@link Validator}.
 *
 * @author Jaxydog
 * @since 0.1.0
 */
public class ValidatorException extends RuntimeException {

    /**
     * The active {@link Validator}'s {@link Validator.Context} at the time that this was thrown.
     *
     * @since 0.1.0
     */
    private final @NotNull Validator<?>.Context context;

    /**
     * Creates a new {@link ValidatorException}.
     *
     * @param context The current {@link Validator.Context}.
     *
     * @since 0.1.0
     */
    public ValidatorException(final @NotNull Validator<?>.Context context) {
        super(ValidatorException.createMessage(context, null));

        this.context = context;
    }

    /**
     * Creates a new {@link ValidatorException}.
     *
     * @param context The current {@link Validator.Context}.
     * @param cause The cause of this exception.
     *
     * @since 0.1.0
     */
    public ValidatorException(final @NotNull Validator<?>.Context context, final @Nullable Exception cause) {
        super(ValidatorException.createMessage(context, null), cause);

        this.context = context;
    }

    /**
     * Creates a new {@link ValidatorException}.
     *
     * @param context The current {@link Validator.Context}.
     * @param extended The extended message.
     *
     * @since 0.1.0
     */
    public ValidatorException(final @NotNull Validator<?>.Context context, final @NotNull String extended) {
        super(ValidatorException.createMessage(context, extended));

        this.context = context;
    }

    /**
     * Creates a new {@link ValidatorException}.
     *
     * @param context The current {@link Validator.Context}.
     * @param extended The extended message.
     * @param cause The cause of this exception.
     *
     * @since 0.1.0
     */
    public ValidatorException(
        final @NotNull Validator<?>.Context context,
        final @NotNull String extended,
        final @Nullable Exception cause
    )
    {
        super(ValidatorException.createMessage(context, extended), cause);

        this.context = context;
    }

    /**
     * Creates an exception's message using the given context and extended description.
     *
     * @param context The context.
     * @param extended The extended message.
     *
     * @return A new exception message.
     *
     * @since 0.1.0
     */
    private static @NotNull String createMessage(
        final @NotNull Validator<?>.Context context,
        final @Nullable String extended
    )
    {
        if (Objects.isNull(extended)) {
            return "Validation step '%s' failed".formatted(context.getDescription());
        } else {
            return "Validation step '%s' failed: %s".formatted(context.getDescription(), extended);
        }
    }

    /**
     * Returns the active {@link Validator.Context} from when this exception was thrown.
     *
     * @return The {@link Validator.Context}.
     *
     * @since 0.1.0
     */
    public final @NotNull Validator<?>.Context getContext() {
        return this.context;
    }

}
