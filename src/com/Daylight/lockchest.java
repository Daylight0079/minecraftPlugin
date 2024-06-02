package com.Daylight;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class lockchest extends JavaPlugin{
	
	private Map<String, UUID> lockedChests;

	@Override
	public void onEnable() {
		// TODO Auto-generated method stub
        saveDefaultConfig();
        loadLockedChests();
		Bukkit.getLogger().info("LockChestPlugin has been enabled!");
        getServer().getPluginManager().registerEvents(new lockchestListener(this), this);
    }
	
	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
        Bukkit.getLogger().info("LockChestPlugin has been disabled!");
	}
	
	public Map<String, UUID> getLockedChests() {
        return lockedChests;
    }
	
	private void loadLockedChests() {
        lockedChests = new HashMap<>();
        FileConfiguration config = getConfig();
        if (config.contains("lockedChests")) {
            for (String location : config.getConfigurationSection("lockedChests").getKeys(false)) {
                String uuidString = config.getString("lockedChests." + location);
                lockedChests.put(location, UUID.fromString(uuidString));
            }
        }
    }
	
	private void saveLockedChests() {
        FileConfiguration config = getConfig();
        config.set("lockedChests", null); // 기존 데이터를 지웁니다.
        for (Map.Entry<String, UUID> entry : lockedChests.entrySet()) {
            config.set("lockedChests." + entry.getKey(), entry.getValue().toString());
        }
        saveConfig();
    }
	
}
