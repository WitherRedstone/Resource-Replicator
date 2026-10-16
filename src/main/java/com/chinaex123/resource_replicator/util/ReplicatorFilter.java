package com.chinaex123.resource_replicator.util;

import com.chinaex123.resource_replicator.config.RRServerConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

/**
 * 复制机过滤器类。
 * <p>
 * 负责判断物品或流体是否允许放入复制机，
 * 支持固定管理员黑名单、黑名单模式与白名单模式，
 * 并支持物品 ID、模组 ID（@开头）与 Tag（#开头）匹配。
 */
public class ReplicatorFilter {

    /**
     * 固定的管理员物品（无法被复制）。
     */
    private static final String[] ADMIN_ITEMS = {
            "minecraft:command_block",
            "minecraft:chain_command_block",
            "minecraft:repeating_command_block",
            "minecraft:command_block_minecart",
            "minecraft:jigsaw",
            "minecraft:structure_block",
            "minecraft:structure_void",
            "minecraft:barrier",
            "minecraft:debug_stick",
            "minecraft:light",

            "minecraft:bedrock",
            //"minecraft:spawner",
            //"minecraft:trial_spawner",

            "resource_replicator:*"
    };

    /**
     * 固定的管理员流体（无法被复制）。
     */
    private static final String[] ADMIN_FLUIDS = {};

    /**
     * 检查物品是否可以放入复制机。
     *
     * @param stack 待检查的物品
     * @return 是否允许放入
     */
    public static boolean canInsertItem(ItemStack stack) {
        return canInsertItemWithReason(stack).canInsert();
    }

    /**
     * 检查物品是否可以放入复制机，并返回原因。
     * <p>
     * 依次检查空物品、固定管理员物品列表，
     * 然后根据配置的黑/白名单模式进行过滤。
     *
     * @param stack 待检查的物品
     * @return FilterResult 包含是否允许和原因
     */
    public static FilterResult canInsertItemWithReason(ItemStack stack) {
        if (stack.isEmpty()) {
            return new FilterResult(false, Component.translatable("message.item_replicator.filter.denied"));
        }

        String itemId = getItemId(stack);
        String modId = itemId.split(":")[0];

        // 检查是否在固定的管理员物品列表中
        for (String adminItem : ADMIN_ITEMS) {
            // 支持通配符格式：modid:*
            if (adminItem.endsWith(":*")) {
                String targetModId = adminItem.substring(0, adminItem.length() - 2);
                if (modId.equals(targetModId)) {
                    return new FilterResult(false, Component.translatable("message.item_replicator.filter.denied"));
                }
            } else if (itemId.equals(adminItem)) {
                return new FilterResult(false, Component.translatable("message.item_replicator.filter.denied"));
            }
        }

        boolean isBlacklistMode = RRServerConfig.BLACKLIST_MODE.get();

        if (isBlacklistMode) {
            // 黑名单模式：检查是否在黑名单中
            if (isInList(itemId, modId, stack, RRServerConfig.getBlacklistItems())) {
                return new FilterResult(false, Component.translatable("message.item_replicator.filter.denied"));
            }
        } else {
            // 白名单模式：检查是否在白名单中
            if (!isInList(itemId, modId, stack, RRServerConfig.getWhitelistItems())) {
                return new FilterResult(false, Component.translatable("message.item_replicator.filter.denied"));
            }
        }

        return new FilterResult(true, Component.empty());
    }

    /**
     * 检查流体是否可以放入复制机。
     *
     * @param fluidStack 待检查的流体
     * @return 是否允许放入
     */
    public static boolean canInsertFluid(FluidStack fluidStack) {
        return canInsertFluidWithReason(fluidStack).canInsert();
    }

    /**
     * 检查流体是否可以放入复制机，并返回原因。
     * <p>
     * 依次检查空流体、固定管理员流体列表，
     * 然后根据配置的黑/白名单模式进行过滤。
     *
     * @param fluidStack 待检查的流体
     * @return FilterResult 包含是否允许和原因
     */
    public static FilterResult canInsertFluidWithReason(FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            return new FilterResult(false, Component.translatable("message.fluid_replicator.filter.denied"));
        }

        String fluidId = getFluidId(fluidStack);
        String modId = fluidId.split(":")[0];

        // 检查是否在固定的管理员流体列表中
        for (String adminFluid : ADMIN_FLUIDS) {
            if (fluidId.equals(adminFluid)) {
                return new FilterResult(false, Component.translatable("message.fluid_replicator.filter.denied"));
            }
        }

        boolean isBlacklistMode = RRServerConfig.FLUID_BLACKLIST_MODE.get();

