package com.berttowne.modgpt.openai.api;

public class ModerationRequest {

    private final String input;
    private final String model;

    public ModerationRequest(String input, String model) {
        this.input = input;
        this.model = model;
    }

    public String getInput() {
        return input;
    }

    public String getModel() {
        return model;
    }

}