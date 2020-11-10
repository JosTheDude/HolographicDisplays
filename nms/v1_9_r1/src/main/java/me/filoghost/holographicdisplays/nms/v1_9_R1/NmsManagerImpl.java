/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.holographicdisplays.nms.v1_9_R1;

import me.filoghost.holographicdisplays.api.line.HologramLine;
import me.filoghost.holographicdisplays.api.line.ItemLine;
import me.filoghost.holographicdisplays.nms.interfaces.CustomNameHelper;
import me.filoghost.holographicdisplays.nms.interfaces.ItemPickupManager;
import me.filoghost.holographicdisplays.nms.interfaces.NMSManager;
import me.filoghost.holographicdisplays.nms.interfaces.entity.NMSArmorStand;
import me.filoghost.holographicdisplays.nms.interfaces.entity.NMSEntityBase;
import me.filoghost.holographicdisplays.nms.interfaces.entity.NMSItem;
import me.filoghost.holographicdisplays.util.ConsoleLogger;
import me.filoghost.holographicdisplays.util.Validator;
import me.filoghost.holographicdisplays.util.reflection.ReflectField;
import net.minecraft.server.v1_9_R1.Entity;
import net.minecraft.server.v1_9_R1.EntityTypes;
import net.minecraft.server.v1_9_R1.MathHelper;
import net.minecraft.server.v1_9_R1.World;
import net.minecraft.server.v1_9_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.Map;

public class NmsManagerImpl implements NMSManager {
    
    private static final ReflectField<Map<Class<?>, String>> ENTITY_NAMES_BY_CLASS_FIELD = new ReflectField<>(EntityTypes.class, "d");
    private static final ReflectField<Map<Class<?>, Integer>> ENTITY_IDS_BY_CLASS_FIELD = new ReflectField<>(EntityTypes.class, "f");

    private Method validateEntityMethod;
    
    @Override
    public void setup() throws Exception {
        registerCustomEntity(EntityNMSArmorStand.class, "ArmorStand", 30);
        registerCustomEntity(EntityNMSItem.class, "Item", 1);
        registerCustomEntity(EntityNMSSlime.class, "Slime", 55);
        
        validateEntityMethod = World.class.getDeclaredMethod("b", Entity.class);
        validateEntityMethod.setAccessible(true);
    }
    
    public void registerCustomEntity(Class<?> entityClass, String name, int id) throws Exception {
        ENTITY_NAMES_BY_CLASS_FIELD.getStatic().put(entityClass, name);
        ENTITY_IDS_BY_CLASS_FIELD.getStatic().put(entityClass, Integer.valueOf(id));
    }
    
    @Override
    public NMSItem spawnNMSItem(org.bukkit.World bukkitWorld, double x, double y, double z, ItemLine parentPiece, ItemStack stack, ItemPickupManager itemPickupManager) {
        WorldServer nmsWorld = ((CraftWorld) bukkitWorld).getHandle();
        EntityNMSItem customItem = new EntityNMSItem(nmsWorld, parentPiece, itemPickupManager);
        customItem.setLocationNMS(x, y, z);
        customItem.setItemStackNMS(stack);
        if (!addEntityToWorld(nmsWorld, customItem)) {
            ConsoleLogger.handleSpawnFail(parentPiece);
        }
        return customItem;
    }
    
    @Override
    public EntityNMSSlime spawnNMSSlime(org.bukkit.World bukkitWorld, double x, double y, double z, HologramLine parentPiece) {
        WorldServer nmsWorld = ((CraftWorld) bukkitWorld).getHandle();
        EntityNMSSlime touchSlime = new EntityNMSSlime(nmsWorld, parentPiece);
        touchSlime.setLocationNMS(x, y, z);
        if (!addEntityToWorld(nmsWorld, touchSlime)) {
            ConsoleLogger.handleSpawnFail(parentPiece);
        }
        return touchSlime;
    }
    
    @Override
    public NMSArmorStand spawnNMSArmorStand(org.bukkit.World world, double x, double y, double z, HologramLine parentPiece, boolean broadcastLocationPacket) {
        WorldServer nmsWorld = ((CraftWorld) world).getHandle();
        EntityNMSArmorStand invisibleArmorStand = new EntityNMSArmorStand(nmsWorld, parentPiece);
        invisibleArmorStand.setLocationNMS(x, y, z, broadcastLocationPacket);
        if (!addEntityToWorld(nmsWorld, invisibleArmorStand)) {
            ConsoleLogger.handleSpawnFail(parentPiece);
        }
        return invisibleArmorStand;
    }
    
    private boolean addEntityToWorld(WorldServer nmsWorld, Entity nmsEntity) {
        Validator.isTrue(Bukkit.isPrimaryThread(), "Async entity add");
        
        final int chunkX = MathHelper.floor(nmsEntity.locX / 16.0);
        final int chunkZ = MathHelper.floor(nmsEntity.locZ / 16.0);
        
        if (!nmsWorld.getChunkProviderServer().isChunkLoaded(chunkX, chunkZ)) {
            // This should never happen
            nmsEntity.dead = true;
            return false;
        }
        
        nmsWorld.getChunkAt(chunkX, chunkZ).a(nmsEntity);
        nmsWorld.entityList.add(nmsEntity);
        
        try {
            validateEntityMethod.invoke(nmsWorld, nmsEntity);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    @Override
    public boolean isNMSEntityBase(org.bukkit.entity.Entity bukkitEntity) {
        return ((CraftEntity) bukkitEntity).getHandle() instanceof NMSEntityBase;
    }

    @Override
    public NMSEntityBase getNMSEntityBase(org.bukkit.entity.Entity bukkitEntity) {
        Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
        
        if (nmsEntity instanceof NMSEntityBase) {
            return ((NMSEntityBase) nmsEntity);
        } else {
            return null;
        }
    }
    
    @Override
    public NMSEntityBase getNMSEntityBaseFromID(org.bukkit.World bukkitWorld, int entityID) {
        WorldServer nmsWorld = ((CraftWorld) bukkitWorld).getHandle();
        Entity nmsEntity = nmsWorld.getEntity(entityID);
        
        if (nmsEntity instanceof NMSEntityBase) {
            return ((NMSEntityBase) nmsEntity);
        } else {
            return null;
        }
    }
    
    @Override
    public Object replaceCustomNameText(Object customNameObject, String target, String replacement) {
        return CustomNameHelper.replaceCustomNameString(customNameObject, target, replacement);
    }
    
}
