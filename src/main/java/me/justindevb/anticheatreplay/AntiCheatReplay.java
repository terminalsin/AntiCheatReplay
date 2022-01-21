package me.justindevb.anticheatreplay;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import cc.funkemunky.api.Atlas;
import me.justindevb.anticheatreplay.commands.ReloadCommand;
import me.justindevb.anticheatreplay.listeners.PlayerListener;
import me.justindevb.anticheatreplay.utils.Messages;
import me.justindevb.anticheatreplay.utils.UpdateChecker;

public class AntiCheatReplay extends JavaPlugin {

	private HashMap<UUID, PlayerCache> playerCache = new HashMap<>();
	private static AntiCheatReplay instance = null;

	private final Map<AntiCheat, ListenerBase> activeListeners = new EnumMap<>(AntiCheat.class);

	@Override
	public void onEnable() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
			instance = this;

			checkRequiredPlugins();
			registerListener();
			initConfig();
			checkForUpdate();
			handleReload();
			registerCommands();
			initBstats();
		});

	}

	@Override
	public void onDisable() {
		this.playerCache.clear();

		// Properly disinitialize listeners
		for (ListenerBase value : this.activeListeners.values()) {
			value.disinit();
		}

		this.activeListeners.clear();
	}

	private void checkRequiredPlugins() {
		checkReplayAPI();
		findCompatAntiCheat();
	}

	private void registerListener() {
		Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
		// Bukkit.getPluginManager().registerEvents(new OreAnnouncerListener(this),
		// this);
	}

	/**
	 * Find a compatible AntiCheat and register support for it
	 */
	// TODO: Attempt cleaning this up
	private void findCompatAntiCheat() {
		for (AntiCheat value : AntiCheat.values()) {
			if (value.getChecker().apply(this)) {
				final ListenerBase base = value.getInstantiator().apply(this);
				activeListeners.put(value, base);
			}
		}

		if (activeListeners.isEmpty()) {
			disablePlugin();
		}
	}

	/**
	 * Check if ReplayAPI is running on the server. If not disable AntiCheatReplay
	 */
	private void checkReplayAPI() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("AdvancedReplay");
		if (plugin == null || !plugin.isEnabled()) {
			log("AdvancedReplay is required to run this plugin. Shutting down...", true);
			Bukkit.getPluginManager().disablePlugin(this);
		}
	}

	/**
	 * Initialize the Config
	 */
	private void initConfig() {

		initGeneralConfigSettings();

		initDiscordConfigSettings();

		initVulcanConfigSettings();

		// initSpartanConfigSettings();

		initThemisConfigSettings();

		getConfig().options().copyDefaults(true);
		saveConfig();

		updateConfig();

		new Messages();
	}

	public void reloadReplayConfig() {
		if (activeListeners.isEmpty())
			Atlas.getInstance().getEventManager().unregisterAll(this);
		else {
			for (ListenerBase value : activeListeners.values()) {
				if (value instanceof Listener) {
					HandlerList.unregisterAll((Listener) value);
				}

				value.disinit();
			}
		}
		reloadConfig();
		findCompatAntiCheat();
		new Messages();
		log("Reloaded Messages.yml", false);
	}

	private void initBstats() {
		final int pluginId = 13402;
		Metrics metrics = new Metrics(this, pluginId);

		metrics.addCustomChart(new SimplePie("anticheat", () -> activeListeners.keySet().stream().map(AntiCheat::getName).collect(Collectors.joining("-"))));
	}

	/**
	 * Return a player in the Player cache if they exist. If not, add them to the
	 * cache
	 * 
	 * @param uuid of Player to fetch
	 * @return PlayerCache representing the Player
	 */
	public PlayerCache getCachedPlayer(UUID uuid) {
		if (this.playerCache.containsKey(uuid))
			return this.playerCache.get(uuid);

		final PlayerCache cachedPlayer = new PlayerCache(Bukkit.getPlayer(uuid), this);
		this.playerCache.put(uuid, cachedPlayer);
		return cachedPlayer;

	}

	/**
	 * Add a player to the PlayerCache
	 * 
	 * @param uuid
	 * @param player
	 */
	public void putCachedPlayer(UUID uuid, PlayerCache player) {
		if (this.playerCache.containsKey(uuid))
			return;
		this.playerCache.put(uuid, player);
	}

	/**
	 * Check if player is Cached
	 * 
	 * @return
	 */
	public boolean isPlayerCached(UUID uuid) {
		return this.playerCache.containsKey(uuid);
	}

	/**
	 * Remove a player from the PlayerCache
	 * 
	 * @param uuid
	 */
	public void removeCachedPlayer(UUID uuid) {
		if (isPlayerCached(uuid))
			this.playerCache.remove(uuid);
	}

	/**
	 * Log a message to the console
	 * 
	 * @param msg to log
	 * @param severe Whether this is a severe message or not
	 */
	public void log(String msg, boolean severe) {
		if (severe)
			getLogger().log(Level.SEVERE, msg);
		else
			getLogger().log(Level.INFO, msg);
	}

	private void checkForUpdate() {
		if (!getConfig().getBoolean("General.Check-Update"))
			return;
		new UpdateChecker(this, 97845).getVersion(version -> {
			if (this.getDescription().getVersion().equals(version))
				log("You are up to date!", false);
			else
				log("There is an update available! Download at: https://www.spigotmc.org/resources/vulcan-replay.97845/",
						true);
		});
	}

	/**
	 * Attempt to handle if the plugin was reloaded with /reload or PlugMan
	 */
	private void handleReload() {
		Bukkit.getScheduler().runTask(this, () -> {
			if (!Bukkit.getOnlinePlayers().isEmpty()) {
				for (Player p : Bukkit.getOnlinePlayers()) {
					PlayerCache cachedPlayer = new PlayerCache(p, this);
					putCachedPlayer(p.getUniqueId(), cachedPlayer);
				}
			}
		});
	}

	/**
	 * Disable plugin if no supported AntiCheat is found
	 */
	private void disablePlugin() {
		log("No supported AntiCheat detected!", true);
		Bukkit.getPluginManager().disablePlugin(this);
	}

	private void initDiscordConfigSettings() {
		String path = "Discord.";
		FileConfiguration config = getConfig();
		config.addDefault(path + "Enabled", true);
		config.addDefault(path + "Webhook", "Enter webhook here");
		config.addDefault(path + "Avatar", "https://i.imgur.com/JPG1Kwk.png");
		config.addDefault(path + "Username", "AntiCheatReplay");
		config.addDefault(path + "Server-Name", "Server");
	}

	private void initVulcanConfigSettings() {
		List<String> list = new ArrayList<>();
		list.add("timer");
		list.add("strafe");
		getConfig().addDefault("Vulcan.Disabled-Recordings", list);
	}

	private void initThemisConfigSettings() {
		List<String> list = new ArrayList<>();
		list.add("notify");
		getConfig().addDefault("Themis.Disabled-Actions", list);
	}

	@SuppressWarnings("unused")
	private void initSpartanConfigSettings() {
		List<String> list = new ArrayList<>();
		list.add("timer");
		list.add("strafe");
		getConfig().addDefault("Spartan.Disabled-Recordings", list);
	}

	private void initGeneralConfigSettings() {
		FileConfiguration config = getConfig();
		config.addDefault("General.Check-Update", true);
		config.addDefault("General.Nearby-Range", 30);

		config.addDefault("General.Recording-Length", 2);
		config.addDefault("General.Overwrite", false);
	}

	/**
	 * Register Plugin Commands
	 */
	private void registerCommands() {
		this.getCommand("replayreload").setExecutor(new ReloadCommand());
	}

	public static AntiCheatReplay getInstance() {
		return instance;
	}

	/**
	 * Update from VulcanReplay to AntiCheatReplay
	 */
	private void updateConfig() {
		String separator = System.getProperty("file.separator");
		File file = new File(this.getDataFolder().getParentFile(), "VulcanReplay" + separator + "config.yml");
		if (file.exists()) {

			log("Outdated config found...", true);
			log("Copying old config to new plugin folder...", true);

			File config = new File(this.getDataFolder().getParentFile(), "VulcanReplay" + separator + "config.yml");
			File movedConfig = new File(this.getDataFolder().getParentFile(),
					"VulcanReplay" + separator + "configOLD.yml");
			Path src = Paths.get(config.toURI());
			Path movedSrc = Paths.get(movedConfig.toURI());
			File dstFile = new File(this.getDataFolder() + separator + "config.yml");
			Path dst = Paths.get(dstFile.toURI());

			try {
				Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
				Files.move(src, movedSrc, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				log("Error copying config from VulcanReplay" + separator + "config.yml ->" + "AntiCheatReplay"
						+ separator + "config.yml", true);
				e.printStackTrace();
				return;
			}
			log("Successfully copied config", false);
		}
	}

	public boolean isChecking(final AntiCheat antiCheat) {
		return activeListeners.containsKey(antiCheat);
	}


}
