package i.mrhua269.zutilsplugin;

import i.mrhua269.zutils.api.ZAPIEntryPoint;
import org.bukkit.plugin.java.JavaPlugin;

public final class ZUtils extends JavaPlugin {

    @Override
    public void onEnable() {
        ZAPIEntryPoint.init();
    }

    @Override
    public void onDisable() {

    }
}
