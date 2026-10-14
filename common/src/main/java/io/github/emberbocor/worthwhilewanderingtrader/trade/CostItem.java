package io.github.emberbocor.worthwhilewanderingtrader.trade;

import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.trading.ItemCost;

public record CostItem(Item item, int count, Optional<Holder<Potion>> potion) {
    public ItemCost toItemCost() {
        ItemCost cost = new ItemCost(item, count);
        return potion.map(value -> cost.withComponents(components -> components.expect(DataComponents.POTION_CONTENTS, new PotionContents(value))))
                .orElse(cost);
    }
}
