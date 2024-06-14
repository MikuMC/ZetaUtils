package i.mrhua269.zutils.shared;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class Utils {
    @NotNull
    public static String getServerNMSVersion(){
        return "v" + Bukkit.getServer().getMinecraftVersion().replace(".","_");
    }
}
