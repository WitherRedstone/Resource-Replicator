package com.chinaex123.resource_replicator.block.compat.Mekanism;

import com.mojang.serialization.MapCodec;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ChemicalReplicatorBlock extends BaseEntityBlock {
    private final int tier;

    public static final MapCodec<ChemicalReplicatorBlock> CODEC = simpleCodec(ChemicalReplicatorBlock::new);

    /**
     * 返回此方块的 MapCodec 序列化器
     *
     * @return CODEC - 用于序列化和反序列化此方块实例的编解码器
     */
    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /**
     * 化学品复制机方块构造函数（默认等级）
     * <p>
     * 用于创建默认的 T1 等级化学品复制机方块。当不指定等级参数时，使用此构造函数创建的复制机默认为等级 1。
     *
     * @param properties 方块属性对象，包含方块的物理特性（如硬度、颜色、声音等）
     */
    public ChemicalReplicatorBlock(Properties properties) {
        super(properties);
        this.tier = 1;
    }

    /**
     * 化学品复制机方块构造函数（带等级参数）
     * <p>
     * 用于创建指定等级的化学品复制机方块。通过此构造函数可以一次性创建不同等级（T1-T5）的复制机，
     * 每个等级的复制机具有不同的处理速度和输出量。
     *
     * @param properties 方块属性对象，包含方块的物理特性（如硬度、颜色、声音等）
     * @param tier 复制机等级（1-5），数字越大表示机器等级越高，处理速度越快，产量越高
     */
    public ChemicalReplicatorBlock(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    /**
     * 获取方块的渲染形状类型
     *
     * @param state 方块状态，包含方块的所有属性信息
     * @return RenderShape 渲染形状类型，此处固定返回 MODEL 使用标准模型渲染
     */
    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    /**
     * 创建新的化学品复制机方块实体
     * <p>
     * 此方法在 Minecraft 需要为方块创建对应的方块实体时被调用。它会创建一个指定等级的化学品复制机
     * 方块实体，并设置其等级属性。方块实体负责存储方块的数据、处理逻辑和同步状态。
     *
     * @param pos 方块在世界中的坐标位置
     * @param state 方块的当前状态，包含方块的所有属性信息
     * @return ChemicalReplicatorBlockEntity 新创建的化学品复制机方块实体，已设置好等级参数
     */
    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        ChemicalReplicatorBlockEntity blockEntity = new ChemicalReplicatorBlockEntity(pos, state);
        blockEntity.setTier(tier);
        return blockEntity;
    }

    /**
     * 获取方块实体的刻更新器（Ticker）
     * <p>
     * 此方法用于注册方块实体的 tick 更新逻辑。仅在服务器端注册 tick 更新器，客户端不执行任何逻辑。
     * 当方块实体需要每刻更新时（如化学品复制机的工作进度），会调用注册的更新器。
     *
     * @param level 当前世界实例，用于判断是客户端还是服务端
     * @param state 方块的当前状态
     * @param type 方块实体类型，用于匹配正确的方块实体
     * @return BlockEntityTicker<T> 方块实体的刻更新器，如果是客户端则返回 null，否则返回执行 serverTick() 的更新器
     */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof ChemicalReplicatorBlockEntity) {
                ((ChemicalReplicatorBlockEntity) be).serverTick();
            }
        };
    }

    /**
     * 处理玩家空手右键点击方块的行为
     * <p>
     * 当玩家不持有任何物品时右键点击化学品复制机，会显示当前机器中存储的化学品信息。
     * 此方法使用反射来检查化学品状态，避免了编译时对 Mekanism API 的直接依赖。
     * 如果机器为空或发生错误，会提示"空"；否则显示化学品的名称。
     * 
     * @param state 方块的当前状态
     * @param level 当前世界实例
     * @param pos 方块在世界中的坐标位置
     * @param player 执行操作的玩家
     * @param hitResult 点击检测结果，包含点击位置和面等信息
     * @return InteractionResult 交互结果，客户端直接返回 SUCCESS，服务端根据操作结果返回相应值
     */
    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ChemicalReplicatorBlockEntity replicator) {
            // 显示当前方块中的化学品
            Object inputChemical = replicator.getInputChemical();
            if (inputChemical == null) {
                player.displayClientMessage(Component.translatable("message.chemical_replicator.empty"), true);
            } else {
                try {
                    var isEmptyMethod = inputChemical.getClass().getMethod("isEmpty");
                    Boolean isEmpty = (Boolean) isEmptyMethod.invoke(inputChemical);

                    if (isEmpty) {
                        player.displayClientMessage(Component.translatable("message.chemical_replicator.empty"), true);
                    } else {
                        var getChemicalMethod = inputChemical.getClass().getMethod("getChemical");
                        Object chemical = getChemicalMethod.invoke(inputChemical);
                        var getTranslationKeyMethod = chemical.getClass().getMethod("getTranslationKey");
                        String translationKey = (String) getTranslationKeyMethod.invoke(chemical);

                        player.displayClientMessage(Component.translatable("message.chemical_replicator.contains",
                                Component.translatable(translationKey).withStyle(style -> style.withColor(ChatFormatting.AQUA))), true);
                    }
                } catch (Exception e) {
                    player.displayClientMessage(Component.translatable("message.chemical_replicator.empty"), true);
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    /**
     * 处理玩家手持物品右键点击方块的行为
     * <p>
     * 此方法处理玩家手持化学品储罐与复制机的交互，主要功能包括：
     * </p>
     * <ul>
     *     <li><strong>清空机器</strong>：当玩家手持空的化学品储罐右键时，清除机器内的所有化学品</li>
     *     <li><strong>注入化学品</strong>：将玩家储罐中的化学品注入机器（每次至少 1000mB）</li>
     *     <li><strong>数量检查</strong>：只有储罐中化学品≥1000mB 时才允许注入</li>
     *     <li><strong>数据同步</strong>：使用反射读取和修改 Minecraft 1.21 的 Data Component 系统</li>
     * </ul>
     * <p>
     * 该方法完全通过反射操作 Mekanism 的化学品储罐组件，避免了编译时对 Mekanism API 的直接依赖。
     * 
     * @param stack 玩家手持的物品堆
     * @param state 方块的当前状态
     * @param level 当前世界实例
     * @param pos 方块在世界中的坐标位置
     * @param player 执行操作的玩家
     * @param hand 使用的交互手（主手或副手）
     * @param hitResult 点击检测结果，包含点击位置和面等信息
     * @return ItemInteractionResult 物品交互结果，成功时返回 sidedSuccess，失败时返回 PASS_TO_DEFAULT_BLOCK_INTERACTION
     */
    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                       @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (level.isClientSide) {
            return ItemInteractionResult.sidedSuccess(true);
        }

        // 获取当前位置的方块实体
        BlockEntity blockEntity = level.getBlockEntity(pos);
        // 检查方块实体是否为化学品复制机类型，如果不是则返回默认交互
        if (!(blockEntity instanceof ChemicalReplicatorBlockEntity replicator)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // 如果玩家手持空的化学品储罐（没有 chemicals 组件或化学品数量为 0），清除所有化学品
        String stackItemId = stack.getItem().toString();
        boolean isEmptyChemicalTank = stackItemId.contains("chemical_tank");

        // 检查是否有 chemicals 组件以及是否包含有效化学品
        boolean hasValidChemicals = false;
        long amount = 0;
        Object chemicalStack = null;
        Class<?> chemicalStackClass = null;

        // 使用反射读取物品储罐中的化学品数据
        try {
            // 获取 ItemStack 的 components 字段
            Field componentsField = stack.getClass().getDeclaredField("components");
            // 设置可访问私有字段
            componentsField.setAccessible(true);
            // 读取当前物品的数据组件对象
            Object components = componentsField.get(stack);

            // 如果数据组件不为空，继续解析
            if (components != null) {
                // 获取数据组件 Map 的 Class 对象
                Class<?> componentMapClass = components.getClass();
                // 获取 keySet() 方法用于遍历所有组件类型
                Method keySetMethod = componentMapClass.getMethod("keySet");
                // 调用 keySet() 获取所有数据组件类型的集合
                Set<?> keySet = (Set<?>) keySetMethod.invoke(components);

                // 遍历所有数据组件
                for (Object key : keySet) {
                    // 查找包含 "mekanism:chemicals" 的组件
                    if (key.toString().contains("mekanism:chemicals")) {
                        // 获取 get() 方法用于根据类型获取组件值
                        Method getMethod = componentMapClass.getMethod("get", DataComponentType.class);
                        // 调用 get() 获取化学品组件对象
                        Object chemicalsComponent = getMethod.invoke(components, key);

                        // 如果化学品组件不为空，继续解析
                        if (chemicalsComponent != null) {
                            // 获取 AttachedChemicals 类的 Class 对象
                            Class<?> attachedChemicalsClass = chemicalsComponent.getClass();
                            // 获取 containers() 方法用于获取化学品容器列表
                            Method containersMethod = attachedChemicalsClass.getMethod("containers");
                            // 调用 containers() 获取化学品容器列表
                            Object containersList = containersMethod.invoke(chemicalsComponent);

                            // 检查返回的是否为 List 类型
                            if (containersList instanceof List<?> containers) {
                                // 如果容器列表不为空
                                if (!containers.isEmpty()) {
                                    // 获取第一个容器中的化学品堆
                                    chemicalStack = containers.getFirst();
                                    // 获取化学品堆的 Class 对象
                                    chemicalStackClass = chemicalStack.getClass();
                                    // 获取 getAmount() 方法用于获取化学品数量
                                    Method getAmountMethod = chemicalStackClass.getMethod("getAmount");
                                    // 调用 getAmount() 获取化学品的毫桶数量
                                    amount = (Long) getAmountMethod.invoke(chemicalStack);

                                    // 如果化学品数量大于 0，标记为有效
                                    if (amount > 0) {
                                        hasValidChemicals = true;
                                    }
                                }
                            }
                        }
                        break; // 找到化学品组件后跳出循环
                    }
                }
            }
        } catch (Exception e) {
            // 反射操作失败时忽略异常，hasValidChemicals 保持为 false
        }

        // 处理空储罐清空机器的逻辑
        if (isEmptyChemicalTank && !hasValidChemicals) {
            // 获取复制机内的当前化学品并检查是否为空
            if (!isChemicalEmpty(replicator.getInputChemical())) {
                replicator.clearAllChemicals(); // 清除机器内的所有化学品
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, player.getSoundSource(), 1.0F, 1.0F);
                player.displayClientMessage(Component.translatable("message.chemical_replicator.cleared")
                        .withStyle(style -> style.withColor(ChatFormatting.YELLOW)), true);
            } else {
                // 机器本身为空，提示玩家
                player.displayClientMessage(Component.translatable("message.chemical_replicator.empty"), true);
            }
            return ItemInteractionResult.sidedSuccess(false);
        }

        // 尝试将玩家手中的化学品倒入复制机
        // 只有当储罐中有至少 1000mB 时才允许放入
        if (!hasValidChemicals || amount < 1000) {
            if (hasValidChemicals) {
                player.displayClientMessage(Component.translatable("message.chemical_replicator.insufficient_chemicals", amount), true);
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // 使用反射将玩家储罐中的化学品注入复制机
        try {
            // 获取 ItemStack 的 components 字段
            java.lang.reflect.Field componentsField = stack.getClass().getDeclaredField("components");
            // 设置可访问私有字段
            componentsField.setAccessible(true);
            // 读取当前物品的数据组件对象
            Object components = componentsField.get(stack);

            // 如果数据组件为空，返回默认交互
            if (components == null) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }

            // 获取数据组件 Map 的 Class 对象
            Class<?> componentMapClass = components.getClass();
            // 获取 keySet() 方法用于遍历所有组件类型
            Method keySetMethod = componentMapClass.getMethod("keySet");
            // 调用 keySet() 获取所有数据组件类型的集合
            Set<?> keySet = (Set<?>) keySetMethod.invoke(components);

            // 遍历所有数据组件
            for (Object key : keySet) {
                // 跳过不包含 "mekanism:chemicals" 的组件
                if (!key.toString().contains("mekanism:chemicals")) {
                    continue;
                }

                // 获取 get() 方法用于根据类型获取组件值
                Method getMethod = componentMapClass.getMethod("get", DataComponentType.class);
                // 调用 get() 获取化学品组件对象
                Object chemicalsComponent = getMethod.invoke(components, key);

                // 如果化学品组件为空，跳过
                if (chemicalsComponent == null) {
                    continue;
                }

                // 获取 AttachedChemicals 类的 Class 对象
                Class<?> attachedChemicalsClass = chemicalsComponent.getClass();
                // 获取 containers() 方法用于获取化学品容器列表
                Method containersMethod = attachedChemicalsClass.getMethod("containers");
                // 调用 containers() 获取化学品容器列表
                Object containersList = containersMethod.invoke(chemicalsComponent);

                // 如果不是 List 类型，跳过
                if (!(containersList instanceof List<?> containers)) {
                    continue;
                }

                // 如果容器列表为空，跳过
                if (containers.isEmpty()) {
                    continue;
                }

                // 获取第一个容器中的化学品堆
                Object chemStack = containers.getFirst();
                // 获取化学品堆的 Class 对象
                Class<?> chemStackClass = chemStack.getClass();
                // 获取 getAmount() 方法用于获取化学品数量
                Method getAmountMethod = chemStackClass.getMethod("getAmount");
                // 调用 getAmount() 获取化学品的毫桶数量
                long currentAmount = (Long) getAmountMethod.invoke(chemStack);

                // 再次确认至少有 1000mB，不足则跳过
                if (currentAmount < 1000) {
                    continue;
                }

                // 获取 getChemical() 方法用于获取化学品对象
                Method getChemicalMethod = chemStackClass.getMethod("getChemical");
                // 调用 getChemical() 获取化学品
                Object chemical = getChemicalMethod.invoke(chemStack);

                // 获取化学品类的 Class 对象
                Class<?> chemicalClass = chemical.getClass();
                // 获取 getRegistryName() 方法用于获取注册表名称
                Method getRegistryNameMethod = chemicalClass.getMethod("getRegistryName");
                // 调用 getRegistryName() 获取化学品的注册表位置
                ResourceLocation chemicalLocation = (ResourceLocation) getRegistryNameMethod.invoke(chemical);

                // 拼接化学品 ID 字符串
                String chemicalId = chemicalLocation.toString();
                String translationKey = "chemical." + chemicalId.replace(":", ".");

                // 获取复制机的化学品处理器
                Object handler = replicator.getChemicalHandler(null);

                // 如果处理器不是 IChemicalHandler 类型，跳过
                if (!(handler instanceof IChemicalHandler chemicalHandler)) {
                    continue;
                }

                // 创建要插入的 1000mB 化学品堆
                ChemicalStack toInsert = ((ChemicalStack) chemStack).copyWithAmount(1000);
                // 尝试将化学品插入复制机的输入罐
                ChemicalStack remainder = chemicalHandler.insertChemical(0, toInsert, Action.EXECUTE);

                // 如果剩余为空，表示全部成功插入
                if (remainder.isEmpty()) {
                    // 计算储罐中剩余的化学品数量
                    long newAmount = currentAmount - 1000;

                    // 如果全部转移成功，清空储罐的化学品组件
                    if (newAmount <= 0) {
                        // 获取 remove() 方法用于移除组件
                        Method removeMethod = componentMapClass.getMethod("remove", DataComponentType.class);
                        // 调用 remove() 移除化学品组件
                        removeMethod.invoke(components, key);
                    } else {
                        // 部分转移，更新储罐中的化学品数量
                        // 获取 copyWithAmount() 方法用于创建新数量的化学品堆
                        Method copyWithAmountMethod = chemStackClass.getMethod("copyWithAmount", long.class);
                        // 调用 copyWithAmount() 创建新数量的化学品堆
                        ChemicalStack newStack = (ChemicalStack) copyWithAmountMethod.invoke(chemStack, newAmount);

                        // 获取 AttachedChemicals 的构造函数
                        Constructor<?> attachedChemicalsConstructor = attachedChemicalsClass.getDeclaredConstructor(List.class);
                        // 设置构造函数可访问
                        attachedChemicalsConstructor.setAccessible(true);

                        // 创建新的容器列表
                        List<Object> newContainers = new ArrayList<>();
                        // 将新化学品堆添加到列表
                        newContainers.add(newStack);
                        // 使用构造函数创建新的 AttachedChemicals 对象
                        Object newAttachedChemicals = attachedChemicalsConstructor.newInstance(newContainers);

                        // 获取 set() 方法用于设置组件值
                        Method setMethod = componentMapClass.getMethod("set", DataComponentType.class, Object.class);
                        // 调用 set() 更新物品数据组件中的化学品
                        setMethod.invoke(components, key, newAttachedChemicals);
                    }

                    Component chemicalName = Component.translatable(translationKey).withStyle(style -> style.withColor(ChatFormatting.GOLD));
                    player.displayClientMessage(Component.translatable("message.chemical_replicator.inserted", chemicalName), true);
                    level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, player.getSoundSource(), 1.0F, 1.0F);

                    return ItemInteractionResult.sidedSuccess(false);
                } else {
                    // 由于容量限制，无法完全放入 1000mB
                    // 计算实际插入的数量
                    long inserted = 1000 - remainder.getAmount();
                    // 如果有部分插入成功
                    if (inserted > 0) {
                        // 计算储罐中剩余的化学品数量
                        long newAmount = currentAmount - inserted;
                        // 获取 copyWithAmount() 方法用于创建新数量的化学品堆
                        Method copyWithAmountMethod = chemStackClass.getMethod("copyWithAmount", long.class);
                        // 调用 copyWithAmount() 创建新数量的化学品堆
                        ChemicalStack newStack = (ChemicalStack) copyWithAmountMethod.invoke(chemStack, newAmount);

                        // 获取 AttachedChemicals 的构造函数
                        Constructor<?> attachedChemicalsConstructor = attachedChemicalsClass.getDeclaredConstructor(List.class);
                        // 设置构造函数可访问
                        attachedChemicalsConstructor.setAccessible(true);

                        // 创建新的容器列表
                        List<Object> newContainers = new ArrayList<>();
                        // 将新化学品堆添加到列表
                        newContainers.add(newStack);
                        // 使用构造函数创建新的 AttachedChemicals 对象
                        Object newAttachedChemicals = attachedChemicalsConstructor.newInstance(newContainers);

                        // 获取 set() 方法用于设置组件值
                        Method setMethod = componentMapClass.getMethod("set", DataComponentType.class, Object.class);
                        // 调用 set() 更新物品数据组件中的化学品
                        setMethod.invoke(components, key, newAttachedChemicals);

                        Component chemicalName = Component.translatable(translationKey).withStyle(style -> style.withColor(ChatFormatting.GOLD));
                        player.displayClientMessage(Component.translatable("message.chemical_replicator.inserted", chemicalName), true);
                        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, player.getSoundSource(), 1.0F, 1.0F);

                        return ItemInteractionResult.sidedSuccess(false);
                    }
                }
            }
        } catch (Exception e) {
            // 反射操作失败时忽略异常，继续执行后续代码
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /**
     * 使用反射检查化学品堆是否为空
     * <p>
     * 此方法通过反射调用 ChemicalStack 的 isEmpty() 方法来判断化学品堆是否为空，
     * 避免了编译时对 Mekanism API 的直接依赖。
     * </p>
     * 
     * @param chemicalStack 待检查的化学品堆对象（可能为 null）
     * @return boolean 如果化学品堆为 null 或为空则返回 true，否则返回 false
     */
    private boolean isChemicalEmpty(Object chemicalStack) {
        if (chemicalStack == null) {
            return true;
        }
        
        try {
            // 调用 ChemicalStack 的 isEmpty() 方法判断是否为空
            var isEmptyMethod = chemicalStack.getClass().getMethod("isEmpty");
            Boolean isEmpty = (Boolean) isEmptyMethod.invoke(chemicalStack);
            return isEmpty != null && isEmpty;
        } catch (Exception e) {
            // 反射调用失败时返回 true（视为空）
            return true;
        }
    }
}
