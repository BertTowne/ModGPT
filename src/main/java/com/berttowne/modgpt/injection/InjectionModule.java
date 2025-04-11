package com.berttowne.modgpt.injection;

import com.berttowne.modgpt.ModGPT;
import com.berttowne.modgpt.config.ConfigService;
import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.bukkit.Bukkit;
import org.bukkit.Server;

public class InjectionModule extends AbstractModule {

    private final ModGPT plugin;

    public InjectionModule(ModGPT plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        AppInjector.getServices(Module.class).forEach(this::install);

        bind(ModGPT.class).toInstance(this.plugin);
        bind(Server.class).toInstance(Bukkit.getServer());
        bind(Gson.class).toInstance(new Gson());
    }

}