package net.devscape.project.pvplog.utils;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Objects;

public class RegionUtils {


    public static boolean isInRegion(Player player, String regionId) {
        WorldGuardPlugin worldGuardPlugin = WorldGuardPlugin.inst();
        WorldGuard worldGuard = WorldGuard.getInstance();

        Location playerLocation = player.getLocation();
        LocalPlayer localPlayer = worldGuardPlugin.wrapPlayer(player);

        BlockVector3 blockVector3 = BlockVector3.at(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());

        ApplicableRegionSet regionSet = Objects.requireNonNull(worldGuard.getPlatform().getRegionContainer().get(localPlayer.getWorld())).getApplicableRegions(blockVector3);

        for (ProtectedRegion region : regionSet) {
            if (region.getId().equalsIgnoreCase(regionId)) {
                return true;
            }
        }

        return false;
    }
}
