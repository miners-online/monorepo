package uk.minersonline.games.world_management;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.instance.block.Block;
import net.sandrohc.schematic4j.SchematicLoader;
import net.sandrohc.schematic4j.nbt.io.NBTUtil;
import net.sandrohc.schematic4j.nbt.io.NamedTag;
import net.sandrohc.schematic4j.nbt.tag.CompoundTag;
import net.sandrohc.schematic4j.schematic.Schematic;
import net.sandrohc.schematic4j.schematic.types.Pair;
import net.sandrohc.schematic4j.schematic.types.SchematicBlock;
import net.sandrohc.schematic4j.schematic.types.SchematicBlockPos;
import org.intellij.lang.annotations.Subst;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

/**
 * <p>A wrapper around schematic4J's Schematic class to facilitate using schematics in Minestom.</p>
 *
 * <p>Provides methods to load schematics from InputStreams, including GZIP-compressed streams, and to access block
 * data in a Minestom-friendly way.</p>
 *
 * @since 0.0.1
 */
public class MinestomSchematic {
    private final Schematic schematic;
    private final List<Pair<BlockVec, Block>> blocks;

    private MinestomSchematic(Schematic schematic) {
        this.schematic = schematic;
        this.blocks = new ArrayList<>();

        schematic.blocks().forEach((pair -> {
            SchematicBlockPos relative = pair.left;
            SchematicBlock block = pair.right;

            BlockVec pos = new BlockVec(relative.x(), relative.y(), relative.z());
            @Subst("minecraft:air") String name = block.name();
            Block b = Block.fromKey(Key.key(name));

            if (b == null) {
                b = Block.AIR;
            }
            for (Map.Entry<String, String> entry : block.states.entrySet()) {
                b = b.withProperty(entry.getKey(), entry.getValue());
            }
            this.blocks.add(new Pair<>(pos, b));
        }));
    }

    public BlockVec offset() {
        return new BlockVec(schematic.offset().x(), schematic.offset().y(), schematic.offset().z());
    }

    public int width() {
        return schematic.width();
    }

    public int height() {
        return schematic.height();
    }

    public int length() {
        return schematic.length();
    }

    public List<Pair<BlockVec, Block>> blocks() {
        return blocks;
    }

    public void apply(Consumer<Pair<BlockVec, Block>> consumer) {
        blocks.forEach(consumer);
    }

    /**
    * <p>Load a schematic from an InputStream. The stream is not closed by this method.</p>
    *
    * <p>Furthermore, this fixes a bug in schematic4J where it doesn't find the root tag correctly.</p>
    * @param is InputStream to read the schematic from
    * @return MinestomSchematic object
    * @throws Exception if an error occurs while reading the schematic
    */
    public static MinestomSchematic load(InputStream is) throws Exception {
        final NamedTag rootTag = NBTUtil.Reader.read().from(is);
        CompoundTag schematicRoot = rootTag!=null&& rootTag.getTag() instanceof CompoundTag? (CompoundTag) rootTag.getTag() :null;

        if (schematicRoot == null) {
            throw new IllegalArgumentException("Invalid schematic file: Root tag is not a CompoundTag");
        }

        boolean hasSchematicTag = schematicRoot.containsKey("Schematic");
        if (hasSchematicTag) {
            schematicRoot = schematicRoot.getCompoundTag("Schematic");
        }

        Schematic schematic = SchematicLoader.parse(schematicRoot);
        return new MinestomSchematic(schematic);
    }

    /**
    * <p>Load a compressed schematic from an InputStream. The stream is not closed by this method.</p>
    *
    * <p>This is a convenience method that wraps the InputStream in a GZIPInputStream and calls {@link #load(InputStream)}.</p>
    *
    * @param is InputStream to read the compressed schematic from
    * @return MinestomSchematic object
    * @throws Exception if an error occurs while reading the schematic
    */
    public static MinestomSchematic loadGzip(InputStream is) throws Exception {
        GZIPInputStream gis = new GZIPInputStream(is);
        return load(gis);
    }
}
