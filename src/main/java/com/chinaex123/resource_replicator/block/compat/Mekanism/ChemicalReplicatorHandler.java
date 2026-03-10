package com.chinaex123.resource_replicator.block.compat.Mekanism;

import com.chinaex123.resource_replicator.config.ServerConfig;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * 化学品处理器 - 单独分离出来避免编译时依赖
 */
public class ChemicalReplicatorHandler implements IChemicalHandler {
    private final ChemicalReplicatorBlockEntity blockEntity;

    public ChemicalReplicatorHandler(ChemicalReplicatorBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    /**
     * 获取指定储罐中的化学品
     * <p>
     * 此方法用于从化学品处理器中读取特定储罐的当前化学品。根据储罐索引返回对应的化学品堆：
     * <ul>
     *     <li><strong>tank 0</strong>：输入罐，存储待复制的原料化学品</li>
     *     <li><strong>tank 1</strong>：输出罐，存储已复制完成的产物化学品</li>
     * </ul>
     * 
     * @param tank 储罐索引（0 = 输入罐，1 = 输出罐）
     * @return ChemicalStack 指定储罐中的化学品堆，如果储罐为空则返回 ChemicalStack.EMPTY
     */
    @Nonnull
    @Override
    public ChemicalStack getChemicalInTank(int tank) {
        return tank == 0 ? blockEntity.inputChemical : blockEntity.outputChemical;
    }

    /**
     * 设置指定储罐中的化学品
     * <p>
     * 此方法用于向化学品处理器写入特定储罐的化学品数据。根据储罐索引将化学品堆设置到对应的罐中，
     * 并标记方块实体为已更新状态以同步到客户端。
     * <ul>
     *     <li><strong>tank 0</strong>：输入罐，存储待复制的原料化学品</li>
     *     <li><strong>tank 1</strong>：输出罐，存储已复制完成的产物化学品</li>
     * </ul>
     * 
     * @param tank 储罐索引（0 = 输入罐，1 = 输出罐）
     * @param stack 要设置的化学品堆，可以是空堆表示清空该储罐
     */
    @Override
    public void setChemicalInTank(int tank, ChemicalStack stack) {
        if (tank == 0) {
            blockEntity.inputChemical = stack;
        } else if (tank == 1) {
            blockEntity.outputChemical = stack;
        }
        blockEntity.markUpdated();
    }

    /**
     * 获取指定储罐的化学品容量
     * <p>
     * 此方法用于查询特定储罐能够容纳的最大化学品数量（以毫桶 mB 为单位）。
     * 输入罐和输出罐的容量可能不同，具体数值在方块实体中定义。
     * 
     * @param tank 储罐索引（0 = 输入罐，1 = 输出罐）
     * @return long 指定储罐的最大容量（单位：mB）
     */
    @Override
    public long getChemicalTankCapacity(int tank) {
        return tank == 0 ? ChemicalReplicatorBlockEntity.INPUT_TANK_CAPACITY : blockEntity.getCurrentOutputTankCapacity();
    }

    /**
     * 检查化学品是否可以放入指定储罐
     * <p>
     * 此方法用于验证给定的化学品堆是否能够被放入特定的储罐中。
     * 当前实现只允许将化学品放入输入罐（tank 0），输出罐（tank 1）不允许直接插入化学品。
     * 
     * @param tank 储罐索引（0 = 输入罐，1 = 输出罐）
     * @param stack 待验证的化学品堆
     * @return boolean 如果该储罐可以接受此化学品则返回 true，否则返回 false
     */
    @Override
    public boolean isValid(int tank, @NotNull ChemicalStack stack) {
        return tank == 0;
    }

    /**
     * 向指定储罐插入化学品
     * <p>
     * 此方法用于将化学品堆插入到指定的储罐中，支持模拟执行和实际执行两种模式。
     * 方法会检查配置项决定是否启用管道销毁功能：
     * </p>
     * <ul>
     *     <li><strong>销毁功能开启</strong>：管道输入的化学品会被瞬间销毁（返回 EMPTY）</li>
     *     <li><strong>销毁功能关闭</strong>：拒绝管道输入，只允许玩家手动右键放入</li>
     *     <li><strong>玩家操作</strong>：正常存入化学品，不受销毁功能影响</li>
     * </ul>
     * <p>
     * 插入前会验证储罐有效性、容量限制和化学品种类匹配。如果目标罐为空则直接存入；
     * 如果不为空但化学品种类相同则累加数量；如果种类不同则拒绝插入。
     * 
     * @param tank 储罐索引（0 = 输入罐，1 = 输出罐）
     * @param stack 要插入的化学品堆
     * @param action 执行模式（EXECUTE = 实际执行并修改状态，SIMULATE = 仅模拟不修改）
     * @return ChemicalStack 插入后剩余的化学品堆，如果全部插入成功则返回 EMPTY
     */
    @Override
    public ChemicalStack insertChemical(int tank, ChemicalStack stack, @NotNull Action action) {
        if (!isValid(tank, stack)) {
            return stack;
        }

        // 检查是否是通过管道（非玩家操作）插入到输入槽的化学品
        boolean isPipeInsertion = !isPlayerInsertion();

        // 如果启用了销毁功能，且是管道插入，直接销毁
        if (tank == 0 && ServerConfig.isChemicalReplicatorDestroyEnabled() && isPipeInsertion) {
            // 瞬间销毁：返回空表示全部"消耗"掉了
            return ChemicalStack.EMPTY;
        }

        // 如果没有启用销毁功能，但仍然是管道插入，拒绝输入
        if (tank == 0 && isPipeInsertion && !ServerConfig.isChemicalReplicatorDestroyEnabled()) {
            // 拒绝管道输入：返回原 stack，表示没有接受任何化学品
            return stack;
        }

        long capacity = getChemicalTankCapacity(tank);
        ChemicalStack current = getChemicalInTank(tank);

        // 如果当前储罐为空，直接插入新化学品
        if (current.isEmpty()) {
            // 计算可以插入的最大数量（取请求数量和罐容量的较小值）
            long amount = Math.min(stack.getAmount(), capacity);
            // 创建要插入的化学品堆副本，数量为计算出的可插入量
            ChemicalStack toInsert = stack.copyWithAmount(amount);

            // 如果是实际执行模式（不是模拟），更新储罐状态
            if (action == Action.EXECUTE) {
                setChemicalInTank(tank, toInsert);
            }

            // 返回剩余的化学品堆（原数量减去已插入的数量）
            return stack.copyWithAmount(stack.getAmount() - amount);
        } 
        // 如果当前储罐中的化学品与待插入的化学品种类相同
        else if (current.getChemical() == stack.getChemical()) {
            // 计算还能容纳多少数量（容量减去当前已有数量）
            long amount = Math.min(stack.getAmount(), capacity - current.getAmount());

            // 如果是实际执行模式（不是模拟），累加到储罐中
            if (action == Action.EXECUTE) {
                setChemicalInTank(tank, current.copyWithAmount(current.getAmount() + amount));
            }

            // 返回剩余的化学品堆（原数量减去已插入的数量）
            return stack.copyWithAmount(stack.getAmount() - amount);
        }

        return stack;
    }

    // 判断是否是玩家插入（通过检查调用栈）
    private boolean isPlayerInsertion() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        // 检查调用栈中是否包含 ChemicalReplicatorBlock 的 useItemOn 方法
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().contains("ChemicalReplicatorBlock") &&
                    element.getMethodName().contains("useItemOn")) {
                return true;
            }
        }

        return false;
    }

    /**
     * 从指定储罐提取化学品
     * <p>
     * 此方法用于从输出罐中提取指定数量的化学品，支持模拟执行和实际执行两种模式。
     * 只能从输出罐（tank 1）提取化学品，输入罐（tank 0）不允许提取操作。
     * </p>
     * <ul>
     *     <li><strong>检查有效性</strong>：验证储罐索引是否为输出罐</li>
     *     <li><strong>空罐检查</strong>：如果输出罐为空则直接返回空</li>
     *     <li><strong>数量计算</strong>：提取数量为请求数量和当前数量的较小值</li>
     *     <li><strong>状态更新</strong>：仅在 EXECUTE 模式下减少储罐中的化学品数量</li>
     * </ul>
     * 
     * @param tank 储罐索引（只允许 tank 1 = 输出罐）
     * @param amount 要提取的化学品数量（单位：mB）
     * @param action 执行模式（EXECUTE = 实际执行并修改状态，SIMULATE = 仅模拟不修改）
     * @return ChemicalStack 提取到的化学品堆，如果无法提取则返回 ChemicalStack.EMPTY
     */
    @Override
    public ChemicalStack extractChemical(int tank, long amount, Action action) {
        if (tank != 1) {
            return ChemicalStack.EMPTY;
        }

        ChemicalStack current = getChemicalInTank(tank);
        if (current.isEmpty()) {
            return ChemicalStack.EMPTY;
        }

        long toExtract = Math.min(amount, current.getAmount());
        ChemicalStack extracted = current.copyWithAmount(toExtract);

        if (action == Action.EXECUTE) {
            setChemicalInTank(tank, current.copyWithAmount(current.getAmount() - toExtract));
        }

        return extracted;
    }

    /**
     * 获取化学品储罐的总数量
     * <p>
     * 此方法返回复制机可用的独立储罐数量。化学品复制机共有两个储罐：
     * </p>
     * <ul>
     *     <li><strong>tank 0</strong>：输入罐，用于存储待复制的原料化学品</li>
     *     <li><strong>tank 1</strong>：输出罐，用于存储已复制完成的产物化学品</li>
     * </ul>
     * 
     * @return int 储罐总数（固定为 2）
     */
    @Override
    public int getChemicalTanks() {
        return 2;
    }
}
