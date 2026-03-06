package com.chinaex123.resource_replicator.block.entity;

import com.chinaex123.resource_replicator.block.FluidReplicatorBlock;
import com.chinaex123.resource_replicator.block.enumTier.FluidReplicatorTier;
import com.chinaex123.resource_replicator.config.ServerConfig;
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
    private static final int INPUT_TANK_CAPACITY = 1000;
    private static final int OUTPUT_TANK_CAPACITY;

    static {
        OUTPUT_TANK_CAPACITY = ServerConfig.getFluidReplicatorOutputTankSize();
    }

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

    private final IFluidHandler fluidHandler = new IFluidHandler() {
        @Override
        public int getTanks() {
            return 2;
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
            return tank == 0;
        }

        @Override
        public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
            return inputTank.fill(resource, action);
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
            if (!outputTank.isEmpty() &&
                    outputTank.getFluid().getFluidHolder().equals(resource.getFluidHolder())) {
                return outputTank.drain(resource.getAmount(), action);
            }
            return FluidStack.EMPTY;
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
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
            inputTank.setFluid(FluidStack.EMPTY);
        }
        if (tanksTag.contains("outputTank")) {
            outputTank.readFromNBT(registries, tanksTag.getCompound("outputTank"));
        } else {
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
            if (!blockEntity.tier.canReplicateFluid(inputFluid)) {
                return;
            }

            int actualSpeed = blockEntity.tier.getActualProcessSpeed(inputFluid);

            if (blockEntity.tickCounter >= actualSpeed) {
                blockEntity.tickCounter = 0;

                int actualOutput = blockEntity.tier.getActualOutputAmount(inputFluid);

                Direction[] directions = Direction.values();
                int remainingOutput = actualOutput;
                boolean hasUpdated = false;

                for (Direction dir : directions) {
                    if (remainingOutput <= 0) break;

                    BlockPos neighborPos = pos.relative(dir);

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

                if (remainingOutput > 0) {
                    FluidStack currentOutput = blockEntity.outputTank.getFluid();

                    if (currentOutput.isEmpty()) {
                        FluidStack newOutput = new FluidStack(inputFluid.getFluid(), remainingOutput);
                        blockEntity.outputTank.setFluid(newOutput);
                        hasUpdated = true;
                    } else if (currentOutput.getFluidHolder().equals(inputFluid.getFluidHolder())) {
                        int spaceAvailable = blockEntity.outputTank.getCapacity() - currentOutput.getAmount();
                        int toAdd = Math.min(spaceAvailable, remainingOutput);

                        if (toAdd > 0) {
                            currentOutput.grow(toAdd);
                            hasUpdated = true;
                        }
                    }
                }

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
    }

    public void clearAllFluids() {
        inputTank.setFluid(FluidStack.EMPTY);
        outputTank.setFluid(FluidStack.EMPTY);
        markUpdated();
    }

    private void markUpdated() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);

            if (!level.isClientSide) {
                var packet = ClientboundBlockEntityDataPacket.create(this);
                var players = level.players();
                for (var player : players) {
                    if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                        double distance = Math.abs(serverPlayer.getX() - getBlockPos().getX()) +
                                Math.abs(serverPlayer.getY() - getBlockPos().getY()) +
                                Math.abs(serverPlayer.getZ() - getBlockPos().getZ());
                        if (distance < 64) {
                            serverPlayer.connection.send(packet);
                        }
                    }
                }
            }
        }
    }

    public FluidStack getInputFluid() {
        return inputTank.getFluid();
    }

    public FluidStack getOutputFluid() {
        return outputTank.getFluid();
    }

    @Nullable
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return fluidHandler;
    }
}
