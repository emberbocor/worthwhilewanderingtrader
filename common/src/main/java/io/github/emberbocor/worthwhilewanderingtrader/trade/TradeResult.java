package io.github.emberbocor.worthwhilewanderingtrader.trade;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.levelgen.structure.Structure;

public sealed interface TradeResult {
    record ItemList(List<Item> items, int count, Optional<Holder<Potion>> potion, Optional<HolderSet<Instrument>> instrument) implements TradeResult {
    }

    record StructureMap(List<Destination> destinations) implements TradeResult {
    }

    record Destination(String id, HolderSet<Structure> structures) {
    }
}
