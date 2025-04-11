package com.berttowne.modgpt.utils;

import java.util.List;

/**
 * Each of the categories of chat violations that OpenAI currently detects and what to
 * do which them.
 *
 * @param type      The type of the chat violation category.
 * @param filtered  Whether chat violations of this type are to be filtered from public view.
 * @param threshold The minimum score a chat violation of this type needs to be filtered.
 * @param autoRun   A set of commands to automatically execute when this policy is violated.
 */
public record ChatPolicy(Type type, boolean filtered, double threshold, List<String> autoRun) {

    public enum Type {

        HATE("hate", "Hate",
                "Content that expresses, incites, or promotes hate based on race, gender, ethnicity, religion, " +
                        "nationality, sexual orientation, disability status, or caste. Hateful content aimed at " +
                        "non-protected groups (e.g. chess players) is harassment."),

        HATE_THREATENING("hate/threatening", "Threatening Hate",
                "Hateful content that also includes violence or serious harm towards the targeted group based on race, " +
                        "gender, ethnicity, religion, nationality, sexual orientation, disability status, or caste."),

        HARASSMENT("harassment", "Harassment",
                "Content that expresses, incites, or promotes harassing language towards any target."),

        HARASSMENT_THREATENING("harassment/threatening", "Threatening Harassment",
                "Harassment content that also includes violence or serious harm towards any target."),

        SELF_HARM("self-harm", "Self-Harm",
                "Content that promotes, encourages, or depicts acts of self-harm, such as suicide, cutting, and eating disorders."),

        SELF_HARM_INTENT("self-harm/intent", "Self-Harm Intent",
                "Content where the speaker expresses that they are engaging or intend to engage in acts of self-harm, " +
                        "such as suicide, cutting, and eating disorders."),

        SELF_HARM_INSTRUCTIONS("self-harm/instructions", "Self-Harm Instructions",
                "Content that encourages performing acts of self-harm, such as suicide, cutting, and eating disorders, " +
                        "or that gives instructions or advice on how to commit such acts."),

        SEXUAL("sexual", "Sexual Content",
                "Content meant to arouse sexual excitement, such as the description of sexual activity, or that promotes" +
                        " sexual services (excluding sex education and wellness)."),

        SEXUAL_MINORS("sexual/minors", "Sexual Content Involving Minors",
                "Sexual content that includes an individual who is under 18 years old."),

        VIOLENCE("violence", "Violence",
                "Content that depicts death, violence, or physical injury."),

        GRAPHIC("violence/graphic", "Graphic Violence",
                "Content that depicts death, violence, or physical injury in graphic detail."),

        ILLICIT("illicit", "Illicit Content",
                "Content that gives advice or instruction on how to commit illicit acts. A phrase like \"how to " +
                        "shoplift\" would fit this category."),

        ILLICIT_VIOLENT("illicit/violent", "Violent Illicit Content",
                "The same types of content flagged by the illicit category, but also includes references to violence " +
                        "or procuring a weapon.");

        private final String serializedName;
        private final String displayName;
        private final String description;

        Type(String serializedName, String displayName, String description) {
            this.serializedName = serializedName;
            this.displayName = displayName;
            this.description = description;
        }

        public String getSerializedName() {
            return serializedName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Gets the minimum score a chat violation needs to be filtered.
     *
     * @return the minimum score a chat violation needs to be filtered.
     */
    public double getThreshold() {
        return Math.max(-1, Math.min(1, threshold));
    }

}