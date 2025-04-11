package com.berttowne.modgpt.injection;

public interface InjectionRoot {

    // Attempt to auto handle injection for plugins.
    default void onLoad() {
        AppInjector.registerInjectionRoot(this);
    }

}
