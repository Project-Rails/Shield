/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  PluginReference.RainbowUtils
 */
package Shield;

import PluginReference.ChatColor;
import PluginReference.MC_Location;
import PluginReference.MC_Player;
import PluginReference.MC_Server;
import PluginReference.RainbowUtils;
import Shield.MyPlugin;
import Shield.ZoneFlags;
import Shield.ZoneInfo;
import Shield.ZoneMatch;
import Shield._JoeUtils;
import Shield._SerializableLocation;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ZoneManager {
    public static ConcurrentHashMap<String, ZoneInfo> mapZoneData = new ConcurrentHashMap();
    private static ConcurrentHashMap<String, MC_Location> mapSelection1 = new ConcurrentHashMap();
    private static ConcurrentHashMap<String, MC_Location> mapSelection2 = new ConcurrentHashMap();
    public static String MyDirectory = "plugins_mod" + File.separator + "RainbowZones" + File.separator;
    public static String Filename = String.valueOf(MyDirectory) + "RZoneData.dat";

    public static void SaveData() {
        try {
            _JoeUtils.EnsureDirectory(MyDirectory);
            long msStart = System.currentTimeMillis();
            File file = new File(Filename);
            FileOutputStream f = new FileOutputStream(file);
            ObjectOutputStream s = new ObjectOutputStream(new BufferedOutputStream(f));
            s.writeObject(mapZoneData);
            s.close();
            long msEnd = System.currentTimeMillis();
            String msg = String.valueOf(ChatColor.YELLOW) + String.format("%-20s: %5d zones.       Took %3d ms", "Zones", mapZoneData.size(), msEnd - msStart);
            _JoeUtils.ConsoleMsg(msg);
        }
        catch (Throwable exc) {
            _JoeUtils.ConsoleMsg("ZoneData Save Problem: " + exc.toString());
        }
    }

    public static void LoadData() {
        try {
            _JoeUtils.EnsureDirectory(MyDirectory);
            File file = new File(Filename);
            FileInputStream f = new FileInputStream(file);
            ObjectInputStream s = new ObjectInputStream(new BufferedInputStream(f));
            mapZoneData = (ConcurrentHashMap)s.readObject();
            s.close();
        }
        catch (Throwable exc) {
            _JoeUtils.ConsoleMsg("No Zone file: " + Filename);
            _JoeUtils.ConsoleMsg("Assuming first run, will start fresh...");
            mapZoneData = new ConcurrentHashMap();
        }
    }

    public static void Toggle(MC_Player p, String zoneName, String flagName) {
        ZoneInfo zone = ZoneManager.GetZone(zoneName);
        if (zone == null) {
            p.sendMessage(String.valueOf(ChatColor.RED) + "No zone named " + ChatColor.YELLOW + zoneName);
            return;
        }
        if ((flagName = flagName.toLowerCase()).equalsIgnoreCase("Damage") || flagName.equalsIgnoreCase("Dmg")) {
            zone.flags.set(ZoneFlags.ALLOW_PVP.ordinal(), !zone.flags.get(ZoneFlags.ALLOW_PVP.ordinal()));
        } else if (flagName.equalsIgnoreCase("Grief") || flagName.equalsIgnoreCase("Greif")) {
            zone.flags.set(ZoneFlags.ALLOW_BREAK.ordinal(), !zone.flags.get(ZoneFlags.ALLOW_BREAK.ordinal()));
        } else if (flagName.equalsIgnoreCase("Flow")) {
            zone.flags.set(ZoneFlags.ALLOW_FLOW.ordinal(), !zone.flags.get(ZoneFlags.ALLOW_FLOW.ordinal()));
        } else if (flagName.equalsIgnoreCase("Mobs")) {
            zone.flags.set(ZoneFlags.ALLOW_MOB_SPAWN.ordinal(), !zone.flags.get(ZoneFlags.ALLOW_MOB_SPAWN.ordinal()));
        } else {
            p.sendMessage(String.valueOf(ChatColor.RED) + "Unknown zone flag");
            return;
        }
        p.sendMessage(String.valueOf(ChatColor.GREEN) + "Adjusted Setting: " + ChatColor.AQUA + flagName);
        p.sendMessage(zone.toString());
        ZoneManager.PutZone(zoneName, zone);
    }

    public static ZoneInfo GetZone(String argName) {
        String key = argName.toLowerCase();
        ZoneInfo zone = mapZoneData.get(key);
        return zone;
    }

    public static void PutZone(String argName, ZoneInfo zone) {
        double tmp;
        if (zone.sloc.x > zone.eloc.x) {
            tmp = zone.sloc.x;
            zone.sloc.x = zone.eloc.x;
            zone.eloc.x = tmp;
        }
        if (zone.sloc.z > zone.eloc.z) {
            tmp = zone.sloc.z;
            zone.sloc.z = zone.eloc.z;
            zone.eloc.z = tmp;
        }
        if (zone.sloc.y > zone.eloc.y) {
            tmp = zone.sloc.y;
            zone.sloc.y = zone.eloc.y;
            zone.eloc.y = tmp;
        }
        String key = argName.toLowerCase();
        mapZoneData.put(key, zone);
        ZoneManager.SaveData();
    }

    public static void SetStartPosition(MC_Player p, MC_Location loc) {
        mapSelection1.put(p.getName(), loc);
        p.sendMessage(String.valueOf(ChatColor.GREEN) + "OK! " + ChatColor.GOLD + "Selection 1: " + ChatColor.WHITE + loc.toString());
    }

    public static void SetEndPosition(MC_Player p, MC_Location loc) {
        mapSelection2.put(p.getName(), loc);
        p.sendMessage(String.valueOf(ChatColor.GREEN) + "OK! " + ChatColor.GOLD + "Selection 2: " + ChatColor.WHITE + loc.toString());
    }

    public static void CreateZone(MC_Player p, String zoneName) {
        ZoneInfo zone = ZoneManager.GetZone(zoneName);
        if (zone != null) {
            p.sendMessage(String.valueOf(ChatColor.RED) + "Zone name already exists: " + ChatColor.YELLOW + zoneName);
            return;
        }
        MC_Location loc1 = mapSelection1.get(p.getName());
        if (loc1 == null) {
            p.sendMessage(String.valueOf(ChatColor.RED) + "No 1st selection found.");
            return;
        }
        MC_Location loc2 = mapSelection2.get(p.getName());
        if (loc2 == null) {
            p.sendMessage(String.valueOf(ChatColor.RED) + "No 2nd selection found.");
            return;
        }
        zone = new ZoneInfo(zoneName, p);
        zone.sloc = new _SerializableLocation(loc1);
        zone.eloc = new _SerializableLocation(loc2);
        ZoneManager.PutZone(zoneName, zone);
        p.sendMessage(String.valueOf(ChatColor.GREEN) + "Created Zone: " + ChatColor.GOLD + zone.toString());
    }

    public static void ListZones(MC_Player p, int page) {
        int pageSize = 10;
        if (page < 1) {
            page = 1;
        }
        ArrayList keys = new ArrayList(mapZoneData.keySet());
        Collections.sort(keys);
        int nZones = keys.size();
        int numPages = (nZones - 1) / pageSize + 1;
        if (page > numPages) {
            page = numPages;
        }
        int startIdx = (page - 1) * pageSize;
        int endIdx = startIdx + pageSize - 1;
        p.sendMessage(" ");
        p.sendMessage(String.valueOf(ChatColor.LIGHT_PURPLE) + ChatColor.BOLD + "Zones " + ChatColor.AQUA + ChatColor.ITALIC + String.format("Page %d of %d.", page, numPages));
        int idx = 0;
        int finalIdx = startIdx;
        for (Object key : keys) {
            if (idx >= startIdx && idx <= endIdx) {
                finalIdx = idx;
                ZoneInfo zone = mapZoneData.get(key);
                p.sendMessage(String.valueOf(ChatColor.GOLD) + zone.toString());
            }
            ++idx;
        }
        p.sendMessage(String.valueOf(ChatColor.GREEN) + String.format("Listed %d-%d of %d zones.", startIdx + 1, finalIdx + 1, nZones));
    }

    public static void DeleteZone(MC_Player p, String zoneName) {
        ZoneInfo zone = ZoneManager.GetZone(zoneName);
        if (zone == null) {
            p.sendMessage(String.valueOf(ChatColor.RED) + "No zone named " + ChatColor.YELLOW + zoneName);
            return;
        }
        mapZoneData.remove(zoneName.toLowerCase());
        p.sendMessage(String.valueOf(ChatColor.GREEN) + "Deleted Zone: " + ChatColor.YELLOW + zone.name);
    }

    public static ZoneInfo GetZoneAt(MC_Location loc) {
        return ZoneManager.GetZoneAt((int)loc.x, (int)loc.y, (int)loc.z, loc.dimension, true);
    }

    public static ZoneInfo GetZoneAt(int x, int y, int z, int dimen, boolean checkY) {
        for (Map.Entry<String, ZoneInfo> entry : mapZoneData.entrySet()) {
            ZoneInfo zone = entry.getValue();
            if (zone.sloc.dimension != dimen || (double)x < zone.sloc.x || (double)x > zone.eloc.x || (double)z < zone.sloc.z || (double)z > zone.eloc.z || checkY && ((double)y < zone.sloc.y || (double)y > zone.eloc.y)) continue;
            return zone;
        }
        return null;
    }

    public static ZoneMatch GetNearestZone(MC_Location loc) {
        ZoneMatch match = new ZoneMatch();
        match.dist = 8.988465674311579E307;
        match.zone = null;
        for (Map.Entry<String, ZoneInfo> entry : mapZoneData.entrySet()) {
            ZoneInfo zone = entry.getValue();
            if (zone.sloc.dimension != loc.dimension) continue;
            double dx = 0.0;
            if (loc.x < zone.sloc.x) {
                dx = zone.sloc.x - loc.x;
            } else if (loc.x > zone.eloc.x) {
                dx = loc.x - zone.eloc.x;
            }
            double dy = 0.0;
            if (loc.y < zone.sloc.y) {
                dy = zone.sloc.y - loc.y;
            } else if (loc.y > zone.eloc.y) {
                dy = loc.y - zone.eloc.y;
            }
            double dz = 0.0;
            if (loc.z < zone.sloc.z) {
                dz = zone.sloc.z - loc.z;
            } else if (loc.z > zone.eloc.z) {
                dz = loc.z - zone.eloc.z;
            }
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist >= match.dist) continue;
            match.zone = zone;
            match.dist = dist;
            if (match.dist > 1.0E-5) continue;
            return match;
        }
        if (match.zone == null) {
            return null;
        }
        return match;
    }

    public static void ShowZoneInfo(MC_Player p, ZoneInfo zone) {
        p.sendMessage(zone.toString());
        String ownerName = MyPlugin.server.getLastKnownPlayerNameFromUUID(zone.ownerUUID);
        if (ownerName == null) {
            ownerName = "Unknown";
        }
        String strMembers = "None";
        int labelLen = 13;
        p.sendMessage(String.valueOf(RainbowUtils.TextLabel((String)new StringBuilder(String.valueOf(ChatColor.GOLD)).append("Owner:").toString(), (int)labelLen)) + ChatColor.GREEN + ownerName);
        if (zone.memberUUIDs != null && zone.memberUUIDs.size() > 0) {
            ArrayList<String> members = new ArrayList<String>();
            for (String uid : zone.memberUUIDs.keySet()) {
                String memberName = MyPlugin.server.getLastKnownPlayerNameFromUUID(uid);
                if (memberName == null) {
                    memberName = "UID_" + uid.substring(0, 4) + "..." + uid.substring(32);
                }
                members.add(memberName);
            }
            strMembers = RainbowUtils.GetCommaList(members);
        }
        p.sendMessage(String.valueOf(RainbowUtils.TextLabel((String)new StringBuilder(String.valueOf(ChatColor.GOLD)).append("Members:").toString(), (int)labelLen)) + ChatColor.YELLOW + strMembers);
    }

    public static void ShowZoneInfo(MC_Player p, MC_Location loc) {
        ZoneInfo zone = ZoneManager.GetZoneAt(loc);
        if (zone == null) {
            p.sendMessage(String.valueOf(ChatColor.DARK_AQUA) + "No zone at " + ChatColor.WHITE + loc.toString());
            return;
        }
        ZoneManager.ShowZoneInfo(p, zone);
    }

    public static void ShowZoneInfo(MC_Player p, String zoneName) {
        ZoneInfo zone = ZoneManager.GetZone(zoneName);
        if (zone == null) {
            p.sendMessage(String.valueOf(ChatColor.RED) + "No zone named: " + ChatColor.YELLOW + zoneName);
            return;
        }
        ZoneManager.ShowZoneInfo(p, zone);
    }

    public static boolean IsOwnerOfZone(MC_Player p, ZoneInfo zone) {
        if (p == null) {
            return false;
        }
        if (p.hasPermission(MyPlugin.AdminPerm)) {
            return true;
        }
        String uid = p.getUUID().toString();
        if (zone.ownerUUID.equalsIgnoreCase(uid)) {
            return true;
        }
        return false;
    }

    public static boolean IsOwnerOrMember(MC_Player p, MC_Location loc) {
        if (p == null) {
            return false;
        }
        ZoneInfo zone = ZoneManager.GetZoneAt(loc);
        if (zone == null) {
            return true;
        }
        return ZoneManager.IsOwnerOrMember(p, zone);
    }

    public static boolean IsOwnerOrMember(MC_Player p, ZoneInfo zone) {
        if (p == null) {
            return false;
        }
        if (p.hasPermission(MyPlugin.AdminPerm)) {
            return true;
        }
        String uid = p.getUUID().toString();
        if (zone.ownerUUID.equalsIgnoreCase(uid)) {
            return true;
        }
        if (zone.memberUUIDs != null && zone.memberUUIDs.containsKey(uid)) {
            return true;
        }
        return false;
    }

    public static boolean CanBreakAt(MC_Player p, ZoneInfo zone) {
        if (zone == null) {
            return true;
        }
        if (p != null && ZoneManager.IsOwnerOrMember(p, zone)) {
            return true;
        }
        return zone.flags.get(ZoneFlags.ALLOW_BREAK.ordinal());
    }

    public static boolean CanBreakAt(MC_Player p, MC_Location loc) {
        ZoneInfo zone = ZoneManager.GetZoneAt(loc);
        if (zone == null) {
            return true;
        }
        if (p != null && ZoneManager.IsOwnerOrMember(p, zone)) {
            return true;
        }
        return zone.flags.get(ZoneFlags.ALLOW_BREAK.ordinal());
    }

    public static boolean CanByDamagedAt(MC_Player p, ZoneInfo zone) {
        return zone.flags.get(ZoneFlags.ALLOW_PVP.ordinal());
    }

    public static boolean CanFlowAt(ZoneInfo zone) {
        if (zone == null) {
            return true;
        }
        return zone.flags.get(ZoneFlags.ALLOW_FLOW.ordinal());
    }

    public static void HandleAddMember(MC_Player p, String zoneName, String tgtName) {
        ZoneInfo zone = ZoneManager.GetZone(zoneName);
        if (zone == null) {
            p.sendMessage(String.valueOf(ChatColor.RED) + "No zone named: " + ChatColor.YELLOW + zoneName);
            return;
        }
        if (!ZoneManager.IsOwnerOfZone(p, zone)) {
            p.sendMessage(String.valueOf(ChatColor.RED) + "You don't have owner rights to zone: " + ChatColor.YELLOW + zoneName);
            return;
        }
        String exactName = MyPlugin.server.getPlayerExactName(tgtName);
        String uid = MyPlugin.server.getPlayerUUIDFromName(exactName);
        if (uid == null) {
            p.sendMessage(String.valueOf(ChatColor.RED) + "That player has never logged in: " + ChatColor.YELLOW + tgtName);
            return;
        }
        if (zone.memberUUIDs != null && zone.memberUUIDs.containsKey(uid)) {
            p.sendMessage(String.valueOf(ChatColor.RED) + "That player is already a member of zone: " + ChatColor.YELLOW + zoneName);
            return;
        }
        if (zone.memberUUIDs == null) {
            zone.memberUUIDs = new ConcurrentHashMap();
        }
        zone.memberUUIDs.put(uid, true);
        ZoneManager.PutZone(zoneName, zone);
        p.sendMessage(String.valueOf(ChatColor.GREEN) + "Added " + ChatColor.YELLOW + exactName + ChatColor.GREEN + " to zone " + ChatColor.GOLD + zoneName);
    }

    public static void HandleRemoveMember(MC_Player p, String zoneName, String tgtName) {
        ZoneInfo zone = ZoneManager.GetZone(zoneName);
        if (zone == null) {
            p.sendMessage(String.valueOf(ChatColor.RED) + "No zone named: " + ChatColor.YELLOW + zoneName);
            return;
        }
        if (!ZoneManager.IsOwnerOfZone(p, zone)) {
            p.sendMessage(String.valueOf(ChatColor.RED) + "You don't have owner rights to zone: " + ChatColor.YELLOW + zoneName);
            return;
        }
        String exactName = MyPlugin.server.getPlayerExactName(tgtName);
        String uid = MyPlugin.server.getPlayerUUIDFromName(exactName);
        if (uid == null) {
            p.sendMessage(String.valueOf(ChatColor.RED) + "That player has never logged in: " + ChatColor.YELLOW + tgtName);
            return;
        }
        if (zone.memberUUIDs == null || !zone.memberUUIDs.containsKey(uid)) {
            p.sendMessage(String.valueOf(ChatColor.RED) + "That player is not a member of zone: " + ChatColor.YELLOW + zoneName);
            return;
        }
        zone.memberUUIDs.remove(uid);
        if (zone.memberUUIDs.size() == 0) {
            zone.memberUUIDs = null;
        }
        ZoneManager.PutZone(zoneName, zone);
        p.sendMessage(String.valueOf(ChatColor.GREEN) + "Removed " + ChatColor.YELLOW + exactName + ChatColor.GREEN + " to zone " + ChatColor.GOLD + zoneName);
    }

    public static void HandleSetOwner(MC_Player p, String zoneName, String tgtName) {
        ZoneInfo zone = ZoneManager.GetZone(zoneName);
        if (zone == null) {
            p.sendMessage(String.valueOf(ChatColor.RED) + "No zone named: " + ChatColor.YELLOW + zoneName);
            return;
        }
        if (!ZoneManager.IsOwnerOfZone(p, zone)) {
            p.sendMessage(String.valueOf(ChatColor.RED) + "You don't have owner rights to zone: " + ChatColor.YELLOW + zoneName);
            return;
        }
        String exactName = MyPlugin.server.getPlayerExactName(tgtName);
        String uid = MyPlugin.server.getPlayerUUIDFromName(exactName);
        if (uid == null) {
            p.sendMessage(String.valueOf(ChatColor.RED) + "That player has never logged in: " + ChatColor.YELLOW + tgtName);
            return;
        }
        zone.ownerUUID = uid;
        ZoneManager.PutZone(zoneName, zone);
        p.sendMessage(String.valueOf(ChatColor.GREEN) + "Set " + ChatColor.YELLOW + exactName + ChatColor.GREEN + " as owner of zone " + ChatColor.GOLD + zoneName);
    }

    public static void SetMaxY(MC_Player p, String zoneName) {
        ZoneInfo zone = ZoneManager.GetZone(zoneName);
        if (zone == null) {
            p.sendMessage(String.valueOf(ChatColor.RED) + "No zone named " + ChatColor.YELLOW + zoneName);
            return;
        }
        zone.sloc.y = 0.0;
        zone.eloc.y = MyPlugin.server.getMaxBuildHeight();
        p.sendMessage(zone.toString());
        ZoneManager.PutZone(zoneName, zone);
    }
}

