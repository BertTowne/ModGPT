package com.berttowne.modgpt.injection;

public interface Service {

    default void onEnable() { }

    default void postEnable() { }

    default void onDisable() {}

}