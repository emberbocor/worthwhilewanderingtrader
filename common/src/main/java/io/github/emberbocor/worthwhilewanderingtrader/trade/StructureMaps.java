package io.github.emberbocor.worthwhilewanderingtrader.trade;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mojang.datafixers.util.Pair;

import io.github.emberbocor.worthwhilewanderingtrader.WorthwhileWanderingTrader;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

final class StructureMaps {
    private static final Map<ResourceKey<Structure>, Look> VANILLA = Map.ofEntries(
            Map.entry(BuiltinStructures.VILLAGE_PLAINS, new Look(MapDecorationTypes.PLAINS_VILLAGE, "filled_map.village_plains")),
            Map.entry(BuiltinStructures.VILLAGE_DESERT, new Look(MapDecorationTypes.DESERT_VILLAGE, "filled_map.village_desert")),
            Map.entry(BuiltinStructures.VILLAGE_SAVANNA, new Look(MapDecorationTypes.SAVANNA_VILLAGE, "filled_map.village_savanna")),
            Map.entry(BuiltinStructures.VILLAGE_SNOWY, new Look(MapDecorationTypes.SNOWY_VILLAGE, "filled_map.village_snowy")),
            Map.entry(BuiltinStructures.VILLAGE_TAIGA, new Look(MapDecorationTypes.TAIGA_VILLAGE, "filled_map.village_taiga")),
            Map.entry(BuiltinStructures.JUNGLE_TEMPLE, new Look(MapDecorationTypes.JUNGLE_TEMPLE, "filled_map.explorer_jungle")),
            Map.entry(BuiltinStructures.SWAMP_HUT, new Look(MapDecorationTypes.SWAMP_HUT, "filled_map.explorer_swamp")),
            Map.entry(BuiltinStructures.WOODLAND_MANSION, new Look(MapDecorationTypes.WOODLAND_MANSION, "filled_map.mansion")),
            Map.entry(BuiltinStructures.OCEAN_MONUMENT, new Look(MapDecorationTypes.OCEAN_MONUMENT, "filled_map.monument")),
            Map.entry(BuiltinStructures.TRIAL_CHAMBERS, new Look(MapDecorationTypes.TRIAL_CHAMBERS, "filled_map.trial_chambers")),
            Map.entry(BuiltinStructures.BURIED_TREASURE, new Look(MapDecorationTypes.RED_X, "filled_map.buried_treasure")));

    private StructureMaps() {
    }

    static ItemStack create(WanderingTrader trader, TradeResult.StructureMap map, int searchRadius) {
        if (!(trader.level() instanceof ServerLevel level) || !level.getServer().getWorldData().worldGenOptions().generateStructures()) {
            return ItemStack.EMPTY;
        }
        List<TradeResult.Destination> destinations = new ArrayList<>(map.destinations());
        Util.shuffle(destinations, trader.getRandom());
        for (TradeResult.Destination destination : destinations) {
            long start = System.nanoTime();
            Pair<BlockPos, Holder<Structure>> found = level.getChunkSource().getGenerator()
                    .findNearestMapStructure(level, destination.structures(), trader.blockPosition(), searchRadius, true);
            WorthwhileWanderingTrader.LOGGER.debug("Map search for {} from {} in {} took {} ms: {}", destination.id(), trader.blockPosition(),
                    level.dimension().location(), (System.nanoTime() - start) / 1_000_000,
                    found == null ? "nothing within radius " + searchRadius : found.getSecond().getRegisteredName() + " at " + found.getFirst());
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
        MapItemSavedData.addTargetDecoration(stack, pos, "+", look != null ? look.decoration() : MapDecorationTypes.RED_X);
        stack.set(DataComponents.ITEM_NAME, look != null ? Component.translatable(look.name()) : fallbackName(structure));
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

    private record Look(Holder<MapDecorationType> decoration, String name) {
    }
}
