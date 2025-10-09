package uk.minersonline.games.world_management;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.DimensionType;
import org.intellij.lang.annotations.Subst;
import uk.minersonline.games.server_bootstrap.feature.Feature;
import uk.minersonline.games.server_bootstrap.feature.FeatureImplementation;
import uk.minersonline.games.server_bootstrap.game.Game;


@FeatureImplementation(
    id = "miners_online:world_management",
    version = "0.0.1"
)
public class WorldManagement implements Feature {
    private static Instance worldInstance;
    private GlobalEventHandler geh;

    @Override
    public void onInit(Game game) {
        geh = MinecraftServer.getGlobalEventHandler();
    }

    @Override
    public void onStart() {
        geh.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            if (worldInstance != null) {
                event.setSpawningInstance(worldInstance);
            } else {
                event.getPlayer().kick(
                        Component.text("No world instance available, please contact the server administrator.")
                                .color(NamedTextColor.RED)
                );
            }
        });
    }

    @Override
    public void onStop() {

    }

    /**
     * <p>Set the default Instance that players will spawn in when they join the server.</p>
     * @param worldInstance Instance to set as the default
     */
    public static void setDefaultInstance(Instance worldInstance) {
        WorldManagement.worldInstance = worldInstance;
    }

    /**
     * <p>Create an InstanceContainer from a MinestomSchematic.</p>
     *
     * <p>The MinestomSchematic will be placed with its offset centered on (0, 0) in the world with the
     * bottom being aligned at y=0.</p>
     * @param schematic MinestomSchematic to create the instance from
     * @param type DimensionType of the instance
     * @return InstanceContainer created from the schematic
     */
    public static InstanceContainer instanceFromSchematic(MinestomSchematic schematic, DimensionType type) {
        @Subst("100") long timel = System.currentTimeMillis();
        String newId = "world_" + timel;
        RegistryKey<DimensionType> dimKey =
                MinecraftServer.getDimensionTypeRegistry().register(Key.key("miners_online:"+newId), type);
        InstanceContainer instance = MinecraftServer.getInstanceManager().createInstanceContainer(dimKey);

        final BlockVec offset = schematic.offset(); // reference point in schematic
        schematic.apply((pair -> {
            BlockVec relative = pair.left;
            Block block = pair.right;

            int absX = (int) (relative.x() + offset.x());
            int absY = (int) relative.y(); // bottom-aligned
            int absZ = (int) (relative.z() + offset.z());

            BlockVec pos = new BlockVec(absX, absY, absZ);
            instance.setBlock(pos, block);
        }));

        return instance;
    }

}
