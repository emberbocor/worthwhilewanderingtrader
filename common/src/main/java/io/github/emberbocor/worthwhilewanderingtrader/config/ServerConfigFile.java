package io.github.emberbocor.worthwhilewanderingtrader.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.toml.TomlFormat;

import io.github.emberbocor.worthwhilewanderingtrader.WorthwhileWanderingTrader;

final class ServerConfigFile {
    static final String FILE_NAME = WorthwhileWanderingTrader.MODID + "-server.toml";

    private static final String DEFAULT_TEXT = """
            # Worthwhile Wandering Trader server config.
            # Created on the first server start and never rewritten afterwards, so your edits and comments stay.
            # Changes take effect after a server restart (in singleplayer: when the world is opened again).
            # A wandering trader picks its trades the first time it is opened or saved. Traders that already have
            # trades keep them.

            # "replace": wandering traders only offer the trades in this file.
            # "add": the trades in this file are added after the vanilla trades.
            mode = "replace"

            # Number of trades each trader gets from each group below. Whole numbers from 0 to 64; 0 turns a group off.
            # A group with fewer entries than slots offers all of its entries. No entry appears twice in one trader.
            buySlots = 3
            veryRareSlots = 1
            # Chance from 0.0 to 1.0 that a very rare slot is filled. When it is not, the trader gets one extra rare
            # trade instead.
            veryRareChance = 0.25
            rareSlots = 4
            usefulSlots = 3

            # How far structure maps search, in the same unit as the cartographer's explorer maps (100 = vanilla).
            # Larger values find structures farther away but can make the server pause longer while a trader picks
            # its trades. Whole number from 1 to 1000.
            mapSearchRadius = 100

            # ---------------------------------------------------------------------------------------------------------
            # Trades
            #
            # Every trade is a [[buy]], [[veryRare]], [[rare]] or [[useful]] table with these keys:
            #   price    what the player pays (required)
            #   price2   a second item the player pays (optional)
            #   result   what the player gets (required)
            #   maxUses  how many times the trade can be used, whole number of at least 1 (required)
            #   weight   how often the entry is picked compared to the others in its group, whole number of at
            #            least 1 (optional, default 1)
            #
            # price, price2 and result are written like this:
            #   { item = "minecraft:emerald", count = 8 }
            #       One item. count is 1 to 64 and at most the item's max stack size (default 1).
            #   { item = "minecraft:potion", potion = "minecraft:water" }
            #       An item with a potion: here a water bottle. As a price only that exact potion is accepted, so a
            #       water bottle price refuses every other potion. Works for potions, splash and lingering potions
            #       and tipped arrows.
            # result can also be:
            #   { items = ["minecraft:oak_log", "minecraft:birch_log"], count = 8 }
            #       One of the items, picked at random for each trader. Unknown items in the list are left out.
            #   { item = "minecraft:goat_horn", instrument = "#minecraft:regular_goat_horns" }
            #       A goat horn with an instrument: an instrument id (minecraft:ponder_goat_horn) or a #tag, of which
            #       one is picked at random for each trader.
            #   { map = ["#minecraft:village", "minecraft:desert_pyramid"] }
            #       An explorer map to the nearest structure that is not on another map yet. Structure ids or #tags.
            #       One is picked at random; when it is not found within mapSearchRadius the others are tried, and
            #       when none is found the trader gets another trade from the same group instead.
            #
            # Example: 4 emeralds and 4 bone meal for 8 random logs, 4 times per trader, picked twice as often:
            #   [[useful]]
            #   price = { item = "minecraft:emerald", count = 4 }
            #   price2 = { item = "minecraft:bone_meal", count = 4 }
            #   result = { items = ["minecraft:oak_log", "minecraft:birch_log"], count = 8 }
            #   maxUses = 4
            #   weight = 2
            #
            # Invalid entries (unknown items, counts out of range, unknown structures, ...) are skipped with a warning
            # in the log. A group without entries offers nothing.
            # ---------------------------------------------------------------------------------------------------------

            # Buy: the trader buys these from the player.

            [[buy]]
            price = { item = "minecraft:hay_block", count = 1 }
            result = { item = "minecraft:emerald", count = 1 }
            maxUses = 2

            [[buy]]
            price = { item = "minecraft:potion", potion = "minecraft:water", count = 1 }
            result = { item = "minecraft:emerald", count = 1 }
            maxUses = 2

            [[buy]]
            price = { item = "minecraft:baked_potato", count = 4 }
            result = { item = "minecraft:emerald", count = 1 }
            maxUses = 2

            [[buy]]
            price = { item = "minecraft:milk_bucket", count = 1 }
            result = { item = "minecraft:emerald", count = 2 }
            maxUses = 2

            [[buy]]
            price = { item = "minecraft:water_bucket", count = 1 }
            result = { item = "minecraft:emerald", count = 2 }
            maxUses = 2

            [[buy]]
            price = { item = "minecraft:fermented_spider_eye", count = 1 }
            result = { item = "minecraft:emerald", count = 3 }
            maxUses = 2

            # Very rare

            [[veryRare]]
            price = { item = "minecraft:emerald", count = 32 }
            result = { item = "minecraft:heart_of_the_sea", count = 1 }
            maxUses = 1

            [[veryRare]]
            price = { item = "minecraft:emerald", count = 24 }
            result = { item = "minecraft:sniffer_egg", count = 1 }
            maxUses = 1

            [[veryRare]]
            price = { item = "minecraft:emerald", count = 48 }
            result = { item = "minecraft:totem_of_undying", count = 1 }
            maxUses = 1

            [[veryRare]]
            price = { item = "minecraft:emerald", count = 40 }
            result = { item = "minecraft:trident", count = 1 }
            maxUses = 1

            # Rare

            [[rare]]
            price = { item = "minecraft:emerald", count = 8 }
            result = { item = "minecraft:name_tag", count = 1 }
            maxUses = 2

            [[rare]]
            price = { item = "minecraft:emerald", count = 8 }
            result = { item = "minecraft:saddle", count = 1 }
            maxUses = 2

            [[rare]]
            price = { item = "minecraft:emerald", count = 12 }
            result = { count = 1, items = [
                "minecraft:music_disc_13",
                "minecraft:music_disc_cat",
                "minecraft:music_disc_blocks",
                "minecraft:music_disc_chirp",
                "minecraft:music_disc_creator",
                "minecraft:music_disc_creator_music_box",
                "minecraft:music_disc_far",
                "minecraft:music_disc_mall",
                "minecraft:music_disc_mellohi",
                "minecraft:music_disc_stal",
                "minecraft:music_disc_strad",
                "minecraft:music_disc_ward",
                "minecraft:music_disc_11",
                "minecraft:music_disc_wait",
                "minecraft:music_disc_otherside",
                "minecraft:music_disc_relic",
                "minecraft:music_disc_5",
                "minecraft:music_disc_pigstep",
                "minecraft:music_disc_precipice",
            ] }
            maxUses = 1

            [[rare]]
            price = { item = "minecraft:emerald", count = 16 }
            result = { count = 1, items = [
                "minecraft:sentry_armor_trim_smithing_template",
                "minecraft:dune_armor_trim_smithing_template",
                "minecraft:coast_armor_trim_smithing_template",
                "minecraft:wild_armor_trim_smithing_template",
                "minecraft:ward_armor_trim_smithing_template",
                "minecraft:eye_armor_trim_smithing_template",
                "minecraft:vex_armor_trim_smithing_template",
                "minecraft:tide_armor_trim_smithing_template",
                "minecraft:snout_armor_trim_smithing_template",
                "minecraft:rib_armor_trim_smithing_template",
                "minecraft:spire_armor_trim_smithing_template",
                "minecraft:wayfinder_armor_trim_smithing_template",
                "minecraft:shaper_armor_trim_smithing_template",
                "minecraft:silence_armor_trim_smithing_template",
                "minecraft:raiser_armor_trim_smithing_template",
                "minecraft:host_armor_trim_smithing_template",
                "minecraft:flow_armor_trim_smithing_template",
                "minecraft:bolt_armor_trim_smithing_template",
            ] }
            maxUses = 1

            [[rare]]
            price = { item = "minecraft:emerald", count = 6 }
            result = { item = "minecraft:sponge", count = 1 }
            maxUses = 4

            [[rare]]
            price = { item = "minecraft:emerald", count = 8 }
            result = { item = "minecraft:echo_shard", count = 1 }
            maxUses = 4

            [[rare]]
            price = { item = "minecraft:emerald", count = 10 }
            result = { item = "minecraft:goat_horn", instrument = "#minecraft:regular_goat_horns", count = 1 }
            maxUses = 1

            [[rare]]
            price = { item = "minecraft:emerald", count = 6 }
            result = { item = "minecraft:ender_pearl", count = 2 }
            maxUses = 4

            [[rare]]
            price = { item = "minecraft:emerald", count = 8 }
            result = { item = "minecraft:ender_eye", count = 1 }
            maxUses = 2

            [[rare]]
            price = { item = "minecraft:emerald", count = 12 }
            result = { map = [
                "#minecraft:village",
                "minecraft:desert_pyramid",
                "minecraft:jungle_pyramid",
                "minecraft:ancient_city",
                "minecraft:trial_chambers",
            ] }
            maxUses = 1

            # Useful

            [[useful]]
            price = { item = "minecraft:emerald", count = 2 }
            result = { item = "minecraft:ice", count = 8 }
            maxUses = 6

            [[useful]]
            price = { item = "minecraft:emerald", count = 1 }
            result = { count = 8, items = [
                "minecraft:cherry_log",
                "minecraft:mangrove_log",
                "minecraft:jungle_log",
                "minecraft:acacia_log",
                "minecraft:dark_oak_log",
            ] }
            maxUses = 8

            [[useful]]
            price = { item = "minecraft:emerald", count = 1 }
            result = { count = 2, items = [
                "minecraft:cherry_sapling",
                "minecraft:mangrove_propagule",
                "minecraft:jungle_sapling",
                "minecraft:acacia_sapling",
                "minecraft:dark_oak_sapling",
                "minecraft:bamboo",
                "minecraft:cocoa_beans",
            ] }
            maxUses = 6

            [[useful]]
            price = { item = "minecraft:emerald", count = 8 }
            result = { count = 1, items = [
                "minecraft:zombie_head",
                "minecraft:skeleton_skull",
                "minecraft:creeper_head",
                "minecraft:piglin_head",
            ] }
            maxUses = 1

            [[useful]]
            price = { item = "minecraft:emerald", count = 4 }
            result = { count = 1, items = [
                "minecraft:angler_pottery_sherd",
                "minecraft:archer_pottery_sherd",
                "minecraft:arms_up_pottery_sherd",
                "minecraft:blade_pottery_sherd",
                "minecraft:brewer_pottery_sherd",
                "minecraft:burn_pottery_sherd",
                "minecraft:danger_pottery_sherd",
                "minecraft:explorer_pottery_sherd",
                "minecraft:flow_pottery_sherd",
                "minecraft:friend_pottery_sherd",
                "minecraft:guster_pottery_sherd",
                "minecraft:heart_pottery_sherd",
                "minecraft:heartbreak_pottery_sherd",
                "minecraft:howl_pottery_sherd",
                "minecraft:miner_pottery_sherd",
                "minecraft:mourner_pottery_sherd",
                "minecraft:plenty_pottery_sherd",
                "minecraft:prize_pottery_sherd",
                "minecraft:scrape_pottery_sherd",
                "minecraft:sheaf_pottery_sherd",
                "minecraft:shelter_pottery_sherd",
                "minecraft:skull_pottery_sherd",
                "minecraft:snort_pottery_sherd",
            ] }
            maxUses = 2

            [[useful]]
            price = { item = "minecraft:emerald", count = 5 }
            result = { item = "minecraft:nautilus_shell", count = 1 }
            maxUses = 3
            """;

