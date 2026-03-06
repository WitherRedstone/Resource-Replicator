package com.chinaex123.resource_replicator.util;

import com.chinaex123.resource_replicator.config.ServerConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ReplicatorFilter {
    // 固定的管理员物品（无法被复制）
    private static final String[] ADMIN_ITEMS = {
            "minecraft:bedrock",
            "minecraft:command_block",
            "minecraft:repeating_command_block",
            "minecraft:chain_command_block",
            "minecraft:structure_block",
            "minecraft:jigsaw",
            "minecraft:barrier",
            "minecraft:light"
    };

    /**
     * 检查物品是否可以放入复制机
     */
    public static boolean canInsertItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        String itemId = getItemId(stack);
        String modId = itemId.split(":")[0];

        // 检查是否在固定的管理员物品列表中
        for (String adminItem : ADMIN_ITEMS) {
            if (itemId.equals(adminItem)) {
                return false;
            }
        }

        boolean isBlacklistMode = ServerConfig.isBlacklistMode();

        if (isBlacklistMode) {
            // 黑名单模式：检查是否在黑名单中
            return !isInList(itemId, modId, stack, ServerConfig.getBlacklistItems());
        } else {
            // 白名单模式：检查是否在白名单中
            return isInList(itemId, modId, stack, ServerConfig.getWhitelistItems());
        }
    }

    /**
     * 检查物品是否在指定列表中
     * @param itemId 物品 ID（如 minecraft:diamond）
     * @param modId 模组 ID（如 minecraft）
     * @param stack 物品堆
     * @param list 要检查的列表
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
                    ResourceLocation tagLocation = ResourceLocation.parse(entry.substring(1));
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
     * 获取物品的完整 ID
     */
    private static String getItemId(ItemStack stack) {
        Item item = stack.getItem();
        ResourceLocation location = BuiltInRegistries.ITEM.getKey(item);
        return location.toString();
    }
}
