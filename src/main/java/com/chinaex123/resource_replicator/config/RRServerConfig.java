package com.chinaex123.resource_replicator.config;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class RRServerConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec CONFIG_SPEC;

    // ======================= 物品复制机配置 =======================
    public static final ModConfigSpec.BooleanValue ITEM_REPLICATOR_ENABLE_DESTROY;
    public static final ModConfigSpec.BooleanValue ITEM_REPLICATOR_AUTO_OUTPUT;
    public static final ModConfigSpec.EnumValue<Direction> ITEM_REPLICATOR_AUTO_OUTPUT_DIRECTION;

    // 黑白名单配置
    public static final ModConfigSpec.BooleanValue BLACKLIST_MODE;
    public static final ModConfigSpec.ConfigValue<List<?>> BLACKLIST_ITEMS;
    public static final ModConfigSpec.ConfigValue<List<?>> WHITELIST_ITEMS;

    // T1-T5 物品复制机配置 - 输出槽、输出数量、输出时间
    public static final ModConfigSpec.IntValue ITEM_TIER1_OUTPUT_SLOTS;
    public static final ModConfigSpec.IntValue ITEM_TIER1_OUTPUT_AMOUNT;
    public static final ModConfigSpec.IntValue ITEM_TIER1_OUTPUT_TIME;
    public static final ModConfigSpec.IntValue ITEM_TIER2_OUTPUT_SLOTS;
    public static final ModConfigSpec.IntValue ITEM_TIER2_OUTPUT_AMOUNT;
    public static final ModConfigSpec.IntValue ITEM_TIER2_OUTPUT_TIME;
    public static final ModConfigSpec.IntValue ITEM_TIER3_OUTPUT_SLOTS;
    public static final ModConfigSpec.IntValue ITEM_TIER3_OUTPUT_AMOUNT;
    public static final ModConfigSpec.IntValue ITEM_TIER3_OUTPUT_TIME;
    public static final ModConfigSpec.IntValue ITEM_TIER4_OUTPUT_SLOTS;
    public static final ModConfigSpec.IntValue ITEM_TIER4_OUTPUT_AMOUNT;
    public static final ModConfigSpec.IntValue ITEM_TIER4_OUTPUT_TIME;
    public static final ModConfigSpec.IntValue ITEM_TIER5_OUTPUT_SLOTS;
    public static final ModConfigSpec.IntValue ITEM_TIER5_OUTPUT_AMOUNT;
    public static final ModConfigSpec.IntValue ITEM_TIER5_OUTPUT_TIME;

    // T1-T5 物品复制机能量配置
    public static final ModConfigSpec.IntValue ITEM_TIER1_ENERGY_CAPACITY;
    public static final ModConfigSpec.IntValue ITEM_TIER1_ENERGY_CONSUMPTION;
    public static final ModConfigSpec.IntValue ITEM_TIER2_ENERGY_CAPACITY;
    public static final ModConfigSpec.IntValue ITEM_TIER2_ENERGY_CONSUMPTION;
    public static final ModConfigSpec.IntValue ITEM_TIER3_ENERGY_CAPACITY;
    public static final ModConfigSpec.IntValue ITEM_TIER3_ENERGY_CONSUMPTION;
    public static final ModConfigSpec.IntValue ITEM_TIER4_ENERGY_CAPACITY;
    public static final ModConfigSpec.IntValue ITEM_TIER4_ENERGY_CONSUMPTION;
    public static final ModConfigSpec.IntValue ITEM_TIER5_ENERGY_CAPACITY;
    public static final ModConfigSpec.IntValue ITEM_TIER5_ENERGY_CONSUMPTION;


    // ======================= 流体复制机配置 =======================
    public static final ModConfigSpec.BooleanValue FLUID_REPLICATOR_ENABLE_DESTROY;
    public static final ModConfigSpec.BooleanValue FLUID_REPLICATOR_AUTO_OUTPUT;
    public static final ModConfigSpec.EnumValue<Direction> FLUID_REPLICATOR_AUTO_OUTPUT_DIRECTION;

    // 流体黑白名单配置
    public static final ModConfigSpec.BooleanValue FLUID_BLACKLIST_MODE;
    public static final ModConfigSpec.ConfigValue<List<?>> FLUID_BLACKLIST_ITEMS;
    public static final ModConfigSpec.ConfigValue<List<?>> FLUID_WHITELIST_ITEMS;

    // T1-T5 流体复制机配置 - 输出槽、输出数量、输出时间
    public static final ModConfigSpec.IntValue FLUID_TIER1_OUTPUT_TANK_CAPACITY;
    public static final ModConfigSpec.IntValue FLUID_TIER1_OUTPUT_AMOUNT;
    public static final ModConfigSpec.IntValue FLUID_TIER1_WATER_AMOUNT;
    public static final ModConfigSpec.IntValue FLUID_TIER1_LAVA_AMOUNT;
    public static final ModConfigSpec.IntValue FLUID_TIER1_OUTPUT_TIME;
    public static final ModConfigSpec.IntValue FLUID_TIER2_OUTPUT_TANK_CAPACITY;
    public static final ModConfigSpec.IntValue FLUID_TIER2_OUTPUT_AMOUNT;
    public static final ModConfigSpec.IntValue FLUID_TIER2_WATER_AMOUNT;
    public static final ModConfigSpec.IntValue FLUID_TIER2_LAVA_AMOUNT;
    public static final ModConfigSpec.IntValue FLUID_TIER2_OUTPUT_TIME;
    public static final ModConfigSpec.IntValue FLUID_TIER3_OUTPUT_TANK_CAPACITY;
    public static final ModConfigSpec.IntValue FLUID_TIER3_OUTPUT_AMOUNT;
    public static final ModConfigSpec.IntValue FLUID_TIER3_WATER_AMOUNT;
    public static final ModConfigSpec.IntValue FLUID_TIER3_LAVA_AMOUNT;
    public static final ModConfigSpec.IntValue FLUID_TIER3_OUTPUT_TIME;
    public static final ModConfigSpec.IntValue FLUID_TIER4_OUTPUT_TANK_CAPACITY;
    public static final ModConfigSpec.IntValue FLUID_TIER4_OUTPUT_AMOUNT;
    public static final ModConfigSpec.IntValue FLUID_TIER4_WATER_AMOUNT;
    public static final ModConfigSpec.IntValue FLUID_TIER4_LAVA_AMOUNT;
    public static final ModConfigSpec.IntValue FLUID_TIER4_OUTPUT_TIME;
    public static final ModConfigSpec.IntValue FLUID_TIER5_OUTPUT_TANK_CAPACITY;
    public static final ModConfigSpec.IntValue FLUID_TIER5_OUTPUT_AMOUNT;
    public static final ModConfigSpec.IntValue FLUID_TIER5_WATER_AMOUNT;
    public static final ModConfigSpec.IntValue FLUID_TIER5_LAVA_AMOUNT;
    public static final ModConfigSpec.IntValue FLUID_TIER5_OUTPUT_TIME;

    // T1-T5 流体复制机能量配置
    public static final ModConfigSpec.IntValue FLUID_TIER1_ENERGY_CAPACITY;
    public static final ModConfigSpec.IntValue FLUID_TIER1_ENERGY_CONSUMPTION;
    public static final ModConfigSpec.IntValue FLUID_TIER2_ENERGY_CAPACITY;
    public static final ModConfigSpec.IntValue FLUID_TIER2_ENERGY_CONSUMPTION;
    public static final ModConfigSpec.IntValue FLUID_TIER3_ENERGY_CAPACITY;
    public static final ModConfigSpec.IntValue FLUID_TIER3_ENERGY_CONSUMPTION;
    public static final ModConfigSpec.IntValue FLUID_TIER4_ENERGY_CAPACITY;
    public static final ModConfigSpec.IntValue FLUID_TIER4_ENERGY_CONSUMPTION;
    public static final ModConfigSpec.IntValue FLUID_TIER5_ENERGY_CAPACITY;
    public static final ModConfigSpec.IntValue FLUID_TIER5_ENERGY_CONSUMPTION;


    // ==================== 化学品复制机参数（Mekanism 联动） ====================
    public static final ModConfigSpec.BooleanValue CHEMICAL_REPLICATOR_ENABLE_DESTROY;
    public static final ModConfigSpec.BooleanValue CHEMICAL_REPLICATOR_AUTO_OUTPUT;
    public static final ModConfigSpec.EnumValue<Direction> CHEMICAL_REPLICATOR_AUTO_OUTPUT_DIRECTION;
    public static final ModConfigSpec.BooleanValue CHEMICAL_BLACKLIST_MODE;
    public static final ModConfigSpec.ConfigValue<List<?>> CHEMICAL_BLACKLIST_ITEMS;
    public static final ModConfigSpec.ConfigValue<List<?>> CHEMICAL_WHITELIST_ITEMS;

    // T1-T5 化学品复制机配置 - 输出罐容量、输出数量、输出时间
    public static final ModConfigSpec.IntValue CHEMICAL_TIER_1_OUTPUT_TANK_CAPACITY;
    public static final ModConfigSpec.IntValue CHEMICAL_TIER_1_OUTPUT_AMOUNT;
    public static final ModConfigSpec.IntValue CHEMICAL_TIER_1_OUTPUT_TIME;
    public static final ModConfigSpec.IntValue CHEMICAL_TIER_2_OUTPUT_TANK_CAPACITY;
    public static final ModConfigSpec.IntValue CHEMICAL_TIER_2_OUTPUT_AMOUNT;
    public static final ModConfigSpec.IntValue CHEMICAL_TIER_2_OUTPUT_TIME;
    public static final ModConfigSpec.IntValue CHEMICAL_TIER_3_OUTPUT_TANK_CAPACITY;
    public static final ModConfigSpec.IntValue CHEMICAL_TIER_3_OUTPUT_AMOUNT;
    public static final ModConfigSpec.IntValue CHEMICAL_TIER_3_OUTPUT_TIME;
    public static final ModConfigSpec.IntValue CHEMICAL_TIER_4_OUTPUT_TANK_CAPACITY;
    public static final ModConfigSpec.IntValue CHEMICAL_TIER_4_OUTPUT_AMOUNT;
    public static final ModConfigSpec.IntValue CHEMICAL_TIER_4_OUTPUT_TIME;
    public static final ModConfigSpec.IntValue CHEMICAL_TIER_5_OUTPUT_TANK_CAPACITY;
    public static final ModConfigSpec.IntValue CHEMICAL_TIER_5_OUTPUT_AMOUNT;
    public static final ModConfigSpec.IntValue CHEMICAL_TIER_5_OUTPUT_TIME;

    // T1-T5 化学品复制机能量配置
    public static final ModConfigSpec.IntValue CHEMICAL_TIER_1_ENERGY_CAPACITY;
    public static final ModConfigSpec.IntValue CHEMICAL_TIER_1_ENERGY_CONSUMPTION;
    public static final ModConfigSpec.IntValue CHEMICAL_TIER_2_ENERGY_CAPACITY;
    public static final ModConfigSpec.IntValue CHEMICAL_TIER_2_ENERGY_CONSUMPTION;
    public static final ModConfigSpec.IntValue CHEMICAL_TIER_3_ENERGY_CAPACITY;
    public static final ModConfigSpec.IntValue CHEMICAL_TIER_3_ENERGY_CONSUMPTION;
    public static final ModConfigSpec.IntValue CHEMICAL_TIER_4_ENERGY_CAPACITY;
    public static final ModConfigSpec.IntValue CHEMICAL_TIER_4_ENERGY_CONSUMPTION;
    public static final ModConfigSpec.IntValue CHEMICAL_TIER_5_ENERGY_CAPACITY;
    public static final ModConfigSpec.IntValue CHEMICAL_TIER_5_ENERGY_CONSUMPTION;

    static {
        BUILDER.comment("物品复制机").push("Item Replicator");
        ITEM_REPLICATOR_ENABLE_DESTROY = BUILDER
                .comment("是否启用销毁功能。启用后，通过管道输入到输入槽的物品会被销毁。")
                .comment("Whether to enable the destruction function. When enabled, items piped into the input slot will be destroyed.")
                .define("enableDestroy", false);
        ITEM_REPLICATOR_AUTO_OUTPUT = BUILDER
                .comment("是否启用自动输出功能")
                .comment("Whether to enable the automatic output function.")
                .define("autoOutput", true);
        ITEM_REPLICATOR_AUTO_OUTPUT_DIRECTION = BUILDER
                .comment("自动输出的方向。可选值：UP, DOWN, NORTH, SOUTH, EAST, WEST")
                .comment("The automatic output direction. Available values: UP, DOWN, NORTH, SOUTH, EAST, WEST.")
                .defineEnum("autoOutputDirection", Direction.UP);

        BUILDER.comment("黑名单 & 白名单").push("Whitelist & Blacklist");
        BLACKLIST_MODE = BUILDER
                .comment("是否使用黑名单模式（true=黑名单，false=白名单）")
                .comment("Whether to use the blacklist mode.")
                .define("blacklistMode", true);
        BLACKLIST_ITEMS = BUILDER
                .comment("物品黑名单列表 (如: minecraft:diamond, @mekanism, #c:ingots/iron)")
                .comment("The list items in the item blacklist.")
                .defineList("blacklistItems", List.of(), obj -> obj instanceof String);
        WHITELIST_ITEMS = BUILDER
                .comment("物品白名单列表 (如: minecraft:diamond, @mekanism, #c:ingots/iron)")
                .comment("The list items in the item whitelist.")
                .defineList("whitelistItems", List.of(), obj -> obj instanceof String);
        BUILDER.pop();

        BUILDER.comment("等级1").push("Tier 1");
        ITEM_TIER1_OUTPUT_SLOTS = BUILDER
                .comment("输出槽数量")
                .comment("Number of output slots.")
                .defineInRange("outputSlots", 1, 1, 9);
        ITEM_TIER1_OUTPUT_AMOUNT = BUILDER
                .comment("每次操作产生的物品数量")
                .comment("Number of items produced per operation.")
                .defineInRange("outputAmount", 4, 1, Integer.MAX_VALUE);
        ITEM_TIER1_OUTPUT_TIME = BUILDER
                .comment("每次操作所需的 tick")
                .comment("Ticks required per operation")
                .defineInRange("outputTime", 20, 1, Integer.MAX_VALUE);
        ITEM_TIER1_ENERGY_CAPACITY = BUILDER
                .comment("最大能量存储")
                .comment("Maximum energy storage")
                .defineInRange("energyCapacity", 10000, 1000, Integer.MAX_VALUE);
        ITEM_TIER1_ENERGY_CONSUMPTION = BUILDER
                .comment("每次操作消耗的能量")
                .comment("Energy consumed per operation")
                .defineInRange("energyConsumption", 2000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("等级2").push("Tier 2");
        ITEM_TIER2_OUTPUT_SLOTS = BUILDER
                .comment("输出槽数量")
                .comment("Number of output slots.")
                .defineInRange("outputSlots", 2, 1, 9);
        ITEM_TIER2_OUTPUT_AMOUNT = BUILDER
                .comment("每次操作产生的物品数量")
                .comment("Number of items produced per operation.")
                .defineInRange("outputAmount", 16, 1, Integer.MAX_VALUE);
        ITEM_TIER2_OUTPUT_TIME = BUILDER
                .comment("每次操作所需的 tick")
                .comment("Ticks required per operation")
                .defineInRange("outputTime", 15, 1, Integer.MAX_VALUE);
        ITEM_TIER2_ENERGY_CAPACITY = BUILDER
                .comment("最大能量存储")
                .comment("Maximum energy storage")
                .defineInRange("energyCapacity", 50000, 1000, Integer.MAX_VALUE);
        ITEM_TIER2_ENERGY_CONSUMPTION = BUILDER
                .comment("每次操作消耗的能量")
                .comment("Energy consumed per operation")
                .defineInRange("energyConsumption", 4000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("等级3").push("Tier 3");
        ITEM_TIER3_OUTPUT_SLOTS = BUILDER
                .comment("输出槽数量")
                .comment("Number of output slots.")
                .defineInRange("outputSlots", 3, 1, 9);
        ITEM_TIER3_OUTPUT_AMOUNT = BUILDER
                .comment("每次操作产生的物品数量")
                .comment("Number of items produced per operation.")
                .defineInRange("outputAmount", 32, 1, Integer.MAX_VALUE);
        ITEM_TIER3_OUTPUT_TIME = BUILDER
                .comment("每次操作所需的 tick")
                .comment("Ticks required per operation")
                .defineInRange("outputTime", 10, 1, Integer.MAX_VALUE);
        ITEM_TIER3_ENERGY_CAPACITY = BUILDER
                .comment("最大能量存储")
                .comment("Maximum energy storage")
                .defineInRange("energyCapacity", 100000, 1000, Integer.MAX_VALUE);
        ITEM_TIER3_ENERGY_CONSUMPTION = BUILDER
                .comment("每次操作消耗的能量")
                .comment("Energy consumed per operation")
                .defineInRange("energyConsumption", 6000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("等级4").push("Tier 4");
        ITEM_TIER4_OUTPUT_SLOTS = BUILDER
                .comment("输出槽数量")
                .comment("Number of output slots.")
                .defineInRange("outputSlots", 5, 1, 9);
        ITEM_TIER4_OUTPUT_AMOUNT = BUILDER
                .comment("每次操作产生的物品数量")
                .comment("Number of items produced per operation.")
                .defineInRange("outputAmount", 64, 1, Integer.MAX_VALUE);
        ITEM_TIER4_OUTPUT_TIME = BUILDER
                .comment("每次操作所需的 tick")
                .comment("Ticks required per operation")
                .defineInRange("outputTime", 5, 1, Integer.MAX_VALUE);
        ITEM_TIER4_ENERGY_CAPACITY = BUILDER
                .comment("最大能量存储")
                .comment("Maximum energy storage")
                .defineInRange("energyCapacity", 500000, 1000, Integer.MAX_VALUE);
        ITEM_TIER4_ENERGY_CONSUMPTION = BUILDER
                .comment("每次操作消耗的能量")
                .comment("Energy consumed per operation")
                .defineInRange("energyConsumption", 8000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("等级5").push("Tier 5");
        ITEM_TIER5_OUTPUT_SLOTS = BUILDER
                .comment("输出槽数量")
                .comment("Number of output slots.")
                .defineInRange("outputSlots", 9, 1, 9);
        ITEM_TIER5_OUTPUT_AMOUNT = BUILDER
                .comment("每次操作产生的物品数量")
                .comment("Number of items produced per operation.")
                .defineInRange("outputAmount", 128, 1, Integer.MAX_VALUE);
        ITEM_TIER5_OUTPUT_TIME = BUILDER
                .comment("每次操作所需的 tick")
                .comment("Ticks required per operation")
                .defineInRange("outputTime", 1, 1, Integer.MAX_VALUE);
        ITEM_TIER5_ENERGY_CAPACITY = BUILDER
                .comment("最大能量存储")
                .comment("Maximum energy storage")
                .defineInRange("energyCapacity", 1000000, 1000, Integer.MAX_VALUE);
        ITEM_TIER5_ENERGY_CONSUMPTION = BUILDER
                .comment("每次操作消耗的能量")
                .comment("Energy consumed per operation")
                .defineInRange("energyConsumption", 10000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.pop();


        BUILDER.comment("流体复制机").push("Fluid Replicator");
        FLUID_REPLICATOR_ENABLE_DESTROY = BUILDER
                .comment("是否启用销毁功能。启用后，通过管道输入到输入槽的流体会被销毁。")
                .comment("Whether to enable the destruction function. When enabled, fluids piped into the input slot will be destroyed.")
                .define("enableDestroy", false);
        FLUID_REPLICATOR_AUTO_OUTPUT = BUILDER
                .comment("是否启用自动输出功能。")
                .comment("Whether to enable the automatic output function.")
                .define("autoOutput", true);
        FLUID_REPLICATOR_AUTO_OUTPUT_DIRECTION = BUILDER
                .comment("自动输出的方向。可选值：UP, DOWN, NORTH, SOUTH, EAST, WEST")
                .comment("The automatic output direction. Available values: UP, DOWN, NORTH, SOUTH, EAST, WEST.")
                .defineEnum("autoOutputDirection", Direction.UP);

        BUILDER.push("黑白名单模式").push("Whitelist & Blacklist");
        FLUID_BLACKLIST_MODE = BUILDER
                .comment("是否使用流体黑名单模式（true=黑名单，false=白名单）")
                .comment("Whether to use the fluid blacklist mode. true for blacklist, false for whitelist.")
                .define("fluidBlacklistMode", true);
        FLUID_BLACKLIST_ITEMS = BUILDER
                .comment("流体黑名单列表 (如: minecraft:lava, @create, #c:fuels)")
                .comment("The list items in the fluid blacklist.")
                .defineList("fluidBlacklistItems", List.of(), obj -> obj instanceof String);
        FLUID_WHITELIST_ITEMS = BUILDER
                .comment("流体白名单列表 (如: minecraft:lava, @create, #c:fuels)")
                .comment("The list items in the fluid whitelist.")
                .defineList("whitelistItems", List.of(), obj -> obj instanceof String);
        BUILDER.pop();

        BUILDER.comment("等级1").push("Tier 1");
        FLUID_TIER1_OUTPUT_TANK_CAPACITY = BUILDER
                .comment("输出罐容量")
                .comment("Output tank capacity")
                .defineInRange("outputTankCapacity", 8000, 1000, Integer.MAX_VALUE);
        FLUID_TIER1_OUTPUT_AMOUNT = BUILDER
                .comment("每次操作产生的流体数量")
                .comment("Number of fluids produced per operation")
                .defineInRange("outputAmount", 1000, 1, Integer.MAX_VALUE);
        FLUID_TIER1_OUTPUT_TIME = BUILDER
                .comment("每次操作所需的 tick")
                .comment("Ticks required per operation")
                .defineInRange("outputTime", 20, 1, Integer.MAX_VALUE);
        FLUID_TIER1_WATER_AMOUNT = BUILDER
                .comment("复制水时的特殊产量")
                .comment("Special water production amount")
                .defineInRange("waterAmount", 1000, 1, Integer.MAX_VALUE);
        FLUID_TIER1_LAVA_AMOUNT = BUILDER
                .comment("复制岩浆时的特殊产量")
                .comment("Special lava production amount")
                .defineInRange("lavaAmount", 10, 1, Integer.MAX_VALUE);
        FLUID_TIER1_ENERGY_CAPACITY = BUILDER
                .comment("最大能量存储")
                .comment("Maximum energy storage")
                .defineInRange("energyCapacity", 10000, 1000, Integer.MAX_VALUE);
        FLUID_TIER1_ENERGY_CONSUMPTION = BUILDER
                .comment("每 1000mB 流体消耗的能量")
                .comment("Energy consumed per operation per 1000mB of fluid")
                .defineInRange("energyConsumption", 2000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("等级2").push("Tier 2");
        FLUID_TIER2_OUTPUT_TANK_CAPACITY = BUILDER
                .comment("输出罐容量")
                .comment("Output tank capacity")
                .defineInRange("outputTankCapacity", 10000, 1000, Integer.MAX_VALUE);
        FLUID_TIER2_OUTPUT_AMOUNT = BUILDER
                .comment("每次操作产生的流体数量")
                .comment("Number of fluids produced per operation")
                .defineInRange("outputAmount", 2500, 1, Integer.MAX_VALUE);
        FLUID_TIER2_OUTPUT_TIME = BUILDER
                .comment("每次操作所需的 tick")
                .comment("Ticks required per operation")
                .defineInRange("outputTime", 15, 1, Integer.MAX_VALUE);
        FLUID_TIER2_WATER_AMOUNT = BUILDER
                .comment("复制水时的特殊产量")
                .comment("Special water production amount")
                .defineInRange("waterAmount", 10000, 1, Integer.MAX_VALUE);
        FLUID_TIER2_LAVA_AMOUNT = BUILDER
                .comment("复制岩浆时的特殊产量")
                .comment("Special lava production amount")
                .defineInRange("lavaAmount", 50, 1, Integer.MAX_VALUE);
        FLUID_TIER2_ENERGY_CAPACITY = BUILDER
                .comment("最大能量存储")
                .comment("Maximum energy storage")
                .defineInRange("energyCapacity", 50000, 1000, Integer.MAX_VALUE);
        FLUID_TIER2_ENERGY_CONSUMPTION = BUILDER
                .comment("每 1000mB 流体消耗的能量")
                .comment("Energy consumed per operation per 1000mB of fluid")
                .defineInRange("energyConsumption", 4000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("等级3").push("Tier 3");
        FLUID_TIER3_OUTPUT_TANK_CAPACITY = BUILDER
                .comment("输出罐容量")
                .comment("Output tank capacity")
                .defineInRange("outputTankCapacity", 30000, 1000, Integer.MAX_VALUE);
        FLUID_TIER3_OUTPUT_AMOUNT = BUILDER
                .comment("每次操作产生的流体数量")
                .comment("Number of fluids produced per operation")
                .defineInRange("outputAmount", 5000, 1, Integer.MAX_VALUE);
        FLUID_TIER3_OUTPUT_TIME = BUILDER
                .comment("每次操作所需的 tick")
                .comment("Ticks required per operation")
                .defineInRange("outputTime", 10, 1, Integer.MAX_VALUE);
        FLUID_TIER3_WATER_AMOUNT = BUILDER
                .comment("复制水时的特殊产量")
                .comment("Special water production amount")
                .defineInRange("waterAmount", 100000, 1, Integer.MAX_VALUE);
        FLUID_TIER3_LAVA_AMOUNT = BUILDER
                .comment("复制岩浆时的特殊产量")
                .comment("Special lava production amount")
                .defineInRange("lavaAmount", 100, 1, Integer.MAX_VALUE);
        FLUID_TIER3_ENERGY_CAPACITY = BUILDER
                .comment("最大能量存储")
                .comment("Maximum energy storage")
                .defineInRange("energyCapacity", 100000, 1000, Integer.MAX_VALUE);
        FLUID_TIER3_ENERGY_CONSUMPTION = BUILDER
                .comment("每 1000mB 流体消耗的能量")
                .comment("Energy consumed per operation per 1000mB of fluid")
                .defineInRange("energyConsumption", 6000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("等级4").push("Tier 4");
        FLUID_TIER4_OUTPUT_TANK_CAPACITY = BUILDER
                .comment("输出罐容量")
                .comment("Output tank capacity")
                .defineInRange("outputTankCapacity", 100000, 1000, Integer.MAX_VALUE);
        FLUID_TIER4_OUTPUT_AMOUNT = BUILDER
                .comment("每次操作产生的流体数量")
                .comment("Number of fluids produced per operation")
                .defineInRange("outputAmount", 10000, 1, Integer.MAX_VALUE);
        FLUID_TIER4_OUTPUT_TIME = BUILDER
                .comment("每次操作所需的 tick")
                .comment("Ticks required per operation")
                .defineInRange("outputTime", 5, 1, Integer.MAX_VALUE);
        FLUID_TIER4_WATER_AMOUNT = BUILDER
                .comment("复制水时的特殊产量")
                .comment("Special water production amount")
                .defineInRange("waterAmount", 1000000, 1, Integer.MAX_VALUE);
        FLUID_TIER4_LAVA_AMOUNT = BUILDER
                .comment("复制岩浆时的特殊产量")
                .comment("Special lava production amount")
                .defineInRange("lavaAmount", 500, 1, Integer.MAX_VALUE);
        FLUID_TIER4_ENERGY_CAPACITY = BUILDER
                .comment("最大能量存储")
                .comment("Maximum energy storage")
                .defineInRange("energyCapacity", 500000, 1000, Integer.MAX_VALUE);
        FLUID_TIER4_ENERGY_CONSUMPTION = BUILDER
                .comment("每 1000mB 流体消耗的能量")
                .comment("Energy consumed per operation per 1000mB of fluid")
                .defineInRange("energyConsumption", 8000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("等级5").push("Tier 5");
        FLUID_TIER5_OUTPUT_TANK_CAPACITY = BUILDER
                .comment("输出罐容量")
                .comment("Output tank capacity")
                .defineInRange("outputTankCapacity", 500000, 1000, Integer.MAX_VALUE);
        FLUID_TIER5_OUTPUT_AMOUNT = BUILDER
                .comment("每次操作产生的流体数量")
                .comment("Number of fluids produced per operation")
                .defineInRange("outputAmount", 25000, 1, Integer.MAX_VALUE);
        FLUID_TIER5_OUTPUT_TIME = BUILDER
                .comment("每次操作所需的 tick")
                .comment("Ticks required per operation")
                .defineInRange("outputTime", 1, 1, Integer.MAX_VALUE);
        FLUID_TIER5_WATER_AMOUNT = BUILDER
                .comment("复制水时的特殊产量")
                .comment("Special water production amount")
                .defineInRange("waterAmount", 10000000, 1, Integer.MAX_VALUE);
        FLUID_TIER5_LAVA_AMOUNT = BUILDER
                .comment("复制岩浆时的特殊产量")
                .comment("Special lava production amount")
                .defineInRange("lavaAmount", 1000, 1, Integer.MAX_VALUE);
        FLUID_TIER5_ENERGY_CAPACITY = BUILDER
                .comment("最大能量存储")
                .comment("Maximum energy storage")
                .defineInRange("energyCapacity", 1000000, 1000, Integer.MAX_VALUE);
        FLUID_TIER5_ENERGY_CONSUMPTION = BUILDER
                .comment("每 1000mB 流体消耗的能量")
                .comment("Energy consumed per operation per 1000mB of fluid")
                .defineInRange("energyConsumption", 10000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.pop();


        BUILDER.comment("化学品复制机设置（Mekanism 联动）").push("Chemical Replicator");
        CHEMICAL_REPLICATOR_ENABLE_DESTROY = BUILDER
                .comment("是否启用销毁功能。启用后，通过管道输入的化学品会被销毁。")
                .define("enableDestroy", false);
        CHEMICAL_REPLICATOR_AUTO_OUTPUT = BUILDER
                .comment("是否启用自动输出功能。")
                .define("autoOutput", true);
        CHEMICAL_REPLICATOR_AUTO_OUTPUT_DIRECTION = BUILDER
                .comment("自动输出的方向。可选值：UP, DOWN, NORTH, SOUTH, EAST, WEST")
                .defineEnum("autoOutputDirection", Direction.UP);

        BUILDER.push("黑白名单模式").push("Whitelist & Blacklist");
        CHEMICAL_BLACKLIST_MODE = BUILDER
                .comment("是否使用化学品黑名单模式（true=黑名单，false=白名单）")
                .comment("Whether to use the chemical blacklist mode. true for blacklist, false for whitelist.")
                .define("blacklistMode", true);
        CHEMICAL_BLACKLIST_ITEMS = BUILDER
                .comment("化学品黑名单列表 (如: mekanism:hydrogen, @mekanismgenerators, #mekanism:chemicals)")
                .comment("The list items in the chemical blacklist.")
                .defineList("blacklistItems", List.of(), obj -> obj instanceof String);
        CHEMICAL_WHITELIST_ITEMS = BUILDER
                .comment("化学品白名单列表 (如: mekanism:hydrogen, @mekanismgenerators, #mekanism:chemicals)")
                .comment("The list items in the chemical whitelist.")
                .defineList("whitelistItems", List.of(), obj -> obj instanceof String);
        BUILDER.pop();

        BUILDER.comment("等级1").push("Tier 1");
        CHEMICAL_TIER_1_OUTPUT_TANK_CAPACITY = BUILDER
                .comment("输出罐容量")
                .comment("Output tank capacity")
                .defineInRange("outputTankCapacity", 4000, 1000, Integer.MAX_VALUE);
        CHEMICAL_TIER_1_OUTPUT_AMOUNT = BUILDER
                .comment("每次操作产生的化学品的量")
                .comment("Number of chemicals produced per operation")
                .defineInRange("outputAmount", 10, 1, Integer.MAX_VALUE);
        CHEMICAL_TIER_1_OUTPUT_TIME = BUILDER
                .comment("每次操作所需的 tick")
                .comment("Ticks required per operation")
                .defineInRange("outputTime", 20, 1, Integer.MAX_VALUE);
        CHEMICAL_TIER_1_ENERGY_CAPACITY = BUILDER
                .comment("最大能量存储")
                .comment("Maximum energy storage")
                .defineInRange("energyCapacity", 10000, 1000, Integer.MAX_VALUE);
        CHEMICAL_TIER_1_ENERGY_CONSUMPTION = BUILDER
                .comment("每 1000mB 化学品消耗的能量")
                .comment("Energy consumed per operation per 1000mB of chemicals")
                .defineInRange("energyConsumption", 2000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("等级2").push("Tier 2");
        CHEMICAL_TIER_2_OUTPUT_TANK_CAPACITY = BUILDER
                .comment("输出罐容量")
                .comment("Output tank capacity")
                .defineInRange("outputTankCapacity", 10000, 1000, Integer.MAX_VALUE);
        CHEMICAL_TIER_2_OUTPUT_AMOUNT = BUILDER
                .comment("每次操作产生的化学品的量")
                .comment("Number of chemicals produced per operation")
                .defineInRange("outputAmount", 50, 1, Integer.MAX_VALUE);
        CHEMICAL_TIER_2_OUTPUT_TIME = BUILDER
                .comment("每次操作所需的 tick")
                .comment("Ticks required per operation")
                .defineInRange("outputTime", 15, 1, Integer.MAX_VALUE);
        CHEMICAL_TIER_2_ENERGY_CAPACITY = BUILDER
                .comment("最大能量存储")
                .comment("Maximum energy storage")
                .defineInRange("energyCapacity", 50000, 1000, Integer.MAX_VALUE);
        CHEMICAL_TIER_2_ENERGY_CONSUMPTION = BUILDER
                .comment("每 1000mB 化学品消耗的能量")
                .comment("Energy consumed per operation per 1000mB of chemicals")
                .defineInRange("energyConsumption", 4000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("等级3").push("Tier 3");
        CHEMICAL_TIER_3_OUTPUT_TANK_CAPACITY = BUILDER
                .comment("输出罐容量")
                .comment("Output tank capacity")
                .defineInRange("outputTankCapacity", 25000, 1000, Integer.MAX_VALUE);
        CHEMICAL_TIER_3_OUTPUT_AMOUNT = BUILDER
                .comment("每次操作产生的化学品的量")
                .comment("Number of chemicals produced per operation")
                .defineInRange("outputAmount", 100, 1, Integer.MAX_VALUE);
        CHEMICAL_TIER_3_OUTPUT_TIME = BUILDER
                .comment("每次操作所需的 tick")
                .comment("Ticks required per operation")
                .defineInRange("outputTime", 10, 1, Integer.MAX_VALUE);
        CHEMICAL_TIER_3_ENERGY_CAPACITY = BUILDER
                .comment("最大能量存储")
                .comment("Maximum energy storage")
                .defineInRange("energyCapacity", 100000, 1000, Integer.MAX_VALUE);
        CHEMICAL_TIER_3_ENERGY_CONSUMPTION = BUILDER
                .comment("每 1000mB 化学品消耗的能量")
                .comment("Energy consumed per operation per 1000mB of chemicals")
                .defineInRange("energyConsumption", 6000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("等级4").push("Tier 4");
        CHEMICAL_TIER_4_OUTPUT_TANK_CAPACITY = BUILDER
                .comment("输出罐容量")
                .comment("Output tank capacity")
                .defineInRange("outputTankCapacity", 50000, 1000, Integer.MAX_VALUE);
        CHEMICAL_TIER_4_OUTPUT_AMOUNT = BUILDER
                .comment("每次操作产生的化学品的量")
                .comment("Number of chemicals produced per operation")
                .defineInRange("outputAmount", 500, 1, Integer.MAX_VALUE);
        CHEMICAL_TIER_4_OUTPUT_TIME = BUILDER
                .comment("每次操作所需的 tick")
                .comment("Ticks required per operation")
                .defineInRange("outputTime", 5, 1, Integer.MAX_VALUE);
        CHEMICAL_TIER_4_ENERGY_CAPACITY = BUILDER
                .comment("最大能量存储")
                .comment("Maximum energy storage")
                .defineInRange("energyCapacity", 500000, 1000, Integer.MAX_VALUE);
        CHEMICAL_TIER_4_ENERGY_CONSUMPTION = BUILDER
                .comment("每 1000mB 化学品消耗的能量")
                .comment("Energy consumed per operation per 1000mB of chemicals")
                .defineInRange("energyConsumption", 8000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("等级5").push("Tier 5");
        CHEMICAL_TIER_5_OUTPUT_TANK_CAPACITY = BUILDER
                .comment("输出罐容量")
                .comment("Output tank capacity")
                .defineInRange("outputTankCapacity", 100000, 1000, Integer.MAX_VALUE);
        CHEMICAL_TIER_5_OUTPUT_AMOUNT = BUILDER
                .comment("每次操作产生的化学品的量")
                .comment("Number of chemicals produced per operation")
                .defineInRange("outputAmount", 1000, 1, Integer.MAX_VALUE);
        CHEMICAL_TIER_5_OUTPUT_TIME = BUILDER
                .comment("每次操作所需的 tick")
                .comment("Ticks required per operation")
                .defineInRange("outputTime", 1, 1, Integer.MAX_VALUE);
        CHEMICAL_TIER_5_ENERGY_CAPACITY = BUILDER
                .comment("最大能量存储")
                .comment("Maximum energy storage")
                .defineInRange("energyCapacity", 1000000, 1000, Integer.MAX_VALUE);
        CHEMICAL_TIER_5_ENERGY_CONSUMPTION = BUILDER
                .comment("每 1000mB 化学品消耗的能量")
                .comment("Energy consumed per operation per 1000mB of chemicals")
                .defineInRange("energyConsumption", 10000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.pop();

        CONFIG_SPEC = BUILDER.build();
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
}
