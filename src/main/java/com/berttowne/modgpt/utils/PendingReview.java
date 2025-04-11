package com.berttowne.modgpt.utils;

import java.util.UUID;

/**
 * Holds data for a pending chat review.
 *
 * @param playerUUID    The UUID of the player under review
 * @param chatMsg       The message that was flagged for review
 * @param timeSent      The time the flagged message was sent
 * @param violation     The type of chat policy that was potentially violated in the flagged message
 * @param confidence    The AI's confidence that the message violated the chat policy
 */
public record PendingReview(UUID playerUUID, String chatMsg, long timeSent, ChatPolicy violation, double confidence) {
}