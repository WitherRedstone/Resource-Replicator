package com.chinaex123.resource_replicator.client.tooltip;

import com.chinaex123.resource_replicator.config.RRServerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

@EventBusSubscriber(modid = "resource_replicator")
public class ModTooltips {

    /**
     * 处理物品提示框生成事件
     * <p>
     * 此方法在玩家鼠标悬停在物品上时被调用，用于动态添加复制机类方块的详细信息到提示框中。
     * 根据物品名称判断其类型（物品复制机、流体复制机、化学品复制机）和等级（T1-T5），
     * 然后添加对应的速度、产量、容量等配置信息。
     * </p>
     * <p>
     * 支持三种复制机类型：
     * </p>
     * <ul>
     *     <li><strong>物品复制机</strong>：显示每次操作的输出数量和每刻的处理速度</li>
     *     <li><strong>流体复制机</strong>：显示普通流体、水和岩浆的输出量及处理速度</li>
     *     <li><strong>化学品复制机</strong>：显示化学品的输出量和处理速度（Mekanism 联动）</li>
     * </ul>
     * 
     * @param event 物品提示框事件，包含物品堆、提示框列表和提示标志等信息
     */
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        List<Component> toolTip = event.getToolTip();
        TooltipFlag flags = event.getFlags();

        // 检查是否是物品复制机
        String itemName = itemStack.getItem().getDescriptionId();
        if (itemName.contains("item_replicator")) {
            int tier = getTierFromName(itemName);
            if (tier > 0) {
                addReplicatorTooltip(toolTip, tier);
            }
        } else if (itemName.contains("fluid_replicator")) {
            // 检查是否是流体复制机
            int tier = getTierFromName(itemName);
            if (tier > 0) {
                addFluidReplicatorTooltip(toolTip, tier);
            }
        } else if (itemName.contains("chemical_replicator")) {
            // 检查是否是化学品复制机
            int tier = getTierFromName(itemName);
            if (tier > 0) {
                addChemicalReplicatorTooltip(toolTip, tier);
            }
        }
    }

    private static int getTierFromName(String itemName) {
        if (itemName.endsWith("tier1")) return 1;
        if (itemName.endsWith("tier2")) return 2;
        if (itemName.endsWith("tier3")) return 3;
        if (itemName.endsWith("tier4")) return 4;
        if (itemName.endsWith("tier5")) return 5;
        return 0;
    }

    private static void addReplicatorTooltip(List<Component> toolTip, int tier) {
        toolTip.add(Component.empty());

        // 获取配置的速度和输出量
        int outputAmount = getItemTierOutputAmount(tier);
        int outputTime = getItemTierOutputTime(tier);
        int energyConsumption = getItemTierEnergyConsumption(tier);
        int outputSlots = getItemTierOutputSlots(tier);
        double ticksPerSecond = 20.0 / outputTime;
        double itemsPerSecond = outputAmount * ticksPerSecond;

        // 显示每次操作的输出数量
        toolTip.add(Component.translatable("tooltip.item_replicator.output_amount",
                        Component.literal(String.valueOf(outputAmount)).withStyle(ChatFormatting.GREEN))
                .withStyle(ChatFormatting.GRAY));

        // 显示每 tick 的速度
        toolTip.add(Component.translatable("tooltip.item_replicator.speed",
                        Component.literal(String.valueOf(outputTime)).withStyle(ChatFormatting.AQUA))
                .withStyle(ChatFormatting.GRAY));

        // 显示每秒输出量
        toolTip.add(Component.translatable("tooltip.item_replicator.items_per_second",
                        Component.literal(String.format("%.1f", itemsPerSecond)).withStyle(ChatFormatting.YELLOW))
                .withStyle(ChatFormatting.GRAY));

        // 显示输出槽数量
        toolTip.add(Component.translatable("tooltip.item_replicator.output_slots",
                        Component.literal(String.valueOf(outputSlots)).withStyle(ChatFormatting.LIGHT_PURPLE))
                .withStyle(ChatFormatting.GRAY));

        // 显示能量消耗
        if (energyConsumption > 0) {
            toolTip.add(Component.translatable("tooltip.item_replicator.energy_consumption",
                            Component.literal(String.valueOf(energyConsumption)).withStyle(ChatFormatting.RED))
                    .withStyle(ChatFormatting.GRAY));
        }

        // 显示自动输出状态
        boolean autoOutputEnabled = RRServerConfig.ITEM_REPLICATOR_AUTO_OUTPUT.get();
        if (autoOutputEnabled) {
            String directionName = RRServerConfig.ITEM_REPLICATOR_AUTO_OUTPUT_DIRECTION.get().getName();
            toolTip.add(Component.translatable("tooltip.resource_replicator.auto_output_direction",
                            Component.literal(directionName).withStyle(ChatFormatting.AQUA))
                    .withStyle(ChatFormatting.GRAY));
        } else {
            toolTip.add(Component.translatable("tooltip.resource_replicator.auto_output_disabled")
                    .withStyle(ChatFormatting.RED));
        }
    }

    private static void addFluidReplicatorTooltip(List<Component> toolTip, int tier) {
        toolTip.add(Component.empty());

        // 获取配置的速度和输出量
        int outputAmount = getFluidTierOutputAmount(tier);
        int outputTime = getFluidTierOutputTime(tier);
        int energyConsumption = getFluidTierEnergyConsumption(tier);
        int tankCapacity = getFluidTierOutputTankCapacity(tier);
        double ticksPerSecond = 20.0 / outputTime;
        double mbPerSecond = outputAmount * ticksPerSecond;

        // 显示每次操作的输出数量（普通流体）
        toolTip.add(Component.translatable("tooltip.fluid_replicator.fluid_output_amount",
                        Component.literal(String.valueOf(outputAmount)).withStyle(ChatFormatting.GREEN))
                .withStyle(ChatFormatting.GRAY));

        // 显示水和岩浆的特殊输出量
        int waterOutput = getFluidTierWaterOutput(tier);
        int lavaOutput = getFluidTierLavaOutput(tier);

        // 总是显示水输出信息（无论是否与普通输出量相同）
        toolTip.add(Component.translatable("tooltip.fluid_replicator.water_output_amount",
                        Component.literal(String.valueOf(waterOutput)).withStyle(ChatFormatting.AQUA))
                .withStyle(ChatFormatting.GRAY));

        // 只在岩浆输出量不同时显示岩浆信息
        if (lavaOutput != outputAmount) {
            toolTip.add(Component.translatable("tooltip.fluid_replicator.lava_output_amount",
                            Component.literal(String.valueOf(lavaOutput)).withStyle(ChatFormatting.RED))
                    .withStyle(ChatFormatting.GRAY));
        }

        // 显示每 tick 的速度
        toolTip.add(Component.translatable("tooltip.fluid_replicator.speed",
                        Component.literal(String.valueOf(outputTime)).withStyle(ChatFormatting.AQUA))
                .withStyle(ChatFormatting.GRAY));

        // 显示每秒输出量
        toolTip.add(Component.translatable("tooltip.fluid_replicator.mb_per_second",
                        Component.literal(String.format("%.1f", mbPerSecond)).withStyle(ChatFormatting.YELLOW))
                .withStyle(ChatFormatting.GRAY));

        // 显示输出罐容量
        toolTip.add(Component.translatable("tooltip.fluid_replicator.tank_capacity",
                        Component.literal(String.valueOf(tankCapacity)).withStyle(ChatFormatting.LIGHT_PURPLE))
                .withStyle(ChatFormatting.GRAY));

        // 显示能量消耗
        if (energyConsumption > 0) {
            toolTip.add(Component.translatable("tooltip.fluid_replicator.energy_consumption",
                            Component.literal(String.valueOf(energyConsumption)).withStyle(ChatFormatting.RED))
                    .withStyle(ChatFormatting.GRAY));
        }

        // 显示自动输出状态
        boolean autoOutputEnabled = RRServerConfig.FLUID_REPLICATOR_AUTO_OUTPUT.get();
        if (autoOutputEnabled) {
            String directionName = RRServerConfig.FLUID_REPLICATOR_AUTO_OUTPUT_DIRECTION.get().getName();
            toolTip.add(Component.translatable("tooltip.resource_replicator.auto_output_direction",
                            Component.literal(directionName).withStyle(ChatFormatting.AQUA))
                    .withStyle(ChatFormatting.GRAY));
        } else {
            toolTip.add(Component.translatable("tooltip.resource_replicator.auto_output_disabled")
                    .withStyle(ChatFormatting.RED));
        }
    }

    private static int getItemTierOutputAmount(int tier) {
        return switch (tier) {
            case 1 -> RRServerConfig.ITEM_TIER1_OUTPUT_AMOUNT.get();
            case 2 -> RRServerConfig.ITEM_TIER2_OUTPUT_AMOUNT.get();
            case 3 -> RRServerConfig.ITEM_TIER3_OUTPUT_AMOUNT.get();
            case 4 -> RRServerConfig.ITEM_TIER4_OUTPUT_AMOUNT.get();
            case 5 -> RRServerConfig.ITEM_TIER5_OUTPUT_AMOUNT.get();
            default -> 1;
        };
    }

    private static int getItemTierOutputSlots(int tier) {
        return switch (tier) {
            case 1 -> RRServerConfig.ITEM_TIER1_OUTPUT_SLOTS.get();
            case 2 -> RRServerConfig.ITEM_TIER2_OUTPUT_SLOTS.get();
            case 3 -> RRServerConfig.ITEM_TIER3_OUTPUT_SLOTS.get();
            case 4 -> RRServerConfig.ITEM_TIER4_OUTPUT_SLOTS.get();
            case 5 -> RRServerConfig.ITEM_TIER5_OUTPUT_SLOTS.get();
            default -> 1;
        };
    }

    private static int getItemTierOutputTime(int tier) {
        return switch (tier) {
            case 1 -> RRServerConfig.ITEM_TIER1_OUTPUT_TIME.get();
            case 2 -> RRServerConfig.ITEM_TIER2_OUTPUT_TIME.get();
            case 3 -> RRServerConfig.ITEM_TIER3_OUTPUT_TIME.get();
            case 4 -> RRServerConfig.ITEM_TIER4_OUTPUT_TIME.get();
            case 5 -> RRServerConfig.ITEM_TIER5_OUTPUT_TIME.get();
            default -> 20;
        };
    }

    private static int getItemTierEnergyConsumption(int tier) {
        return switch (tier) {
            case 1 -> RRServerConfig.ITEM_TIER1_ENERGY_CONSUMPTION.get();
            case 2 -> RRServerConfig.ITEM_TIER2_ENERGY_CONSUMPTION.get();
            case 3 -> RRServerConfig.ITEM_TIER3_ENERGY_CONSUMPTION.get();
            case 4 -> RRServerConfig.ITEM_TIER4_ENERGY_CONSUMPTION.get();
            case 5 -> RRServerConfig.ITEM_TIER5_ENERGY_CONSUMPTION.get();
            default -> 0;
        };
    }

    private static int getFluidTierOutputAmount(int tier) {
        return switch (tier) {
            case 1 -> RRServerConfig.FLUID_TIER1_OUTPUT_AMOUNT.get();
            case 2 -> RRServerConfig.FLUID_TIER2_OUTPUT_AMOUNT.get();
            case 3 -> RRServerConfig.FLUID_TIER3_OUTPUT_AMOUNT.get();
            case 4 -> RRServerConfig.FLUID_TIER4_OUTPUT_AMOUNT.get();
            case 5 -> RRServerConfig.FLUID_TIER5_OUTPUT_AMOUNT.get();
            default -> 1000;
        };
    }

    private static int getFluidTierOutputTankCapacity(int tier) {
        return switch (tier) {
            case 1 -> RRServerConfig.FLUID_TIER1_OUTPUT_TANK_CAPACITY.get();
            case 2 -> RRServerConfig.FLUID_TIER2_OUTPUT_TANK_CAPACITY.get();
            case 3 -> RRServerConfig.FLUID_TIER3_OUTPUT_TANK_CAPACITY.get();
            case 4 -> RRServerConfig.FLUID_TIER4_OUTPUT_TANK_CAPACITY.get();
            case 5 -> RRServerConfig.FLUID_TIER5_OUTPUT_TANK_CAPACITY.get();
            default -> 4000;
        };
    }

    private static int getFluidTierOutputTime(int tier) {
        return switch (tier) {
            case 1 -> RRServerConfig.FLUID_TIER1_OUTPUT_TIME.get();
            case 2 -> RRServerConfig.FLUID_TIER2_OUTPUT_TIME.get();
            case 3 -> RRServerConfig.FLUID_TIER3_OUTPUT_TIME.get();
            case 4 -> RRServerConfig.FLUID_TIER4_OUTPUT_TIME.get();
            case 5 -> RRServerConfig.FLUID_TIER5_OUTPUT_TIME.get();
            default -> 20;
        };
    }

    private static int getFluidTierWaterOutput(int tier) {
        return switch (tier) {
            case 1 -> RRServerConfig.FLUID_TIER1_WATER_AMOUNT.get();
            case 2 -> RRServerConfig.FLUID_TIER2_WATER_AMOUNT.get();
            case 3 -> RRServerConfig.FLUID_TIER3_WATER_AMOUNT.get();
            case 4 -> RRServerConfig.FLUID_TIER4_WATER_AMOUNT.get();
            case 5 -> RRServerConfig.FLUID_TIER5_WATER_AMOUNT.get();
            default -> 1000;
        };
    }

    private static int getFluidTierLavaOutput(int tier) {
        return switch (tier) {
            case 1 -> RRServerConfig.FLUID_TIER1_LAVA_AMOUNT.get();
            case 2 -> RRServerConfig.FLUID_TIER2_LAVA_AMOUNT.get();
            case 3 -> RRServerConfig.FLUID_TIER3_LAVA_AMOUNT.get();
            case 4 -> RRServerConfig.FLUID_TIER4_LAVA_AMOUNT.get();
            case 5 -> RRServerConfig.FLUID_TIER5_LAVA_AMOUNT.get();
            default -> 1000;
        };
    }

    private static int getFluidTierEnergyConsumption(int tier) {
        return switch (tier) {
            case 1 -> RRServerConfig.FLUID_TIER1_ENERGY_CONSUMPTION.get();
            case 2 -> RRServerConfig.FLUID_TIER2_ENERGY_CONSUMPTION.get();
            case 3 -> RRServerConfig.FLUID_TIER3_ENERGY_CONSUMPTION.get();
            case 4 -> RRServerConfig.FLUID_TIER4_ENERGY_CONSUMPTION.get();
            case 5 -> RRServerConfig.FLUID_TIER5_ENERGY_CONSUMPTION.get();
            default -> 0;
        };
    }

    private static void addChemicalReplicatorTooltip(List<Component> toolTip, int tier) {
        toolTip.add(Component.empty());

        // 获取配置的速度和输出量
        int outputAmount = getChemicalTierOutputAmount(tier);
        int outputTime = getChemicalTierOutputTime(tier);
        int energyConsumption = getChemicalTierEnergyConsumption(tier);
        int outputTankCapacity = getChemicalTierOutputTankCapacity(tier);
        double ticksPerSecond = 20.0 / outputTime;
        double mbPerSecond = outputAmount * ticksPerSecond;

        // 显示每次操作的输出数量
        toolTip.add(Component.translatable("tooltip.chemical_replicator.chemical_output_amount",
                        Component.literal(String.valueOf(outputAmount)).withStyle(ChatFormatting.GREEN))
                .withStyle(ChatFormatting.GRAY));

        // 显示每 tick 的速度
        toolTip.add(Component.translatable("tooltip.chemical_replicator.speed",
                        Component.literal(String.valueOf(outputTime)).withStyle(ChatFormatting.AQUA))
                .withStyle(ChatFormatting.GRAY));

        // 显示每秒输出量
        toolTip.add(Component.translatable("tooltip.chemical_replicator.chemical_per_second",
                        Component.literal(String.format("%.1f", mbPerSecond)).withStyle(ChatFormatting.YELLOW))
                .withStyle(ChatFormatting.GRAY));

        // 显示输出储罐容量
        toolTip.add(Component.translatable("tooltip.chemical_replicator.chemical_output_capacity",
                        Component.literal(String.valueOf(outputTankCapacity)).withStyle(ChatFormatting.LIGHT_PURPLE))
                .withStyle(ChatFormatting.GRAY));

        // 显示能量消耗
        if (energyConsumption > 0) {
            toolTip.add(Component.translatable("tooltip.chemical_replicator.energy_consumption",
                            Component.literal(String.valueOf(energyConsumption)).withStyle(ChatFormatting.RED))
                    .withStyle(ChatFormatting.GRAY));
        }

        // 显示自动输出状态
        boolean autoOutputEnabled = RRServerConfig.CHEMICAL_REPLICATOR_AUTO_OUTPUT.get();
        if (autoOutputEnabled) {
            String directionName = RRServerConfig.CHEMICAL_REPLICATOR_AUTO_OUTPUT_DIRECTION.get().getName();
            toolTip.add(Component.translatable("tooltip.resource_replicator.auto_output_direction",
                            Component.literal(directionName).withStyle(ChatFormatting.AQUA))
                    .withStyle(ChatFormatting.GRAY));
        } else {
            toolTip.add(Component.translatable("tooltip.resource_replicator.auto_output_disabled")
                    .withStyle(ChatFormatting.RED));
        }
    }

    private static int getChemicalTierOutputAmount(int tier) {
        return switch (tier) {
            case 1 -> RRServerConfig.CHEMICAL_TIER_1_OUTPUT_AMOUNT.get();
            case 2 -> RRServerConfig.CHEMICAL_TIER_2_OUTPUT_AMOUNT.get();
            case 3 -> RRServerConfig.CHEMICAL_TIER_3_OUTPUT_AMOUNT.get();
            case 4 -> RRServerConfig.CHEMICAL_TIER_4_OUTPUT_AMOUNT.get();
            case 5 -> RRServerConfig.CHEMICAL_TIER_5_OUTPUT_AMOUNT.get();
            default -> 1000;
        };
    }

    private static int getChemicalTierOutputTankCapacity(int tier) {
        return switch (tier) {
            case 1 -> RRServerConfig.CHEMICAL_TIER_1_OUTPUT_TANK_CAPACITY.get();
            case 2 -> RRServerConfig.CHEMICAL_TIER_2_OUTPUT_TANK_CAPACITY.get();
            case 3 -> RRServerConfig.CHEMICAL_TIER_3_OUTPUT_TANK_CAPACITY.get();
            case 4 -> RRServerConfig.CHEMICAL_TIER_4_OUTPUT_TANK_CAPACITY.get();
            case 5 -> RRServerConfig.CHEMICAL_TIER_5_OUTPUT_TANK_CAPACITY.get();
            default -> 4000;
        };
    }

    private static int getChemicalTierOutputTime(int tier) {
        return switch (tier) {
            case 1 -> RRServerConfig.CHEMICAL_TIER_1_OUTPUT_TIME.get();
            case 2 -> RRServerConfig.CHEMICAL_TIER_2_OUTPUT_TIME.get();
            case 3 -> RRServerConfig.CHEMICAL_TIER_3_OUTPUT_TIME.get();
            case 4 -> RRServerConfig.CHEMICAL_TIER_4_OUTPUT_TIME.get();
            case 5 -> RRServerConfig.CHEMICAL_TIER_5_OUTPUT_TIME.get();
            default -> 20;
        };
    }

    private static int getChemicalTierEnergyConsumption(int tier) {
        return switch (tier) {
            case 1 -> RRServerConfig.CHEMICAL_TIER_1_ENERGY_CONSUMPTION.get();
            case 2 -> RRServerConfig.CHEMICAL_TIER_2_ENERGY_CONSUMPTION.get();
            case 3 -> RRServerConfig.CHEMICAL_TIER_3_ENERGY_CONSUMPTION.get();
            case 4 -> RRServerConfig.CHEMICAL_TIER_4_ENERGY_CONSUMPTION.get();
            case 5 -> RRServerConfig.CHEMICAL_TIER_5_ENERGY_CONSUMPTION.get();
            default -> 0;
        };
    }
}