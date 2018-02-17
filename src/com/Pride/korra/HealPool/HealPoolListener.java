package com.Pride.korra.GroupHealing;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;

public class GroupHealingListener implements Listener {
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerSneak(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (event.isCancelled() || bPlayer == null) {
			return;
		}

		CoreAbility coreAbil = bPlayer.getBoundAbility();
		String abil = bPlayer.getBoundAbilityName();
		if (coreAbil == null) {
			return;
		}

		if (bPlayer.isChiBlocked()) {
			event.setCancelled(true);
			return;
		}

		if (!player.isSneaking() && bPlayer.canBendIgnoreCooldowns(coreAbil)) {
			if (coreAbil instanceof WaterAbility && bPlayer.isElementToggled(Element.WATER) == true) {
				if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Water.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("HealPool")) {
					new GroupHealing(player);
				}
			}
		}
	}

}
