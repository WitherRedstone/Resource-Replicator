package com.chinaex123.resource_replicator.tooltip;

import com.chinaex123.resource_replicator.config.ServerConfig;
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
        boolean autoOutputEnabled = ServerConfig.isItemReplicatorAutoOutputEnabled();
        if (autoOutputEnabled) {
            String directionName = ServerConfig.getItemReplicatorAutoOutputDirection().getName();
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
        boolean autoOutputEnabled = ServerConfig.isFluidReplicatorAutoOutputEnabled();
        if (autoOutputEnabled) {
            String directionName = ServerConfig.getFluidReplicatorAutoOutputDirection().getName();
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
            case 1 -> ServerConfig.getItemTier1OutputAmount();
            case 2 -> ServerConfig.getItemTier2OutputAmount();
            case 3 -> ServerConfig.getItemTier3OutputAmount();
            case 4 -> ServerConfig.getItemTier4OutputAmount();
            case 5 -> ServerConfig.getItemTier5OutputAmount();
            default -> 1;
        };
    }

    private static int getItemTierOutputSlots(int tier) {
        return switch (tier) {
            case 1 -> ServerConfig.getItemTier1OutputSlots();
            case 2 -> ServerConfig.getItemTier2OutputSlots();
            case 3 -> ServerConfig.getItemTier3OutputSlots();
            case 4 -> ServerConfig.getItemTier4OutputSlots();
            case 5 -> ServerConfig.getItemTier5OutputSlots();
            default -> 1;
        };
    }

    private static int getItemTierOutputTime(int tier) {
        return switch (tier) {
            case 1 -> ServerConfig.getItemTier1OutputTime();
            case 2 -> ServerConfig.getItemTier2OutputTime();
            case 3 -> ServerConfig.getItemTier3OutputTime();
            case 4 -> ServerConfig.getItemTier4OutputTime();
            case 5 -> ServerConfig.getItemTier5OutputTime();
            default -> 20;
        };
    }

    private static int getItemTierEnergyConsumption(int tier) {
        return switch (tier) {
            case 1 -> ServerConfig.getItemTier1EnergyConsumption();
            case 2 -> ServerConfig.getItemTier2EnergyConsumption();
            case 3 -> ServerConfig.getItemTier3EnergyConsumption();
            case 4 -> ServerConfig.getItemTier4EnergyConsumption();
            case 5 -> ServerConfig.getItemTier5EnergyConsumption();
            default -> 0;
        };
    }

    private static int getFluidTierOutputAmount(int tier) {
        return switch (tier) {
            case 1 -> ServerConfig.getFluidTier1OutputAmount();
            case 2 -> ServerConfig.getFluidTier2OutputAmount();
            case 3 -> ServerConfig.getFluidTier3OutputAmount();
            case 4 -> ServerConfig.getFluidTier4OutputAmount();
            case 5 -> ServerConfig.getFluidTier5OutputAmount();
            default -> 1000;
        };
    }

    private static int getFluidTierOutputTankCapacity(int tier) {
        return switch (tier) {
            case 1 -> ServerConfig.getFluidTier1OutputTankCapacity();
            case 2 -> ServerConfig.getFluidTier2OutputTankCapacity();
            case 3 -> ServerConfig.getFluidTier3OutputTankCapacity();
            case 4 -> ServerConfig.getFluidTier4OutputTankCapacity();
            case 5 -> ServerConfig.getFluidTier5OutputTankCapacity();
            default -> 4000;
        };
    }

    private static int getFluidTierOutputTime(int tier) {
        return switch (tier) {
            case 1 -> ServerConfig.getFluidTier1OutputTime();
            case 2 -> ServerConfig.getFluidTier2OutputTime();
            case 3 -> ServerConfig.getFluidTier3OutputTime();
            case 4 -> ServerConfig.getFluidTier4OutputTime();
            case 5 -> ServerConfig.getFluidTier5OutputTime();
            default -> 20;
        };
    }

    private static int getFluidTierWaterOutput(int tier) {
        return switch (tier) {
            case 1 -> ServerConfig.getFluidTier1WaterAmount();
            case 2 -> ServerConfig.getFluidTier2WaterAmount();
            case 3 -> ServerConfig.getFluidTier3WaterAmount();
            case 4 -> ServerConfig.getFluidTier4WaterAmount();
            case 5 -> ServerConfig.getFluidTier5WaterAmount();
            default -> 1000;
        };
    }

    private static int getFluidTierLavaOutput(int tier) {
        return switch (tier) {
            case 1 -> ServerConfig.getFluidTier1LavaAmount();
            case 2 -> ServerConfig.getFluidTier2LavaAmount();
            case 3 -> ServerConfig.getFluidTier3LavaAmount();
            case 4 -> ServerConfig.getFluidTier4LavaAmount();
            case 5 -> ServerConfig.getFluidTier5LavaAmount();
            default -> 1000;
        };
    }

    private static int getFluidTierEnergyConsumption(int tier) {
        return switch (tier) {
            case 1 -> ServerConfig.getFluidTier1EnergyConsumption();
            case 2 -> ServerConfig.getFluidTier2EnergyConsumption();
            case 3 -> ServerConfig.getFluidTier3EnergyConsumption();
            case 4 -> ServerConfig.getFluidTier4EnergyConsumption();
            case 5 -> ServerConfig.getFluidTier5EnergyConsumption();
            default -> 0;
        };
    }
}
