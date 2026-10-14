package io.github.emberbocor.worthwhilewanderingtrader.trade;

import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;

public record CostItem(Item item, int count, Optional<Holder<Potion>> potion) {
}
