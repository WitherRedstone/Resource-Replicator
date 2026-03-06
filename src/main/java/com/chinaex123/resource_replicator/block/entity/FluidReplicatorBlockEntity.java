package com.chinaex123.resource_replicator.block.entity;

import com.chinaex123.resource_replicator.block.FluidReplicatorBlock;
import com.chinaex123.resource_replicator.block.enumTier.FluidReplicatorTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class FluidReplicatorBlockEntity extends BlockEntity {
    private static final int INPUT_TANK_CAPACITY = 1000;   // 输入槽
    private static final int OUTPUT_TANK_CAPACITY = 100000; // 输出槽

    private final FluidTank inputTank = new FluidTank(INPUT_TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            markUpdated();
        }
    };

    private final FluidTank outputTank = new FluidTank(OUTPUT_TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            markUpdated();
        }
    };

    // 流体处理器 - 分离输入和输出
    private final IFluidHandler fluidHandler = new IFluidHandler() {
        @Override
        public int getTanks() {
            return 2; // 输入罐和输出罐
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            if (tank == 0) {
                return inputTank.getFluid();
            } else if (tank == 1) {
                return outputTank.getFluid();
            }
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? INPUT_TANK_CAPACITY : OUTPUT_TANK_CAPACITY;
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            // 只允许输入槽接受流体
            return tank == 0;
        }

        @Override
        public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
            // 只允许填充输入槽
            return inputTank.fill(resource, action);
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
            // 只允许从输出槽抽取
            if (!outputTank.isEmpty() &&
                    outputTank.getFluid().getFluidHolder().equals(resource.getFluidHolder())) {
                return outputTank.drain(resource.getAmount(), action);
            }
            return FluidStack.EMPTY;
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
            // 只允许从输出槽抽取
            return outputTank.drain(maxDrain, action);
        }
    };

    private int tickCounter = 0;
    private FluidReplicatorTier tier = FluidReplicatorTier.FLUID_TIER_1;

    public FluidReplicatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_REPLICATOR.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("tickCounter", tickCounter);
        tag.putInt("tier", tier.getId());

        CompoundTag tanksTag = new CompoundTag();
        if (!inputTank.isEmpty()) {
            tanksTag.put("inputTank", inputTank.writeToNBT(registries, new CompoundTag()));
        }
        if (!outputTank.isEmpty()) {
            tanksTag.put("outputTank", outputTank.writeToNBT(registries, new CompoundTag()));
        }
        tag.put("tanks", tanksTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        tickCounter = tag.getInt("tickCounter");
        if (tag.contains("tier")) {
            this.tier = FluidReplicatorTier.fromId(tag.getInt("tier"));
        }

        CompoundTag tanksTag = tag.getCompound("tanks");
        if (tanksTag.contains("inputTank")) {
            inputTank.readFromNBT(registries, tanksTag.getCompound("inputTank"));
        } else {
            // 如果没有 inputTank 标签，清空流体
            inputTank.setFluid(FluidStack.EMPTY);
        }
        if (tanksTag.contains("outputTank")) {
            outputTank.readFromNBT(registries, tanksTag.getCompound("outputTank"));
        } else {
            // 如果没有 outputTank 标签，清空流体
            outputTank.setFluid(FluidStack.EMPTY);
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("tickCounter", tickCounter);
        tag.putInt("tier", tier.getId());

        CompoundTag tanksTag = new CompoundTag();
        if (!inputTank.isEmpty()) {
            tanksTag.put("inputTank", inputTank.writeToNBT(registries, new CompoundTag()));
        }
        if (!outputTank.isEmpty()) {
            tanksTag.put("outputTank", outputTank.writeToNBT(registries, new CompoundTag()));
        }
        tag.put("tanks", tanksTag);

        return tag;
    }

    public void setTier(int tierId) {
        this.tier = FluidReplicatorTier.fromId(tierId);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FluidReplicatorBlockEntity blockEntity) {
        blockEntity.tickCounter++;

        FluidStack inputFluid = blockEntity.inputTank.getFluid();
        if (!inputFluid.isEmpty()) {
            // 检查是否可以复制该流体
            if (!blockEntity.tier.canReplicateFluid(inputFluid)) {
                return;
            }

            // 获取实际处理速度（根据流体类型）
            int actualSpeed = blockEntity.tier.getActualProcessSpeed(inputFluid);

            if (blockEntity.tickCounter >= actualSpeed) {
                blockEntity.tickCounter = 0;

                // 获取实际输出量（根据流体类型）
                int actualOutput = blockEntity.tier.getActualOutputAmount(inputFluid);

                // 尝试向周围输出流体
                Direction[] directions = Direction.values();
                int remainingOutput = actualOutput;
                boolean hasUpdated = false;

                for (Direction dir : directions) {
                    if (remainingOutput <= 0) break;

                    BlockPos neighborPos = pos.relative(dir);

                    // 检查邻居是否是流体复制器，如果是则跳过
                    BlockState neighborState = level.getBlockState(neighborPos);
                    if (neighborState.getBlock() instanceof FluidReplicatorBlock) {
                        continue;
                    }

                    IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, neighborPos, dir.getOpposite());

                    if (handler != null) {
                        FluidStack outputStack = new FluidStack(inputFluid.getFluid(), remainingOutput);
                        int filled = handler.fill(outputStack, IFluidHandler.FluidAction.EXECUTE);

                        if (filled > 0) {
                            remainingOutput -= filled;
                            hasUpdated = true;
                        }
                    }
                }

                // 将剩余的流体存入输出槽
                if (remainingOutput > 0) {
                    FluidStack currentOutput = blockEntity.outputTank.getFluid();

                    if (currentOutput.isEmpty()) {
                        // 输出槽为空，创建新的流体堆
                        FluidStack newOutput = new FluidStack(inputFluid.getFluid(), remainingOutput);
                        blockEntity.outputTank.setFluid(newOutput);
                        hasUpdated = true;
                    } else if (currentOutput.getFluidHolder().equals(inputFluid.getFluidHolder())) {
                        // 输出槽已有相同流体，尝试合并
                        int spaceAvailable = blockEntity.outputTank.getCapacity() - currentOutput.getAmount();
                        int toAdd = Math.min(spaceAvailable, remainingOutput);

                        if (toAdd > 0) {
                            currentOutput.grow(toAdd);
                            hasUpdated = true;
                        }
                    }
                }

                // 如果有更新，通知客户端
                if (hasUpdated) {
                    blockEntity.markUpdated();
                }
            }
        }
    }

    public boolean addFluid(FluidStack stack) {
        if (inputTank.isEmpty()) {
            inputTank.fill(stack, IFluidHandler.FluidAction.EXECUTE);
            markUpdated();
            return true;
        } else if (inputTank.getFluid().getFluidHolder().equals(stack.getFluidHolder())) {
            int canAdd = inputTank.getCapacity() - inputTank.getFluidAmount();
            if (canAdd > 0) {
                int added = inputTank.fill(new FluidStack(stack.getFluid(), Math.min(canAdd, stack.getAmount())), IFluidHandler.FluidAction.EXECUTE);
                markUpdated();
                return added > 0;
            }
        }
        return false;
    }

    public FluidStack extractFluid(int amount) {
        if (!inputTank.isEmpty()) {
            FluidStack extracted = inputTank.drain(amount, IFluidHandler.FluidAction.EXECUTE);
            markUpdated();
            return extracted;
        }
        return FluidStack.EMPTY;
    }

    public void clearInputFluid() {
        inputTank.setFluid(FluidStack.EMPTY);
        markUpdated();
        // 强制在客户端立即刷新渲染
        if (level != null && level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void clearAllFluids() {
        inputTank.setFluid(FluidStack.EMPTY);
        outputTank.setFluid(FluidStack.EMPTY);
        markUpdated();
        // 强制在客户端立即刷新渲染
        if (level != null && level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    private void markUpdated() {
        setChanged();
        if (level != null) {
            // 强制通知客户端更新 - 同时更新 BlockState 和 BlockEntity 数据
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);

            // 如果是服务端，需要手动同步 BlockEntity 数据到客户端
            if (!level.isClientSide) {
                // 请求重新同步 BlockEntity 数据
                var packet = ClientboundBlockEntityDataPacket.create(this);
                // 发送给所有追踪这个方块的玩家
                var players = level.players();
                for (var player : players) {
                    if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                        double distance = Math.abs(serverPlayer.getX() - getBlockPos().getX()) +
                                Math.abs(serverPlayer.getY() - getBlockPos().getY()) +
                                Math.abs(serverPlayer.getZ() - getBlockPos().getZ());
                        if (distance < 64) { // 只在一定范围内发送
                            serverPlayer.connection.send(packet);
                        }
                    }
                }
            }
        }
    }

    public FluidStack getInputFluid() {
        // 无论客户端还是服务端都返回 inputTank 的真实数据
        // 客户端的数据会通过 BlockEntity 同步机制更新
        return inputTank.getFluid();
    }

    public FluidStack getOutputFluid() {
        return outputTank.getFluid();
    }

    // 获取流体处理器（用于 NeoForge 能力系统）
    @Nullable
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return fluidHandler;
    }
}
