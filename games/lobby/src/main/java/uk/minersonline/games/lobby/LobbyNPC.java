package uk.minersonline.games.lobby;

import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.entity.metadata.avatar.MannequinMeta;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.event.entity.EntitySpawnEvent;
import net.minestom.server.network.player.ResolvableProfile;
import net.minestom.server.tag.Tag;
import uk.minersonline.games.message_exchange.proxy.ServerInfo;

public class LobbyNPC extends EntityCreature {
    public static final Tag<String> NPC_TAG = Tag.String("lobby:server_npc");
    public static final Tag<UUID> NPC_TEXT_PARENT = Tag.UUID("lobby:server_npc_text_parent");

    public LobbyNPC(String serverName, String skinName) {
        super(EntityType.MANNEQUIN);

        editEntityMeta(MannequinMeta.class, meta -> {
            ResolvableProfile profile = new ResolvableProfile(PlayerSkin.fromUsername(skinName));
            meta.setProfile(profile);
            meta.setDescription(null);
        });

        this.setTag(NPC_TAG, serverName);
        this.setNoGravity(true);
        this.setCustomNameVisible(false);

        this.eventNode().addListener(EntitySpawnEvent.class, (event) -> {
            if (event.getEntity() != this) return;
            Entity textDisplay = new Entity(EntityType.TEXT_DISPLAY);
            textDisplay.setTag(NPC_TEXT_PARENT, this.getUuid());

            Component initialText = Component.text(this.getTag(NPC_TAG)).color(NamedTextColor.YELLOW).append(
                Component.text("\nUNKNOWN").color(NamedTextColor.GRAY)
            );

            textDisplay.editEntityMeta(TextDisplayMeta.class, meta -> {
                meta.setText(initialText);
                meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
            });
            textDisplay.setVelocity(new Vec(0));
            textDisplay.setNoGravity(true);

            textDisplay.setInstance(this.getInstance(), this.getPosition().add(0, 2.5, 0));
        });
    }

    public void refresh(ServerInfo serverInfo) {
        this.getInstance().getEntities().stream()
            .filter(e -> e.getTag(NPC_TEXT_PARENT) != null)
            .filter(e -> e.getTag(NPC_TEXT_PARENT).equals(this.getUuid()))
            .forEach(e -> {
                updateTextDisplay(e, serverInfo);
            });
    }

    private void updateTextDisplay(Entity entity, ServerInfo serverInfo) {
        Component statusComponent;
        if (serverInfo == null) {
            statusComponent = Component.text(this.getTag(NPC_TAG)).color(NamedTextColor.YELLOW).append(
                Component.text("\nUNKNOWN").color(NamedTextColor.GRAY)
            );
        } else {
            if (serverInfo.isAlive()) {
                statusComponent = 
                Component.text(this.getTag(NPC_TAG)).color(NamedTextColor.YELLOW).append(
                    Component.text("\nONLINE\n").color(NamedTextColor.GREEN).append(
                    Component.text("Players: " + serverInfo.getPlayerCount()).color(NamedTextColor.WHITE)
                ));
            } else {
                statusComponent = Component.text(this.getTag(NPC_TAG)).color(NamedTextColor.YELLOW).append(
                    Component.text("\nOFFLINE").color(NamedTextColor.RED)
                );
            }
        }

        entity.editEntityMeta(TextDisplayMeta.class, meta -> {
            meta.setText(statusComponent);
        });
    }
}
