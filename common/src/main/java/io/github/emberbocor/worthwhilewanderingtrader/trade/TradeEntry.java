package io.github.emberbocor.worthwhilewanderingtrader.trade;

import java.util.Optional;

public record TradeEntry(String name, CostItem price, Optional<CostItem> price2, TradeResult result, int maxUses, int weight) {
}
