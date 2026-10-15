package io.github.emberbocor.worthwhilewanderingtrader.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.slf4j.helpers.MessageFormatter;

import com.electronwill.nightconfig.core.UnmodifiableConfig;

import io.github.emberbocor.worthwhilewanderingtrader.WorthwhileWanderingTrader;
import io.github.emberbocor.worthwhilewanderingtrader.trade.CostItem;
import io.github.emberbocor.worthwhilewanderingtrader.trade.TradeEntry;
import io.github.emberbocor.worthwhilewanderingtrader.trade.TradeResult;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TippedArrowItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.levelgen.structure.Structure;

final class ServerConfigReader {
    private static final int MAX_COUNT = 64;
    private static final int MAX_SLOTS = 64;
    private static final int MAX_SEARCH_RADIUS = 1000;

    private static final String MODE = "mode";
    private static final String VERY_RARE_CHANCE = "veryRareChance";
    private static final String MAP_SEARCH_RADIUS = "mapSearchRadius";
    private static final Set<String> KEYS = Set.of(MODE, "buySlots", "veryRareSlots", VERY_RARE_CHANCE, "rareSlots", "usefulSlots",
            MAP_SEARCH_RADIUS, "buy", "veryRare", "rare", "useful");
    private static final Set<String> ENTRY_KEYS = Set.of("price", "price2", "result", "maxUses", "weight");
    private static final Set<String> COST_KEYS = Set.of("item", "count", "potion");
    private static final Set<String> RESULT_KEYS = Set.of("item", "items", "count", "potion", "instrument", "map");

    private final Path file;
    private final Registry<Structure> structures;
    private final FeatureFlagSet features;

    ServerConfigReader(Path file, RegistryAccess registries, FeatureFlagSet features) {
        this.file = file;
        this.structures = registries.registryOrThrow(Registries.STRUCTURE);
        this.features = features;
    }

    ServerConfig.Settings read(UnmodifiableConfig config) {
        for (UnmodifiableConfig.Entry entry : config.entrySet()) {
            if (!KEYS.contains(entry.getKey())) {
                warn("unknown option '{}', ignored", entry.getKey());
            }
        }
        ServerConfig.Settings settings = new ServerConfig.Settings(
                mode(config.get(MODE)),
                group(config, "buy", 3),
                group(config, "veryRare", 1),
                chance(config.get(VERY_RARE_CHANCE)),
                group(config, "rare", 4),
                group(config, "useful", 3),
                searchRadius(config.get(MAP_SEARCH_RADIUS)));
        WorthwhileWanderingTrader.LOGGER.info("Loaded {}: mode {}, {} buy, {} very rare, {} rare and {} useful trades",
                file, settings.mode().name().toLowerCase(), settings.buy().entries().size(), settings.veryRare().entries().size(),
                settings.rare().entries().size(), settings.useful().entries().size());
        return settings;
    }

    private ServerConfig.Mode mode(@Nullable Object value) {
        if (value == null || "replace".equals(value)) {
            return ServerConfig.Mode.REPLACE;
        }
        if ("add".equals(value)) {
            return ServerConfig.Mode.ADD;
        }
        warn("{} must be \"replace\" or \"add\", got '{}'; using \"replace\"", MODE, value);
        return ServerConfig.Mode.REPLACE;
    }

    private double chance(@Nullable Object value) {
        if (value == null) {
            return 0.25;
        }
        if (value instanceof Number number && number.doubleValue() >= 0 && number.doubleValue() <= 1) {
            return number.doubleValue();
        }
        warn("{} must be a number from 0.0 to 1.0, got '{}'; using 0.25", VERY_RARE_CHANCE, value);
        return 0.25;
    }

    private int searchRadius(@Nullable Object value) {
        if (value == null) {
            return 100;
        }
        Long number = wholeNumber(value);
        if (number != null && number >= 1 && number <= MAX_SEARCH_RADIUS) {
            return number.intValue();
        }
        warn("{} must be a whole number from 1 to {}, got '{}'; using 100", MAP_SEARCH_RADIUS, MAX_SEARCH_RADIUS, value);
        return 100;
    }

    private ServerConfig.Group group(UnmodifiableConfig config, String name, int defaultSlots) {
        String slotsKey = name + "Slots";
        Object slotsValue = config.get(slotsKey);
        int slots = defaultSlots;
        if (slotsValue != null) {
            Long number = wholeNumber(slotsValue);
            if (number != null && number >= 0 && number <= MAX_SLOTS) {
                slots = number.intValue();
            } else {
                warn("{} must be a whole number from 0 to {}, got '{}'; using {}", slotsKey, MAX_SLOTS, slotsValue, defaultSlots);
            }
        }
        return new ServerConfig.Group(slots, entries(config.get(name), name));
    }

