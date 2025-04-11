package com.berttowne.modgpt.openai.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ModerationResponse {

    private String id;
    private String model;
    private List<Results> results;

    public String getId() {
        return id;
    }

    public String getModel() {
        return model;
    }

    public List<Results> getResults() {
        return results;
    }

    public static class Results {

        private boolean flagged;
        private Categories categories;
        @SerializedName("category_scores")
        private CategoryScores categoryScores;

        public boolean isFlagged() {
            return flagged;
        }

        public Categories getCategories() {
            return categories;
        }

        public CategoryScores getCategoryScores() {
            return categoryScores;
        }

    }

    public static class Categories {

        private boolean hate;
        @SerializedName("hate/threatening")
        private boolean hateThreatening;
        private boolean harassment;
        @SerializedName("harassment/threatening")
        private boolean harassmentThreatening;
        private boolean illicit;
        @SerializedName("illicit/violent")
        private boolean illicitViolent;
        @SerializedName("self-harm")
        private boolean selfHarm;
        @SerializedName("self-harm/intent")
        private boolean selfHarmIntent;
        @SerializedName("self-harm/instructions")
        private boolean selfHarmInstructions;
        private boolean sexual;
        @SerializedName("sexual/minors")
        private boolean sexualMinors;
        private boolean violence;
        @SerializedName("violence/graphic")
        private boolean violenceGraphic;

        public boolean isHate() {
            return hate;
        }

        public boolean isHateThreatening() {
            return hateThreatening;
        }

        public boolean isHarassment() {
            return harassment;
        }

        public boolean isHarassmentThreatening() {
            return harassmentThreatening;
        }

        public boolean isIllicit() {
            return illicit;
        }

        public boolean isIllicitViolent() {
            return illicitViolent;
        }

        public boolean isSelfHarm() {
            return selfHarm;
        }

        public boolean isSelfHarmIntent() {
            return selfHarmIntent;
        }

        public boolean isSelfHarmInstructions() {
            return selfHarmInstructions;
        }

        public boolean isSexual() {
            return sexual;
        }

        public boolean isSexualMinors() {
            return sexualMinors;
        }

        public boolean isViolence() {
            return violence;
        }

        public boolean isViolenceGraphic() {
            return violenceGraphic;
        }

    }

    public static class CategoryScores {

        private double hate;
        @SerializedName("hate/threatening")
        private double hateThreatening;
        private double harassment;
        @SerializedName("harassment/threatening")
        private double harassmentThreatening;
        private double illicit;
        @SerializedName("illicit/violent")
        private double illicitViolent;
        @SerializedName("self-harm")
        private double selfHarm;
        @SerializedName("self-harm/intent")
        private double selfHarmIntent;
        @SerializedName("self-harm/instructions")
        private double selfHarmInstructions;
        private double sexual;
        @SerializedName("sexual/minors")
        private double sexualMinors;
        private double violence;
        @SerializedName("violence/graphic")
        private double violenceGraphic;

        public double getHate() {
            return hate;
        }

        public double getHateThreatening() {
            return hateThreatening;
        }

        public double getHarassment() {
            return harassment;
        }

        public double getHarassmentThreatening() {
            return harassmentThreatening;
        }

        public double getIllicit() {
            return illicit;
        }

        public double getIllicitViolent() {
            return illicitViolent;
        }

        public double getSelfHarm() {
            return selfHarm;
        }

        public double getSelfHarmIntent() {
            return selfHarmIntent;
        }

        public double getSelfHarmInstructions() {
            return selfHarmInstructions;
        }

        public double getSexual() {
            return sexual;
        }

        public double getSexualMinors() {
            return sexualMinors;
        }

        public double getViolence() {
            return violence;
        }

        public double getViolenceGraphic() {
            return violenceGraphic;
        }

    }

}