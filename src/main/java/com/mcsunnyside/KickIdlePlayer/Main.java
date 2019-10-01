package com.mcsunnyside.KickIdlePlayer;

import com.earth2me.essentials.Essentials;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
    private int max_idle_time = 600;
    private int players = 8;
    private Essentials essentials;
    private String kickMsg;
    @Override
    public void onEnable() {
        saveDefaultConfig();
        max_idle_time = getConfig().getInt("max-idle-time");
        players = getConfig().getInt("players");
        kickMsg = getConfig().getString("kick-message");
        getLogger().info("KickIdlePlayer was loaded: Kick the idle more than "+max_idle_time+" when player more than "+players+".");
        kickMsg = ChatColor.translateAlternateColorCodes('&',kickMsg);
        getLogger().info("Kick msg was set to :"+kickMsg);
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Essentials");
        if(plugin == null){
            getLogger().severe("Must have EssentialsX.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        essentials = (Essentials)plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e){
        if(Bukkit.getOnlinePlayers().size() <= players){
            return; //Player not enough
        }
        long currentTime = System.currentTimeMillis();
        long maxAllowIdleTime = max_idle_time*1000;
        essentials.getOnlineUsers().forEach((user )->{
            if(user.isAfk()){
                if((currentTime - user.getAfkSince()) > maxAllowIdleTime){
                    getLogger().info("Kicking player "+user.getName()+" cause server is busying but player is idle too long time.");
                    user.getBase().kickPlayer(kickMsg);
                }
            }
        });
    }

}
