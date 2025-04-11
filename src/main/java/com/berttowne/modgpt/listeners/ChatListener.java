package com.berttowne.modgpt.listeners;

import com.berttowne.modgpt.ModGPT;
import com.berttowne.modgpt.config.ConfigService;
import com.berttowne.modgpt.openai.OpenAIService;
import com.berttowne.modgpt.openai.api.ModerationResponse;
import com.berttowne.modgpt.utils.ChatPolicy;
import com.berttowne.modgpt.utils.PendingReview;
import com.berttowne.modgpt.utils.Scheduler;
import com.google.auto.service.AutoService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.metadata.FixedMetadataValue;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Singleton
@AutoService(Listener.class)
public class ChatListener implements Listener {

    private static final Component PREFIX = MiniMessage.miniMessage().deserialize("<b><color:#01a3a4>[ModGPT]</color></b> ").color(TextColor.fromHexString("#00d2d3"));
    private static final Component PREFIX_ERROR = MiniMessage.miniMessage().deserialize("<b><color:#01a3a4>[ModGPT]</color></b> ").color(TextColor.fromHexString("#ee5253"));
    private static final Component STRIKETHROUGH = MiniMessage.miniMessage().deserialize("<st><gradient:#01a3a4:#00d2d3:#01a3a4>====================================================</gradient></st>");

    private final ModGPT plugin;
    private final ConfigService configService;
    private final OpenAIService openAiService;
    private final Cache<UUID, PendingReview> pendingReviews;

    @Inject
    public ChatListener(ModGPT plugin, @Nonnull ConfigService configService, OpenAIService openAiService) {
        this.plugin = plugin;
        this.configService = configService;
        this.openAiService = openAiService;
        this.pendingReviews = CacheBuilder.newBuilder()
                .concurrencyLevel(4)
                .expireAfterWrite(configService.getReviewTimeout(), TimeUnit.SECONDS)
                .build();
    }

    @SuppressWarnings("deprecation")
    public void sendFilteredMessage(@Nonnull Player player, @Nonnull ChatPolicy.Type violatedPolicy) {
        player.sendMessage(STRIKETHROUGH);
        player.sendMessage(Component.text("Your message was not sent, as it was detected to include ").color(NamedTextColor.WHITE)
                .append(Component.text("[" + violatedPolicy.getDescription() + "]").color(TextColor.fromHexString("#00d2d3"))
                        .hoverEvent(HoverEvent.showText(Component.text(WordUtils.wrap(violatedPolicy.getDescription(), 45)).color(NamedTextColor.GRAY)))));
        player.sendMessage(" ");
        player.sendMessage(Component.text("Automatic action has been taken against your account.").color(TextColor.fromHexString("#ee5253")));
        player.sendMessage(STRIKETHROUGH);
    }

    @SuppressWarnings("deprecation")
    public void sendReviewMessage(@Nonnull Player player, @Nonnull ChatPolicy.Type violatedPolicy) {
        player.sendMessage(STRIKETHROUGH);
        player.sendMessage(Component.text("Your message was not sent, as it was detected to include ").color(NamedTextColor.WHITE)
                .append(Component.text("[" + violatedPolicy.getDescription() + "]").color(TextColor.fromHexString("#00d2d3"))
                        .hoverEvent(HoverEvent.showText(Component.text(WordUtils.wrap(violatedPolicy.getDescription(), 45)).color(NamedTextColor.GRAY)))));
        player.sendMessage(" ");
        player.sendMessage(Component.text("Please be patient while a staff member reviews your message.").color(NamedTextColor.WHITE));
        if (configService.muteWhileReviewing()) {
            player.sendMessage(Component.text("You will not be able to chat until your message is reviewed.").color(TextColor.fromHexString("#ee5253")));
        }
        player.sendMessage(STRIKETHROUGH);
    }

