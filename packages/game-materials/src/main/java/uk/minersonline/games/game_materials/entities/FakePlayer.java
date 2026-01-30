package uk.minersonline.games.game_materials.entities;

import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.avatar.MannequinMeta;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.network.player.ResolvableProfile;

import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FakePlayer extends Entity {
    private final Component description;
    private final @Nullable ResolvableProfile profile;
    private @Nullable Function<Player, Void> onInteract;

    public FakePlayer(@NotNull Component description, @Nullable String skinName) {
        super(EntityType.MANNEQUIN);
        this.description = description;

        MannequinMeta meta = (MannequinMeta) this.getEntityMeta();
        if (skinName != null) {
            PlayerSkin skin = PlayerSkin.fromUsername(skinName);
            this.profile = new ResolvableProfile(skin);
            meta.setProfile(this.profile);
        } else {
            this.profile = null;
        }

        this.set(DataComponents.CUSTOM_NAME, this.description);

        this.eventNode().addListener(PlayerEntityInteractEvent.class, event -> {
            if (event.getTarget().getEntityId() == this.getEntityId()) {
                if (onInteract != null) {
                    onInteract.apply(event.getPlayer());
                }
            }
        });
    }

    public void setOnInteract(@Nullable Function<Player, Void> onInteract) {
        this.onInteract = onInteract;
    }
}
