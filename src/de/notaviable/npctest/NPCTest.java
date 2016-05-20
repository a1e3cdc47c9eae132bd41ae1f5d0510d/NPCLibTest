package de.notaviable.npctest;

import de.notaviable.npcs.HitListener;
import de.notaviable.npcs.NPC;
import de.notaviable.npcs.NPCLib;
import de.notaviable.npcs.data.EquipSlot;
import de.notaviable.npcs.exceptions.NotCompatibleException;
import de.notaviable.npcs.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Project: NPCLibTest
 * Created by notaviable on 29.04.2016.
 */
public class NPCTest extends JavaPlugin implements Listener, HitListener {
    public HashMap<Player, NPC> npcs = new HashMap<>();
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        ((NPCLib)Bukkit.getPluginManager().getPlugin("NPCLib")).registerListener(this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                ArrayList<Player> left = new ArrayList<Player>();
                for (Player player : npcs.keySet()) {
                    if (!player.isOnline())
                        left.add(player);
                }
                for (Player player : left) {
                    npcs.remove(player);
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!npcs.containsKey(player)) {
                        try {
                            npcs.put(player, spawnNPC(player));
                        } catch (NotCompatibleException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        NPC npc = npcs.get(player);
                        Location rotate = player.getLocation();
                        rotate.setPitch(rotate.getPitch() - 90);
                        try {
                            npc.setLocation(player.getLocation().add(rotate.getDirection().normalize().multiply(3f)));
                        } catch (NotCompatibleException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }, 1, 1);
    }

    @EventHandler
    public void onDeath(PlayerRespawnEvent event) {
        NPC npc = npcs.get(event.getPlayer());
        if (npc == null) return;
        try {
            npc.despawn();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        npcs.remove(event.getPlayer());
    }

    private NPC spawnNPC(Player player) throws NotCompatibleException, InvocationTargetException {
        NPC npc = new NPC(player);
        npc.setLocation(player.getLocation().add(player.getLocation().getDirection().normalize().multiply(3f)));
        npc.showInTab = true;
        Player fake = PlayerUtils.getRandomOnlinePlayer(player.getPlayer());
        npc.username = fake.getName();
        npc.uuid = fake.getUniqueId();
        npc.setVisible(true);
        npc.spawn();
        npc.setEquipmentInSlot(EquipSlot.MAIN_HAND, fake.getItemInHand());
        npc.setEquipmentInSlot(EquipSlot.BOOTS, fake.getInventory().getBoots());
        npc.setEquipmentInSlot(EquipSlot.LEGGINGS, fake.getInventory().getLeggings());
        npc.setEquipmentInSlot(EquipSlot.CHESTPLATE, fake.getInventory().getChestplate());
        npc.setEquipmentInSlot(EquipSlot.HELMET, fake.getInventory().getHelmet());
        return npc;
    }

    @Override
    public void onDisable() {
        ((NPCLib)Bukkit.getPluginManager().getPlugin("NPCLib")).unregisterListener(this);
        for (Map.Entry<Player, NPC> players : npcs.entrySet()) {
            if (players.getKey().isOnline()) {
                try {
                    players.getValue().despawn();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void onHit(Player player, int entityId) {
        NPC npc = npcs.get(player);
        if (npc != null) {
            if (npc.getID() == entityId) {
                player.setHealth(0);
            }
        }
    }
}
