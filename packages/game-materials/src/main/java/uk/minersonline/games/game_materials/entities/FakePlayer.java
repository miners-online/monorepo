package uk.minersonline.games.game_materials.entities;

import net.minestom.server.entity.*;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class FakePlayer extends Entity {
    private final String username;
    private final PlayerSkin skin;

    public FakePlayer(@NotNull String username, @NotNull String skinName) {
        super(EntityType.PLAYER);
        this.username = username;

        this.skin = PlayerSkin.fromUsername(skinName);

        setNoGravity(true);
    }

    @Override
    public void updateNewViewer(@NotNull Player player) {
        var properties = new ArrayList<PlayerInfoUpdatePacket.Property>();
        properties.add(new PlayerInfoUpdatePacket.Property("textures", skin.textures(), skin.signature()));
        var entry = new PlayerInfoUpdatePacket.Entry(getUuid(), username, properties, false,
                0, GameMode.SURVIVAL, null, null, 0, true);
        player.sendPacket(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.ADD_PLAYER, entry));

        // Spawn the player entity
        super.updateNewViewer(player);

        // Enable skin layers
        player.sendPackets(new EntityMetaDataPacket(getEntityId(), Map.of(17, Metadata.Byte((byte) 127))));
    }

    @Override
    public void updateOldViewer(@NotNull Player player) {
        super.updateOldViewer(player);

        player.sendPacket(new PlayerInfoRemovePacket(getUuid()));
    }
}
