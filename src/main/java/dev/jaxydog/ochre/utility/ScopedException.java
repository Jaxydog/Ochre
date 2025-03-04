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

import java.util.Objects;

/**
 * An exception that is thrown when code within a {@link Scoped.Scope} throws.
 *
 * @author Jaxydog
 * @since 0.1.0
 */
public class ScopedException
    extends RuntimeException
{

    /**
     * The source {@link Scoped} instance.
     *
     * @since 0.1.0
     */
    private final @NotNull Scoped<?> source;
    /**
     * The {@link Scoped.Scope}'s context.
     *
     * @since 0.1.0
     */
    private final @NotNull Object context;

    /**
     * Creates a new {@link ScopedException}.
     *
     * @param source The source {@link Scoped} instance.
     * @param context The {@link Scoped.Scope}'s context.
     *
     * @since 0.1.0
     */
    ScopedException(final @NotNull Scoped<?> source, final @NotNull Object context) {
        super(ScopedException.createMessage(source, context, null));

        this.source = source;
        this.context = context;
    }

    /**
     * Creates a new {@link ScopedException}.
     *
     * @param source The source {@link Scoped} instance.
     * @param context The {@link Scoped.Scope}'s context.
     * @param message The exception's message.
     *
     * @since 0.1.0
     */
    ScopedException(final @NotNull Scoped<?> source, final @NotNull Object context, final @Nullable String message)
    {
        super(ScopedException.createMessage(source, context, message));

        this.source = source;
        this.context = context;
    }

    /**
     * Creates a new {@link ScopedException}.
     *
     * @param source The source {@link Scoped} instance.
     * @param context The {@link Scoped.Scope}'s context.
     * @param cause The cause of this exception.
     *
     * @since 0.1.0
     */
    ScopedException(final @NotNull Scoped<?> source, final @NotNull Object context, final @Nullable Throwable cause)
    {
        super(ScopedException.createMessage(source, context, null), cause);

        this.source = source;
        this.context = context;
    }

    /**
     * Creates a new {@link ScopedException}.
     *
     * @param source The source {@link Scoped} instance.
     * @param context The {@link Scoped.Scope}'s context.
     * @param message The exception's message.
     * @param cause The cause of this exception.
     *
     * @since 0.1.0
     */
    ScopedException(
        final @NotNull Scoped<?> source,
        final @NotNull Object context,
        final @Nullable String message,
        final @Nullable Throwable cause
    )
    {
        super(ScopedException.createMessage(source, context, message), cause);

        this.source = source;
        this.context = context;
    }

    /**
     * Creates a new message for an exception using the given source value and context.
     *
     * @param source The source {@link Scoped} instance.
     * @param context The {@link Scoped.Scope}'s context.
     * @param additionalMessage An additional message provided via the constructor.
     *
     * @return A new message.
     *
     * @since 0.1.0
     */
    private static @NotNull String createMessage(
        final @NotNull Scoped<?> source,
        final @NotNull Object context,
        final @Nullable String additionalMessage
    )
    {
        final @NotNull String typeName = source.getClass().getSimpleName();

        if (Objects.isNull(additionalMessage)) {
            return "Exception within '%s' scope (context: %s)".formatted(typeName, context);
        } else {
            return "Exception within '%s' scope (context: %s): %s".formatted(typeName, context, additionalMessage);
        }
    }

    /**
     * Returns the source {@link Scoped} instance.
     *
     * @return The source instance.
     *
     * @since 0.1.0
     */
    public final @NotNull Scoped<?> getSource() {
        return this.source;
    }

    /**
     * Returns the source {@link Scoped.Scope}'s context.
     *
     * @return The context.
     *
     * @since 0.1.0
     */
    public final @NotNull Object getContext() {
        return this.context;
    }

    /**
     * Returns {@code true} if this exception was thrown in the given scope.
     *
     * @param scope The scope to test against.
     *
     * @return Whether this was thrown in the given scope.
     *
     * @since 0.1.0
     */
    public boolean matchesScope(final @NotNull Scoped<?>.Scope scope) {
        return this.getSource().equals(scope.getSource()) && this.getContext().equals(scope.getContext());
    }

}