        if (isBlacklistMode) {
            // 黑名单模式：检查是否在黑名单中
            if (isFluidInList(fluidId, modId, fluidStack, RRServerConfig.getFluidBlacklistItems())) {
                return new FilterResult(false, Component.translatable("message.fluid_replicator.filter.denied"));
            }
        } else {
            // 白名单模式：检查是否在白名单中
            if (!isFluidInList(fluidId, modId, fluidStack, RRServerConfig.getFluidWhitelistItems())) {
                return new FilterResult(false, Component.translatable("message.fluid_replicator.filter.denied"));
            }
        }

        return new FilterResult(true, Component.empty());
    }

    /**
     * 检查物品是否在指定列表中。
     * <p>
     * 支持直接匹配物品 ID、匹配模组 ID（@开头）
     * 以及匹配 Tag（#开头）。
     *
     * @param itemId 物品 ID
     * @param modId  模组 ID
     * @param stack  待检查的物品
     * @param list   过滤列表
     * @return 是否在列表中
     */
    private static boolean isInList(String itemId, String modId, ItemStack stack, List<String> list) {
        for (String entry : list) {
            // 直接匹配物品 ID
            if (entry.equals(itemId)) {
                return true;
            }

            // 匹配模组 ID（@开头）
            if (entry.startsWith("@") && modId.equals(entry.substring(1))) {
                return true;
            }

            // 匹配 Tag（#开头）
            if (entry.startsWith("#")) {
                try {
                    Identifier tagLocation = Identifier.parse(entry.substring(1));
                    TagKey<Item> tag = TagKey.create(net.minecraft.core.registries.Registries.ITEM, tagLocation);
                    if (stack.is(tag)) {
                        return true;
                    }
                } catch (Exception e) {
                    // Tag 解析失败，跳过
                }
            }
        }
        return false;
    }

    /**
     * 检查流体是否在指定列表中。
     * <p>
     * 支持直接匹配流体 ID、匹配模组 ID（@开头）
     * 以及匹配 Tag（#开头）。
     *
     * @param fluidId    流体 ID
     * @param modId      模组 ID
     * @param fluidStack 待检查的流体
     * @param list       过滤列表
     * @return 是否在列表中
     */
    private static boolean isFluidInList(String fluidId, String modId, FluidStack fluidStack, List<String> list) {
        for (String entry : list) {
            // 直接匹配流体 ID
            if (entry.equals(fluidId)) {
                return true;
            }

            // 匹配模组 ID（@开头）
            if (entry.startsWith("@") && modId.equals(entry.substring(1))) {
                return true;
            }

            // 匹配 Tag（#开头）
            if (entry.startsWith("#")) {
                try {
                    Identifier tagLocation = Identifier.parse(entry.substring(1));
                    TagKey<Fluid> tag = TagKey.create(net.minecraft.core.registries.Registries.FLUID, tagLocation);
                    if (fluidStack.is(tag)) {
                        return true;
                    }
                } catch (Exception e) {
                    // Tag 解析失败，跳过
                }
            }
        }
        return false;
    }

    /**
     * 获取物品的完整 ID。
     *
     * @param stack 物品
     * @return 物品 ID 字符串
     */
    private static String getItemId(ItemStack stack) {
        Item item = stack.getItem();
        Identifier location = BuiltInRegistries.ITEM.getKey(item);
        return location.toString();
    }

    /**
     * 获取流体的完整 ID。
     *
     * @param fluidStack 流体
     * @return 流体 ID 字符串
     */
    private static String getFluidId(FluidStack fluidStack) {
        Fluid fluid = fluidStack.getFluid();
        Identifier location = BuiltInRegistries.FLUID.getKey(fluid);
        return location.toString();
    }

    /**
     * 过滤结果类。
     * <p>
     * 包含是否允许放入的标志以及拒绝原因。
     */
    public static class FilterResult {

        /**
         * 是否允许放入。
         */
        private final boolean canInsert;

        /**
         * 拒绝原因。
         */
        private final Component reason;

        /**
         * 构造过滤结果。
         *
         * @param canInsert 是否允许放入
         * @param reason    原因
         */
        public FilterResult(boolean canInsert, Component reason) {
            this.canInsert = canInsert;
            this.reason = reason;
        }

        /**
         * 获取是否允许放入。
         *
         * @return 是否允许放入
         */
        public boolean canInsert() {
            return canInsert;
        }

        /**
         * 获取拒绝原因。
         *
         * @return 原因
         */
        public Component getReason() {
            return reason;
        }
    }
}