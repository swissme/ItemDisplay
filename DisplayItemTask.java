package com.orbixmc.prisoncore.core.commands;

import com.comphenix.protocol.*;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.*;
import com.orbixmc.prisoncore.*;
import it.unimi.dsi.fastutil.ints.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.scheduler.*;
import org.bukkit.util.Vector;

import java.lang.reflect.*;
import java.util.*;

/**
 * @author Swiss (swiss@swissdev.com)
 */
public class DisplayItemTask extends BukkitRunnable {

    int tick = 0;
    int entityId;
    UUID entityUUID;
    Player player;
    Location oldLocation;
    ItemStack itemStack;

    public DisplayItemTask(int entityId, UUID entityUUID, Player player, Location oldLocation, ItemStack itemStack) {
        this.entityId = entityId;
        this.entityUUID = entityUUID;
        this.player = player;
        this.oldLocation = oldLocation;
        this.itemStack = itemStack;
    }

    @Override
    public void run() {
        if(tick == 100) {
            PacketContainer destroyEntity = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
            destroyEntity.getModifier().write(0, new IntArrayList(new int[]{entityId}));
            try {
                PrisonCore.getInstance().getProtocolManager().sendServerPacket(player, destroyEntity);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            cancel();
            return;
        }

        PacketContainer setVelocityPacket = PrisonCore.getInstance().getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_VELOCITY);

        setVelocityPacket.getIntegers().write(0, entityId);

        Location location = player.getLocation().add(player.getLocation().getDirection().multiply(2));

        Vector pos = oldLocation.toVector();
        oldLocation = location;
        Vector endPoint = location.toVector();
        Vector velocity = endPoint.subtract(pos);

        setVelocityPacket.getIntegers().write(1, (int) (velocity.getX() * 2000.0D));
        setVelocityPacket.getIntegers().write(2, (int) (velocity.getY() * 2000.0D));
        setVelocityPacket.getIntegers().write(3, (int) (velocity.getZ() * 2000.0D));

        try {
            PrisonCore.getInstance().getProtocolManager().sendServerPacket(player, setVelocityPacket);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        WrappedDataWatcher entityMetaData = new WrappedDataWatcher();

        entityMetaData.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), true);
        entityMetaData.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(8, WrappedDataWatcher.Registry.getItemStackSerializer(false)), itemStack);

        PacketContainer metaDataPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        metaDataPacket.getIntegers().write(0, entityId);
        metaDataPacket.getWatchableCollectionModifier().write(0, entityMetaData.getWatchableObjects());

        try {
            PrisonCore.getInstance().getProtocolManager().sendServerPacket(player, metaDataPacket);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        tick += 4;
    }

}
