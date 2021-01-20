package xyz.nucleoid.dungeons.dungeons.game.map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import xyz.nucleoid.dungeons.dungeons.game.scripting.ScriptTemplateInstantiationError;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.TemplateRegion;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.Map;
import java.util.Optional;

public class BlockReplacementRegion {
    public final BlockBounds region;
    public final Map<Block, Block> replacements;

    public BlockReplacementRegion(BlockBounds region, Map<Block, Block> replacements) {
        this.region = region;
        this.replacements = replacements;
    }

    public static BlockReplacementRegion parse(TemplateRegion region) throws ScriptTemplateInstantiationError {
        CompoundTag data = region.getData();
        BlockReplacementRegion blockReplacementRegion = new BlockReplacementRegion(region.getBounds(), new Object2ObjectOpenHashMap<>());

        for (String key : data.getKeys()) {
            Identifier fromId = Identifier.tryParse(key);
            Identifier toId = Identifier.tryParse(data.getString(key));

            Optional<Block> from = Registry.BLOCK.getOrEmpty(fromId);
            Optional<Block> to = Registry.BLOCK.getOrEmpty(toId);

            if (!from.isPresent()) {
                throw new ScriptTemplateInstantiationError("Invalid block id to replace from: `" + fromId +"`");
            }

            if (!to.isPresent()) {
                throw new ScriptTemplateInstantiationError("Invalid block id to replace to: `" + toId +"`");
            }

            blockReplacementRegion.replacements.put(from.get(), to.get());
        }

        return blockReplacementRegion;
    }

    public void replaceAll(MapTemplate template) {
        for (BlockPos pos : this.region) {
            Block replacedTo = this.replacements.get(template.getBlockState(pos).getBlock());

            if (replacedTo != null) {
                template.setBlockState(pos, replacedTo.getDefaultState());
            }
        }
    }
}