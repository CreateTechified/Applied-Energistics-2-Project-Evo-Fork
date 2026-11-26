package appeng.api.networking.crafting;

import java.util.Collection;

/**
 * A grid machine that exposes crafting CPUs to the grid.
 *
 * <p>
 * Implementations must return the same CPU objects across calls. Post a
 * {@link appeng.api.networking.events.GridCraftingCpuChange} when the exposed set or its stats change. Called on the
 * server thread. The provider's grid node must be the node owner.
 */
public interface ICraftingCpuProvider {
    Collection<? extends ICraftingCpuHost> getExposedCpus();
}
