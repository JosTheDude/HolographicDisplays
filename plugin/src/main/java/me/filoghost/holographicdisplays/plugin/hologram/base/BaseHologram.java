/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.holographicdisplays.plugin.hologram.base;

import me.filoghost.fcommons.Preconditions;
import me.filoghost.holographicdisplays.plugin.hologram.tracking.LineTrackerManager;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseHologram extends BaseHologramComponent {

    private final WorldAwareHologramPosition position;
    private final LineTrackerManager lineTrackerManager;

    public BaseHologram(BaseHologramPosition position, LineTrackerManager lineTrackerManager) {
        this.position = new WorldAwareHologramPosition(position);
        this.lineTrackerManager = lineTrackerManager;
    }

    protected abstract BaseHologramLines<? extends EditableHologramLine> getLines();

    protected abstract boolean isVisibleTo(Player player);

    public abstract Plugin getCreatorPlugin();

    protected final LineTrackerManager getTrackerManager() {
        return lineTrackerManager;
    }

    @Override
    public final void setDeleted() {
        super.setDeleted();
        getLines().setDeleted();
    }

    public BaseHologramPosition getBasePosition() {
        return position.toBasePosition();
    }

    public @Nullable World getPositionWorldIfLoaded() {
        return position.getWorldIfLoaded();
    }

    public @NotNull String getPositionWorldName() {
        return position.getWorldName();
    }

    public double getPositionX() {
        return position.getX();
    }

    public double getPositionY() {
        return position.getY();
    }

    public double getPositionZ() {
        return position.getZ();
    }

    public void setPosition(@NotNull BaseHologramPosition position) {
        Preconditions.notNull(position, "position");
        setPosition(position.getWorldName(), position.getX(), position.getY(), position.getZ());
    }

    public void setPosition(@NotNull Location location) {
        Preconditions.notNull(location, "location");
        Preconditions.notNull(location.getWorld(), "location's world");
        setPosition(location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
    }

    public void setPosition(@NotNull World world, double x, double y, double z) {
        Preconditions.notNull(world, "world");
        setPosition(world.getName(), x, y, z);
    }

    public void setPosition(@NotNull String worldName, double x, double y, double z) {
        Preconditions.notNull(worldName, "worldName");
        checkNotDeleted();

        position.set(worldName, x, y, z);
        getLines().updateLinePositions();
    }

    protected void onWorldLoad(World world) {
        position.onWorldLoad(world);
    }

    protected void onWorldUnload(World world) {
        position.onWorldUnload(world);
    }

    protected void onChunkLoad(Chunk chunk) {
        position.onChunkLoad(chunk);
    }

    protected void onChunkUnload(Chunk chunk) {
        position.onChunkUnload(chunk);
    }

    protected boolean isInLoadedChunk() {
        return position.isChunkLoaded();
    }

    @Override
    public String toString() {
        return "Hologram{"
                + "position=" + position
                + ", lines=" + getLines()
                + ", deleted=" + isDeleted()
                + "}";
    }

}
