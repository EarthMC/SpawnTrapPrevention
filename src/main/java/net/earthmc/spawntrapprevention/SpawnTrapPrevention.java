package net.earthmc.spawntrapprevention;

import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.event.damage.TownBlockPVPTestEvent;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class SpawnTrapPrevention extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onTownBlockTestPVP(TownBlockPVPTestEvent event) {
        if (isCloseToNationSpawn(event.getTownBlock().getWorldCoord()) && !SiegeWarDistanceUtil.isLocationInActiveSiegeZone(toLocation(event.getTownBlock().getWorldCoord())))
            event.setPvp(false);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (isCloseToNationSpawn(event.getEntity().getLocation()) && !SiegeWarDistanceUtil.isLocationInActiveSiegeZone(event.getEntity().getLocation())) {
            event.setKeepInventory(true);
            event.setKeepLevel(true);
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }

    private static boolean isCloseToNationSpawn(Location location) {
        return isCloseToNationSpawn(WorldCoord.parseWorldCoord(location));
    }

    private static boolean isCloseToNationSpawn(WorldCoord worldCoord) {
        for (WorldCoord coord : selectArea(worldCoord)) {
            Town town = coord.getTownOrNull();
            try {
                if (town != null && town.isCapital() && WorldCoord.parseWorldCoord(town.getNationOrNull().getSpawn()).equals(coord))
                    return true;
            } catch (TownyException ignored) {}
        }
        return false;
    }

    private static Set<WorldCoord> selectArea(WorldCoord centre) {
        Set<WorldCoord> coords = new HashSet<>();

        coords.add(centre.add(-1, -1));
        coords.add(centre.add(-1, 0));
        coords.add(centre.add(-1, 1));
        coords.add(centre.add(0, -1));
        coords.add(centre);
        coords.add(centre.add(0, 1));
        coords.add(centre.add(1, -1));
        coords.add(centre.add(1, 0));
        coords.add(centre.add(1, 1));

        return coords;
    }

    private static Location toLocation(WorldCoord coord) {
        int tbSize = TownySettings.getTownBlockSize();
        return new Location(coord.getBukkitWorld(), coord.getX() * tbSize, 64, coord.getZ() * tbSize);
    }
}
