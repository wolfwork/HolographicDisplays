package com.gmail.filoghost.holographicdisplays.object.line;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Location;
import org.bukkit.World;

import com.gmail.filoghost.holographicdisplays.HolographicDisplays;
import com.gmail.filoghost.holographicdisplays.api.handler.TouchHandler;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.gmail.filoghost.holographicdisplays.nms.interfaces.entity.NMSCanMount;
import com.gmail.filoghost.holographicdisplays.nms.interfaces.entity.NMSEntityBase;
import com.gmail.filoghost.holographicdisplays.nms.interfaces.entity.NMSNameable;
import com.gmail.filoghost.holographicdisplays.object.CraftHologram;
import com.gmail.filoghost.holographicdisplays.placeholder.PlaceholdersManager;
import com.gmail.filoghost.holographicdisplays.util.Offsets;

public class CraftTextLine extends CraftTouchableLine implements TextLine {

	private String text;
	
	private NMSNameable nmsNameble;
	
	// Legacy code for < 1.7, not used in 1.8 and greater
	private NMSEntityBase nmsSkullVehicle;
	
	
	public CraftTextLine(CraftHologram parent, String text) {
		super(0.23, parent);
		setText(text);
	}
	
	
	@Override
	public String getText() {
		return text;
	}
	
	@Override
	public void setText(String text) {
		this.text = text;
		
		if (nmsNameble != null) {
			if (text != null && !text.isEmpty()) {
				nmsNameble.setCustomNameNMS(text);
				if (getParent().isAllowPlaceholders()) {
					PlaceholdersManager.trackIfNecessary(this);
				}
			} else {
				nmsNameble.setCustomNameNMS(""); // It will not appear
				if (getParent().isAllowPlaceholders()) {
					PlaceholdersManager.untrack(this);
				}
			}
		}
	}
	
	public void setTouchHandler(TouchHandler touchHandler) {
		
		if (nmsNameble != null) {
			
			Location loc = nmsNameble.getBukkitEntityNMS().getLocation();
			
			if (HolographicDisplays.is1_8()) {
				super.setTouchHandler(touchHandler, loc.getWorld(), loc.getX(), loc.getY() - Offsets.ARMOR_STAND_ALONE, loc.getZ());
			} else {
				super.setTouchHandler(touchHandler, loc.getWorld(), loc.getX(), loc.getY() - Offsets.WITHER_SKULL_WITH_HORSE, loc.getZ());
			}
			
		} else {
			super.setTouchHandler(touchHandler, null, 0, 0, 0);
		}
	}

	@Override
	public void spawn(World world, double x, double y, double z) {
		super.spawn(world, x, y, z);
			
		if (HolographicDisplays.is1_8()) {
			nmsNameble = HolographicDisplays.getNMSManager().spawnNMSArmorStand(world, x, y + Offsets.ARMOR_STAND_ALONE, z, this);
		} else {
			nmsNameble = HolographicDisplays.getNMSManager().spawnNMSHorse(world, x, y + Offsets.WITHER_SKULL_WITH_HORSE, z, this);
			nmsSkullVehicle = HolographicDisplays.getNMSManager().spawnNMSWitherSkull(world, x, y + Offsets.WITHER_SKULL_WITH_HORSE, z, this);
			
			// In 1.7 it must be an instanceof NMSCanMount
			((NMSCanMount) nmsNameble).setPassengerOfNMS(nmsSkullVehicle);
			nmsSkullVehicle.setLockTick(true);
		}
		
		if (text != null && !text.isEmpty()) {
			nmsNameble.setCustomNameNMS(text);
		}
		
		nmsNameble.setLockTick(true);
	}

	
	@Override
	public void despawn() {
		super.despawn();
		
		if (nmsSkullVehicle != null) {
			nmsSkullVehicle.killEntityNMS();
			nmsSkullVehicle = null;
		}
		
		if (nmsNameble != null) {
			nmsNameble.killEntityNMS();
			nmsNameble = null;
		}
	}

	
	@Override
	public void teleport(double x, double y, double z) {
		super.teleport(x, y, z);
		
		if (nmsSkullVehicle != null) {
			nmsSkullVehicle.setLocationNMS(x, y + Offsets.WITHER_SKULL_WITH_HORSE, z);
		}
		
		if (nmsNameble != null) {
			nmsNameble.setLocationNMS(x, y + (HolographicDisplays.is1_8() ? Offsets.ARMOR_STAND_ALONE : Offsets.WITHER_SKULL_WITH_HORSE), z);
		}
	}
	
	@Override
	public int[] getEntitiesIDs() {
		if (isSpawned()) {
			if (nmsSkullVehicle != null) {
				if (touchSlime != null) {
					return ArrayUtils.addAll(new int[] {nmsNameble.getIdNMS(), nmsSkullVehicle.getIdNMS()}, touchSlime.getEntitiesIDs());
				} else {
					return new int[] {nmsNameble.getIdNMS(), nmsSkullVehicle.getIdNMS()};
				}
			} else {
				if (touchSlime != null) {
					return ArrayUtils.add(touchSlime.getEntitiesIDs(), nmsNameble.getIdNMS());
				} else {
					return new int[] {nmsNameble.getIdNMS()};
				}
			}
		} else {
			return new int[0];
		}
	}

	public NMSNameable getNmsNameble() {
		return nmsNameble;
	}

	public NMSEntityBase getNmsSkullVehicle() {
		return nmsSkullVehicle;
	}


	@Override
	public String toString() {
		return "CraftTextLine [text=" + text + "]";
	}
	
}