    private List<TradeEntry> entries(@Nullable Object value, String group) {
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof List<?> list)) {
            warn("'{}' must be a list of [[{}]] tables, the group has no trades", group, group);
            return List.of();
        }
        List<TradeEntry> entries = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            String name = group + " trade " + (i + 1);
            if (!(list.get(i) instanceof UnmodifiableConfig table)) {
                warn("{} is not a table, skipped", name);
                continue;
            }
            try {
                entries.add(entry(name, table));
            } catch (InvalidEntryException e) {
                warn("{}: {}, skipped", name, e.getMessage());
            }
        }
        return List.copyOf(entries);
    }

    private TradeEntry entry(String name, UnmodifiableConfig table) {
        unknownKeys(name, table, ENTRY_KEYS);
        CostItem price = cost(name + " price", table.get("price"));
        Object price2Value = table.get("price2");
        Optional<CostItem> price2 = price2Value == null ? Optional.empty() : Optional.of(cost(name + " price2", price2Value));
        TradeResult result = result(name + " result", table.get("result"));

        Object maxUsesValue = table.get("maxUses");
        Long maxUses = maxUsesValue == null ? null : wholeNumber(maxUsesValue);
        if (maxUses == null || maxUses < 1 || maxUses > Integer.MAX_VALUE) {
            throw new InvalidEntryException(maxUsesValue == null ? "maxUses is missing" : "maxUses must be a whole number of at least 1, got '" + maxUsesValue + "'");
        }
        Object weightValue = table.get("weight");
        Long weight = weightValue == null ? Long.valueOf(1) : wholeNumber(weightValue);
        if (weight == null || weight < 1 || weight > Integer.MAX_VALUE) {
            throw new InvalidEntryException("weight must be a whole number of at least 1, got '" + weightValue + "'");
        }
        return new TradeEntry(name, price, price2, result, maxUses.intValue(), weight.intValue());
    }

    private CostItem cost(String name, @Nullable Object value) {
        if (value == null) {
            throw new InvalidEntryException(shortName(name) + " is missing");
        }
        if (!(value instanceof UnmodifiableConfig table)) {
            throw new InvalidEntryException(shortName(name) + " must be { item = \"...\", count = ... }");
        }
        unknownKeys(name, table, COST_KEYS);
        Item item = item(table.get("item"));
        if (item == null) {
            throw new InvalidEntryException("unknown item '" + table.get("item") + "' in " + shortName(name));
        }
        Optional<Potion> potion = potion(table.get("potion"), name);
        if (potion.isPresent()) {
            checkHoldsPotion(item);
        }
        int count = count(table.get("count"), name);
        checkStackSize(item, count, name);
        return new CostItem(item, count, potion);
    }

    private TradeResult result(String name, @Nullable Object value) {
        if (value == null) {
            throw new InvalidEntryException("result is missing");
        }
        if (!(value instanceof UnmodifiableConfig table)) {
            throw new InvalidEntryException("result must be a table such as { item = \"...\", count = ... }");
        }
        unknownKeys(name, table, RESULT_KEYS);
        if (table.contains("map")) {
            for (String key : List.of("item", "items", "count", "potion", "instrument")) {
                if (table.contains(key)) {
                    warn("{}: '{}' is ignored together with 'map'", name, key);
                }
            }
            return map(name, table.get("map"));
        }

        Object itemValue = table.get("item");
        Object itemsValue = table.get("items");
        if ((itemValue == null) == (itemsValue == null)) {
            throw new InvalidEntryException("result needs exactly one of 'item', 'items' or 'map'");
        }
        int count = count(table.get("count"), name);
        List<Item> items;
        if (itemValue != null) {
            Item item = item(itemValue);
            if (item == null) {
                throw new InvalidEntryException("unknown item '" + itemValue + "' in result");
            }
            checkStackSize(item, count, name);
            items = List.of(item);
        } else {
            items = items(name, itemsValue, count);
        }

        Optional<Potion> potion = potion(table.get("potion"), name);
        Optional<HolderSet<Instrument>> instrument = instrument(table.get("instrument"), name);
        for (Item item : items) {
            if (potion.isPresent()) {
                checkHoldsPotion(item);
            }
            if (instrument.isPresent() && !(item instanceof InstrumentItem)) {
                throw new InvalidEntryException(BuiltInRegistries.ITEM.getKey(item) + " cannot have an instrument");
            }
        }
        return new TradeResult.ItemList(items, count, potion, instrument);
    }

    private List<Item> items(String name, Object value, int count) {
        if (!(value instanceof List<?> ids)) {
            throw new InvalidEntryException("'items' must be a list of item ids");
        }
        List<Item> items = new ArrayList<>();
        for (Object id : ids) {
            Item item = item(id);
            if (item == null) {
                warn("{}: unknown item '{}' left out of the list", name, id);
            } else if (count > item.getMaxStackSize()) {
                warn("{}: {} left out of the list, count {} is above its max stack size of {}",
                        name, BuiltInRegistries.ITEM.getKey(item), count, item.getMaxStackSize());
            } else if (!items.contains(item)) {
                items.add(item);
            }
        }
        if (items.isEmpty()) {
            throw new InvalidEntryException("'items' has no usable item");
        }
        return List.copyOf(items);
    }

    private TradeResult map(String name, @Nullable Object value) {
        if (!(value instanceof List<?> ids)) {
            throw new InvalidEntryException("'map' must be a list of structure ids or #tags");
        }
        List<TradeResult.Destination> destinations = new ArrayList<>();
        for (Object id : ids) {
            HolderSet<Structure> set = structures(id);
            if (set == null || set.size() == 0) {
                warn("{}: unknown structure or empty tag '{}' left out of the map list", name, id);
            } else {
                destinations.add(new TradeResult.Destination(id.toString(), set));
            }
        }
        if (destinations.isEmpty()) {
            throw new InvalidEntryException("'map' has no known structure");
        }
        return new TradeResult.StructureMap(List.copyOf(destinations));
    }

    @Nullable
    private HolderSet<Structure> structures(Object value) {
        if (!(value instanceof String string)) {
            return null;
        }
        if (string.startsWith("#")) {
            ResourceLocation id = ResourceLocation.tryParse(string.substring(1));
            return id == null ? null : structures.getTag(TagKey.create(Registries.STRUCTURE, id)).orElse(null);
        }
        ResourceLocation id = ResourceLocation.tryParse(string);
        return id == null ? null : structures.getHolder(ResourceKey.create(Registries.STRUCTURE, id)).<HolderSet<Structure>>map(holder -> HolderSet.direct(List.of(holder))).orElse(null);
    }

    private static Optional<Potion> potion(@Nullable Object value, String name) {
        if (value == null) {
            return Optional.empty();
        }
        ResourceLocation id = value instanceof String string ? ResourceLocation.tryParse(string) : null;
        Optional<Potion> potion = id == null ? Optional.empty() : BuiltInRegistries.POTION.getOptional(id);
        if (potion.isEmpty()) {
            throw new InvalidEntryException("unknown potion '" + value + "' in " + shortName(name));
        }
        return potion;
    }

    private static void checkHoldsPotion(Item item) {
        if (!(item instanceof PotionItem) && !(item instanceof TippedArrowItem)) {
            throw new InvalidEntryException(BuiltInRegistries.ITEM.getKey(item) + " cannot hold a potion");
        }
    }

    private static Optional<HolderSet<Instrument>> instrument(@Nullable Object value, String name) {
        if (value == null) {
            return Optional.empty();
        }
        HolderSet<Instrument> instruments = null;
        if (value instanceof String string && string.startsWith("#")) {
            ResourceLocation id = ResourceLocation.tryParse(string.substring(1));
            instruments = id == null ? null : BuiltInRegistries.INSTRUMENT.getTag(TagKey.create(Registries.INSTRUMENT, id)).orElse(null);
        } else if (value instanceof String string) {
            ResourceLocation id = ResourceLocation.tryParse(string);
            instruments = id == null ? null : BuiltInRegistries.INSTRUMENT.getHolder(ResourceKey.create(Registries.INSTRUMENT, id)).<HolderSet<Instrument>>map(holder -> HolderSet.direct(List.of(holder))).orElse(null);
        }
        if (instruments == null || instruments.size() == 0) {
            throw new InvalidEntryException("unknown instrument '" + value + "' in " + shortName(name));
        }
        return Optional.of(instruments);
    }

    private int count(@Nullable Object value, String name) {
        if (value == null) {
            return 1;
        }
        Long count = wholeNumber(value);
        if (count == null || count < 1 || count > MAX_COUNT) {
            throw new InvalidEntryException("count in " + shortName(name) + " must be a whole number from 1 to " + MAX_COUNT + ", got '" + value + "'");
        }
        return count.intValue();
    }

    private static void checkStackSize(Item item, int count, String name) {
        if (count > item.getMaxStackSize()) {
            throw new InvalidEntryException("count " + count + " in " + shortName(name) + " is above the max stack size of "
                    + BuiltInRegistries.ITEM.getKey(item) + " (" + item.getMaxStackSize() + ")");
        }
    }

    @Nullable
    private Item item(@Nullable Object value) {
        ResourceLocation id = value instanceof String string ? ResourceLocation.tryParse(string) : null;
        return id == null ? null : BuiltInRegistries.ITEM.getOptional(id)
                .filter(item -> item != Items.AIR && item.isEnabled(features))
                .orElse(null);
    }

    @Nullable
    private static Long wholeNumber(Object value) {
        return value instanceof Integer || value instanceof Long ? ((Number) value).longValue() : null;
    }

    private void unknownKeys(String name, UnmodifiableConfig table, Set<String> known) {
        for (UnmodifiableConfig.Entry entry : table.entrySet()) {
            if (!known.contains(entry.getKey())) {
                warn("{}: unknown key '{}', ignored", name, entry.getKey());
            }
        }
    }

    private static String shortName(String name) {
        return name.substring(name.lastIndexOf(' ') + 1);
    }

    private void warn(String message, Object... args) {
        WorthwhileWanderingTrader.LOGGER.warn("{}: {}", file, MessageFormatter.arrayFormat(message, args).getMessage());
    }

    private static final class InvalidEntryException extends RuntimeException {
        InvalidEntryException(String message) {
            super(message, null, false, false);
        }
    }
}
