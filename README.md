![](https://i.gyazo.com/e398029329ba363a5ef8360b9da2a2ec.png)

[![JitPack](https://jitpack.io/v/BertTowne/InlineHeads.svg)](https://jitpack.io/#BertTowne/ModGPT)
![GitHub repo size](https://img.shields.io/github/repo-size/BertTowne/ModGPT)
![GitHub issues](https://img.shields.io/github/issues-raw/BertTowne/ModGPT)
![CodeFactor grade](https://img.shields.io/codefactor/grade/github/BertTowne/ModGPT)
---

## Description

ModGPT utilizes the power of AI to prevent toxicity and harmful content in Minecraft servers before it even happens. By
using OpenAI's Moderation Endpoint, ModGPT analyzes player messages and determines if they are harmful or not. If the AI
determines that the message is harmful, it will automatically remove the message before it is even sent to chat.

AI isn't always perfect though, so ModGPT has you covered! Server administrators have the option to enable manual reviews
of messages that the AI flags as harmful. This way, staff members can still allow messages through the filter that the AI
may have mistakenly flagged.

---

## Requirements
- Java 21+
- Paper or Folia 1.21.4+ (normal Spigot is NOT supported)

---

## Getting Started

**WARNING:** This plugin REQUIRES a valid OpenAI API key to function. You can get one for free at [OpenAI](https://platform.openai.com/api-keys).
From there click " + Create new secret key", leave all settings as default, name it "ModGPT", click "Create secret key",
and paste the key into the `openai-key` field in the `config.yml` file.

1. Download the latest release from the [releases page]() and place it in your server's `plugins` folder.
2. Start your server and let the plugin generate the `config.yml` file. **Note that the plugin will not start without a valid OpenAI API key, so the plugin will not work the first time it is on the server.**
3. Open the `config.yml` file and paste your OpenAI API key into the `openai-key` field.
4. Restart your server, and you're good to go!

---

## Permissions

- `modgpt.bypass` - Allows players to bypass the AI filter and send messages that would otherwise be blocked.
- `modgpt.review` - Allows players to review messages that the AI has flagged as harmful. Intended for server moderators.
- `modgpt.seeusernames` - Allows players to see the usernames of players who have been flagged by the AI. Intended for 
server moderators who have the `modgpt.review` permission.

---

## Usage

After installing the plugin, ModGPT will automatically start filtering messages in chat. If a message is flagged as harmful,
the AI will automatically remove the message before it is sent to chat.

By default, filtered messages will be sent to staff
members with the `modgpt.review` permission for manual review. If the message is deemed safe, the staff member can allow the
message to be sent to chat. If the message is deemed to be against the server's rules, the staff member can choose to block
the message, which will then perform the commands specified in the `config.yml` file to automatically punish the player.

---

## Credits

This plugin was inspired by, and heavily based on, the [Openai-Moderation](https://github.com/Paneddo/Openai-Moderation) plugin by Paneddo.