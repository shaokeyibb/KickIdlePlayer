package com.mcsunnyside.KickIdlePlayer;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin implements Listener {
    private int max_idle_time = 600;
    private int players = 8;
    private String kickMsg;
    private double kickTps;
    private boolean kickFull;
    private String kickFullMsg;
    private CMI cmi;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        max_idle_time = getConfig().getInt("max-idle-time");
        players = getConfig().getInt("players");
        kickMsg = getConfig().getString("kick-message");
        kickTps = getConfig().getDouble("kick-tps");
        getLogger().info("KickIdlePlayer was loaded: Kick the idle more than " + max_idle_time + " when player more than " + players + ".");
        kickMsg = ChatColor.translateAlternateColorCodes('&', kickMsg);
        getLogger().info("Kick msg was set to:" + kickMsg);
        kickFull = getConfig().getBoolean("kick-afking-players-when-server-is-full");
        kickFullMsg = getConfig().getString("kick-afking-full-message");
        Plugin plugin = Bukkit.getPluginManager().getPlugin("CMI");
        if (plugin == null) {
            getLogger().severe("Must have CMI to run plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        cmi = CMI.getInstance();
        Bukkit.getPluginManager().registerEvents(this, this);

        new BukkitRunnable() {
            @Override
            public void run() {
                if ((Bukkit.getOnlinePlayers().size() <= players) && kickFull) {
                    return; //Player not enough
                }
                if (cmi.getLagMeter().getTPS(60) >= kickTps) {
                    return; //Tps is fine
                }
                long currentTime = System.currentTimeMillis();
                long maxAllowIdleTime = max_idle_time * 1000;
                Bukkit.getOnlinePlayers().forEach((user) -> {
                    CMIUser user2 = cmi.getPlayerManager().getUser(user);
                    if (user2.isAfk()) {
                        if ((currentTime - user2.getAfkInfo().getAfkFrom()) > maxAllowIdleTime) {
                            getLogger().info("Kicking player " + user.getName() + " cause server is busying but player is idle too long time.");
                            user.kickPlayer(kickMsg);
                        }
                    }
                });
            }
        }.runTaskTimerAsynchronously(plugin,0,80);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        if ((Bukkit.getOnlinePlayers().size() <= players) && kickFull) {
            return; //Player not enough
        }
        if (cmi.getLagMeter().getTPS(60) >= kickTps) {
            return; //Tps is fine
        }
        long currentTime = System.currentTimeMillis();
        long maxAllowIdleTime = max_idle_time * 1000;
        Bukkit.getOnlinePlayers().forEach((user) -> {
            CMIUser user2 = cmi.getPlayerManager().getUser(user);
            if (user2.isAfk()) {
                if ((currentTime - user2.getAfkInfo().getAfkFrom()) > maxAllowIdleTime) {
                    getLogger().info("Kicking player " + user.getName() + " cause server is busying but player is idle too long time.");
                    user.kickPlayer(kickMsg);
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(AsyncPlayerPreLoginEvent e) {
        if (!(Bukkit.getOnlinePlayers().size() == Bukkit.getMaxPlayers())) {
            return; //Player not enough
        }
        for (Player user : Bukkit.getOnlinePlayers()) {
            CMIUser user2 = cmi.getPlayerManager().getUser(user);
            if (user2.isAfk()) {
                user.kickPlayer(kickFullMsg);
                break;
            }
        }
        if (e.getLoginResult() == AsyncPlayerPreLoginEvent.Result.KICK_FULL) {
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.ALLOWED);
        }
    }
}
