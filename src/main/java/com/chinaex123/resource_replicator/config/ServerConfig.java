package com.chinaex123.resource_replicator.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class ServerConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec CONFIG_SPEC;

    // ======================= 物品复制机配置 =======================
    private static final ModConfigSpec.BooleanValue ITEM_REPLICATOR_ENABLE_DESTROY;
    private static final ModConfigSpec.BooleanValue ITEM_REPLICATOR_AUTO_OUTPUT;

    // 黑白名单配置
    private static final ModConfigSpec.BooleanValue BLACKLIST_MODE;
    private static final ModConfigSpec.ConfigValue<List<?>> BLACKLIST_ITEMS;
    private static final ModConfigSpec.ConfigValue<List<?>> WHITELIST_ITEMS;

    // T1-T5 物品复制机配置 - 输出槽、输出数量、输出时间
    private static final ModConfigSpec.IntValue ITEM_TIER1_OUTPUT_SLOTS;
    private static final ModConfigSpec.IntValue ITEM_TIER1_OUTPUT_AMOUNT;
    private static final ModConfigSpec.IntValue ITEM_TIER1_OUTPUT_TIME;
    private static final ModConfigSpec.IntValue ITEM_TIER2_OUTPUT_SLOTS;
    private static final ModConfigSpec.IntValue ITEM_TIER2_OUTPUT_AMOUNT;
    private static final ModConfigSpec.IntValue ITEM_TIER2_OUTPUT_TIME;
    private static final ModConfigSpec.IntValue ITEM_TIER3_OUTPUT_SLOTS;
    private static final ModConfigSpec.IntValue ITEM_TIER3_OUTPUT_AMOUNT;
    private static final ModConfigSpec.IntValue ITEM_TIER3_OUTPUT_TIME;
    private static final ModConfigSpec.IntValue ITEM_TIER4_OUTPUT_SLOTS;
    private static final ModConfigSpec.IntValue ITEM_TIER4_OUTPUT_AMOUNT;
    private static final ModConfigSpec.IntValue ITEM_TIER4_OUTPUT_TIME;
    private static final ModConfigSpec.IntValue ITEM_TIER5_OUTPUT_SLOTS;
    private static final ModConfigSpec.IntValue ITEM_TIER5_OUTPUT_AMOUNT;
    private static final ModConfigSpec.IntValue ITEM_TIER5_OUTPUT_TIME;

    // T1-T5 物品复制机能量配置
    private static final ModConfigSpec.IntValue ITEM_TIER1_ENERGY_CAPACITY;
    private static final ModConfigSpec.IntValue ITEM_TIER1_ENERGY_CONSUMPTION;
    private static final ModConfigSpec.IntValue ITEM_TIER2_ENERGY_CAPACITY;
    private static final ModConfigSpec.IntValue ITEM_TIER2_ENERGY_CONSUMPTION;
    private static final ModConfigSpec.IntValue ITEM_TIER3_ENERGY_CAPACITY;
    private static final ModConfigSpec.IntValue ITEM_TIER3_ENERGY_CONSUMPTION;
    private static final ModConfigSpec.IntValue ITEM_TIER4_ENERGY_CAPACITY;
    private static final ModConfigSpec.IntValue ITEM_TIER4_ENERGY_CONSUMPTION;
    private static final ModConfigSpec.IntValue ITEM_TIER5_ENERGY_CAPACITY;
    private static final ModConfigSpec.IntValue ITEM_TIER5_ENERGY_CONSUMPTION;


    // ======================= 流体复制机配置 =======================
    private static final ModConfigSpec.BooleanValue FLUID_REPLICATOR_ENABLE_DESTROY;
    private static final ModConfigSpec.BooleanValue FLUID_REPLICATOR_AUTO_OUTPUT;

    // 流体黑白名单配置
    private static final ModConfigSpec.BooleanValue FLUID_BLACKLIST_MODE;
    private static final ModConfigSpec.ConfigValue<List<?>> FLUID_BLACKLIST_ITEMS;
    private static final ModConfigSpec.ConfigValue<List<?>> FLUID_WHITELIST_ITEMS;

    // T1-T5 流体复制机配置 - 输出槽、输出数量、输出时间
    private static final ModConfigSpec.IntValue FLUID_TIER1_OUTPUT_TANK_CAPACITY;
    private static final ModConfigSpec.IntValue FLUID_TIER1_OUTPUT_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER1_WATER_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER1_LAVA_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER1_OUTPUT_TIME;
    private static final ModConfigSpec.IntValue FLUID_TIER2_OUTPUT_TANK_CAPACITY;
    private static final ModConfigSpec.IntValue FLUID_TIER2_OUTPUT_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER2_WATER_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER2_LAVA_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER2_OUTPUT_TIME;
    private static final ModConfigSpec.IntValue FLUID_TIER3_OUTPUT_TANK_CAPACITY;
    private static final ModConfigSpec.IntValue FLUID_TIER3_OUTPUT_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER3_WATER_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER3_LAVA_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER3_OUTPUT_TIME;
    private static final ModConfigSpec.IntValue FLUID_TIER4_OUTPUT_TANK_CAPACITY;
    private static final ModConfigSpec.IntValue FLUID_TIER4_OUTPUT_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER4_WATER_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER4_LAVA_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER4_OUTPUT_TIME;
    private static final ModConfigSpec.IntValue FLUID_TIER5_OUTPUT_TANK_CAPACITY;
    private static final ModConfigSpec.IntValue FLUID_TIER5_OUTPUT_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER5_WATER_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER5_LAVA_AMOUNT;
    private static final ModConfigSpec.IntValue FLUID_TIER5_OUTPUT_TIME;

    // T1-T5 流体复制机能量配置
    private static final ModConfigSpec.IntValue FLUID_TIER1_ENERGY_CAPACITY;
    private static final ModConfigSpec.IntValue FLUID_TIER1_ENERGY_CONSUMPTION;
    private static final ModConfigSpec.IntValue FLUID_TIER2_ENERGY_CAPACITY;
    private static final ModConfigSpec.IntValue FLUID_TIER2_ENERGY_CONSUMPTION;
    private static final ModConfigSpec.IntValue FLUID_TIER3_ENERGY_CAPACITY;
    private static final ModConfigSpec.IntValue FLUID_TIER3_ENERGY_CONSUMPTION;
    private static final ModConfigSpec.IntValue FLUID_TIER4_ENERGY_CAPACITY;
    private static final ModConfigSpec.IntValue FLUID_TIER4_ENERGY_CONSUMPTION;
    private static final ModConfigSpec.IntValue FLUID_TIER5_ENERGY_CAPACITY;
    private static final ModConfigSpec.IntValue FLUID_TIER5_ENERGY_CONSUMPTION;


    // ==================== 化学品复制机参数（Mekanism 联动） ====================
    private static final ModConfigSpec.BooleanValue CHEMICAL_REPLICATOR_ENABLE_DESTROY;
    private static final ModConfigSpec.BooleanValue CHEMICAL_BLACKLIST_MODE;
    private static final ModConfigSpec.ConfigValue<List<?>> CHEMICAL_BLACKLIST_ITEMS;
    private static final ModConfigSpec.ConfigValue<List<?>> CHEMICAL_WHITELIST_ITEMS;

    // T1-T5 化学品复制机配置 - 输出罐容量、输出数量、输出时间
    private static final ModConfigSpec.IntValue CHEMICAL_TIER_1_OUTPUT_TANK_CAPACITY;
    private static final ModConfigSpec.IntValue CHEMICAL_TIER_1_OUTPUT_AMOUNT;
    private static final ModConfigSpec.IntValue CHEMICAL_TIER_1_OUTPUT_TIME;
    private static final ModConfigSpec.IntValue CHEMICAL_TIER_2_OUTPUT_TANK_CAPACITY;
    private static final ModConfigSpec.IntValue CHEMICAL_TIER_2_OUTPUT_AMOUNT;
    private static final ModConfigSpec.IntValue CHEMICAL_TIER_2_OUTPUT_TIME;
    private static final ModConfigSpec.IntValue CHEMICAL_TIER_3_OUTPUT_TANK_CAPACITY;
    private static final ModConfigSpec.IntValue CHEMICAL_TIER_3_OUTPUT_AMOUNT;
    private static final ModConfigSpec.IntValue CHEMICAL_TIER_3_OUTPUT_TIME;
    private static final ModConfigSpec.IntValue CHEMICAL_TIER_4_OUTPUT_TANK_CAPACITY;
    private static final ModConfigSpec.IntValue CHEMICAL_TIER_4_OUTPUT_AMOUNT;
    private static final ModConfigSpec.IntValue CHEMICAL_TIER_4_OUTPUT_TIME;
    private static final ModConfigSpec.IntValue CHEMICAL_TIER_5_OUTPUT_TANK_CAPACITY;
    private static final ModConfigSpec.IntValue CHEMICAL_TIER_5_OUTPUT_AMOUNT;
    private static final ModConfigSpec.IntValue CHEMICAL_TIER_5_OUTPUT_TIME;

    // T1-T5 化学品复制机能量配置
    private static final ModConfigSpec.IntValue CHEMICAL_TIER_1_ENERGY_CAPACITY;
    private static final ModConfigSpec.IntValue CHEMICAL_TIER_1_ENERGY_CONSUMPTION;
    private static final ModConfigSpec.IntValue CHEMICAL_TIER_2_ENERGY_CAPACITY;
    private static final ModConfigSpec.IntValue CHEMICAL_TIER_2_ENERGY_CONSUMPTION;
    private static final ModConfigSpec.IntValue CHEMICAL_TIER_3_ENERGY_CAPACITY;
    private static final ModConfigSpec.IntValue CHEMICAL_TIER_3_ENERGY_CONSUMPTION;
    private static final ModConfigSpec.IntValue CHEMICAL_TIER_4_ENERGY_CAPACITY;
    private static final ModConfigSpec.IntValue CHEMICAL_TIER_4_ENERGY_CONSUMPTION;
    private static final ModConfigSpec.IntValue CHEMICAL_TIER_5_ENERGY_CAPACITY;
    private static final ModConfigSpec.IntValue CHEMICAL_TIER_5_ENERGY_CONSUMPTION;


    static {
        BUILDER.push("物品复制机设置");

        BUILDER.push("销毁功能设置");
        ITEM_REPLICATOR_ENABLE_DESTROY = BUILDER
                .comment("是否启用物品复制机的销毁功能（默认：false）。启用后，通过管道输入到输入槽的物品会被销毁。")
                .define("enableDestroy", false);
        BUILDER.pop();

        BUILDER.push("自动输出功能设置");
        ITEM_REPLICATOR_AUTO_OUTPUT = BUILDER
                .comment("是否启用物品复制机的自动输出功能（默认：true）。启用后，复制机会自动向周围相邻的容器输出物品。")
                .define("autoOutput", true);
        BUILDER.pop();

        BUILDER.push("黑白名单设置");
        BLACKLIST_MODE = BUILDER
                .comment("是否使用黑名单模式（true=黑名单，false=白名单）")
                .define("blacklistMode", true);
        BLACKLIST_ITEMS = BUILDER
                .comment("""
                        物品黑名单列表。支持以下格式：
                        - 物品 ID: "minecraft:diamond"
                        - 模组 ID: "@mekanism" (禁止整个模组的物品)
                        - 物品标签："#c:ingots/iron"
                        示例：["minecraft:bedrock", "@create", "#c:ores/diamond"]""")
                .defineList("blacklistItems", List::of, obj -> obj instanceof String);
        WHITELIST_ITEMS = BUILDER
                .comment("""
                        物品白名单列表。格式同黑名单。
                        只在 blacklistMode=false 时生效。
                        示例：["minecraft:cobblestone", "minecraft:sand", "#minecraft:planks"]""")
                .defineList("whitelistItems", List::of, obj -> obj instanceof String);
        BUILDER.pop();


        BUILDER.push("等级设置");

        BUILDER.push("Tier 1");
        ITEM_TIER1_OUTPUT_SLOTS = BUILDER
                .comment("等级 1 的输出槽数量（默认：1）")
                .defineInRange("outputSlots", 1, 1, 9);
        ITEM_TIER1_OUTPUT_AMOUNT = BUILDER
                .comment("等级 1 每次操作产生的物品数量 (默认:4)")
                .defineInRange("outputAmount", 4, 1, Integer.MAX_VALUE);
        ITEM_TIER1_OUTPUT_TIME = BUILDER
                .comment("等级 1 每次操作所需的 tick(默认:20)")
                .defineInRange("outputTime", 20, 1, Integer.MAX_VALUE);
        ITEM_TIER1_ENERGY_CAPACITY = BUILDER
                .comment("等级 1 的最大能量存储 (单位:FE，默认:10000)")
                .defineInRange("energyCapacity", 10000, 1000, Integer.MAX_VALUE);
        ITEM_TIER1_ENERGY_CONSUMPTION = BUILDER
                .comment("等级 1 每次操作消耗的能量 (单位:FE，默认:2000)")
                .defineInRange("energyConsumption", 2000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Tier 2");
        ITEM_TIER2_OUTPUT_SLOTS = BUILDER
                .comment("等级 2 的输出槽数量（默认：2）")
                .defineInRange("outputSlots", 2, 1, 9);
        ITEM_TIER2_OUTPUT_AMOUNT = BUILDER
                .comment("等级 2 每次操作产生的物品数量 (默认:16)")
                .defineInRange("outputAmount", 16, 1, Integer.MAX_VALUE);
        ITEM_TIER2_OUTPUT_TIME = BUILDER
                .comment("等级 2 每次操作所需的 tick(默认:15)")
                .defineInRange("outputTime", 15, 1, Integer.MAX_VALUE);
        ITEM_TIER2_ENERGY_CAPACITY = BUILDER
                .comment("等级 2 的最大能量存储 (单位:FE，默认:50000)")
                .defineInRange("energyCapacity", 50000, 1000, Integer.MAX_VALUE);
        ITEM_TIER2_ENERGY_CONSUMPTION = BUILDER
                .comment("等级 2 每次操作消耗的能量 (单位:FE，默认:4000)")
                .defineInRange("energyConsumption", 4000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Tier 3");
        ITEM_TIER3_OUTPUT_SLOTS = BUILDER
                .comment("等级 3 的输出槽数量（默认：3）")
                .defineInRange("outputSlots", 3, 1, 9);
        ITEM_TIER3_OUTPUT_AMOUNT = BUILDER
                .comment("等级 3 每次操作产生的物品数量 (默认:32)")
                .defineInRange("outputAmount", 32, 1, Integer.MAX_VALUE);
        ITEM_TIER3_OUTPUT_TIME = BUILDER
                .comment("等级 3 每次操作所需的 tick(默认:10)")
                .defineInRange("outputTime", 10, 1, Integer.MAX_VALUE);
        ITEM_TIER3_ENERGY_CAPACITY = BUILDER
                .comment("等级 3 的最大能量存储 (单位:FE，默认:100000)")
                .defineInRange("energyCapacity", 100000, 1000, Integer.MAX_VALUE);
        ITEM_TIER3_ENERGY_CONSUMPTION = BUILDER
                .comment("等级 3 每次操作消耗的能量 (单位:FE，默认:6000)")
                .defineInRange("energyConsumption", 6000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Tier 4");
        ITEM_TIER4_OUTPUT_SLOTS = BUILDER
                .comment("等级 4 的输出槽数量（默认：5）")
                .defineInRange("outputSlots", 5, 1, 9);
        ITEM_TIER4_OUTPUT_AMOUNT = BUILDER
                .comment("等级 4 每次操作产生的物品数量 (默认:64)")
                .defineInRange("outputAmount", 64, 1, Integer.MAX_VALUE);
        ITEM_TIER4_OUTPUT_TIME = BUILDER
                .comment("等级 4 每次操作所需的 tick(默认:5)")
                .defineInRange("outputTime", 5, 1, Integer.MAX_VALUE);
        ITEM_TIER4_ENERGY_CAPACITY = BUILDER
                .comment("等级 4 的最大能量存储 (单位:FE，默认:500000)")
                .defineInRange("energyCapacity", 500000, 1000, Integer.MAX_VALUE);
        ITEM_TIER4_ENERGY_CONSUMPTION = BUILDER
                .comment("等级 4 每次操作消耗的能量 (单位:FE，默认:8000)")
                .defineInRange("energyConsumption", 8000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Tier 5");
        ITEM_TIER5_OUTPUT_SLOTS = BUILDER
                .comment("等级 5 的输出槽数量（默认：9）")
                .defineInRange("outputSlots", 9, 1, 9);
        ITEM_TIER5_OUTPUT_AMOUNT = BUILDER
                .comment("等级 5 每次操作产生的物品数量 (默认:128)")
                .defineInRange("outputAmount", 128, 1, Integer.MAX_VALUE);
        ITEM_TIER5_OUTPUT_TIME = BUILDER
                .comment("等级 5 每次操作所需的 tick(默认:1)")
                .defineInRange("outputTime", 1, 1, Integer.MAX_VALUE);
        ITEM_TIER5_ENERGY_CAPACITY = BUILDER
                .comment("等级 5 的最大能量存储 (单位:FE，默认:1000000)")
                .defineInRange("energyCapacity", 1000000, 1000, Integer.MAX_VALUE);
        ITEM_TIER5_ENERGY_CONSUMPTION = BUILDER
                .comment("等级 5 每次操作消耗的能量 (单位:FE，默认:10000)")
                .defineInRange("energyConsumption", 10000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.pop();
        BUILDER.pop();


        BUILDER.push("流体复制机设置");

        BUILDER.push("销毁功能设置");
        FLUID_REPLICATOR_ENABLE_DESTROY = BUILDER
                .comment("是否启用流体复制机的销毁功能 (默认:false)。启用后，通过管道输入到输入罐的流体会被销毁。")
                .define("enableDestroy", false);
        BUILDER.pop();

        BUILDER.push("自动输出功能设置");
        FLUID_REPLICATOR_AUTO_OUTPUT = BUILDER
                .comment("是否启用流体复制机的自动输出功能 (默认:true)。启用后，复制机会自动向周围相邻的容器输出流体。")
                .define("autoOutput", true);
        BUILDER.pop();

        BUILDER.push("黑白名单模式");
        FLUID_BLACKLIST_MODE = BUILDER
                .comment("是否使用流体黑名单模式（true=黑名单，false=白名单）")
                .define("fluidBlacklistMode", true);
        FLUID_BLACKLIST_ITEMS = BUILDER
                .comment("""
                        流体黑名单列表。支持以下格式：
                        - 流体 ID: "minecraft:water"
                        - 模组 ID: "@mekanism" (禁止整个模组的流体)
                        - 流体标签："#c:water"
                        示例：["minecraft:lava", "@create", "#c:fuels"]""")
                .defineList("fluidBlacklistItems", List::of, obj -> obj instanceof String);
        FLUID_WHITELIST_ITEMS = BUILDER
                .comment("""
                        流体白名单列表。格式同流体黑名单。
                        只在 fluidBlacklistMode=false 时生效。
                        示例：["minecraft:water", "minecraft:lava", "#c:experience"]""")
                .defineList("fluidWhitelistItems", List::of, obj -> obj instanceof String);
        BUILDER.pop();

        BUILDER.push("等级设置");

        BUILDER.push("Tier 1");
        FLUID_TIER1_OUTPUT_TANK_CAPACITY = BUILDER
                .comment("等级 1 的输出罐容量 (单位:mB，默认:8000)")
                .defineInRange("outputTankCapacity", 8000, 1000, Integer.MAX_VALUE);
        FLUID_TIER1_OUTPUT_AMOUNT = BUILDER
                .comment("等级 1 每次操作产生的流体数量 (单位:mB，默认:1000)")
                .defineInRange("outputAmount", 1000, 1, Integer.MAX_VALUE);
        FLUID_TIER1_OUTPUT_TIME = BUILDER
                .comment("等级 1 每次操作所需的 tick(默认:20)")
                .defineInRange("outputTime", 20, 1, Integer.MAX_VALUE);
        FLUID_TIER1_WATER_AMOUNT = BUILDER
                .comment("等级 1 复制水时的特殊产量 (单位:mB，默认:1000)")
                .defineInRange("waterAmount", 1000, 1, Integer.MAX_VALUE);
        FLUID_TIER1_LAVA_AMOUNT = BUILDER
                .comment("等级 1 复制岩浆时的特殊产量 (单位:mB，默认:10)")
                .defineInRange("lavaAmount", 10, 1, Integer.MAX_VALUE);
        FLUID_TIER1_ENERGY_CAPACITY = BUILDER
                .comment("等级 1 的最大能量存储 (单位:FE，默认:10000)")
                .defineInRange("energyCapacity", 10000, 1000, Integer.MAX_VALUE);
        FLUID_TIER1_ENERGY_CONSUMPTION = BUILDER
                .comment("等级 1 每 1000mB 流体消耗的能量 (单位:FE，默认:2000)")
                .defineInRange("energyConsumption", 2000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Tier 2");
        FLUID_TIER2_OUTPUT_TANK_CAPACITY = BUILDER
                .comment("等级 2 的输出罐容量 (单位:mB，默认:10000)")
                .defineInRange("outputTankCapacity", 10000, 1000, Integer.MAX_VALUE);
        FLUID_TIER2_OUTPUT_AMOUNT = BUILDER
                .comment("等级 2 每次操作产生的流体数量 (单位:mB，默认:2500)")
                .defineInRange("outputAmount", 2500, 1, Integer.MAX_VALUE);
        FLUID_TIER2_OUTPUT_TIME = BUILDER
                .comment("等级 2 每次操作所需的 tick(默认:15)")
                .defineInRange("outputTime", 15, 1, Integer.MAX_VALUE);
        FLUID_TIER2_WATER_AMOUNT = BUILDER
                .comment("等级 2 复制水时的特殊产量 (单位:mB，默认:10000)")
                .defineInRange("waterAmount", 10000, 1, Integer.MAX_VALUE);
        FLUID_TIER2_LAVA_AMOUNT = BUILDER
                .comment("等级 2 复制岩浆时的特殊产量 (单位:mB，默认:50)")
                .defineInRange("lavaAmount", 50, 1, Integer.MAX_VALUE);
        FLUID_TIER2_ENERGY_CAPACITY = BUILDER
                .comment("等级 2 的最大能量存储 (单位:FE，默认:50000)")
                .defineInRange("energyCapacity", 50000, 1000, Integer.MAX_VALUE);
        FLUID_TIER2_ENERGY_CONSUMPTION = BUILDER
                .comment("等级 2 每 1000mB 流体消耗的能量 (单位:FE，默认:4000)")
                .defineInRange("energyConsumption", 4000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Tier 3");
        FLUID_TIER3_OUTPUT_TANK_CAPACITY = BUILDER
                .comment("等级 3 的输出罐容量 (单位:mB，默认:30000)")
                .defineInRange("outputTankCapacity", 30000, 1000, Integer.MAX_VALUE);
        FLUID_TIER3_OUTPUT_AMOUNT = BUILDER
                .comment("等级 3 每次操作产生的流体数量 (单位:mB，默认:5000)")
                .defineInRange("outputAmount", 5000, 1, Integer.MAX_VALUE);
        FLUID_TIER3_OUTPUT_TIME = BUILDER
                .comment("等级 3 每次操作所需的 tick(默认:10)")
                .defineInRange("outputTime", 10, 1, Integer.MAX_VALUE);
        FLUID_TIER3_WATER_AMOUNT = BUILDER
                .comment("等级 3 复制水时的特殊产量 (单位:mB，默认:100000)")
                .defineInRange("waterAmount", 100000, 1, Integer.MAX_VALUE);
        FLUID_TIER3_LAVA_AMOUNT = BUILDER
                .comment("等级 3 复制岩浆时的特殊产量 (单位:mB，默认:100)")
                .defineInRange("lavaAmount", 100, 1, Integer.MAX_VALUE);
        FLUID_TIER3_ENERGY_CAPACITY = BUILDER
                .comment("等级 3 的最大能量存储 (单位:FE，默认:100000)")
                .defineInRange("energyCapacity", 100000, 1000, Integer.MAX_VALUE);
        FLUID_TIER3_ENERGY_CONSUMPTION = BUILDER
                .comment("等级 3 每 1000mB 流体消耗的能量 (单位:FE，默认:6000)")
                .defineInRange("energyConsumption", 6000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Tier 4");
        FLUID_TIER4_OUTPUT_TANK_CAPACITY = BUILDER
                .comment("等级 4 的输出罐容量 (单位:mB，默认:100000)")
                .defineInRange("outputTankCapacity", 100000, 1000, Integer.MAX_VALUE);
        FLUID_TIER4_OUTPUT_AMOUNT = BUILDER
                .comment("等级 4 每次操作产生的流体数量 (单位:mB，默认:10000)")
                .defineInRange("outputAmount", 10000, 1, Integer.MAX_VALUE);
        FLUID_TIER4_OUTPUT_TIME = BUILDER
                .comment("等级 4 每次操作所需的 tick(默认:5)")
                .defineInRange("outputTime", 5, 1, Integer.MAX_VALUE);
        FLUID_TIER4_WATER_AMOUNT = BUILDER
                .comment("等级 4 复制水时的特殊产量 (单位:mB，默认:1000000)")
                .defineInRange("waterAmount", 1000000, 1, Integer.MAX_VALUE);
        FLUID_TIER4_LAVA_AMOUNT = BUILDER
                .comment("等级 4 复制岩浆时的特殊产量 (单位:mB，默认:500)")
                .defineInRange("lavaAmount", 500, 1, Integer.MAX_VALUE);
        FLUID_TIER4_ENERGY_CAPACITY = BUILDER
                .comment("等级 4 的最大能量存储 (单位:FE，默认:500000)")
                .defineInRange("energyCapacity", 500000, 1000, Integer.MAX_VALUE);
        FLUID_TIER4_ENERGY_CONSUMPTION = BUILDER
                .comment("等级 4 每 1000mB 流体消耗的能量 (单位:FE，默认:8000)")
                .defineInRange("energyConsumption", 8000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Tier 5");
        FLUID_TIER5_OUTPUT_TANK_CAPACITY = BUILDER
                .comment("等级 5 的输出罐容量 (单位:mB，默认:500000)")
                .defineInRange("outputTankCapacity", 500000, 1000, Integer.MAX_VALUE);
        FLUID_TIER5_OUTPUT_AMOUNT = BUILDER
                .comment("等级 5 每次操作产生的流体数量 (单位:mB，默认:25000)")
                .defineInRange("outputAmount", 25000, 1, Integer.MAX_VALUE);
        FLUID_TIER5_OUTPUT_TIME = BUILDER
                .comment("等级 5 每次操作所需的 tick(默认:1)")
                .defineInRange("outputTime", 1, 1, Integer.MAX_VALUE);
        FLUID_TIER5_WATER_AMOUNT = BUILDER
                .comment("等级 5 复制水时的特殊产量 (单位:mB，默认:10000000)")
                .defineInRange("waterAmount", 10000000, 1, Integer.MAX_VALUE);
        FLUID_TIER5_LAVA_AMOUNT = BUILDER
                .comment("等级 5 复制岩浆时的特殊产量 (单位:mB，默认:1000)")
                .defineInRange("lavaAmount", 1000, 1, Integer.MAX_VALUE);
        FLUID_TIER5_ENERGY_CAPACITY = BUILDER
                .comment("等级 5 的最大能量存储 (单位:FE，默认:1000000)")
                .defineInRange("energyCapacity", 1000000, 1000, Integer.MAX_VALUE);
        FLUID_TIER5_ENERGY_CONSUMPTION = BUILDER
                .comment("等级 5 每 1000mB 流体消耗的能量 (单位:FE，默认:10000)")
                .defineInRange("energyConsumption", 10000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.pop();
        BUILDER.pop();


        // 化学品复制机配置（Mekanism 联动）
        BUILDER.push("化学品复制机设置（Mekanism 联动）");

        BUILDER.push("销毁功能设置");
        CHEMICAL_REPLICATOR_ENABLE_DESTROY = BUILDER
                .comment("是否启用化学品复制机的销毁功能（默认：false）。启用后，通过管道输入的化学品会被销毁。")
                .define("enableDestroy", false);
        BUILDER.pop();

        BUILDER.push("黑白名单设置");
        CHEMICAL_BLACKLIST_MODE = BUILDER
                .comment("是否使用化学品黑名单模式（true=黑名单，false=白名单）")
                .define("blacklistMode", true);
        CHEMICAL_BLACKLIST_ITEMS = BUILDER
                .comment("""
                        化学品黑名单列表。支持以下格式：
                        - 化学品 ID: "mekanism:hydrogen"
                        - 模组 ID: "@mekanismgenerators" (禁止整个模组的化学品)
                        - 化学品标签："#mekanism:chemicals"
                        示例：["mekanism:antimatter", "@mekanismgenerators", "#c:fuels"]""")
                .defineList("blacklistItems", List::of, obj -> obj instanceof String);
        CHEMICAL_WHITELIST_ITEMS = BUILDER
                .comment("""
                        化学品白名单列表。格式同黑名单。
                        只在 blacklistMode=false 时生效。
                        示例：["mekanism:deuterium", "mekanism:tritium", "#mekanism:infused"]""")
                .defineList("whitelistItems", List::of, obj -> obj instanceof String);
        BUILDER.pop();

        BUILDER.push("等级设置");

        BUILDER.push("Tier 1");
        CHEMICAL_TIER_1_OUTPUT_TANK_CAPACITY = BUILDER
                .comment("等级 1 的输出罐容量 (单位:mB，默认:4000)")
                .defineInRange("outputTankCapacity", 4000, 1000, Integer.MAX_VALUE);
        CHEMICAL_TIER_1_OUTPUT_AMOUNT = BUILDER
                .comment("等级 1 每次操作产生的化学品的量（单位：mB，默认：10）")
                .defineInRange("outputAmount", 10, 1, Integer.MAX_VALUE);
        CHEMICAL_TIER_1_OUTPUT_TIME = BUILDER
                .comment("等级 1 每次操作所需的 tick（默认：20）")
                .defineInRange("outputTime", 20, 1, Integer.MAX_VALUE);
        CHEMICAL_TIER_1_ENERGY_CAPACITY = BUILDER
                .comment("等级 1 的最大能量存储 (单位:FE，默认:10000)")
                .defineInRange("energyCapacity", 10000, 1000, Integer.MAX_VALUE);
        CHEMICAL_TIER_1_ENERGY_CONSUMPTION = BUILDER
                .comment("等级 1 每 1000mB 化学品消耗的能量 (单位:FE，默认:2000)")
                .defineInRange("energyConsumption", 2000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Tier 2");
        CHEMICAL_TIER_2_OUTPUT_TANK_CAPACITY = BUILDER
                .comment("等级 2 的输出罐容量 (单位:mB，默认:10000)")
                .defineInRange("outputTankCapacity", 10000, 1000, Integer.MAX_VALUE);
        CHEMICAL_TIER_2_OUTPUT_AMOUNT = BUILDER
                .comment("等级 2 每次操作产生的化学品的量（单位：mB，默认：50）")
                .defineInRange("outputAmount", 50, 1, Integer.MAX_VALUE);
        CHEMICAL_TIER_2_OUTPUT_TIME = BUILDER
                .comment("等级 2 每次操作所需的 tick（默认：15）")
                .defineInRange("outputTime", 15, 1, Integer.MAX_VALUE);
        CHEMICAL_TIER_2_ENERGY_CAPACITY = BUILDER
                .comment("等级 2 的最大能量存储 (单位:FE，默认:50000)")
                .defineInRange("energyCapacity", 50000, 1000, Integer.MAX_VALUE);
        CHEMICAL_TIER_2_ENERGY_CONSUMPTION = BUILDER
                .comment("等级 2 每 1000mB 化学品消耗的能量 (单位:FE，默认:4000)")
                .defineInRange("energyConsumption", 4000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Tier 3");
        CHEMICAL_TIER_3_OUTPUT_TANK_CAPACITY = BUILDER
                .comment("等级 3 的输出罐容量 (单位:mB，默认:25000)")
                .defineInRange("outputTankCapacity", 25000, 1000, Integer.MAX_VALUE);
        CHEMICAL_TIER_3_OUTPUT_AMOUNT = BUILDER
                .comment("等级 3 每次操作产生的化学品的量（单位：mB，默认：100）")
                .defineInRange("outputAmount", 100, 1, Integer.MAX_VALUE);
        CHEMICAL_TIER_3_OUTPUT_TIME = BUILDER
                .comment("等级 3 每次操作所需的 tick（默认：10）")
                .defineInRange("outputTime", 10, 1, Integer.MAX_VALUE);
        CHEMICAL_TIER_3_ENERGY_CAPACITY = BUILDER
                .comment("等级 3 的最大能量存储 (单位:FE，默认:100000)")
                .defineInRange("energyCapacity", 100000, 1000, Integer.MAX_VALUE);
        CHEMICAL_TIER_3_ENERGY_CONSUMPTION = BUILDER
                .comment("等级 3 每 1000mB 化学品消耗的能量 (单位:FE，默认:6000)")
                .defineInRange("energyConsumption", 6000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Tier 4");
        CHEMICAL_TIER_4_OUTPUT_TANK_CAPACITY = BUILDER
                .comment("等级 4 的输出罐容量 (单位:mB，默认:50000)")
                .defineInRange("outputTankCapacity", 50000, 1000, Integer.MAX_VALUE);
        CHEMICAL_TIER_4_OUTPUT_AMOUNT = BUILDER
                .comment("等级 4 每次操作产生的化学品的量（单位：mB，默认：500）")
                .defineInRange("outputAmount", 500, 1, Integer.MAX_VALUE);
        CHEMICAL_TIER_4_OUTPUT_TIME = BUILDER
                .comment("等级 4 每次操作所需的 tick（默认：5）")
                .defineInRange("outputTime", 5, 1, Integer.MAX_VALUE);
        CHEMICAL_TIER_4_ENERGY_CAPACITY = BUILDER
                .comment("等级 4 的最大能量存储 (单位:FE，默认:500000)")
                .defineInRange("energyCapacity", 500000, 1000, Integer.MAX_VALUE);
        CHEMICAL_TIER_4_ENERGY_CONSUMPTION = BUILDER
                .comment("等级 4 每 1000mB 化学品消耗的能量 (单位:FE，默认:8000)")
                .defineInRange("energyConsumption", 8000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Tier 5");
        CHEMICAL_TIER_5_OUTPUT_TANK_CAPACITY = BUILDER
                .comment("等级 5 的输出罐容量 (单位:mB，默认:100000)")
                .defineInRange("outputTankCapacity", 100000, 1000, Integer.MAX_VALUE);
        CHEMICAL_TIER_5_OUTPUT_AMOUNT = BUILDER
                .comment("等级 5 每次操作产生的化学品的量（单位：mB，默认：1000）")
                .defineInRange("outputAmount", 1000, 1, Integer.MAX_VALUE);
        CHEMICAL_TIER_5_OUTPUT_TIME = BUILDER
                .comment("等级 5 每次操作所需的 tick（默认：1）")
                .defineInRange("outputTime", 1, 1, Integer.MAX_VALUE);
        CHEMICAL_TIER_5_ENERGY_CAPACITY = BUILDER
                .comment("等级 5 的最大能量存储 (单位:FE，默认:1000000)")
                .defineInRange("energyCapacity", 1000000, 1000, Integer.MAX_VALUE);
        CHEMICAL_TIER_5_ENERGY_CONSUMPTION = BUILDER
                .comment("等级 5 每 1000mB 化学品消耗的能量 (单位:FE，默认:10000)")
                .defineInRange("energyConsumption", 10000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.pop();
        BUILDER.pop();

        CONFIG_SPEC = BUILDER.build();
    }

    // ======================= 物品复制机配置获取方法 =======================
    public static boolean isItemReplicatorDestroyEnabled() {return ITEM_REPLICATOR_ENABLE_DESTROY.get();}
    // 物品复制机的自动输出
    public static boolean isItemReplicatorAutoOutputEnabled() {return ITEM_REPLICATOR_AUTO_OUTPUT.get();}

    // 物品复制机的黑白名单配置
    public static boolean isBlacklistMode() {
        return BLACKLIST_MODE.get();
    }

    // 物品复制机的黑名单列表
    public static List<String> getBlacklistItems() {
        return BLACKLIST_ITEMS.get().stream()
                .map(obj -> (String) obj)
                .toList();
    }

    // 物品复制机的白名单列表
    public static List<String> getWhitelistItems() {
        return WHITELIST_ITEMS.get().stream()
                .map(obj -> (String) obj)
                .toList();
    }

    // Tier 1
    public static int getItemTier1OutputSlots() {return ITEM_TIER1_OUTPUT_SLOTS.get();}
    public static int getItemTier1OutputAmount() {return ITEM_TIER1_OUTPUT_AMOUNT.get();}
    public static int getItemTier1OutputTime() {return ITEM_TIER1_OUTPUT_TIME.get();}
    public static int getItemTier1EnergyCapacity() {return ITEM_TIER1_ENERGY_CAPACITY.get();}
    public static int getItemTier1EnergyConsumption() {return ITEM_TIER1_ENERGY_CONSUMPTION.get();}

    // Tier 2
    public static int getItemTier2OutputSlots() {return ITEM_TIER2_OUTPUT_SLOTS.get();}
    public static int getItemTier2OutputAmount() {return ITEM_TIER2_OUTPUT_AMOUNT.get();}
    public static int getItemTier2OutputTime() {return ITEM_TIER2_OUTPUT_TIME.get();}
    public static int getItemTier2EnergyCapacity() {return ITEM_TIER2_ENERGY_CAPACITY.get();}
    public static int getItemTier2EnergyConsumption() {return ITEM_TIER2_ENERGY_CONSUMPTION.get();}

    // Tier 3
    public static int getItemTier3OutputSlots() {return ITEM_TIER3_OUTPUT_SLOTS.get();}
    public static int getItemTier3OutputAmount() {return ITEM_TIER3_OUTPUT_AMOUNT.get();}
    public static int getItemTier3OutputTime() {return ITEM_TIER3_OUTPUT_TIME.get();}
    public static int getItemTier3EnergyCapacity() {return ITEM_TIER3_ENERGY_CAPACITY.get();}
    public static int getItemTier3EnergyConsumption() {return ITEM_TIER3_ENERGY_CONSUMPTION.get();}

    // Tier 4
    public static int getItemTier4OutputSlots() {return ITEM_TIER4_OUTPUT_SLOTS.get();}
    public static int getItemTier4OutputAmount() {return ITEM_TIER4_OUTPUT_AMOUNT.get();}

    public static int getItemTier4OutputTime() {return ITEM_TIER4_OUTPUT_TIME.get();}
    public static int getItemTier4EnergyCapacity() {return ITEM_TIER4_ENERGY_CAPACITY.get();}
    public static int getItemTier4EnergyConsumption() {return ITEM_TIER4_ENERGY_CONSUMPTION.get();}

    // Tier 5
    public static int getItemTier5OutputSlots() {return ITEM_TIER5_OUTPUT_SLOTS.get();}
    public static int getItemTier5OutputAmount() {return ITEM_TIER5_OUTPUT_AMOUNT.get();}
    public static int getItemTier5OutputTime() {return ITEM_TIER5_OUTPUT_TIME.get();}
    public static int getItemTier5EnergyCapacity() {return ITEM_TIER5_ENERGY_CAPACITY.get();}
    public static int getItemTier5EnergyConsumption() {return ITEM_TIER5_ENERGY_CONSUMPTION.get();}


    // ======================= 流体复制机配置获取方法 =======================
    public static boolean isFluidReplicatorDestroyEnabled() {return FLUID_REPLICATOR_ENABLE_DESTROY.get();}

    // 流体复制机的自动输出
    public static boolean isFluidReplicatorAutoOutputEnabled() {return FLUID_REPLICATOR_AUTO_OUTPUT.get();}

    // 流体复制机黑白名单配置
    public static boolean isFluidBlacklistMode() {
        return FLUID_BLACKLIST_MODE.get();
    }

    // 流体复制机黑名单列表
    public static List<String> getFluidBlacklistItems() {
        return FLUID_BLACKLIST_ITEMS.get().stream()
                .map(obj -> (String) obj)
                .toList();
    }

    // 流体复制机白名单列表
    public static List<String> getFluidWhitelistItems() {
        return FLUID_WHITELIST_ITEMS.get().stream()
                .map(obj -> (String) obj)
                .toList();
    }

    // Tier 1 Fluid
    public static int getFluidTier1OutputTankCapacity() {return FLUID_TIER1_OUTPUT_TANK_CAPACITY.get();}
    public static int getFluidTier1OutputAmount() {return FLUID_TIER1_OUTPUT_AMOUNT.get();}
    public static int getFluidTier1OutputTime() {return FLUID_TIER1_OUTPUT_TIME.get();}
    public static int getFluidTier1WaterAmount() {return FLUID_TIER1_WATER_AMOUNT.get();}
    public static int getFluidTier1LavaAmount() {return FLUID_TIER1_LAVA_AMOUNT.get();}
    public static int getFluidTier1EnergyCapacity() {return FLUID_TIER1_ENERGY_CAPACITY.get();}
    public static int getFluidTier1EnergyConsumption() {return FLUID_TIER1_ENERGY_CONSUMPTION.get();}

    // Tier 2 Fluid
    public static int getFluidTier2OutputTankCapacity() {return FLUID_TIER2_OUTPUT_TANK_CAPACITY.get();}
    public static int getFluidTier2OutputAmount() {return FLUID_TIER2_OUTPUT_AMOUNT.get();}
    public static int getFluidTier2OutputTime() {return FLUID_TIER2_OUTPUT_TIME.get();}
    public static int getFluidTier2WaterAmount() {return FLUID_TIER2_WATER_AMOUNT.get();}
    public static int getFluidTier2LavaAmount() {return FLUID_TIER2_LAVA_AMOUNT.get();}
    public static int getFluidTier2EnergyCapacity() {return FLUID_TIER2_ENERGY_CAPACITY.get();}
    public static int getFluidTier2EnergyConsumption() {return FLUID_TIER2_ENERGY_CONSUMPTION.get();}

    // Tier 3 Fluid
    public static int getFluidTier3OutputTankCapacity() {return FLUID_TIER3_OUTPUT_TANK_CAPACITY.get();}
    public static int getFluidTier3OutputAmount() {return FLUID_TIER3_OUTPUT_AMOUNT.get();}
    public static int getFluidTier3OutputTime() {return FLUID_TIER3_OUTPUT_TIME.get();}
    public static int getFluidTier3WaterAmount() {return FLUID_TIER3_WATER_AMOUNT.get();}
    public static int getFluidTier3LavaAmount() {return FLUID_TIER3_LAVA_AMOUNT.get();}
    public static int getFluidTier3EnergyCapacity() {return FLUID_TIER3_ENERGY_CAPACITY.get();}
    public static int getFluidTier3EnergyConsumption() {return FLUID_TIER3_ENERGY_CONSUMPTION.get();}

    // Tier 4 Fluid
    public static int getFluidTier4OutputTankCapacity() {return FLUID_TIER4_OUTPUT_TANK_CAPACITY.get();}
    public static int getFluidTier4OutputAmount() {return FLUID_TIER4_OUTPUT_AMOUNT.get();}
    public static int getFluidTier4OutputTime() {return FLUID_TIER4_OUTPUT_TIME.get();}
    public static int getFluidTier4WaterAmount() {return FLUID_TIER4_WATER_AMOUNT.get();}
    public static int getFluidTier4LavaAmount() {return FLUID_TIER4_LAVA_AMOUNT.get();}
    public static int getFluidTier4EnergyCapacity() {return FLUID_TIER4_ENERGY_CAPACITY.get();}
    public static int getFluidTier4EnergyConsumption() {return FLUID_TIER4_ENERGY_CONSUMPTION.get();}

    // Tier 5 Fluid
    public static int getFluidTier5OutputTankCapacity() {return FLUID_TIER5_OUTPUT_TANK_CAPACITY.get();}
    public static int getFluidTier5OutputAmount() {return FLUID_TIER5_OUTPUT_AMOUNT.get();}
    public static int getFluidTier5OutputTime() {return FLUID_TIER5_OUTPUT_TIME.get();}
    public static int getFluidTier5WaterAmount() {return FLUID_TIER5_WATER_AMOUNT.get();}
    public static int getFluidTier5LavaAmount() {return FLUID_TIER5_LAVA_AMOUNT.get();}
    public static int getFluidTier5EnergyCapacity() {return FLUID_TIER5_ENERGY_CAPACITY.get();}
    public static int getFluidTier5EnergyConsumption() {return FLUID_TIER5_ENERGY_CONSUMPTION.get();}


    // ======================= 化学品复制机配置获取方法（Mekanism 联动） =======================
    public static boolean isChemicalReplicatorDestroyEnabled() {
        return CHEMICAL_REPLICATOR_ENABLE_DESTROY.get();
    }

    // 化学品复制机黑白名单配置
    public static boolean isChemicalBlacklistMode() {return CHEMICAL_BLACKLIST_MODE.get();}

    // 化学品复制机黑名单列表
    public static List<String> getChemicalBlacklistItems() {
        return CHEMICAL_BLACKLIST_ITEMS.get().stream()
                .map(obj -> (String) obj)
                .toList();
    }

    // 化学品复制机白名单列表
    public static List<String> getChemicalWhitelistItems() {
        return CHEMICAL_WHITELIST_ITEMS.get().stream()
                .map(obj -> (String) obj)
                .toList();
    }

    // Tier 1 Chemical
    public static int getChemicalTier1OutputTankCapacity() {return CHEMICAL_TIER_1_OUTPUT_TANK_CAPACITY.get();}
    public static int getChemicalTier1OutputAmount() {return CHEMICAL_TIER_1_OUTPUT_AMOUNT.get();}
    public static int getChemicalTier1OutputTime() {return CHEMICAL_TIER_1_OUTPUT_TIME.get();}
    public static int getChemicalTier1EnergyCapacity() {return CHEMICAL_TIER_1_ENERGY_CAPACITY.get();}
    public static int getChemicalTier1EnergyConsumption() {return CHEMICAL_TIER_1_ENERGY_CONSUMPTION.get();}

    // Tier 2 Chemical
    public static int getChemicalTier2OutputTankCapacity() {return CHEMICAL_TIER_2_OUTPUT_TANK_CAPACITY.get();}
    public static int getChemicalTier2OutputAmount() {return CHEMICAL_TIER_2_OUTPUT_AMOUNT.get();}
    public static int getChemicalTier2OutputTime() {return CHEMICAL_TIER_2_OUTPUT_TIME.get();}
    public static int getChemicalTier2EnergyCapacity() {return CHEMICAL_TIER_2_ENERGY_CAPACITY.get();}
    public static int getChemicalTier2EnergyConsumption() {return CHEMICAL_TIER_2_ENERGY_CONSUMPTION.get();}

    // Tier 3 Chemical
    public static int getChemicalTier3OutputTankCapacity() {return CHEMICAL_TIER_3_OUTPUT_TANK_CAPACITY.get();}
    public static int getChemicalTier3OutputAmount() {return CHEMICAL_TIER_3_OUTPUT_AMOUNT.get();}
    public static int getChemicalTier3OutputTime() {return CHEMICAL_TIER_3_OUTPUT_TIME.get();}
    public static int getChemicalTier3EnergyCapacity() {return CHEMICAL_TIER_3_ENERGY_CAPACITY.get();}
    public static int getChemicalTier3EnergyConsumption() {return CHEMICAL_TIER_3_ENERGY_CONSUMPTION.get();}

    // Tier 4 Chemical
    public static int getChemicalTier4OutputTankCapacity() {return CHEMICAL_TIER_4_OUTPUT_TANK_CAPACITY.get();}
    public static int getChemicalTier4OutputAmount() {return CHEMICAL_TIER_4_OUTPUT_AMOUNT.get();}
    public static int getChemicalTier4OutputTime() {return CHEMICAL_TIER_4_OUTPUT_TIME.get();}
    public static int getChemicalTier4EnergyCapacity() {return CHEMICAL_TIER_4_ENERGY_CAPACITY.get();}
    public static int getChemicalTier4EnergyConsumption() {return CHEMICAL_TIER_4_ENERGY_CONSUMPTION.get();}

    // Tier 5 Chemical
    public static int getChemicalTier5OutputTankCapacity() {return CHEMICAL_TIER_5_OUTPUT_TANK_CAPACITY.get();}
    public static int getChemicalTier5OutputAmount() {return CHEMICAL_TIER_5_OUTPUT_AMOUNT.get();}
    public static int getChemicalTier5OutputTime() {return CHEMICAL_TIER_5_OUTPUT_TIME.get();}
    public static int getChemicalTier5EnergyCapacity() {return CHEMICAL_TIER_5_ENERGY_CAPACITY.get();}
    public static int getChemicalTier5EnergyConsumption() {return CHEMICAL_TIER_5_ENERGY_CONSUMPTION.get();}
}
