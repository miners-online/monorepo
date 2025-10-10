package uk.minersonline.games.world_management;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.*;
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
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

/**
 * <p>A wrapper around schematic4J's Schematic class to facilitate using schematics in Minestom.</p>
 *
 * <p>Provides methods to load schematics from InputStreams, including GZIP-compressed streams, and to access block
 * (and block entity) data in a Minestom-friendly way.</p>
 *
 * <p>This wrapper fixes a number of bugs in schematic4J including:
 * <ul>
 *     <li>Correctly finding the root tag in schematics that have a "Schematic" compound tag.</li>
 *     <li>Properly removing block states from the block name -
 *     <i>even though schematic4J has a separate states getter it still appends the states to the block name</i></li>
 *     <li>Use the correct "Data" sub tag for block NBT data</li>
 * </ul>
 * </p>
 *
 * @since 0.0.1
 */
public class MinestomSchematic {
    private final Schematic schematic;
    private final List<Pair<BlockVec, ParsedBlock>> blocks;

    private MinestomSchematic(Schematic schematic) {
        this.schematic = schematic;

        List<Pair<BlockVec, ParsedBlock>> blocksToAdd = new ArrayList<>();

        schematic.blocks().forEach((pair -> {
            SchematicBlockPos relative = pair.left;
            SchematicBlock block = pair.right;

            BlockVec pos = new BlockVec(relative.x(), relative.y(), relative.z());
            String name = block.name();
            ParsedBlock b = parseBlockState(name, block.states());

            blocksToAdd.add(new Pair<>(pos, b));
        }));

        schematic.blockEntities().forEach((be) -> {
            TreeMap<String, Object> data = (TreeMap<String, Object>) be.data;
            CompoundBinaryTag nbt = fromTreeMap((TreeMap<String, Object>) data.get("Data"));
            SchematicBlockPos relative = be.pos;
            BlockVec pos = new BlockVec(relative.x(), relative.y(), relative.z());
            blocksToAdd.stream().filter(p -> p.left.equals(pos)).findFirst().ifPresent(p -> p.right.setNbt(nbt));
        });

        this.blocks = Collections.unmodifiableList(blocksToAdd);
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

    public List<Pair<BlockVec, ParsedBlock>> blocks() {
        return blocks;
    }

    public void apply(Consumer<Pair<BlockVec, ParsedBlock>> consumer) {
        blocks.forEach(consumer);
    }

    /**
    * <p>Load a schematic from an InputStream. The stream is not closed by this method.</p>
    *
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

    public static class ParsedBlock {
        private final @Subst("minecraft:air") String id;
        private final Map<String, String> properties;
        private @Nullable CompoundBinaryTag nbt;

        public ParsedBlock(String id, Map<String, String> properties) {
            this.id = id;
            this.properties = properties;
            this.nbt = null;
        }

        public Block toBlock() {
            Block b = Block.fromKey(Key.key(id));
            if (b == null) {
                b = Block.AIR;
            }
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                b = b.withProperty(entry.getKey(), entry.getValue());
            }
            if (nbt != null) {
                b = b.withNbt(nbt);
            }
            return b;
        }

        void setNbt(@Nullable CompoundBinaryTag nbt) {
            this.nbt = nbt;
        }
    }

    private static ParsedBlock parseBlockState(String input, Map<String, String> additionalProperties) {
        String id;
        Map<String, String> properties = new LinkedHashMap<>(); // preserves order

        int bracketIndex = input.indexOf('[');
        if (bracketIndex == -1) {
            // no properties
            id = input;
        } else {
            id = input.substring(0, bracketIndex);
            String propsPart = input.substring(bracketIndex + 1, input.length() - 1); // remove [ and ]
            String[] entries = propsPart.split(",");
            for (String entry : entries) {
                String[] kv = entry.split("=", 2);
                if (kv.length == 2) {
                    properties.put(kv[0], kv[1]);
                }
            }
        }
        if (additionalProperties != null) {
            properties.putAll(additionalProperties);
        }

        return new ParsedBlock(id, properties);
    }

    private static CompoundBinaryTag fromTreeMap(TreeMap<String, Object> map) {
        CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof TreeMap) {
                builder.put(key, fromTreeMap((TreeMap<String, Object>) value));
            } else if (value instanceof List) {
                parseList(value, builder, key);
            } else {
                parsePrimitive(value, builder, key);
            }
        }
        return builder.build();
    }

    private static void parsePrimitive(Object o, CompoundBinaryTag.Builder output, String key) {
        if (o instanceof String) {
            output.putString(key, (String) o);
        } else if (o instanceof Integer) {
            output.putInt(key, (Integer) o);
        } else if (o instanceof Byte) {
            output.putByte(key, (Byte) o);
        } else if (o instanceof Long) {
            output.putLong(key, (Long) o);
        } else if (o instanceof Short) {
            output.putShort(key, (Short) o);
        } else if (o instanceof Float) {
            output.putFloat(key, (Float) o);
        } else if (o instanceof Double) {
            output.putDouble(key, (Double) o);
        } else if (o instanceof byte[]) {
            output.putByteArray(key, (byte[]) o);
        } else if (o instanceof int[]) {
            output.putIntArray(key, (int[]) o);
        } else if (o instanceof long[]) {
            output.putLongArray(key, (long[]) o);
        }
    }

    private static void parseList(Object o, CompoundBinaryTag.Builder output, String key) {
        if (o instanceof List<?> list) {
            ListBinaryTag.Builder<BinaryTag> listBuilder = ListBinaryTag.builder();
            for (Object item : list) {
                if (item instanceof TreeMap) {
                    listBuilder.add(fromTreeMap((TreeMap<String, Object>) item).asBinaryTag());
                } else if (item instanceof String) {
                    listBuilder.add(StringBinaryTag.stringBinaryTag((String) item));
                } else if (item instanceof Integer) {
                    listBuilder.add(IntBinaryTag.intBinaryTag((Integer) item));
                } else if (item instanceof Byte) {
                    listBuilder.add(ByteBinaryTag.byteBinaryTag((Byte) item));
                } else if (item instanceof Long) {
                    listBuilder.add(LongBinaryTag.longBinaryTag((Long) item));
                } else if (item instanceof Short) {
                    listBuilder.add(ShortBinaryTag.shortBinaryTag((Short) item));
                } else if (item instanceof Float) {
                    listBuilder.add(FloatBinaryTag.floatBinaryTag((Float) item));
                } else if (item instanceof Double) {
                    listBuilder.add(DoubleBinaryTag.doubleBinaryTag((Double) item));
                } else if (item instanceof byte[]) {
                    listBuilder.add(ByteArrayBinaryTag.byteArrayBinaryTag((byte[]) item).asBinaryTag());
                } else if (item instanceof int[]) {
                    listBuilder.add(IntArrayBinaryTag.intArrayBinaryTag((int[]) item).asBinaryTag());
                } else if (item instanceof long[]) {
                    listBuilder.add(LongArrayBinaryTag.longArrayBinaryTag((long[]) item).asBinaryTag());
                }
            }
            output.put(key, listBuilder.build());
        }
    }
}
