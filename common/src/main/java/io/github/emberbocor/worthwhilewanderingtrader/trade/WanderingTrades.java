package io.github.emberbocor.worthwhilewanderingtrader.trade;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import io.github.emberbocor.worthwhilewanderingtrader.WorthwhileWanderingTrader;
import io.github.emberbocor.worthwhilewanderingtrader.config.ServerConfig;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public final class WanderingTrades {
    private static final int VILLAGER_XP = 1;
    private static final float PRICE_MULTIPLIER = 0.05F;

    private WanderingTrades() {
    }

    public static void add(WanderingTrader trader) {
        ServerConfig.Settings settings = ServerConfig.settings();
        RandomSource random = trader.getRandom();
        MerchantOffers offers = trader.getOffers();
        int before = offers.size();

        add(trader, settings.buy(), settings.buy().slots(), offers);
        int veryRareSlots = 0;
        for (int i = 0; i < settings.veryRare().slots(); i++) {
            if (random.nextDouble() < settings.veryRareChance()) {
                veryRareSlots++;
            }
        }
        int veryRare = add(trader, settings.veryRare(), veryRareSlots, offers);
        add(trader, settings.rare(), settings.rare().slots() + settings.veryRare().slots() - veryRare, offers);
        add(trader, settings.useful(), settings.useful().slots(), offers);

        WorthwhileWanderingTrader.LOGGER.debug("Added {} trades ({} very rare) to the wandering trader at {}",
                offers.size() - before, veryRare, trader.blockPosition());
    }

    private static int add(WanderingTrader trader, ServerConfig.Group group, int slots, MerchantOffers offers) {
        List<TradeEntry> left = new ArrayList<>(group.entries());
        int added = 0;
        while (added < slots && !left.isEmpty()) {
            MerchantOffer offer = offer(trader, removeWeighted(left, trader.getRandom()));
            if (offer != null) {
                offers.add(offer);
                added++;
            }
        }
        return added;
    }

    private static TradeEntry removeWeighted(List<TradeEntry> entries, RandomSource random) {
        long total = 0;
        for (TradeEntry entry : entries) {
            total += entry.weight();
        }
        long roll = (long) (random.nextDouble() * total);
        for (int i = 0; i < entries.size(); i++) {
            roll -= entries.get(i).weight();
            if (roll < 0) {
                return entries.remove(i);
            }
        }
        return entries.removeLast();
    }

    @Nullable
    private static MerchantOffer offer(WanderingTrader trader, TradeEntry entry) {
        ItemStack result = switch (entry.result()) {
            case TradeResult.ItemList items -> items.create(trader.getRandom());
            case TradeResult.StructureMap map -> StructureMaps.create(trader, map, ServerConfig.settings().mapSearchRadius());
        };
        if (result.isEmpty()) {
            return null;
        }
        return new MerchantOffer(entry.price().toItemCost(), entry.price2().map(CostItem::toItemCost), result,
                entry.maxUses(), VILLAGER_XP, PRICE_MULTIPLIER);
    }
}
