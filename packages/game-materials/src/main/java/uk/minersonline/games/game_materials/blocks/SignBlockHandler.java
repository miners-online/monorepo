package uk.minersonline.games.game_materials.blocks;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.network.packet.server.play.OpenSignEditorPacket;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.registry.TagKey;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class SignBlockHandler implements BlockHandler {
    @Override
    public @NotNull Key getKey() {
        return Key.key("miners_online:sign");
    }

    @Override
    public boolean onInteract(Interaction interaction) {
        interaction.getPlayer().sendPacket(
                new OpenSignEditorPacket(
                        interaction.getBlockPosition(),
                        true
                )
        );

        return true;
    }

    @Override
    public @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return List.of(
                Tag.NBT("front_text"),
                Tag.NBT("back_text"),
                Tag.Boolean("is_waxed")
        );
    }

    public static void register() {
        BlockManager blockManager = MinecraftServer.getBlockManager();
        RegistryTag<Block> tag = Block.staticRegistry().getTag(TagKey.ofHash("#minecraft:all_signs"));
        SignBlockHandler signHandler = new SignBlockHandler();
        for (RegistryKey<Block> key : Objects.requireNonNull(tag)) {
            blockManager.registerHandler(key.key(), () -> signHandler);
        }
    }
}
