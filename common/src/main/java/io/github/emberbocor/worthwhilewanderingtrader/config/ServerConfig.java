package io.github.emberbocor.worthwhilewanderingtrader.config;

import java.nio.file.Path;
import java.util.List;

import com.electronwill.nightconfig.core.UnmodifiableConfig;

import io.github.emberbocor.worthwhilewanderingtrader.platform.Services;
import io.github.emberbocor.worthwhilewanderingtrader.trade.TradeEntry;
import net.minecraft.server.MinecraftServer;

public final class ServerConfig {
    private static final Group NONE = new Group(0, List.of());

    private static volatile Settings settings = new Settings(Mode.ADD, NONE, NONE, 0, NONE, NONE, 100);

    private ServerConfig() {
    }

    public static void load(MinecraftServer server) {
        Path file = Services.PLATFORM.getConfigDirectory().resolve(ServerConfigFile.FILE_NAME);
        UnmodifiableConfig config = ServerConfigFile.load(file).orElseGet(ServerConfigFile::defaults);
        settings = new ServerConfigReader(file, server.registryAccess(), server.getWorldData().enabledFeatures()).read(config);
    }

    public static Settings settings() {
        return settings;
    }

    public enum Mode {
        REPLACE,
        ADD
    }

    public record Settings(Mode mode, Group buy, Group veryRare, double veryRareChance, Group rare, Group useful, int mapSearchRadius) {
    }

    public record Group(int slots, List<TradeEntry> entries) {
    }
}
