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

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Objects;
import java.util.Optional;

/**
 * Implements scoped execution with context-aware exception wrappers.
 *
 * @param <C> The context type.
 *
 * @author Jaxydog
 * @since 0.1.0
 */
@Internal
public abstract class Scoped<C> {

    /**
     * The currently active scope.
     *
     * @since 0.1.0
     */
    private volatile @Nullable Scope currentScope;

    /**
     * Creates a new {@link Scoped}.
     *
     * @since 0.1.0
     */
    protected Scoped() { }

    /**
     * Returns the current {@link Scope}, if one is active.
     *
     * @return The current scope.
     *
     * @since 0.1.0
     */
    public final @NotNull Optional<Scope> getScope() {
        return Optional.ofNullable(this.currentScope);
    }

    /**
     * Returns the current {@link Scope}'s context, if one is active.
     *
     * @return The current context.
     *
     * @since 0.1.0
     */
    public final @NotNull Optional<C> getContext() {
        return this.getScope().map(Scope::getContext);
    }

    /**
     * Creates a new {@link Scope}.
     *
     * @param context The scope's context.
     *
     * @return A new scope.
     *
     * @since 0.1.0
     */
    protected final @NotNull Scope createScope(final @NotNull C context) {
        return this.new Scope(context);
    }

    /**
     * Runs the given function within a temporary scope.
     *
     * @param context The scope's context.
     * @param runnable The function to run.
     *
     * @throws IllegalStateException If another scope is currently active.
     * @throws ScopedException If the given function throws.
     * @since 0.1.0
     */
    protected final void runScoped(
        final @NotNull C context,
        final @NotNull FallibleRunnable<? extends Exception> runnable
    )
        throws @NotNull IllegalStateException, @NotNull ScopedException
    {
        this.runScoped(this.createScope(context), runnable);
    }

    /**
     * Runs the given function within a temporary scope.
     *
     * @param context The scope's context.
     * @param supplier The function to run.
     * @param <T> The function's return type.
     *
     * @return The function's return value.
     *
     * @throws IllegalStateException If another scope is currently active.
     * @throws ScopedException If the given function throws.
     * @since 0.1.0
     */
    protected final <T> @UnknownNullability T runScoped(
        final @NotNull C context,
        final @NotNull FallibleSupplier<T, ? extends Exception> supplier
    )
        throws @NotNull IllegalStateException, @NotNull ScopedException
    {
        return this.runScoped(this.createScope(context), supplier);
    }

    /**
     * Runs the given function within a scope.
     *
     * @param scope The scope.
     * @param runnable The function to run.
     *
     * @throws IllegalStateException If another scope is currently active.
     * @throws ScopedException If the given function throws.
     * @since 0.1.0
     */
    protected final void runScoped(
        final @NotNull Scope scope,
        final @NotNull FallibleRunnable<? extends Exception> runnable
    )
        throws @NotNull IllegalStateException, @NotNull ScopedException
    {
        scope.enter();
        scope.run(runnable);
        scope.exit();
    }

    /**
     * Runs the given function within a scope.
     *
     * @param scope The scope.
     * @param supplier The function to run.
     * @param <T> The function's return type.
     *
     * @return The function's return value.
     *
     * @throws IllegalStateException If another scope is currently active.
     * @throws ScopedException If the given function throws.
     * @since 0.1.0
     */
    protected final <T> @UnknownNullability T runScoped(
        final @NotNull Scope scope,
        final @NotNull FallibleSupplier<T, ? extends Exception> supplier
    )
        throws @NotNull IllegalStateException, @NotNull ScopedException
    {
        final @UnknownNullability T value;

        scope.enter();
        value = scope.run(supplier);
        scope.exit();

        return value;
    }

    /**
     * A temporary scope.
     *
     * @since 0.1.0
     */
    @Internal
    public final class Scope {

        /**
         * The scope's context.
         *
         * @since 0.1.0
         */
        private final @NotNull C context;

        /**
         * Creates a new {@link Scope} with the given context.
         *
         * @param context The scope's context.
         *
         * @since 0.1.0
         */
        private Scope(final @NotNull C context) {
            this.context = context;
        }

