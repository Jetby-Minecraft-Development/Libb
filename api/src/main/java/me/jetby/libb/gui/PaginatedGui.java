package me.jetby.libb.gui;

import me.jetby.libb.color.Serializer;
import me.jetby.libb.color.SerializerType;
import me.jetby.libb.gui.item.ItemWrapper;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PaginatedGui extends AdvancedGui {

    public static final String PREV_KEY = "__prev__";
    public static final String NEXT_KEY = "__next__";

    private int currentPage = 0;
    private Integer[] contentSlots;

    private final List<ItemWrapper> pageItems = new ArrayList<>();

    public PaginatedGui(@NotNull Inventory inventory) { super(inventory); }
    public PaginatedGui(String title) { super(title); }
    public PaginatedGui(String title, int size) { super(title, size); }
    public PaginatedGui(String title, Serializer serializer) { super(title, serializer); }
    public PaginatedGui(String title, int size, Serializer serializer) { super(title, size, serializer); }
    public PaginatedGui(String title, InventoryType inventoryType) { super(title, inventoryType); }
    public PaginatedGui(Component title) { super(title); }
    public PaginatedGui(Component title, int size) { super(title, size); }
    public PaginatedGui(Component title, @NotNull InventoryType inventoryType) { super(title, inventoryType); }

    public void contentSlots(Integer... slots) {
        this.contentSlots = slots;
    }

    public void addItem(ItemWrapper wrapper) {
        pageItems.add(wrapper);
    }

    public void nextPage() {
        if ((currentPage + 1) * contentSlots.length < pageItems.size()) {
            currentPage++;
            openPage(currentPage);
        }
    }

    public void prevPage() {
        if (currentPage > 0) {
            currentPage--;
            openPage(currentPage);
        }
    }

    public void everyPageLogic() {}

    public void openPage(int page) {
        if (contentSlots==null) return;
        this.currentPage = page;

        int perPage = contentSlots.length;
        int from = page * perPage;
        int to = Math.min(from + perPage, pageItems.size());

        for (int slot : contentSlots) {
            getInventory().setItem(slot, null);
        }

        for (int i = from; i < to; i++) {
            int slot = contentSlots[i - from];
            ItemWrapper wrapper = pageItems.get(i);
            wrapper.slots(slot);
            setItem("__page_item_" + i + "__", wrapper);
        }

        everyPageLogic();
    }

    @Override
    public void open(@NotNull Player player) {
        super.open(player);
        openPage(0);
    }
}