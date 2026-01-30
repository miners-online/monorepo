package uk.minersonline.games.game_materials.entities;

import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.avatar.MannequinMeta;
import net.minestom.server.network.player.ResolvableProfile;

import org.jetbrains.annotations.NotNull;

public class FakePlayer extends Entity {
    private final Component description;
    private final ResolvableProfile profile;

    public FakePlayer(@NotNull Component description, @NotNull String skinName) {
        super(EntityType.MANNEQUIN);
        this.description = description;

        PlayerSkin skin = PlayerSkin.fromUsername(skinName);
        this.profile = new ResolvableProfile(skin);

        setNoGravity(true);
        MannequinMeta meta = (MannequinMeta) this.getEntityMeta();
        meta.setProfile(this.profile);
        this.set(DataComponents.CUSTOM_NAME, this.description);
    }
}
