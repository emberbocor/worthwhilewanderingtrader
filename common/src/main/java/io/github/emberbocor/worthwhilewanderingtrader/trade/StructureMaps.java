package io.github.emberbocor.worthwhilewanderingtrader.trade;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;

import io.github.emberbocor.worthwhilewanderingtrader.WorthwhileWanderingTrader;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

final class StructureMaps {
    private static final Map<ResourceKey<Structure>, Look> VANILLA = Map.ofEntries(
            Map.entry(BuiltinStructures.WOODLAND_MANSION, new Look(MapDecoration.Type.MANSION, "filled_map.mansion", null)),
            Map.entry(BuiltinStructures.OCEAN_MONUMENT, new Look(MapDecoration.Type.MONUMENT, "filled_map.monument", null)),
            Map.entry(BuiltinStructures.BURIED_TREASURE, new Look(MapDecoration.Type.RED_X, "filled_map.buried_treasure", null)),
            Map.entry(BuiltinStructures.VILLAGE_PLAINS, new Look(MapDecoration.Type.RED_X, "filled_map.village_plains", "Plains Village Map")),
            Map.entry(BuiltinStructures.VILLAGE_DESERT, new Look(MapDecoration.Type.RED_X, "filled_map.village_desert", "Desert Village Map")),
            Map.entry(BuiltinStructures.VILLAGE_SAVANNA, new Look(MapDecoration.Type.RED_X, "filled_map.village_savanna", "Savanna Village Map")),
            Map.entry(BuiltinStructures.VILLAGE_SNOWY, new Look(MapDecoration.Type.RED_X, "filled_map.village_snowy", "Snowy Village Map")),
            Map.entry(BuiltinStructures.VILLAGE_TAIGA, new Look(MapDecoration.Type.RED_X, "filled_map.village_taiga", "Taiga Village Map")),
            Map.entry(BuiltinStructures.JUNGLE_TEMPLE, new Look(MapDecoration.Type.RED_X, "filled_map.explorer_jungle", "Jungle Explorer Map")),
            Map.entry(BuiltinStructures.SWAMP_HUT, new Look(MapDecoration.Type.RED_X, "filled_map.explorer_swamp", "Swamp Explorer Map")));

    private StructureMaps() {
    }

    static ItemStack create(WanderingTrader trader, TradeResult.StructureMap map, int searchRadius) {
        if (!(trader.level() instanceof ServerLevel level) || !level.getServer().getWorldData().worldGenOptions().generateStructures()) {
            return ItemStack.EMPTY;
        }
        List<TradeResult.Destination> destinations = Util.shuffledCopy(new ObjectArrayList<>(map.destinations()), trader.getRandom());
        for (TradeResult.Destination destination : destinations) {
            long start = System.nanoTime();
            Pair<BlockPos, Holder<Structure>> found = level.getChunkSource().getGenerator()
                    .findNearestMapStructure(level, destination.structures(), trader.blockPosition(), searchRadius, true);
            WorthwhileWanderingTrader.LOGGER.debug("Map search for {} from {} in {} took {} ms: {}", destination.id(), trader.blockPosition(),
                    level.dimension().location(), (System.nanoTime() - start) / 1_000_000,
                    found == null ? "nothing within radius " + searchRadius : id(found.getSecond()) + " at " + found.getFirst());
            if (found != null) {
                return map(level, found.getFirst(), found.getSecond());
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack map(ServerLevel level, BlockPos pos, Holder<Structure> structure) {
        Look look = structure.unwrapKey().map(VANILLA::get).orElse(null);
        ItemStack stack = MapItem.create(level, pos.getX(), pos.getZ(), (byte) 2, true, true);
        MapItem.renderBiomePreviewMap(level, stack);
        MapItemSavedData.addTargetDecoration(stack, pos, "+", look != null ? look.decoration() : MapDecoration.Type.RED_X);
        stack.setHoverName(look != null ? look.name() : fallbackName(structure));
        return stack;
    }

    private static Component fallbackName(Holder<Structure> structure) {
        ResourceLocation id = structure.unwrapKey().map(ResourceKey::location).orElse(null);
        if (id == null) {
            return Component.translatable("filled_map.unknown");
        }
        String path = id.getPath().substring(id.getPath().lastIndexOf('/') + 1);
        StringBuilder name = new StringBuilder();
        for (String word : path.split("_")) {
            if (!word.isEmpty()) {
                name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(' ');
            }
        }
        return Component.translatableWithFallback("filled_map." + id.getNamespace() + "." + id.getPath().replace('/', '.'), name + "Map");
    }

    private static String id(Holder<Structure> structure) {
        return structure.unwrapKey().map(key -> key.location().toString()).orElse("[unregistered]");
    }

    private record Look(MapDecoration.Type decoration, String key, @Nullable String fallback) {
        Component name() {
            return fallback == null ? Component.translatable(key) : Component.translatableWithFallback(key, fallback);
        }
    }
}
