package fakeapi;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FakeItem1 {

    @NotNull
    public final ItemStack build() {
        throw new RuntimeException("empty method");
    }
}
