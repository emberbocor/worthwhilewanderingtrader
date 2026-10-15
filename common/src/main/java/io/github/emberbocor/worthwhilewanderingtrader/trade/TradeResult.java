package io.github.emberbocor.worthwhilewanderingtrader.trade;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.HolderSet;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.levelgen.structure.Structure;

public sealed interface TradeResult {
    record ItemList(List<Item> items, int count, Optional<Potion> potion, Optional<HolderSet<Instrument>> instrument) implements TradeResult {
        public ItemStack create(RandomSource random) {
            Item item = items.get(random.nextInt(items.size()));
            ItemStack stack = instrument.flatMap(set -> set.getRandomElement(random))
                    .map(value -> InstrumentItem.create(item, value))
                    .orElseGet(() -> new ItemStack(item));
            stack.setCount(count);
            potion.ifPresent(value -> PotionUtils.setPotion(stack, value));
            return stack;
        }
    }

    record StructureMap(List<Destination> destinations) implements TradeResult {
    }

    record Destination(String id, HolderSet<Structure> structures) {
    }
}