        /**
         * Throws an exception if this {@link Scope} is not currently active.
         *
         * @throws IllegalStateException If this scope is not active.
         * @since 0.1.0
         */
        public void requireActive()
            throws @NotNull IllegalStateException
        {
            if (Objects.isNull(Scoped.this.currentScope)) {
                throw new IllegalStateException("A scope is not active.");
            } else if (Objects.equals(Scoped.this.currentScope, this)) {
                throw new IllegalStateException("This scope is not active.");
            }
        }

        /**
         * Throws an exception if this {@link Scope} is currently active.
         *
         * @throws IllegalStateException If this scope is active.
         * @since 0.1.0
         */
        public void requireInactive()
            throws @NotNull IllegalStateException
        {
            if (Objects.equals(Scoped.this.currentScope, this)) {
                throw new IllegalStateException("This scope is active.");
            }
        }

        /**
         * Returns {@code true} if this {@link Scope} is currently active.
         *
         * @return Whether this scope is active.
         *
         * @since 0.1.0
         */
        public boolean isActive() {
            return Objects.equals(Scoped.this.currentScope, this);
        }

        /**
         * Returns {@code true} if this {@link Scope} is not currently active.
         *
         * @return Whether this scope is inactive.
         *
         * @since 0.1.0
         */
        public boolean isInactive() {
            return Objects.isNull(Scoped.this.currentScope) || !Objects.equals(Scoped.this.currentScope, this);
        }

        /**
         * Returns this scope's source instance.
         *
         * @return This scope's source.
         *
         * @since 0.1.0
         */
        public @NotNull Scoped<C> getSource() {
            return Scoped.this;
        }

        /**
         * Returns this scope's configured context.
         *
         * @return This scope's context.
         *
         * @since 0.1.0
         */
        public @NotNull C getContext() {
            return this.context;
        }

        /**
         * Wraps the given exception in a {@link ScopedException}.
         *
         * @param exception The exception to wrap.
         * @param <E> The exception's type.
         *
         * @return A new {@link ScopedException}, or the given exception if it is a {@link ScopedException} that matches
         * this scope.
         *
         * @since 0.1.0
         */
        private <E extends Exception> @NotNull ScopedException wrapException(final @UnknownNullability E exception) {
            if (exception instanceof final @NotNull ScopedException scoped && scoped.matchesScope(this)) {
                return scoped;
            } else {
                return new ScopedException(this.getSource(), this.getContext(), exception);
            }
        }

        /**
         * Runs the given function.
         *
         * @param runnable The function to run.
         *
         * @throws IllegalStateException If this scope is not currently active.
         * @throws ScopedException If the given function throws.
         * @since 0.1.0
         */
        public void run(final @NotNull FallibleRunnable<? extends Exception> runnable)
            throws @NotNull IllegalStateException, @NotNull ScopedException
        {
            this.requireActive();

            Fallible.wrapThrown(runnable, this::wrapException);
        }

        /**
         * Runs the given function.
         *
         * @param supplier The function to run.
         * @param <T> The function's return type.
         *
         * @return The function's return value.
         *
         * @throws IllegalStateException If this scope is not currently active.
         * @throws ScopedException If the given function throws.
         * @since 0.1.0
         */
        public <T> @UnknownNullability T run(final @NotNull FallibleSupplier<T, ? extends Exception> supplier)
            throws @NotNull IllegalStateException, @NotNull ScopedException
        {
            this.requireActive();

            return Fallible.wrapThrown(supplier, this::wrapException);
        }

        /**
         * Enters the current scope.
         *
         * @throws IllegalStateException If this or another scope is already active.
         * @since 0.1.0
         */
        public synchronized void enter()
            throws @NotNull IllegalStateException
        {
            this.requireInactive();

            if (Objects.isNull(Scoped.this.currentScope)) {
                Scoped.this.currentScope = this;
            } else {
                throw new IllegalStateException("A scope is already active.");
            }
        }

        /**
         * Exits the current scope.
         *
         * @throws IllegalStateException If this is not the currently active scope.
         * @since 0.1.0
         */
        public synchronized void exit()
            throws @NotNull IllegalStateException
        {
            this.requireActive();

            Scoped.this.currentScope = null;
        }

    }

}
