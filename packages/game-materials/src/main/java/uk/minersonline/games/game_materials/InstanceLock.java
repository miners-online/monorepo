package uk.minersonline.games.game_materials;

import java.util.UUID;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.*;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.tag.Tag;

public class InstanceLock {
    public static final Tag<UUID> BLOCK_LOCK_TAG = Tag.UUID("block_lock");
    public static final Tag<UUID> ITEM_LOCK_TAG = Tag.UUID("item_lock");
    private final InstanceContainer instance;

    public InstanceLock(InstanceContainer instance) {
        this.instance = instance;
        EventNode<InstanceEvent> node = this.instance.eventNode();
        
        // Block interaction locks
        node.addListener(PlayerBlockBreakEvent.class, event -> {
            Player player = event.getPlayer();
            if (player.hasTag(BLOCK_LOCK_TAG)) {
                event.setCancelled(true);
            }
        });

        node.addListener(PlayerBlockInteractEvent.class, event -> {
            Player player = event.getPlayer();
            if (player.hasTag(BLOCK_LOCK_TAG)) {
                event.setCancelled(true);
            }
        });

        node.addListener(PlayerBlockPlaceEvent.class, event -> {
            Player player = event.getPlayer();
            if (player.hasTag(BLOCK_LOCK_TAG)) {
                event.setCancelled(true);
            }
        });

        node.addListener(PlayerStartDiggingEvent.class, event -> {
            Player player = event.getPlayer();
            if (player.hasTag(BLOCK_LOCK_TAG)) {
                event.setCancelled(true);
            }
        });

        // Item interaction locks
        node.addListener(PlayerPreEatEvent.class, event -> {
            Player player = event.getPlayer();
            if (player.hasTag(ITEM_LOCK_TAG)) {
                event.setCancelled(true);
            }
        });

        node.addListener(PlayerSwapItemEvent.class, event -> {
            Player player = event.getPlayer();
            if (player.hasTag(ITEM_LOCK_TAG)) {
                event.setCancelled(true);
            }
        });

        node.addListener(PlayerUseItemEvent.class, event -> {
            Player player = event.getPlayer();
            if (player.hasTag(ITEM_LOCK_TAG)) {
                event.setCancelled(true);
            }
        });
    }

    public InstanceContainer getInstance() {
        return instance;
    }

    public static void lockPlayerBlock(Player player) {
        player.setTag(BLOCK_LOCK_TAG, player.getUuid());
    }

    public static void unlockPlayerBlock(Player player) {
        player.removeTag(BLOCK_LOCK_TAG);
    }

    public static void lockPlayerItem(Player player) {
        player.setTag(ITEM_LOCK_TAG, player.getUuid());
    }

    public static void unlockPlayerItem(Player player) {
        player.removeTag(ITEM_LOCK_TAG);
    }
}
