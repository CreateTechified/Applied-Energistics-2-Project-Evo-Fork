package appeng.api.networking.crafting;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.Level;

import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.GenericStack;
import appeng.crafting.execution.CraftingCpuLogic;

public interface ICraftingCpuHost extends ICraftingCPU {
    boolean isActive();

    boolean isDestroyed();

    void markDirty();

    void updateOutput(@Nullable GenericStack finalOutput);

    @Nullable
    IGrid getGrid();

    IActionSource getSrc();

    Level getLevel();

    CraftingCpuLogic getCraftingLogic();

    default boolean canBeAutoSelectedFor(IActionSource source) {
        return true;
    }

    default boolean isPreferredFor(IActionSource source) {
        return false;
    }
}
