package com.berttowne.modgpt.config;

import com.berttowne.modgpt.ModGPT;
import com.berttowne.modgpt.injection.Service;
import com.berttowne.modgpt.utils.ChatPolicy;
import com.google.auto.service.AutoService;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

@Singleton
@AutoService(Service.class)
public class ConfigService implements Service {

    private final ModGPT plugin;
    private final List<ChatPolicy> policies;

    private boolean requireReview;
    private boolean showUsernamesInReview;
    private boolean muteWhileReviewing;
    private int reviewTimeout;

    @Inject
    public ConfigService(ModGPT plugin) {
        this.plugin = plugin;
        this.policies = Lists.newArrayList();
    }

    @Override
    public void onEnable() {
        if (!getOpenAIKey().startsWith("sk-")) {
            plugin.getLogger().warning("** OpenAI API key is not set or invalid! ModGPT will not start. **");
            plugin.getLogger().info("You can create a personal OpenAI API key at https://platform.openai.com/api-keys");
            plugin.getLogger().info("For the purposes of this plugin, you will not be charged for the usage of this key.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        FileConfiguration config = plugin.getConfig();

        this.requireReview = config.getBoolean("settings.reviewing.enabled", true);
        this.showUsernamesInReview = config.getBoolean("settings.reviewing.show-usernames", true);
        this.muteWhileReviewing = config.getBoolean("settings.reviewing.mute-while-reviewing", true);
        this.reviewTimeout = config.getInt("settings.reviewing.review-timeout", 30);

        if (reviewTimeout == -1) {
            this.reviewTimeout = Integer.MAX_VALUE;
            this.muteWhileReviewing = false; // we're not going to mute forever
        }

        for (ChatPolicy.Type type : ChatPolicy.Type.values()) {
            ConfigurationSection section = config.getConfigurationSection("policies." + type.getSerializedName().replaceAll("/", "-"));

            if (section == null) continue;

            policies.add(new ChatPolicy(type, section.getBoolean("filter", true), section.getDouble("threshold", -1), section.getStringList("auto-run")));
        }
    }

    public String getOpenAIKey() {
        return plugin.getConfig().getString("openai-key", "");
    }

    public ChatPolicy getPolicy(ChatPolicy.Type type) {
        return policies.stream().filter(policy -> policy.type() == type).findFirst().orElse(null);
    }

    public boolean requireReviews() {
        return requireReview;
    }

    public boolean showUsernamesInReview() {
        return showUsernamesInReview;
    }

    public int getReviewTimeout() {
        return reviewTimeout;
    }

    public boolean muteWhileReviewing() {
        return muteWhileReviewing;
    }

}