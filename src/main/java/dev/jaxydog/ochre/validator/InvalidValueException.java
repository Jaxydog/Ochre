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

/**
 * Thrown when a value was deemed invalid by a {@link Validator}.
 *
 * @author Jaxydog
 * @since 0.1.0
 */
public class InvalidValueException extends RuntimeException {

    /**
     * Creates a new {@link InvalidValueException}.
     *
     * @param expected The expected value.
     * @param received The received value.
     *
     * @since 0.1.0
     */
    public InvalidValueException(final @NotNull String expected, final @NotNull String received) {
        super("Value failed validation; expected '%s', received '%s'".formatted(expected, received));
    }

}
