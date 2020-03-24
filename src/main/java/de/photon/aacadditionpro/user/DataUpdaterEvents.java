package de.photon.aacadditionpro.user;

import de.photon.aacadditionpro.olduser.UserManager;
import de.photon.aacadditionpro.util.inventory.InventoryUtils;
import de.photon.aacadditionpro.util.world.BlockUtils;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

public class DataUpdaterEvents implements Listener
{
    @EventHandler
    public void onItemInteract(PlayerInteractEvent event)
    {
        final User user = UserManager.getUser(event.getPlayer().getUniqueId());

        // Not bypassed
        if (user == null) {
            return;
        }

        if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
            user.getTimestampMap().updateTimeStamp(TimestampKey.LAST_RIGHT_CLICK_EVENT);

            if (event.getMaterial().isEdible()) {
                user.getTimestampMap().updateTimeStamp(TimestampKey.LAST_RIGHT_CLICK_CONSUMABLE_ITEM_EVENT);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsume(final PlayerItemConsumeEvent event)
    {
        final User user = UserManager.getUser(event.getPlayer().getUniqueId());

        if (user != null) {
            user.getTimestampMap().updateTimeStamp(TimestampKey.LAST_CONSUME_EVENT);
            user.getDataMap().setValue(DataKey.LAST_CONSUMED_ITEM_STACK, event.getItem());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(final PlayerDeathEvent event)
    {
        final User user = UserManager.getUser(event.getEntity().getUniqueId());

        if (user != null) {
            user.getTimestampMap().nullifyTimeStamp(TimestampKey.INVENTORY_OPENED);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInteract(final PlayerInteractEvent event)
    {
        final User user = UserManager.getUser(event.getPlayer().getUniqueId());

        if ((user != null) && (event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
            // Make sure that the block can open an InventoryView.
            BlockUtils.isInventoryOpenable(event.getClickedBlock()))
        {
            // Make sure that the container is opened and the player doesn't just place a block next to it.
            boolean sneakingRequiredToPlaceBlock = false;
            for (ItemStack handStack : InventoryUtils.getHandContents(event.getPlayer())) {
                // Check if the material is a placable block
                if (handStack.getType().isBlock()) {
                    sneakingRequiredToPlaceBlock = true;
                    break;
                }
            }

            // Not sneaking when the player can place a block that way.
            if (!(sneakingRequiredToPlaceBlock && event.getPlayer().isSneaking())) {
                user.getTimestampMap().updateTimeStamp(TimestampKey.INVENTORY_OPENED);
            }
        }
    }

    // Low to be after the MultiInteract EventHandler.
    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClick(final InventoryClickEvent event)
    {
        final User user = UserManager.getUser(event.getWhoClicked().getUniqueId());

        if (user != null &&
            // Quickbar actions can be performed outside the inventory.
            event.getSlotType() != InventoryType.SlotType.QUICKBAR)
        {
            // Only update if the inventory is currently closed to not interfere with opening time checks.
            if (!user.getInventoryData().hasOpenInventory()) {
                user.getTimestampMap().updateTimeStamp(TimestampKey.INVENTORY_OPENED);
            }

            user.getTimestampMap().updateTimeStamp(TimestampKey.LAST_INVENTORY_CLICK);
            user.getDataMap().setValue(DataKey.LAST_RAW_SLOT_CLICKED, event.getRawSlot());
            user.getDataMap().setValue(DataKey.LAST_MATERIAL_CLICKED, event.getCurrentItem() == null ?
                                                                      Material.AIR :
                                                                      event.getCurrentItem().getType());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(final InventoryCloseEvent event)
    {
        final User user = UserManager.getUser(event.getPlayer().getUniqueId());

        if (user != null) {
            user.getTimestampMap().nullifyTimeStamp(TimestampKey.INVENTORY_OPENED);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryOpen(final InventoryOpenEvent event)
    {
        // Removed theUser.getPlayer().getOpenInventory().getType() != InventoryType.CRAFTING.
        final User user = UserManager.getUser(event.getPlayer().getUniqueId());

        if (user != null) {
            user.getTimestampMap().updateTimeStamp(TimestampKey.INVENTORY_OPENED);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(final PlayerRespawnEvent event)
    {
        final User user = UserManager.getUser(event.getPlayer().getUniqueId());

        if (user != null) {
            user.getTimestampMap().nullifyTimeStamp(TimestampKey.INVENTORY_OPENED);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTeleport(final PlayerTeleportEvent event)
    {
        final User user = UserManager.getUser(event.getPlayer().getUniqueId());

        if (user != null) {
            user.getTimestampMap().nullifyTimeStamp(TimestampKey.INVENTORY_OPENED);
        }
    }

    @EventHandler
    public void onWorldChange(final PlayerChangedWorldEvent event)
    {
        final User user = UserManager.getUser(event.getPlayer().getUniqueId());

        if (user != null) {
            user.getTimestampMap().nullifyTimeStamp(TimestampKey.INVENTORY_OPENED);
        }
    }
}
