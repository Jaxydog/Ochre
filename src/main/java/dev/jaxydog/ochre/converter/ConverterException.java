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

import java.util.Objects;

/**
 * An exception thrown by a {@link Converter}.
 *
 * @author Jaxydog
 * @since 0.1.0
 */
public class ConverterException extends RuntimeException {

    /**
     * The active {@link Converter}'s {@link Converter.Context} at the time that this was thrown.
     *
     * @since 0.1.0
     */
    private final @NotNull Converter<?, ?>.Context context;

    /**
     * Creates a new {@link ConverterException}.
     *
     * @param context The current {@link Converter.Context}.
     *
     * @since 0.1.0
     */
    public ConverterException(final @NotNull Converter<?, ?>.Context context) {
        super(ConverterException.createMessage(context, null));

        this.context = context;
    }

    /**
     * Creates a new {@link ConverterException}.
     *
     * @param context The current {@link Converter.Context}.
     * @param cause The cause of this exception.
     *
     * @since 0.1.0
     */
    public ConverterException(final @NotNull Converter<?, ?>.Context context, final @Nullable Exception cause) {
        super(ConverterException.createMessage(context, null), cause);

        this.context = context;
    }

    /**
     * Creates a new {@link ConverterException}.
     *
     * @param context The current {@link Converter.Context}.
     * @param extended The extended message.
     *
     * @since 0.1.0
     */
    public ConverterException(final @NotNull Converter<?, ?>.Context context, final @NotNull String extended) {
        super(ConverterException.createMessage(context, extended));

        this.context = context;
    }

    /**
     * Creates a new {@link ConverterException}.
     *
     * @param context The current {@link Converter.Context}.
     * @param extended The extended message.
     * @param cause The cause of this exception.
     *
     * @since 0.1.0
     */
    public ConverterException(
        final @NotNull Converter<?, ?>.Context context,
        final @NotNull String extended,
        final @Nullable Exception cause
    )
    {
        super(ConverterException.createMessage(context, extended), cause);

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
        final @NotNull Converter<?, ?>.Context context,
        final @Nullable String extended
    )
    {
        if (Objects.isNull(extended)) {
            return "Failed to invoke %s".formatted(context.getDescription());
        } else {
            return "Failed to invoke %s: %s".formatted(context.getDescription(), extended);
        }
    }

    /**
     * Returns the active {@link Converter.Context} from when this exception was thrown.
     *
     * @return The {@link Converter.Context}.
     *
     * @since 0.1.0
     */
    public final @NotNull Converter<?, ?>.Context getContext() {
        return this.context;
    }

}
