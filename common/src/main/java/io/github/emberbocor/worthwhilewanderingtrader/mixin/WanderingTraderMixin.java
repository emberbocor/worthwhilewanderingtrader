package io.github.emberbocor.worthwhilewanderingtrader.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.emberbocor.worthwhilewanderingtrader.config.ServerConfig;
import io.github.emberbocor.worthwhilewanderingtrader.trade.WanderingTrades;
import net.minecraft.world.entity.npc.WanderingTrader;

@Mixin(WanderingTrader.class)
public abstract class WanderingTraderMixin {
    @Inject(method = "updateTrades", at = @At("HEAD"), cancellable = true)
    private void worthwhilewanderingtrader$replaceTrades(CallbackInfo ci) {
        if (ServerConfig.settings().mode() == ServerConfig.Mode.REPLACE) {
            WanderingTrades.add((WanderingTrader) (Object) this);
            ci.cancel();
        }
    }

    @Inject(method = "updateTrades", at = @At("RETURN"))
    private void worthwhilewanderingtrader$addTrades(CallbackInfo ci) {
        if (ServerConfig.settings().mode() == ServerConfig.Mode.ADD) {
            WanderingTrades.add((WanderingTrader) (Object) this);
        }
    }
}
