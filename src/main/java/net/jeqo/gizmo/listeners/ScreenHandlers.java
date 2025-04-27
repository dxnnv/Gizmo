package net.jeqo.gizmo.listeners;

import net.jeqo.gizmo.Gizmo;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

import static net.jeqo.gizmo.data.Placeholders.screenTitle;
import static net.jeqo.gizmo.data.Placeholders.screenTitleFirstJoin;
import static net.jeqo.gizmo.data.Utilities.createComponent;
import static net.jeqo.gizmo.data.Utilities.pullConfig;
import static net.jeqo.gizmo.listeners.PlayerScreening.saveInv;

public class ScreenHandlers implements Listener {
    final Gizmo plugin = Gizmo.getInstance();

    // config options
    final Boolean playerInvulnerable = Objects.equals(pullConfig("player-invulnerable-during-load"), true);
    final Boolean blindnessOnPrompt = Objects.equals(pullConfig("blindness-during-prompt"), true);
    final Boolean kickOnDecline = Objects.equals(pullConfig("kick-on-decline"), true);

    // Player join handlers
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        // Check and give blindness effect
        if (blindnessOnPrompt) e.getPlayer().addPotionEffect(PotionEffectType.BLINDNESS.createEffect(999999, 1));
    }


    // Resource pack status handler
    @EventHandler
    public void onPackLoad(PlayerResourcePackStatusEvent e) {
        Player player = e.getPlayer();

        if (kickOnDecline) {
            if (e.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
                player.clearActivePotionEffects();
            } else if (e.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED || e.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
                player.clearActivePotionEffects();

                String kickMessage = (String) pullConfig("messages.kick-on-decline");
                if (kickMessage != null)
                    player.kick(createComponent(kickMessage.replaceAll("([|])", "")));
            }
        } else {
            if (e.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
                player.clearActivePotionEffects();
            } else if (e.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED || e.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
                player.clearActivePotionEffects();
                for (String msg : plugin.getConfig().getStringList("messages.no-pack-loaded")) {
                    player.sendMessage(createComponent(msg));
                }
            }
        }
    }

    // Restore player inventory event
    @EventHandler
    public void restoreInv(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        if (e.getView().title().equals(screenTitle()) || e.getView().title().equals(screenTitleFirstJoin()))
            p.getInventory().setContents(saveInv.get(p.getUniqueId()));
    }

    // Disabled events while screen is active
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (isInScreening(e.getPlayer()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player player && isInScreening(player))
            e.setCancelled(true);
    }

    @EventHandler
    public void onSlotClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (isInScreening(player))
            e.setCancelled(true);
    }

    // Toggleable damage events
    @EventHandler
    public void onPlayerDamage(EntityDamageByBlockEvent e) {
        if (e.getEntity() instanceof Player player && isInScreening(player))
            e.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (!playerInvulnerable) return;
        if (e.getEntity() instanceof Player player && isInScreening(player))
            e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerItemDamage(PlayerItemDamageEvent e) {
        if (playerInvulnerable && isInScreening(e.getPlayer()))
            e.setCancelled(true);
    }

    private boolean isInScreening(Player p) {
        if (!PlayerScreening.playersScreenActive.containsKey(p.getUniqueId())) return false;
        return PlayerScreening.playersScreenActive.get(p.getUniqueId()).equals(Boolean.TRUE);
    }
}