    @SuppressWarnings("deprecation")
    public void sendAutoModConfirmation(@Nonnull Player staff, @Nonnull Player offender,
                                               @Nonnull ChatPolicy.Type policy, double confidence, @Nonnull UUID chatId,
                                               @Nonnull String chatMsg) {
        boolean showUsername = staff.hasPermission("modgpt.seeusernames") || configService.showUsernamesInReview();

        staff.sendMessage(STRIKETHROUGH);
        staff.sendMessage((showUsername ? offender.name() : Component.text("Someone")).color(NamedTextColor.WHITE)
                .append(Component.text(" sent a potentially harmful message:").color(TextColor.fromHexString("#00d2d3"))).decorate(TextDecoration.BOLD));
        staff.sendMessage(Component.text(chatMsg).color(NamedTextColor.WHITE));
        staff.sendMessage(" ");
        staff.sendMessage(Component.text("Violation Type: ").color(NamedTextColor.WHITE)
                .append(Component.text(policy.getDisplayName()).color(TextColor.fromHexString("#00d2d3"))
                        .hoverEvent(HoverEvent.showText(Component.text(WordUtils.wrap(policy.getDescription(), 45)).color(NamedTextColor.GRAY)))));
        staff.sendMessage(Component.text("Confidence: ").color(NamedTextColor.WHITE).append(
                Component.text(NumberFormat.getPercentInstance().format(confidence)).color(TextColor.fromHexString("#00d2d3"))));
        staff.sendMessage(" ");
        staff.sendMessage(Component.text("[Block Message]").color(NamedTextColor.RED).decorate(TextDecoration.BOLD)
                .hoverEvent(HoverEvent.showText(Component.text("Prevent this message from sending and punish the player.").color(NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.runCommand("/modgptdeny " + chatId))
                .append(Component.text(" [Allow Message]").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD)
                        .hoverEvent(HoverEvent.showText(Component.text("Allow this message to be sent.").color(NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.runCommand("/modgptconfirm " + chatId))));
        staff.sendMessage(STRIKETHROUGH);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(@Nonnull PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("modgpt.reviewer")) return;

        if (event.getMessage().toLowerCase().startsWith("/modgptdeny ")) {
            event.setCancelled(true);

            String[] args = event.getMessage().split(" ");

            if (args.length != 2) return;

            UUID chatId = UUID.fromString(args[1]);

            PendingReview pendingReview = pendingReviews.getIfPresent(chatId);

            if (pendingReview == null) {
                player.sendMessage(PREFIX_ERROR.append(Component.text("This review has already been processed.")));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                return;
            }

            pendingReviews.invalidate(chatId);
            Bukkit.getScheduler().runTask(plugin, () ->
                    pendingReview.violation().autoRun().forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("%p", Objects.requireNonNull(Bukkit.getOfflinePlayer(pendingReview.playerUUID()).getName())))));

            player.sendMessage(PREFIX.append(Component.text("Thank you for your review. This message will not be shown to chat, and the player has been punished.")));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);

            Player chatter = Bukkit.getPlayer(pendingReview.playerUUID());

            if (chatter == null || !chatter.isOnline()) return;

            chatter.removeMetadata("modgpt-pendingreview", plugin);

            return;
        }

        if (event.getMessage().toLowerCase().startsWith("/modgptconfirm ")) {
            event.setCancelled(true);

            String[] args = event.getMessage().split(" ");

            if (args.length != 2) return;

            UUID chatId = UUID.fromString(args[1]);

            PendingReview pendingReview = pendingReviews.getIfPresent(chatId);

            if (pendingReview == null) {
                player.sendMessage(PREFIX_ERROR.append(Component.text("This review has already been processed.")));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                return;
            }

            pendingReviews.invalidate(chatId);

            player.sendMessage(PREFIX.append(Component.text("Thank you for your review. An attempt to send the original message to chat will be made.")));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);

            Player chatter = Bukkit.getPlayer(pendingReview.playerUUID());

            if (chatter == null || !chatter.isOnline()) return;

            chatter.setMetadata("modgpt-confirmedreview", new FixedMetadataValue(plugin, pendingReview.chatMsg()));
            chatter.removeMetadata("modgpt-pendingreview", plugin);
            player.sendMessage(PREFIX.append(Component.text("Your review was processed and it was determined that no rules were violated. We apologize for any inconvenience.")));
            chatter.chat(pendingReview.chatMsg());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(@Nonnull AsyncChatEvent event) {
        Player player = event.getPlayer();

        if (player.hasMetadata("modgpt-pendingreview") && configService.muteWhileReviewing()) {
            event.setCancelled(true);

            player.sendMessage(PREFIX_ERROR.append(Component.text("You have a message that is under review. You may chat again once it gets processed.")));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);

            return;
        }

        if (player.hasMetadata("modgpt-confirmedreview")) {
            if (event.signedMessage().message().equals(player.getMetadata("modgpt-confirmedreview").get(0).asString())) {
                player.removeMetadata("modgpt-confirmedreview", plugin);
                return; // skip auto mod for a previously falsely filtered message
            }
        }

        // AUTO MOD CHAT FILTERING
        if (player.hasPermission("modgpt.bypass")) return;

        ModerationResponse.Results result;
        try {
            result = openAiService.moderateMessage(event.signedMessage().message()).getResults().get(0);

            if (result == null) {
                player.sendMessage(PREFIX_ERROR.append(Component.text("An error occurred while processing your message. Please try again.")));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                return;
            }

            System.out.println("Moderation result: " + result);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (!result.isFlagged()) return;

        event.setCancelled(true);

        ChatPolicy.Type violated = null;
        double confidence = 0.0;

        if ((configService.getPolicy(ChatPolicy.Type.SEXUAL_MINORS).getThreshold() == -1 && result.getCategories().isSexualMinors())
                || (configService.getPolicy(ChatPolicy.Type.SEXUAL_MINORS).getThreshold() >= result.getCategoryScores().getSexualMinors())) {
            violated = ChatPolicy.Type.SEXUAL_MINORS;
            confidence = result.getCategoryScores().getSexualMinors();
        }

        if (violated == null && (configService.getPolicy(ChatPolicy.Type.SELF_HARM_INTENT).getThreshold() == -1 && result.getCategories().isSelfHarmIntent())
                || (configService.getPolicy(ChatPolicy.Type.SELF_HARM_INTENT).getThreshold() >= result.getCategoryScores().getSelfHarmIntent())) {
            violated = ChatPolicy.Type.SELF_HARM_INTENT;
            confidence = result.getCategoryScores().getSelfHarmIntent();
        }

        if (violated == null && (configService.getPolicy(ChatPolicy.Type.SELF_HARM_INSTRUCTIONS).getThreshold() == -1 && result.getCategories().isSelfHarmInstructions())
                || (configService.getPolicy(ChatPolicy.Type.SELF_HARM_INSTRUCTIONS).getThreshold() >= result.getCategoryScores().getSelfHarmInstructions())) {
            violated = ChatPolicy.Type.SELF_HARM_INSTRUCTIONS;
            confidence = result.getCategoryScores().getSelfHarmInstructions();
        }

        if (violated == null && (configService.getPolicy(ChatPolicy.Type.SELF_HARM).getThreshold() == -1 && result.getCategories().isSelfHarm())
                || (configService.getPolicy(ChatPolicy.Type.SELF_HARM).getThreshold() >= result.getCategoryScores().getSelfHarm())) {
            violated = ChatPolicy.Type.SELF_HARM;
            confidence = result.getCategoryScores().getSelfHarm();
        }

        if (violated == null && (configService.getPolicy(ChatPolicy.Type.HATE_THREATENING).getThreshold() == -1 && result.getCategories().isHateThreatening())
                || (configService.getPolicy(ChatPolicy.Type.HATE_THREATENING).getThreshold() >= result.getCategoryScores().getHateThreatening())) {
            violated = ChatPolicy.Type.HATE_THREATENING;
            confidence = result.getCategoryScores().getHateThreatening();
        }

        if (violated == null && (configService.getPolicy(ChatPolicy.Type.HARASSMENT_THREATENING).getThreshold() == -1 && result.getCategories().isHarassmentThreatening())
                || (configService.getPolicy(ChatPolicy.Type.HARASSMENT_THREATENING).getThreshold() >= result.getCategoryScores().getHarassmentThreatening())) {
            violated = ChatPolicy.Type.HARASSMENT_THREATENING;
            confidence = result.getCategoryScores().getHarassmentThreatening();
        }

        if (violated == null && (configService.getPolicy(ChatPolicy.Type.GRAPHIC).getThreshold() == -1 && result.getCategories().isViolenceGraphic())
                || (configService.getPolicy(ChatPolicy.Type.GRAPHIC).getThreshold() >= result.getCategoryScores().getViolenceGraphic())) {
            violated = ChatPolicy.Type.GRAPHIC;
            confidence = result.getCategoryScores().getViolenceGraphic();
        }

        if (violated == null && (configService.getPolicy(ChatPolicy.Type.VIOLENCE).getThreshold() == -1 && result.getCategories().isViolence())
                || (configService.getPolicy(ChatPolicy.Type.VIOLENCE).getThreshold() >= result.getCategoryScores().getViolence())) {
            violated = ChatPolicy.Type.VIOLENCE;
            confidence = result.getCategoryScores().getViolence();
        }

        if (violated == null && (configService.getPolicy(ChatPolicy.Type.SEXUAL).getThreshold() == -1 && result.getCategories().isSexual())
                || (configService.getPolicy(ChatPolicy.Type.SEXUAL).getThreshold() >= result.getCategoryScores().getSexual())) {
            violated = ChatPolicy.Type.SEXUAL;
            confidence = result.getCategoryScores().getSexual();
        }

        if (violated == null && (configService.getPolicy(ChatPolicy.Type.HATE).getThreshold() == -1 && result.getCategories().isHate())
                || (configService.getPolicy(ChatPolicy.Type.HATE).getThreshold() >= result.getCategoryScores().getHate())) {
            violated = ChatPolicy.Type.HATE;
            confidence = result.getCategoryScores().getHate();
        }

        if (violated == null && (configService.getPolicy(ChatPolicy.Type.ILLICIT_VIOLENT).getThreshold() == -1 && result.getCategories().isIllicitViolent())
                || (configService.getPolicy(ChatPolicy.Type.ILLICIT_VIOLENT).getThreshold() >= result.getCategoryScores().getIllicitViolent())) {
            violated = ChatPolicy.Type.ILLICIT_VIOLENT;
            confidence = result.getCategoryScores().getIllicitViolent();
        }

        if (violated == null && (configService.getPolicy(ChatPolicy.Type.ILLICIT).getThreshold() == -1 && result.getCategories().isIllicit())
                || (configService.getPolicy(ChatPolicy.Type.ILLICIT).getThreshold() >= result.getCategoryScores().getIllicit())) {
            violated = ChatPolicy.Type.ILLICIT;
            confidence = result.getCategoryScores().getIllicit();
        }

        if (violated == null) {
            violated = ChatPolicy.Type.HATE;
        }

        ChatPolicy policy = configService.getPolicy(violated);

        if (configService.requireReviews()) {
            UUID chatId = UUID.randomUUID();
            sendReviewMessage(player, violated);

            pendingReviews.put(chatId, new PendingReview(player.getUniqueId(), event.signedMessage().message(), System.currentTimeMillis(), policy, confidence));
            player.setMetadata("modgpt-pendingreview", new FixedMetadataValue(plugin, true));

            Scheduler.later(player, () -> {
                if (!player.isOnline() || !player.hasMetadata("modgpt-pendingreview")) return;

                player.removeMetadata("modgpt-pendingreview", plugin);
                player.sendMessage(PREFIX.append(Component.text("Your review was not processed in time."
                        + (configService.muteWhileReviewing() ? " You may now chat again." : ""))));
            }, configService.getReviewTimeout() * 20L);

            ChatPolicy.Type finalViolated = violated;

            double finalConfidence = confidence;
            Bukkit.getOnlinePlayers().stream().filter(pl -> pl.hasPermission("modgpt.reviewer")).toList().forEach(staff ->
                    sendAutoModConfirmation(staff, player, finalViolated, finalConfidence, chatId, event.signedMessage().message()));

            return;
        }

        sendFilteredMessage(player, violated);

        Bukkit.getScheduler().runTask(plugin, () ->
                policy.autoRun().forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("%p", player.getName()))));
    }

}