package com.chinaex123.resource_replicator.config;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class ServerConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec CONFIG_SPEC;

    // ======================= 物品复制机配置 =======================
    private static final ModConfigSpec.BooleanValue ITEM_REPLICATOR_ENABLE_DESTROY;
    private static final ModConfigSpec.BooleanValue ITEM_REPLICATOR_AUTO_OUTPUT;
    private static final ModConfigSpec.EnumValue<Direction> ITEM_REPLICATOR_AUTO_OUTPUT_DIRECTION;

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
    private static final ModConfigSpec.EnumValue<Direction> FLUID_REPLICATOR_AUTO_OUTPUT_DIRECTION;

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

        CONFIG_SPEC = BUILDER.build();
    }

    // ======================= 物品复制机配置获取方法 =======================
    public static boolean isItemReplicatorDestroyEnabled() {return ITEM_REPLICATOR_ENABLE_DESTROY.get();}
    // 物品复制机的自动输出
    public static boolean isItemReplicatorAutoOutputEnabled() {return ITEM_REPLICATOR_AUTO_OUTPUT.get();}

    // 物品复制机的自动输出方向
    public static Direction getItemReplicatorAutoOutputDirection() {return ITEM_REPLICATOR_AUTO_OUTPUT_DIRECTION.get();}

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

    // 流体复制机的自动输出方向
    public static Direction getFluidReplicatorAutoOutputDirection() {return FLUID_REPLICATOR_AUTO_OUTPUT_DIRECTION.get();}

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
}
