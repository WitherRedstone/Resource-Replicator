package com.chinaex123.resource_replicator.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class ServerConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec CONFIG_SPEC;

    // ======================= 物品复制机配置 =======================
    private static final ModConfigSpec.IntValue ITEM_REPLICATOR_OUTPUT_SLOTS;

    // 黑白名单配置
    private static final ModConfigSpec.BooleanValue BLACKLIST_MODE;
    private static final ModConfigSpec.ConfigValue<List<?>> BLACKLIST_ITEMS;
    private static final ModConfigSpec.ConfigValue<List<?>> WHITELIST_ITEMS;

    // T1-T5 物品复制机配置
    private static final ModConfigSpec.IntValue ITEM_TIER1_OUTPUT_AMOUNT;
    private static final ModConfigSpec.IntValue ITEM_TIER1_OUTPUT_TIME;
    private static final ModConfigSpec.IntValue ITEM_TIER2_OUTPUT_AMOUNT;
    private static final ModConfigSpec.IntValue ITEM_TIER2_OUTPUT_TIME;
    private static final ModConfigSpec.IntValue ITEM_TIER3_OUTPUT_AMOUNT;
    private static final ModConfigSpec.IntValue ITEM_TIER3_OUTPUT_TIME;
    private static final ModConfigSpec.IntValue ITEM_TIER4_OUTPUT_AMOUNT;
    private static final ModConfigSpec.IntValue ITEM_TIER4_OUTPUT_TIME;
    private static final ModConfigSpec.IntValue ITEM_TIER5_OUTPUT_AMOUNT;
    private static final ModConfigSpec.IntValue ITEM_TIER5_OUTPUT_TIME;

    // ======================= 流体复制机配置 =======================
    private static final ModConfigSpec.IntValue FLUID_REPLICATOR_OUTPUT_TANK_SIZE;

    // T1-T5 流体复制机配置
    private static final ModConfigSpec.IntValue FLUID_TIER1_OUTPUT_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER1_WATER_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER1_LAVA_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER1_OUTPUT_TIME;
    private static final ModConfigSpec.IntValue FLUID_TIER2_OUTPUT_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER2_WATER_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER2_LAVA_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER2_OUTPUT_TIME;
    private static final ModConfigSpec.IntValue FLUID_TIER3_OUTPUT_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER3_WATER_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER3_LAVA_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER3_OUTPUT_TIME;
    private static final ModConfigSpec.IntValue FLUID_TIER4_OUTPUT_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER4_WATER_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER4_LAVA_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER4_OUTPUT_TIME;
    private static final ModConfigSpec.IntValue FLUID_TIER5_OUTPUT_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER5_WATER_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER5_LAVA_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER5_OUTPUT_TIME;

    static {
        BUILDER.push("物品复制机设置");
        
        BUILDER.push("输出槽设置");
        ITEM_REPLICATOR_OUTPUT_SLOTS = BUILDER
                .comment("物品复制机的输出槽数量（默认：1）")
                .defineInRange("outputSlots", 1, 1, 9);
        BUILDER.pop();

        BUILDER.push("黑白名单设置");
        BLACKLIST_MODE = BUILDER
                .comment("是否使用黑名单模式（true=黑名单，false=白名单）")
                .define("blacklistMode", true);
        BLACKLIST_ITEMS = BUILDER
                .comment("""
                        黑名单物品列表。支持以下格式：
                        - 物品 ID: "minecraft:diamond"
                        - 模组 ID: "@mekanism" (禁止整个模组的物品)
                        - 物品标签："#c:ingots/iron"
                        - 模组标签："@c:ingots" (禁止整个标签下的物品)
                        示例：["minecraft:bedrock", "@evilmod", "#c:ores/diamond"]""")
                .defineList("blacklistItems", List::of, obj -> obj instanceof String);

        WHITELIST_ITEMS = BUILDER
                .comment("""
                        白名单物品列表。格式同黑名单。
                        只在 blacklistMode=false 时生效。
                        示例：["minecraft:cobblestone", "minecraft:sand", "#minecraft:planks"]""")
                .defineList("whitelistItems", List::of, obj -> obj instanceof String);
        BUILDER.pop();

        BUILDER.push("等级设置");

        BUILDER.push("Tier 1");
        ITEM_TIER1_OUTPUT_AMOUNT = BUILDER
                .comment("等级 1 每次操作产生的物品数量（默认：4）")
                .defineInRange("outputAmount", 4, 1, Integer.MAX_VALUE);
        ITEM_TIER1_OUTPUT_TIME = BUILDER
                .comment("等级 1 每次操作所需的tick（默认：20）")
                .defineInRange("outputTime", 20, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Tier 2");
        ITEM_TIER2_OUTPUT_AMOUNT = BUILDER
                .comment("等级 2 每次操作产生的物品数量（默认：16）")
                .defineInRange("outputAmount", 16, 1, Integer.MAX_VALUE);
        ITEM_TIER2_OUTPUT_TIME = BUILDER
                .comment("等级 2 每次操作所需的tick（默认：15）")
                .defineInRange("outputTime", 15, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Tier 3");
        ITEM_TIER3_OUTPUT_AMOUNT = BUILDER
                .comment("等级 3 每次操作产生的物品数量（默认：32）")
                .defineInRange("outputAmount", 32, 1, Integer.MAX_VALUE);
        ITEM_TIER3_OUTPUT_TIME = BUILDER
                .comment("等级 3 每次操作所需的tick（默认：10）")
                .defineInRange("outputTime", 10, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Tier 4");
        ITEM_TIER4_OUTPUT_AMOUNT = BUILDER
                .comment("等级 4 每次操作产生的物品数量（默认：64）")
                .defineInRange("outputAmount", 64, 1, Integer.MAX_VALUE);
        ITEM_TIER4_OUTPUT_TIME = BUILDER
                .comment("等级 4 每次操作所需的tick（默认：5）")
                .defineInRange("outputTime", 5, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Tier 5");
        ITEM_TIER5_OUTPUT_AMOUNT = BUILDER
                .comment("等级 5 每次操作产生的物品数量（默认：128）")
                .defineInRange("outputAmount", 128, 1, Integer.MAX_VALUE);
        ITEM_TIER5_OUTPUT_TIME = BUILDER
                .comment("等级 5 每次操作所需的tick（默认：1）")
                .defineInRange("outputTime", 1, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.pop();
        BUILDER.pop();


        BUILDER.push("流体复制机设置");

        BUILDER.push("输出罐容量设置");
        FLUID_REPLICATOR_OUTPUT_TANK_SIZE = BUILDER
                .comment("流体复制机输出罐容量（单位：毫桶，默认：100000）")
                .defineInRange("outputTankCapacity", 100000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("等级设置");

        BUILDER.push("Tier 1");
        FLUID_TIER1_OUTPUT_AMOUNT = BUILDER
                .comment("等级 1 每次操作产生的流体量（单位：毫桶，默认：1000）")
                .defineInRange("outputAmount", 1000, 1, Integer.MAX_VALUE);
        FLUID_TIER1_WATER_AMOUNT = BUILDER
                .comment("等级 1 每次操作产生的水量（单位：毫桶，默认：1000）")
                .defineInRange("waterOutputAmount", 1000, 1, Integer.MAX_VALUE);
        FLUID_TIER1_LAVA_AMOUNT = BUILDER
                .comment("等级 1 每次操作产生的岩浆量（单位：毫桶，默认：10）")
                .defineInRange("lavaOutputAmount", 10, 1, Integer.MAX_VALUE);
        FLUID_TIER1_OUTPUT_TIME = BUILDER
                .comment("等级 1 每次操作所需的 tick（默认：20）")
                .defineInRange("outputTime", 20, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Tier 2");
        FLUID_TIER2_OUTPUT_AMOUNT = BUILDER
                .comment("等级 2 每次操作产生的流体量（单位：毫桶，默认：2500）")
                .defineInRange("outputAmount", 2500, 1, Integer.MAX_VALUE);
        FLUID_TIER2_WATER_AMOUNT = BUILDER
                .comment("等级 2 每次操作产生的水量（单位：毫桶，默认：10000）")
                .defineInRange("waterOutputAmount", 10000, 1, Integer.MAX_VALUE);
        FLUID_TIER2_LAVA_AMOUNT = BUILDER
                .comment("等级 2 每次操作产生的岩浆量（单位：毫桶，默认：50）")
                .defineInRange("lavaOutputAmount", 50, 1, Integer.MAX_VALUE);
        FLUID_TIER2_OUTPUT_TIME = BUILDER
                .comment("等级 2 每次操作所需的 tick（默认：15）")
                .defineInRange("outputTime", 15, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Tier 3");
        FLUID_TIER3_OUTPUT_AMOUNT = BUILDER
                .comment("等级 3 每次操作产生的流体量（单位：毫桶，默认：5000）")
                .defineInRange("outputAmount", 5000, 1, Integer.MAX_VALUE);
        FLUID_TIER3_WATER_AMOUNT = BUILDER
                .comment("等级 3 每次操作产生的水量（单位：毫桶，默认：100000）")
                .defineInRange("waterOutputAmount", 100000, 1, Integer.MAX_VALUE);
        FLUID_TIER3_LAVA_AMOUNT = BUILDER
                .comment("等级 3 每次操作产生的岩浆量（单位：毫桶，默认：5000）")
                .defineInRange("lavaOutputAmount", 100, 1, Integer.MAX_VALUE);
        FLUID_TIER3_OUTPUT_TIME = BUILDER
                .comment("等级 3 每次操作所需的 tick（默认：10）")
                .defineInRange("outputTime", 10, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Tier 4");
        FLUID_TIER4_OUTPUT_AMOUNT = BUILDER
                .comment("等级 4 每次操作产生的流体量（单位：毫桶，默认：10000）")
                .defineInRange("outputAmount", 10000, 1, Integer.MAX_VALUE);
        FLUID_TIER4_WATER_AMOUNT = BUILDER
                .comment("等级 4 每次操作产生的水量（单位：毫桶，默认：1000000）")
                .defineInRange("waterOutputAmount", 1000000, 1, Integer.MAX_VALUE);
        FLUID_TIER4_LAVA_AMOUNT = BUILDER
                .comment("等级 4 每次操作产生的岩浆量（单位：毫桶，默认：500）")
                .defineInRange("lavaOutputAmount", 500, 1, Integer.MAX_VALUE);
        FLUID_TIER4_OUTPUT_TIME = BUILDER
                .comment("等级 4 每次操作所需的 tick（默认：5）")
                .defineInRange("outputTime", 5, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Tier 5");
        FLUID_TIER5_OUTPUT_AMOUNT = BUILDER
                .comment("等级 5 每次操作产生的流体量（单位：毫桶，默认：25000）")
                .defineInRange("outputAmount", 25000, 1, Integer.MAX_VALUE);
        FLUID_TIER5_WATER_AMOUNT = BUILDER
                .comment("等级 5 每次操作产生的水量（单位：毫桶，默认：10000000）")
                .defineInRange("waterOutputAmount", 10000000, 1, Integer.MAX_VALUE);
        FLUID_TIER5_LAVA_AMOUNT = BUILDER
                .comment("等级 5 每次操作产生的岩浆量（单位：毫桶，默认：1000）")
                .defineInRange("lavaOutputAmount", 1000, 1, Integer.MAX_VALUE);
        FLUID_TIER5_OUTPUT_TIME = BUILDER
                .comment("等级 5 每次操作所需的 tick（默认：1）")
                .defineInRange("outputTime", 1, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.pop();
        BUILDER.pop();

        CONFIG_SPEC = BUILDER.build();
    }

    // ======================= 物品复制机配置获取方法 =======================
    public static int getItemReplicatorOutputSlots() {
        return ITEM_REPLICATOR_OUTPUT_SLOTS.get();
    }

    public static boolean isBlacklistMode() {
        return BLACKLIST_MODE.get();
    }

    public static List<String> getBlacklistItems() {
        return BLACKLIST_ITEMS.get().stream()
                .map(obj -> (String) obj)
                .toList();
    }

    public static List<String> getWhitelistItems() {
        return WHITELIST_ITEMS.get().stream()
                .map(obj -> (String) obj)
                .toList();
    }

    // Tier 1
    public static int getItemTier1OutputAmount() {
        return ITEM_TIER1_OUTPUT_AMOUNT.get();
    }

    public static int getItemTier1OutputTime() {
        return ITEM_TIER1_OUTPUT_TIME.get();
    }

    // Tier 2
    public static int getItemTier2OutputAmount() {
        return ITEM_TIER2_OUTPUT_AMOUNT.get();
    }

    public static int getItemTier2OutputTime() {
        return ITEM_TIER2_OUTPUT_TIME.get();
    }

    // Tier 3
    public static int getItemTier3OutputAmount() {
        return ITEM_TIER3_OUTPUT_AMOUNT.get();
    }

    public static int getItemTier3OutputTime() {
        return ITEM_TIER3_OUTPUT_TIME.get();
    }

    // Tier 4
    public static int getItemTier4OutputAmount() {
        return ITEM_TIER4_OUTPUT_AMOUNT.get();
    }

    public static int getItemTier4OutputTime() {
        return ITEM_TIER4_OUTPUT_TIME.get();
    }

    // Tier 5
    public static int getItemTier5OutputAmount() {
        return ITEM_TIER5_OUTPUT_AMOUNT.get();
    }

    public static int getItemTier5OutputTime() {
        return ITEM_TIER5_OUTPUT_TIME.get();
    }

    // ======================= 流体复制机配置获取方法 =======================
    public static int getFluidReplicatorOutputTankSize() {
        return FLUID_REPLICATOR_OUTPUT_TANK_SIZE.get();
    }

    // Tier 1
    public static int getFluidTier1OutputAmount() {
        return FLUID_TIER1_OUTPUT_AMOUNT.get();
    }

    public static int getFluidTier1WaterAmount() {
        return FLUID_TIER1_WATER_AMOUNT.get();
    }

    public static int getFluidTier1LavaAmount() {
        return FLUID_TIER1_LAVA_AMOUNT.get();
    }

    public static int getFluidTier1OutputTime() {
        return FLUID_TIER1_OUTPUT_TIME.get();
    }

    // Tier 2
    public static int getFluidTier2OutputAmount() {
        return FLUID_TIER2_OUTPUT_AMOUNT.get();
    }

    public static int getFluidTier2WaterAmount() {
        return FLUID_TIER2_WATER_AMOUNT.get();
    }

    public static int getFluidTier2LavaAmount() {
        return FLUID_TIER2_LAVA_AMOUNT.get();
    }

    public static int getFluidTier2OutputTime() {
        return FLUID_TIER2_OUTPUT_TIME.get();
    }

    // Tier 3
    public static int getFluidTier3OutputAmount() {
        return FLUID_TIER3_OUTPUT_AMOUNT.get();
    }

    public static int getFluidTier3WaterAmount() {
        return FLUID_TIER3_WATER_AMOUNT.get();
    }

    public static int getFluidTier3LavaAmount() {
        return FLUID_TIER3_LAVA_AMOUNT.get();
    }

    public static int getFluidTier3OutputTime() {
        return FLUID_TIER3_OUTPUT_TIME.get();
    }

    // Tier 4
    public static int getFluidTier4OutputAmount() {
        return FLUID_TIER4_OUTPUT_AMOUNT.get();
    }

    public static int getFluidTier4WaterAmount() {
        return FLUID_TIER4_WATER_AMOUNT.get();
    }

    public static int getFluidTier4LavaAmount() {
        return FLUID_TIER4_LAVA_AMOUNT.get();
    }

    public static int getFluidTier4OutputTime() {
        return FLUID_TIER4_OUTPUT_TIME.get();
    }

    // Tier 5
    public static int getFluidTier5OutputAmount() {
        return FLUID_TIER5_OUTPUT_AMOUNT.get();
    }

    public static int getFluidTier5WaterAmount() {
        return FLUID_TIER5_WATER_AMOUNT.get();
    }

    public static int getFluidTier5LavaAmount() {
        return FLUID_TIER5_LAVA_AMOUNT.get();
    }

    public static int getFluidTier5OutputTime() {
        return FLUID_TIER5_OUTPUT_TIME.get();
    }
}
