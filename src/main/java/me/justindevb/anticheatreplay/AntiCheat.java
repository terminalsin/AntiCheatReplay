package me.justindevb.anticheatreplay;

import me.justindevb.anticheatreplay.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.function.Function;

public enum AntiCheat {
	VULCAN("Vulcan", "Vulcan", null, new Function<AntiCheatReplay, ListenerBase>() {
		@Override
		public ListenerBase apply(AntiCheatReplay antiCheatReplay) {
			return new VulcanListener(antiCheatReplay);
		}
	}),
	SPARTAN("Spartan", "Spartan", null, new Function<AntiCheatReplay, ListenerBase>() {
		@Override
		public ListenerBase apply(AntiCheatReplay antiCheatReplay) {
			return new SpartanListener(antiCheatReplay);
		}
	}),
	MATRIX("Matrix", "Matrix", null, new Function<AntiCheatReplay, ListenerBase>() {
		@Override
		public ListenerBase apply(AntiCheatReplay antiCheatReplay) {
			return new MatrixListener(antiCheatReplay);
		}
	}),
	GODSEYE("GodsEye", "GodsEye", null, new Function<AntiCheatReplay, ListenerBase>() {
		@Override
		public ListenerBase apply(AntiCheatReplay antiCheatReplay) {
			return new GodsEyeListener(antiCheatReplay);
		}
	}),
	KAURI("Kauri", "Kauri", new Function<AntiCheatReplay, Boolean>() {
		@Override
		public Boolean apply(AntiCheatReplay antiCheatReplay) {
			Plugin kauri = Bukkit.getPluginManager().getPlugin("Kauri");
			if (kauri == null || !kauri.isEnabled())
				return false;

			Plugin atlas = Bukkit.getPluginManager().getPlugin("Atlas");
			if (atlas == null || !atlas.isEnabled()) {
				antiCheatReplay.log("Atlas is required to use Kauri!", true);
				return false;
			}

			antiCheatReplay.log("Kauri detected, enabling support...", false);
			return true;
		}
	}, new Function<AntiCheatReplay, ListenerBase>() {
		@Override
		public ListenerBase apply(AntiCheatReplay antiCheatReplay) {
			return new KauriListener(antiCheatReplay);
		}
	}),
	KARHU("Karhu", "KarhuLoader", null, new Function<AntiCheatReplay, ListenerBase>() {
		@Override
		public ListenerBase apply(AntiCheatReplay antiCheatReplay) {
			return new KarhuListener(antiCheatReplay);
		}
	}),
	THEMIS("Themis", "Themis", null, new Function<AntiCheatReplay, ListenerBase>() {
		@Override
		public ListenerBase apply(AntiCheatReplay antiCheatReplay) {
			return new ThemisListener(antiCheatReplay);
		}
	}),
	SOAROMA("Soaroma", "SoaromaSAC", null, new Function<AntiCheatReplay, ListenerBase>() {
		@Override
		public ListenerBase apply(AntiCheatReplay antiCheatReplay) {
			return new SoaromaListener(antiCheatReplay);
		}
	}),
	FLAPPY("FlappyAC", "FlappyAnticheat", null, new Function<AntiCheatReplay, ListenerBase>() {
		@Override
		public ListenerBase apply(AntiCheatReplay antiCheatReplay) {
			return new FlappyACListener(antiCheatReplay);
		}
	}),
	ARTEMIS("Artemis", "Loader", null, new Function<AntiCheatReplay, ListenerBase>() {
		@Override
		public ListenerBase apply(AntiCheatReplay antiCheatReplay) {
			return new ArtemisListener(antiCheatReplay);
		}
	}),
	ANTICHEATRELOADED("AntiCheatReloaded", "AntiCheatReloaded", null, new Function<AntiCheatReplay, ListenerBase>() {
		@Override
		public ListenerBase apply(AntiCheatReplay antiCheatReplay) {
			return new AntiCheatReloadedListener(antiCheatReplay);
		}
	});
	private final String name;
	private final String pluginName;
	private final Function<AntiCheatReplay, Boolean> checker;
	private final Function<AntiCheatReplay, ListenerBase> instantiator;

	AntiCheat(String name, String pluginName, Function<AntiCheatReplay, Boolean> checker, Function<AntiCheatReplay, ListenerBase> instantiator) {
		this.name = name;
		this.pluginName = pluginName;

		if (checker == null) {
			checker = new Function<AntiCheatReplay, Boolean>() {
				@Override
				public Boolean apply(AntiCheatReplay antiCheatReplay) {
					Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
					if (plugin == null || !plugin.isEnabled())
						return false;
					antiCheatReplay.log(name + " detected, enabling support..", false);
					return true;
				}
			};
		}
		this.checker = checker;
		this.instantiator = instantiator;
	}

	public String getName() {
		return name;
	}

	public String getPluginName() {
		return pluginName;
	}

	public Function<AntiCheatReplay, Boolean> getChecker() {
		return checker;
	}

	public Function<AntiCheatReplay, ListenerBase> getInstantiator() {
		return instantiator;
	}
}
