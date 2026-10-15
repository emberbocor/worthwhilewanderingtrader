package io.github.emberbocor.worthwhilewanderingtrader.trade;

import java.util.Optional;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;

public record CostItem(Item item, int count, Optional<Potion> potion) {
    public ItemStack toStack() {
        ItemStack stack = new ItemStack(item, count);
        potion.ifPresent(value -> PotionUtils.setPotion(stack, value));
        return stack;
    }
}