    private ServerConfigFile() {
    }

    static Optional<UnmodifiableConfig> load(Path file) {
        if (!Files.exists(file)) {
            if (write(file)) {
                WorthwhileWanderingTrader.LOGGER.info("Created {}", file);
            }
            return Optional.of(defaults());
        }

        try {
            String text = Files.readString(file, StandardCharsets.UTF_8);
            return parse(file, text.startsWith("﻿") ? text.substring(1) : text);
        } catch (IOException e) {
            WorthwhileWanderingTrader.LOGGER.warn("Could not read {}, using the default settings: {}", file, e.getMessage());
            return Optional.empty();
        }
    }

    static UnmodifiableConfig defaults() {
        return TomlFormat.instance().createParser().parse(DEFAULT_TEXT);
    }

    private static Optional<UnmodifiableConfig> parse(Path file, String text) {
        try {
            return Optional.of(TomlFormat.instance().createParser().parse(text));
        } catch (RuntimeException e) {
            WorthwhileWanderingTrader.LOGGER.warn("Could not parse {}, using the default settings: {}", file, e.getMessage());
            return Optional.empty();
        }
    }

    private static boolean write(Path file) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, DEFAULT_TEXT, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            WorthwhileWanderingTrader.LOGGER.warn("Could not write {}: {}", file, e.getMessage());
            return false;
        }
    }
}
