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

package dev.jaxydog.ochre;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ochre's common entrypoint.
 *
 * @author Jaxydog
 * @since 0.1.0
 */
public final class Ochre implements ModInitializer {

    /**
     * Ochre's mod identifier.
     *
     * @since 0.1.0
     */
    static final String MOD_ID = "ochre";
    /**
     * Ochre's primary logging instance.
     *
     * @since 0.1.0
     */
    static final Logger LOGGER = LoggerFactory.getLogger("Ochre");

    /**
     * Creates a new instance of this entrypoint.
     *
     * @since 0.1.0
     */
    public Ochre() { }

    @Override
    public void onInitialize() {
        final ModContainer mod = FabricLoader.getInstance().getModContainer(Ochre.MOD_ID).orElseThrow();
        final String name = mod.getMetadata().getName();
        final String version = mod.getMetadata().getVersion().getFriendlyString();

        Ochre.LOGGER.info("{} {} loaded!", name, version);
    }

}
