package appeng.api.networking.crafting;

import java.util.Collection;

public interface ICraftingCpuProvider {
    Collection<ICraftingCPU> getExposedCpus();
}
