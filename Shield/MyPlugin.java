package Shield;

import PluginReference.ChatColor;
import PluginReference.MC_Block;
import PluginReference.MC_DamageType;
import PluginReference.MC_DirectionNESWUD;
import PluginReference.MC_Entity;
import PluginReference.MC_EntityType;
import PluginReference.MC_EventInfo;
import PluginReference.MC_Hand;
import PluginReference.MC_ItemStack;
import PluginReference.MC_Location;
import PluginReference.MC_MiscGriefType;
import PluginReference.MC_Player;
import PluginReference.MC_Server;
import PluginReference.PluginBase;
import PluginReference.PluginInfo;
import PluginReference.RainbowUtils;
import Shield.ZoneFlags;
import Shield.ZoneInfo;
import Shield.ZoneManager;
import Shield.ZoneMatch;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MyPlugin
extends PluginBase {
    public static MC_Server server = null;
    public static String AdminPerm = "shield.admin";
    public static String UserPerm = "shield.user";
    public static String ListZonesPerm = "shield.list";
    public static String version = "17w16a";
    public static String PluginName = "Shield";

    @Override
    public void onStartup(MC_Server argServer) {
        System.out.println(String.valueOf(PluginName) + " " + version + " starting up...");
        server = argServer;
        ZoneManager.LoadData();
    }

    @Override
    public void onShutdown() {
        System.out.println(String.valueOf(PluginName) + " " + version + " shutting down...");
        ZoneManager.SaveData();
    }

    public PluginInfo getPluginInfo() {
        PluginInfo info = new PluginInfo();
        info.description = "Zone and region protection (" + version + ")";
        info.eventSortOrder = -1000.0;
        return info;
    }

    @Override
    public void onPlayerInput(MC_Player plr, String msg, MC_EventInfo ei) {
        boolean isUser;
        if (ei.isCancelled) {
            return;
        }
        String[] tokens = RainbowUtils.GetTokens((String)msg);
        if (!(tokens[0].equalsIgnoreCase("/shield") || tokens[0].equalsIgnoreCase("/zone") || tokens[0].equalsIgnoreCase("/z"))) {
            return;
        }
        ei.isCancelled = true;
        boolean isAdmin = plr.hasPermission(AdminPerm);
        boolean bl = isUser = plr.hasPermission(UserPerm) || isAdmin;
        if (!isUser) {
            plr.sendMessage(String.valueOf(ChatColor.GRAY) + "You don't have permission to this!");
            return;
        }
        if (tokens.length == 1) {
            this.ShowUsage(plr);
            return;
        }
        if (tokens.length >= 2 && tokens[1].equalsIgnoreCase("list") && (isAdmin || plr.hasPermission(ListZonesPerm))) {
            int pageNum = 1;
            try {
                String arg = tokens[2];
                if (arg.startsWith("page")) {
                    arg = arg.substring(4);
                }
                pageNum = Integer.parseInt(arg);
            }
            catch (Exception arg) {
                // empty catch block
            }
            ZoneManager.ListZones(plr, pageNum);
            return;
        }
        if (tokens.length == 3 && tokens[1].equalsIgnoreCase("create") && isAdmin) {
            ZoneManager.CreateZone(plr, tokens[2]);
            return;
        }
        if (tokens.length == 3 && tokens[1].equalsIgnoreCase("maxy") && isAdmin) {
            ZoneManager.SetMaxY(plr, tokens[2]);
            return;
        }
        if (tokens.length == 3 && tokens[1].equalsIgnoreCase("delete") && isAdmin) {
            ZoneManager.DeleteZone(plr, tokens[2]);
            return;
        }
        if (tokens.length == 3 && tokens[1].equalsIgnoreCase("info") && isUser) {
            ZoneManager.ShowZoneInfo(plr, tokens[2]);
            return;
        }
        if (isUser && tokens.length == 4 && (tokens[1].equalsIgnoreCase("toggle") || tokens[1].equalsIgnoreCase("flag"))) {
            ZoneManager.Toggle(plr, tokens[2], tokens[3]);
            return;
        }
        if (isUser && tokens.length == 4 && (tokens[1].equalsIgnoreCase("add") || tokens[1].equalsIgnoreCase("addmember"))) {
            ZoneManager.HandleAddMember(plr, tokens[3], tokens[2]);
            return;
        }
        if (isUser && tokens.length == 4 && (tokens[1].equalsIgnoreCase("remove") || tokens[1].equalsIgnoreCase("removemember"))) {
            ZoneManager.HandleRemoveMember(plr, tokens[3], tokens[2]);
            return;
        }
        if (isAdmin && tokens.length == 4 && (tokens[1].equalsIgnoreCase("setowner") || tokens[1].equalsIgnoreCase("owner"))) {
            ZoneManager.HandleSetOwner(plr, tokens[3], tokens[2]);
            return;
        }
        this.ShowUsage(plr);
    }

    public void ShowUsage(MC_Player plr) {
        plr.sendMessage(" ");
        plr.sendMessage(RainbowUtils.RainbowString((String)("\u2581\u2582\u2583\u2585\u2587\u2588  Shield " + version + " \u2588\u2587\u2585\u2583\u2582\u2581"), (String)"b"));
        plr.sendMessage(RainbowUtils.RainbowString((String)"-----------------------------------", (String)"b"));
        int padLen = 28;
        boolean isAdmin = plr.hasPermission(AdminPerm);
        if (isAdmin || plr.hasPermission(ListZonesPerm)) {
            plr.sendMessage(String.valueOf(ChatColor.LIGHT_PURPLE) + RainbowUtils.TextLabel((String)new StringBuilder("/zone ").append(ChatColor.AQUA).append("list [page#]").toString(), (int)padLen) + ChatColor.WHITE + " - List zones");
        }
        if (isAdmin) {
            plr.sendMessage(String.valueOf(ChatColor.LIGHT_PURPLE) + RainbowUtils.TextLabel((String)new StringBuilder("/zone ").append(ChatColor.AQUA).append("create ZoneName").toString(), (int)padLen) + ChatColor.WHITE + " - Create a zone");
            plr.sendMessage(String.valueOf(ChatColor.LIGHT_PURPLE) + RainbowUtils.TextLabel((String)new StringBuilder("/zone ").append(ChatColor.AQUA).append("delete ZoneName").toString(), (int)padLen) + ChatColor.WHITE + " - Delete a zone");
            plr.sendMessage(String.valueOf(ChatColor.LIGHT_PURPLE) + RainbowUtils.TextLabel((String)new StringBuilder("/zone ").append(ChatColor.AQUA).append("setowner Name ZoneName").toString(), (int)padLen) + ChatColor.WHITE + " - Set owner");
            plr.sendMessage(String.valueOf(ChatColor.LIGHT_PURPLE) + RainbowUtils.TextLabel((String)new StringBuilder("/zone ").append(ChatColor.AQUA).append("maxy ZoneName").toString(), (int)padLen) + ChatColor.WHITE + " - Expand Vertically");
        }
        plr.sendMessage(String.valueOf(ChatColor.GOLD) + RainbowUtils.TextLabel((String)new StringBuilder("/zone ").append(ChatColor.AQUA).append("info ZoneName").toString(), (int)padLen) + ChatColor.WHITE + " - Get zone info");
        plr.sendMessage(String.valueOf(ChatColor.GOLD) + RainbowUtils.TextLabel((String)new StringBuilder("/zone ").append(ChatColor.AQUA).append("add Name ZoneName").toString(), (int)padLen) + ChatColor.WHITE + " - Add member");
        plr.sendMessage(String.valueOf(ChatColor.GOLD) + RainbowUtils.TextLabel((String)new StringBuilder("/zone ").append(ChatColor.AQUA).append("remove Name ZoneName").toString(), (int)padLen) + ChatColor.WHITE + " - Remove member");
        plr.sendMessage(String.valueOf(ChatColor.GOLD) + RainbowUtils.TextLabel((String)new StringBuilder("/zone ").append(ChatColor.AQUA).append("toggle ZoneName Flag").toString(), (int)padLen) + ChatColor.WHITE + " - Flags: " + ChatColor.GRAY + "Dmg,Grief,Flow,Mobs");
        if (!isAdmin) {
            ArrayList<String> zoneNamesOwned = new ArrayList<String>();
            String uid = plr.getUUID().toString();
            for (String key : ZoneManager.mapZoneData.keySet()) {
                ZoneInfo zone = ZoneManager.mapZoneData.get(key);
                if (zone.ownerUUID == null || !zone.ownerUUID.equalsIgnoreCase(uid)) continue;
                zoneNamesOwned.add(zone.name);
            }
            if (zoneNamesOwned.size() <= 0) {
                plr.sendMessage(String.valueOf(ChatColor.RED) + "You are not the owner of any zones.");
            } else {
                plr.sendMessage(String.valueOf(ChatColor.GREEN) + "Zones Owned: " + ChatColor.YELLOW + RainbowUtils.GetCommaList(zoneNamesOwned));
            }
        }
    }

    @Override
    public void onAttemptBlockBreak(MC_Player plr, MC_Location loc, MC_EventInfo ei) {
        if (plr.hasPermission(AdminPerm)) {
            MC_ItemStack is = plr.getItemInHand();
            int handID = is.getId();
            if (handID == 269) {
                ei.isCancelled = true;
                ZoneManager.SetStartPosition(plr, loc);
                return;
            }
        } else {
            if (ei.isCancelled) {
                return;
            }
            if (!ZoneManager.CanBreakAt(plr, loc)) {
                plr.sendMessage(String.valueOf(ChatColor.RED) + "That's in a protected zone.");
                ei.isCancelled = true;
                return;
            }
        }
    }

    @Override
    public void onAttemptPlaceOrInteract(MC_Player plr, MC_Location location, MC_DirectionNESWUD dir, MC_Hand hand, MC_EventInfo ei) {
        if (plr.hasPermission(AdminPerm)) {
            MC_ItemStack is = plr.getItemInHand();
            int handID = is.getId();
            if (is.getId() == 269) {
                ei.isCancelled = true;
                ZoneManager.SetEndPosition(plr, location);
                return;
            }
            if (handID == 268) {
                ei.isCancelled = true;
                ZoneManager.ShowZoneInfo(plr, location);
                return;
            }
        } else {
            if (ei.isCancelled) {
                return;
            }
            MC_Location loc = location.getLocationAtDirection(dir);
            ZoneMatch match = ZoneManager.GetNearestZone(loc);
            if (match == null || match.dist > 1.0) {
                return;
            }
            if (loc.y >= 255.0) {
                ei.isCancelled = true;
                return;
            }
            if (match.dist <= 1.0E-4 && !ZoneManager.CanBreakAt(plr, match.zone)) {
                ei.isCancelled = true;
                if (dir != MC_DirectionNESWUD.UNSPECIFIED) {
                    plr.sendMessage(String.valueOf(ChatColor.RED) + "That's in a protected zone.");
                }
                return;
            }
        }
    }

    @Override
    public void onAttemptEntityDamage(MC_Entity ent, MC_DamageType dmgType, double amt, MC_EventInfo ei) {
        if (ei.isCancelled) {
            return;
        }
        if (ent == null) {
            return;
        }
        if (dmgType == MC_DamageType.OUT_OF_WORLD) {
            return;
        }
        if (ent instanceof MC_Player) {
            MC_Player plr = (MC_Player)ent;
            MC_Location loc = plr.getLocation();
            ZoneMatch zoneMatch = ZoneManager.GetNearestZone(loc);
            if (zoneMatch == null) {
                return;
            }
            if (zoneMatch.dist >= 1.25) {
                return;
            }
            if (!ZoneManager.CanByDamagedAt(plr, zoneMatch.zone)) {
                ei.isCancelled = true;
                return;
            }
        } else if (this.IsPassive(ent)) {
            ZoneInfo zone;
            MC_Entity damager = ent.getAttacker();
            MC_Player plr = null;
            if (damager != null && damager instanceof MC_Player) {
                plr = (MC_Player)damager;
            }
            if ((zone = ZoneManager.GetZoneAt(ent.getLocation())) == null) {
                return;
            }
            if (!ZoneManager.CanBreakAt(plr, zone)) {
                if (plr != null) {
                    plr.sendMessage(String.valueOf(ChatColor.RED) + "That's in a protected zone.");
                }
                ei.isCancelled = true;
                return;
            }
        }
    }

    public boolean onAttemptExplodeSpecific(MC_Entity ent, List<MC_Location> locs) {
        boolean res = false;
        int i = locs.size() - 1;
        while (i >= 0) {
            MC_Location loc = locs.get(i);
            if (!ZoneManager.CanBreakAt(null, loc)) {
                res = true;
                locs.remove(i);
            }
            --i;
        }
        return res;
    }

    @Override
    public void onAttemptExplosion(MC_Location loc, MC_EventInfo ei) {
    }

    public void onAttemptPistonAction(MC_Location loc, MC_DirectionNESWUD dir, MC_EventInfo ei) {
        if (ei.isCancelled) {
            return;
        }
        int radius = 12;
        ZoneMatch zoneMatch = ZoneManager.GetNearestZone(loc);
        if (zoneMatch == null) {
            return;
        }
        if (zoneMatch.dist <= 1.0E-4) {
            return;
        }
        if (zoneMatch.dist > (double)radius) {
            return;
        }
        if (!ei.isCancelled && !ZoneManager.CanBreakAt(null, zoneMatch.zone)) {
            ei.isCancelled = true;
            return;
        }
    }

    public void onAttemptBlockFlow(MC_Location loc, MC_Block blk, MC_EventInfo ei) {
        if (ei.isCancelled) {
            return;
        }
        ZoneMatch zoneMatch = ZoneManager.GetNearestZone(loc);
        if (zoneMatch == null) {
            return;
        }
        if (zoneMatch.dist > 0.001) {
            return;
        }
        if (!ZoneManager.CanFlowAt(zoneMatch.zone)) {
            ei.isCancelled = true;
            return;
        }
    }

    public boolean IsPassive(MC_Entity ent) {
        MC_EntityType t = ent.getType();
        if (t == MC_EntityType.BOAT) {
            return true;
        }
        if (t == MC_EntityType.MINECART) {
            return true;
        }
        if (t == MC_EntityType.PIG) {
            return true;
        }
        if (t == MC_EntityType.CHICKEN) {
            return true;
        }
        if (t == MC_EntityType.HORSE) {
            return true;
        }
        if (t == MC_EntityType.ARMOR_STAND) {
            return true;
        }
        if (t == MC_EntityType.WOLF) {
            return true;
        }
        if (t == MC_EntityType.HANGING) {
            return true;
        }
        if (t == MC_EntityType.COW) {
            return true;
        }
        if (t == MC_EntityType.MUSHROOM_COW) {
            return true;
        }
        if (t == MC_EntityType.RABBIT) {
            return true;
        }
        if (t == MC_EntityType.SHEEP) {
            return true;
        }
        if (t == MC_EntityType.VILLAGER) {
            return true;
        }
        if (t == MC_EntityType.SNOWMAN) {
            return true;
        }
        return false;
    }

    public boolean IsNonMobEntity(MC_Entity ent) {
        MC_EntityType t = ent.getType();
        if (t == MC_EntityType.ARMOR_STAND) {
            return true;
        }
        if (t == MC_EntityType.HANGING) {
            return true;
        }
        if (t == MC_EntityType.BOAT) {
            return true;
        }
        if (t == MC_EntityType.MINECART) {
            return true;
        }
        if (t == MC_EntityType.ENDER_CRYSTAL) {
            return true;
        }
        if (t == MC_EntityType.EYE_OF_ENDER_SIGNAL) {
            return true;
        }
        if (t == MC_EntityType.FALLING_SAND) {
            return true;
        }
        if (t == MC_EntityType.FIREWORK) {
            return true;
        }
        if (t == MC_EntityType.FISHING_HOOK) {
            return true;
        }
        if (t == MC_EntityType.ITEM) {
            return true;
        }
        if (t == MC_EntityType.PLAYER) {
            return true;
        }
        if (t == MC_EntityType.SNOWBALL) {
            return true;
        }
        if (t == MC_EntityType.THROWN_EGG) {
            return true;
        }
        if (t == MC_EntityType.THROWN_ENDERPEARL) {
            return true;
        }
        if (t == MC_EntityType.THROWN_EXP_BOTTLE) {
            return true;
        }
        if (t == MC_EntityType.THROWN_POTION) {
            return true;
        }
        if (t == MC_EntityType.PRIMED_TNT) {
            return true;
        }
        if (t == MC_EntityType.ARROW) {
            return true;
        }
        if (t == MC_EntityType.SMALL_FIREBALL) {
            return true;
        }
        if (t == MC_EntityType.FIREBALL) {
            return true;
        }
        return false;
    }

    @Override
    public void onAttemptAttackEntity(MC_Player plr, MC_Entity ent, MC_EventInfo ei) {
        if (ent == null) {
            return;
        }
        if (ei.isCancelled) {
            return;
        }
        if (!this.IsPassive(ent)) {
            return;
        }
        MC_Location loc = plr.getLocation();
        ZoneMatch zoneMatch = ZoneManager.GetNearestZone(loc);
        if (zoneMatch == null) {
            return;
        }
        if (zoneMatch.dist >= 1.25) {
            return;
        }
        if (!ZoneManager.CanBreakAt(plr, zoneMatch.zone)) {
            plr.sendMessage(String.valueOf(ChatColor.RED) + "That's in a protected zone.");
            ei.isCancelled = true;
            return;
        }
    }

    public void onAttemptCropTrample(MC_Entity ent, MC_Location loc, MC_EventInfo ei) {
        if (ent == null) {
            return;
        }
        if (!(ent instanceof MC_Player)) {
            return;
        }
        MC_Player plr = (MC_Player)ent;
        ZoneMatch zoneMatch = ZoneManager.GetNearestZone(loc);
        if (zoneMatch == null) {
            return;
        }
        if (zoneMatch.dist >= 1.25) {
            return;
        }
        if (!ZoneManager.CanBreakAt(plr, zoneMatch.zone)) {
            plr.sendMessage(String.valueOf(ChatColor.RED) + "That's in a protected zone.");
            ei.isCancelled = true;
            return;
        }
    }

    public void onAttemptEntitySpawn(MC_Entity ent, MC_EventInfo ei) {
        ZoneInfo zone = ZoneManager.GetZoneAt(ent.getLocation());
        if (zone == null) {
            return;
        }
        if (this.IsNonMobEntity(ent)) {
            return;
        }
        boolean allowSpawn = zone.flags.get(ZoneFlags.ALLOW_MOB_SPAWN.ordinal());
        if (!allowSpawn) {
            ei.isCancelled = true;
        }
    }

    public void onAttemptEntityMiscGrief(MC_Entity ent, MC_Location loc, MC_MiscGriefType griefType, MC_EventInfo ei) {
        if (!ZoneManager.CanBreakAt(null, loc)) {
            ei.isCancelled = true;
            return;
        }
    }
}

