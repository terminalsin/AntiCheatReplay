package me.justindevb.anticheatreplay.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import godseye.GodsEyePlayerViolationEvent;
import godseye.GodsEyePunishPlayerEvent;
import me.justindevb.anticheatreplay.ListenerBase;
import me.justindevb.anticheatreplay.AntiCheatReplay;

public class GodsEyeListener extends ListenerBase implements Listener {

	public GodsEyeListener(AntiCheatReplay acReplay) {
		super(acReplay);
		Bukkit.getPluginManager().registerEvents(this, AntiCheatReplay.getInstance());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST) 
	public void onFlagEvent(GodsEyePlayerViolationEvent event) {
		Player p = event.getPlayer();

		if (alertList.contains(p.getUniqueId()))
			return;

		alertList.add(p.getUniqueId());

		startRecording(p, getReplayName(p, event.getDetection()));
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPunish(GodsEyePunishPlayerEvent event) {
		final Player p = event.getPlayer();

		if (!punishList.contains(p.getUniqueId()))
			punishList.add(p.getUniqueId());
	}

}
