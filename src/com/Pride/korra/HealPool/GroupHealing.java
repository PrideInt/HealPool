package com.Pride.korra.GroupHealing;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.HealingAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

public class GroupHealing extends HealingAbility implements AddonAbility {
	
	private long cooldown;
	private long time;
	private long range;
	private long duration;
	private boolean pool;
	private int point;
	private static int regenDuration;
	private static int regenPower;
	
	private Location location;
	private Block focusedBlock;

	public GroupHealing(Player player) {
		super(player);
		if (!bPlayer.canBend(this)) {
			return;
		}

		setFields();
		
		time = System.currentTimeMillis();
		if (prepare())
			start();
	}

	private void setFields() {
		cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.WhiteXShadow.HealPool.Cooldown");
		duration = ConfigManager.getConfig().getLong("ExtraAbilities.WhiteXShadow.HealPool.Duration");
		range = ConfigManager.getConfig().getLong("ExtraAbilities.WhiteXShadow.HealPool.Range");
		regenDuration = ConfigManager.getConfig().getInt("ExtraAbilities.WhiteXShadow.HealPool.RegenDuration");
		regenPower = ConfigManager.getConfig().getInt("ExtraAbilities.WhiteXShadow.HealPool.RegenPower");
	}
	
	@SuppressWarnings("deprecation")
	private boolean prepare() {
		Block block = BlockSource.getWaterSourceBlock(player, range, ClickType.SHIFT_DOWN, true, false, false);
		if (block != null && isWater(block) && block.getData() == 0) {
			focusedBlock = block;
			location = focusedBlock.getLocation();
			return true;
		}
		return false;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public String getName() {
		return "HealPool";
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline() || !player.isSneaking()) {
			remove();
			return;
		}
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		if (System.currentTimeMillis() > time + duration) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		displayParticles();
		this.pool = true;
		return;
	}

	private void displayParticles() {
		point++;
		if (point == 32)
			point = 0;
		for (int i = 0; i < 4; i++) {
			GeneralMethods.displayColoredParticle(getCirclePoints(focusedBlock.getLocation().clone().add(0.5, -0.6, 0.5), 32, (i * 90), 3).get(point), "66FFFF", 0f, 0f, 0f);
			GeneralMethods.displayColoredParticle(getCirclePoints(focusedBlock.getLocation().clone().add(0.5, -0.6, 0.5), 32, (i * 90), 3).get(point), "CCFFFF", 0f, 0f, 0f);
			ParticleEffect.SPLASH.display(getCirclePoints(focusedBlock.getLocation().clone().add(0.5, -0.6, 0.5), 32, (i * 90), 1).get(point), 0f, 0f, 0f, 0.05f, 8);
			ParticleEffect.WAKE.display(getCirclePoints(focusedBlock.getLocation().clone().add(0.5, -0.6, 0.5), 32, (i * 90), 1).get(point), 0f, 0f, 0f, 0.02f, 8);
			GeneralMethods.displayColoredParticle(getCirclePoints(focusedBlock.getLocation().clone().add(0.5, -0.6, 0.5), 32, (i * 90), 1).get(point), "66FFFF", 0.35f, 0.35f, 0.35f);
			GeneralMethods.displayColoredParticle(getCirclePoints(focusedBlock.getLocation().clone().add(0.5, -0.6, 0.5), 32, (i * 90), 1).get(point), "CCFFFF", 0.35f, 0.35f, 0.35f);
		}
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(focusedBlock.getLocation(), 4)) {
			if (entity instanceof LivingEntity && inWater(entity) && entity.getEntityId() != player.getEntityId() && !(entity instanceof ArmorStand)) {
				applyHealing((LivingEntity) entity, player);
			}
		}
		this.pool = true;
	}
	
	private static boolean inWater(Entity entity) {
		Block block = entity.getLocation().getBlock();
		if (isWater(block) && !TempBlock.isTempBlock(block))
			return true;
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public static void applyHealing(LivingEntity livingEntity, Player player) {
		if (livingEntity.getHealth() < livingEntity.getMaxHealth()) {
			livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, regenDuration, regenPower));
			AirAbility.breakBreathbendingHold(livingEntity);
		}
	}
	
	private List<Location> getCirclePoints(Location location, int points, int startAngle, double size) {
		List<Location> locations = new ArrayList<Location>();
		for (int i = 0; i < 360; i += 360 / points) {
			double angle = (i * Math.PI / 180);
			double x = size * Math.cos(angle + startAngle);
			double z = size * Math.sin(angle + startAngle);
			Location loc = location.clone();
			loc.add(x, 1, z);
			locations.add(loc);
		}
		return locations;
	}
	
	public boolean isFormingPool() {
		return pool;
	}

	@Override
	public String getAuthor() {
		return "Prride and WhiteXShadow";
	}

	@Override
	public String getVersion() {
		return "Build 1.8";
	}
	
	public String getDescription() {
		return getVersion() + " HealPool" + " created by " + getAuthor() + ". " + "\nPOOL PARTY, YEET." + "\nHeal your friends with this advanced healing move! Some master healers can be able to heal more than 1 person at a time, however at a cost of a weaker healing power.";
	}
	
	public String getInstructions() {
		return "Hold sneak on water to create a pool in which players are able to heal from.";
	}

	@Override
	public void load() {
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new GroupHealingListener(), ProjectKorra.plugin);
		ProjectKorra.log.info(getName() + " " + getVersion() + " by " + getAuthor() + " loaded! ");
		
		ConfigManager.getConfig().addDefault("ExtraAbilities.WhiteXShadow.HealPool.Cooldown", 2000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.WhiteXShadow.HealPool.Duration", 50000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.WhiteXShadow.HealPool.Range", 30);
		ConfigManager.getConfig().addDefault("ExtraAbilities.WhiteXShadow.HealPool.RegenDuration", 100);
		ConfigManager.getConfig().addDefault("ExtraAbilities.WhiteXShadow.HealPool.RegenPower", 0);
		ConfigManager.defaultConfig.save();
	}

	@Override
	public void stop() {
		
	}

}
