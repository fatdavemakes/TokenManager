package me.realized.tokenmanager.shop;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import lombok.Getter;
import me.realized.tokenmanager.Permissions;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.api.event.TMShopPurchaseEvent;
import me.realized.tokenmanager.config.Config;
import me.realized.tokenmanager.shop.gui.guis.ConfirmGui;
import me.realized.tokenmanager.shop.gui.guis.ShopGui;
import me.realized.tokenmanager.util.Placeholders;
import me.realized.tokenmanager.util.inventory.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Slot {

    private static final String PURCHASE_LOG = "%s (%s) purchased %s:%s for %s tokens! (%s -> %s)";

    @Getter
    private final TokenManagerPlugin plugin;
    @Getter
    private final Config config;
    @Getter
    private final Shop shop;
    @Getter
    private final int slot;
    @Getter
    private final int cost;
    @Getter
    private final int emptySlotsRequired;
    @Getter
    private final ItemStack displayed;
    @Getter
    private final String message;
    @Getter
    private final String subshop;
    @Getter
    private final List<String> commands;
    @Getter
    private final boolean usePermission;
    @Getter
    private final boolean confirmPurchase;
    @Getter
    private final String permissionName;
    @Getter
    private final boolean negativePermission;

    public Slot(final TokenManagerPlugin plugin, final Shop shop, final int slot, final int cost, final int emptySlotsRequired, final ItemStack displayed, final String message, final String subshop,
        final List<String> commands, final boolean usePermission, final boolean confirmPurchase, final String permissionName, final boolean negativePermission) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.shop = shop;
        this.slot = slot;
        this.cost = cost;
        this.emptySlotsRequired = emptySlotsRequired;
        this.displayed = displayed;
        this.message = message != null ? Placeholders.replace(message, cost, "price", "cost") : null;
        this.subshop = subshop;
        this.commands = commands;
        this.usePermission = usePermission;
        this.confirmPurchase = confirmPurchase;
        this.permissionName = permissionName;
        this.negativePermission = negativePermission;
        commands.replaceAll(command -> {
            command = Placeholders.replace(command, cost, "price", "cost");

            if (command.startsWith("/")) {
                command = command.substring(1);
            }

            return command;
        });
    }

    public boolean purchase(final Player player, final boolean confirmPurchase, final boolean close) {
        if (plugin.getConfiguration().isCheckInventoryFull() && InventoryUtil.isInventoryFull(player)) {
            plugin.getLang().sendMessage(player, true, "ERROR.inventory-is-full");
            return false;
        }

        final OptionalLong cached = plugin.getDataManager().get(player);

        if (!cached.isPresent()) {
            plugin.doSync(player::closeInventory);
            plugin.getLang().sendMessage(player, false, "&cYour data is improperly loaded, please re-log.");
            return false;
        }

        long balance = cached.getAsLong();

        if (balance - cost < 0) {
            plugin.doSync(player::closeInventory);
            plugin.getLang().sendMessage(player, true, "ERROR.balance-not-enough", "needed", cost - balance);
            return false;
        }

        final TMShopPurchaseEvent event = new TMShopPurchaseEvent(player, cost, shop, this);
        plugin.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        if (cost > 0) {
            // If confirm-purchase is true for shop or slot
            if (confirmPurchase) {
                plugin.getShopManager().open(player, new ConfirmGui(plugin, shop, slot));
                return false;
            }

            plugin.getDataManager().set(player, balance = balance - cost);
        }

        if (config.isLogPurchases()) {
            plugin.getLogger().info(String.format(PURCHASE_LOG, player.getUniqueId(), player.getName(), shop.getName(), slot, cost, balance + cost, balance));
        }

        if (commands != null) {
            for (final String command : commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Placeholders.replace(command, balance, "balance", "tokens").replace("%player%", player.getName()));
            }
        }

        if (message != null && !message.isEmpty()) {
            plugin.getLang().sendMessage(player, false, Placeholders.replace(message, balance, "balance", "tokens"), "player", player.getName());
        }

        if (subshop != null && !subshop.isEmpty()) {
            final Optional<Shop> result = plugin.getShopConfig().getShop(subshop);

            if (!result.isPresent()) {
                plugin.getLang().sendMessage(player, true, "ERROR.shop-not-found", "input", subshop);
                return true;
            }

            final Shop target = result.get();

            if (target.isUsePermission() && !player.hasPermission(Permissions.SHOP + target.getName())) {
                plugin.getLang().sendMessage(player, true, "ERROR.no-permission", "permission", Permissions.SHOP + target.getName());
                if (target.isCloseOnPermissionFail()) {
                    plugin.doSync(player::closeInventory);
                }
                return true;
            }

            plugin.getShopManager().open(player, new ShopGui(plugin, target));
            return true;
        }

        if (shop.isAutoClose() || close) {
            plugin.doSync(player::closeInventory);
        }

        return true;
    }
}
