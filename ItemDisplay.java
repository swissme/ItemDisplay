package com.orbixmc.prisoncore.core.utils;

import com.comphenix.protocol.*;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.*;
import com.orbixmc.prisoncore.*;
import com.orbixmc.prisoncore.core.commands.*;
import lombok.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * @author Swiss (swiss@swissdev.com)
 */

@Getter
@Setter
public class ItemDisplay {

    private ItemStack itemStack;
    private int tick = 0;
    private int entityId;
    private UUID entityUUID;
    private Player player;
    private Location oldLocation;

    public ItemDisplay(Player player, Location oldLocation, ItemStack itemStack) {
        this.player = player;
        this.oldLocation = oldLocation;
        this.itemStack = itemStack;

        Location location = player.getLocation().add(player.getLocation().getDirection().multiply(2));
        int entityId = new Random().nextInt(2000);
        UUID entityUUID = UUID.randomUUID();

        PacketContainer spawnPacket = PrisonCore.getInstance().getProtocolManager().createPacket(PacketType.Play.Server.SPAWN_ENTITY);

        spawnPacket.getIntegers().write(0, entityId);
        spawnPacket.getUUIDs().write(0, entityUUID);
        spawnPacket.getIntegers().write(1, 83);
        spawnPacket.getDoubles().write(0, location.getX());
        spawnPacket.getDoubles().write(1, location.getY() + 1.5);
        spawnPacket.getDoubles().write(2, location.getZ());
        spawnPacket.getEntityTypeModifier().write(0, EntityType.SNOWBALL);

        try {
            PrisonCore.getInstance().getProtocolManager().sendServerPacket(player, spawnPacket);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        WrappedDataWatcher entityMetaData = new WrappedDataWatcher();

        entityMetaData.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), true);
        entityMetaData.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(8, WrappedDataWatcher.Registry.getItemStackSerializer(false)), player.getItemInHand());

        PacketContainer metaDataPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        metaDataPacket.getIntegers().write(0, entityId);
        metaDataPacket.getWatchableCollectionModifier().write(0, entityMetaData.getWatchableObjects());

        try {
            PrisonCore.getInstance().getProtocolManager().sendServerPacket(player, metaDataPacket);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        new DisplayItemTask(entityId, entityUUID, player, location, itemStack).runTaskTimer(PrisonCore.getInstance(), 0, 4);
    }

}
