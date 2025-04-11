package com.berttowne.modgpt;

import com.berttowne.modgpt.injection.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class ModGPT extends JavaPlugin implements InjectionRoot {

    @Override
    public void onLoad() {
        AppInjector.registerInjectionRoot(this);
        AppInjector.registerRootModule(new InjectionModule(this));
    }

    @Override
    public synchronized void onEnable() {
        AppInjector.boot();

        this.getConfig().options().copyDefaults(true);
        this.saveConfig();

        // Boot AutoServices
        GuiceServiceLoader.load(Service.class, getClassLoader()).forEach(Service::onEnable);
        GuiceServiceLoader.load(Listener.class, getClassLoader()).forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));
        GuiceServiceLoader.load(Service.class, getClassLoader()).forEach(Service::postEnable);
    }

    @Override
    public void onDisable() {
        GuiceServiceLoader.load(Service.class, getClassLoader()).forEach(Service::onDisable);
    }

}