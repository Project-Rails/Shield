package Shield;

import PluginReference.ChatColor;
import java.io.File;
import java.io.PrintStream;

public class _JoeUtils {
    public static void ConsoleMsg(String msg) {
        System.out.println("[RZone]: " + ChatColor.StripColor(msg));
    }

    public static void EnsureDirectory(String dirName) {
        File pDir = new File(dirName);
        if (pDir.isDirectory()) {
            return;
        }
        try {
            System.out.println("Creating directory: " + dirName);
            pDir.mkdir();
        }
        catch (Throwable exc) {
            System.out.println("EnsureDirectory " + dirName + ": " + exc.toString());
        }
    }
}

