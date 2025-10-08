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
import net.sandrohc.schematic4j.SchematicLoader;
import net.sandrohc.schematic4j.nbt.io.NBTUtil;
import net.sandrohc.schematic4j.nbt.io.NamedTag;
import net.sandrohc.schematic4j.nbt.tag.CompoundTag;
import net.sandrohc.schematic4j.schematic.Schematic;
import net.sandrohc.schematic4j.schematic.types.SchematicBlock;
import net.sandrohc.schematic4j.schematic.types.SchematicBlockPos;
import org.intellij.lang.annotations.Subst;
import uk.minersonline.games.server_bootstrap.feature.Feature;
import uk.minersonline.games.server_bootstrap.feature.FeatureImplementation;
import uk.minersonline.games.server_bootstrap.game.Game;

import java.io.InputStream;
import java.util.zip.GZIPInputStream;

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
     * <p>Create an InstanceContainer from a Schematic.</p>
     *
     * <p>The Schematic will be placed with its offset centered on (0, 0) in the world with the Schematic's bottom
     * being aligned at y=0.</p>
     * @param schematic Schematic to create the instance from
     * @param type DimensionType of the instance
     * @return InstanceContainer created from the schematic
     */
    public static InstanceContainer instanceFromSchematic(Schematic schematic, DimensionType type) {
        @Subst("100") long timel = System.currentTimeMillis();
        String newId = "world_" + timel;
        RegistryKey<DimensionType> dimKey =
                MinecraftServer.getDimensionTypeRegistry().register(Key.key("miners_online:"+newId), type);
        InstanceContainer instance = MinecraftServer.getInstanceManager().createInstanceContainer(dimKey);

        final SchematicBlockPos offset = schematic.offset(); // reference point in schematic
        schematic.blocks().forEach((pair -> {
            SchematicBlockPos relative = pair.left;
            @Subst("") SchematicBlock block = pair.right;

            int absX = (relative.x() + offset.x());
            int absY = relative.y(); // bottom-aligned
            int absZ = (relative.z() + offset.z());

            BlockVec pos = new BlockVec(absX, absY, absZ);
            @Subst("minecraft:air") String name = block.name();
            Block b = Block.fromKey(Key.key(name));

            if (b == null) {
                b = Block.AIR;
            }
            instance.setBlock(pos, b);
        }));

        return instance;
    }

    /**
     * <p>Load a schematic from an InputStream. The stream is not closed by this method.</p>
     *
     * <p>Furthermore, this fixes a bug in schematic4J where it doesn't find the root tag correctly.</p>
     * @param is InputStream to read the schematic from
     * @return Schematic object
     * @throws Exception if an error occurs while reading the schematic
     */
    public static Schematic loadFromInputStream(InputStream is) throws Exception {
        final NamedTag rootTag = NBTUtil.Reader.read().from(is);
        final CompoundTag nbt = rootTag!=null&& rootTag.getTag() instanceof CompoundTag? (CompoundTag) rootTag.getTag() :null;
        final CompoundTag schematicRoot = nbt.getCompoundTag("Schematic");
        Schematic schematic = SchematicLoader.parse(schematicRoot);
        return schematic;
    }

    /**
     * <p>Load a compressed schematic from an InputStream. The stream is not closed by this method.</p>
     *
     * <p>This is a convenience method that wraps the InputStream in a GZIPInputStream and calls {@link #loadFromInputStream(InputStream)}.</p>
     *
     * @param is InputStream to read the compressed schematic from
     * @return Schematic object
     * @throws Exception if an error occurs while reading the schematic
     */
    public static Schematic loadCompressedFromInputStream(InputStream is) throws Exception {
        GZIPInputStream gis = new GZIPInputStream(is);
        return loadFromInputStream(gis);
    }
}
