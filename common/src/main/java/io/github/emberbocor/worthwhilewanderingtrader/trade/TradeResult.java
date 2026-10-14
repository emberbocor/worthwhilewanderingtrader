package io.github.emberbocor.worthwhilewanderingtrader.trade;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.levelgen.structure.Structure;

public sealed interface TradeResult {
    record ItemList(List<Item> items, int count, Optional<Holder<Potion>> potion, Optional<HolderSet<Instrument>> instrument) implements TradeResult {
        public ItemStack create(RandomSource random) {
            ItemStack stack = new ItemStack(items.get(random.nextInt(items.size())), count);
            potion.ifPresent(value -> stack.set(DataComponents.POTION_CONTENTS, new PotionContents(value)));
            instrument.flatMap(set -> set.getRandomElement(random)).ifPresent(value -> stack.set(DataComponents.INSTRUMENT, value));
            return stack;
        }
    }

    record StructureMap(List<Destination> destinations) implements TradeResult {
    }

    record Destination(String id, HolderSet<Structure> structures) {
    }
}
