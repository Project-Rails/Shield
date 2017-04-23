package Shield;

import PluginReference.ChatColor;
import PluginReference.MC_Location;
import PluginReference.MC_Player;
import PluginReference.RainbowUtils;
import Shield.ZoneFlags;
import Shield._SerializableLocation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ZoneInfo
implements Serializable {
    public _SerializableLocation sloc;
    public _SerializableLocation eloc;
    public String name;
    public long msCreated;
    public String ownerUUID;
    public ConcurrentHashMap<String, Boolean> memberUUIDs;
    int zOrder = 0;
    BitSet flags = null;

    public ZoneInfo(String argName, MC_Player p) {
        this.name = argName;
        this.ownerUUID = p.getUUID().toString();
        this.msCreated = System.currentTimeMillis();
        this.eloc = this.sloc = new _SerializableLocation(p.getLocation());
        this.flags = new BitSet();
        this.flags.set(ZoneFlags.ALLOW_BREAK.ordinal(), false);
        this.flags.set(ZoneFlags.ALLOW_FLOW.ordinal(), false);
        this.flags.set(ZoneFlags.ALLOW_INTERACT.ordinal(), false);
        this.flags.set(ZoneFlags.ALLOW_MOB_SPAWN.ordinal(), false);
        this.flags.set(ZoneFlags.ALLOW_PLACE.ordinal(), false);
        this.flags.set(ZoneFlags.ALLOW_PVP.ordinal(), false);
        this.flags.set(ZoneFlags.ALLOW_TELEPORT_IN.ordinal(), true);
        this.flags.set(ZoneFlags.ALLOW_TELEPORT_OUT.ordinal(), true);
        this.flags.set(ZoneFlags.ALLOW_FLY.ordinal(), true);
        this.flags.set(ZoneFlags.ALLOW_PISTONS.ordinal(), false);
    }

    public String toString() {
        String locStr = String.valueOf(this.sloc.toString()) + String.format(new StringBuilder(String.valueOf(ChatColor.DARK_GRAY)).append(" to ").append(ChatColor.GRAY).append("(%d,%d,%d)").toString(), (int)this.eloc.x, (int)this.eloc.y, (int)this.eloc.z);
        String str = String.valueOf(ChatColor.LIGHT_PURPLE) + RainbowUtils.TextLabel((String)this.name, (int)10) + ChatColor.WHITE + " " + locStr;
        ArrayList<String> arrFlags = new ArrayList<String>();
        if (this.flags.get(ZoneFlags.ALLOW_PVP.ordinal())) {
            arrFlags.add("Dmg");
        }
        if (this.flags.get(ZoneFlags.ALLOW_BREAK.ordinal())) {
            arrFlags.add("Grief");
        }
        if (this.flags.get(ZoneFlags.ALLOW_FLOW.ordinal())) {
            arrFlags.add("Flow");
        }
        if (this.flags.get(ZoneFlags.ALLOW_MOB_SPAWN.ordinal())) {
            arrFlags.add("Mobs");
        }
        if (arrFlags.size() > 0) {
            str = String.valueOf(str) + " " + ChatColor.AQUA + RainbowUtils.GetCommaList(arrFlags);
        }
        return str;
    }
}

