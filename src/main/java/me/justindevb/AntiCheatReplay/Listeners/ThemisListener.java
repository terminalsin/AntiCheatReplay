package me.justindevb.anticheatreplay.Listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.gmail.olexorus.themis.api.ActionEvent;
import com.gmail.olexorus.themis.api.ViolationEvent;

import me.justindevb.anticheatreplay.ListenerBase;
import me.justindevb.anticheatreplay.AntiCheatReplay;

public class ThemisListener extends ListenerBase implements Listener {
	private final AntiCheatReplay acReplay;
	private List<String> disabledActions = new ArrayList<>();

	public ThemisListener(AntiCheatReplay acReplay) {
		super(acReplay);
		Bukkit.getPluginManager().registerEvents(this, AntiCheatReplay.getInstance());
		this.acReplay = acReplay;

		setupThemis();
	}

	private void setupThemis() {
		initThemisSpecificConfig();
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onViolationEvent(ViolationEvent event) {
		Player p = event.getPlayer();

		if (alertList.contains(p.getUniqueId()))
			return;

		alertList.add(p.getUniqueId());

		startRecording(p, getReplayName(p, event.getType().getCheckName()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAction(ActionEvent event) {
		final Player p = event.getPlayer();

		if (disabledActions.contains(event.getActionName().toLowerCase())) {
			return;
		}

		if (!punishList.contains(p.getUniqueId()))
			punishList.add(p.getUniqueId());
	}

	private void initThemisSpecificConfig() {
		this.disabledActions = acReplay.getConfig().getStringList("Themis.Disabled-Actions");
	}

}
