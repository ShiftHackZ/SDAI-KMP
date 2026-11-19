# Nightly announcements

The Android Full Nightly workflow publishes the FullDev APK from `develop`, then
announces it on Discord and in the comments of a Telegram channel post. Both
destinations use the same title, version, recent changes, links and disclaimer.
Discord uses an embed; Telegram uses HTML text with link previews disabled.
Announcements are sent only when a build is published, including manual runs with
`publish: true`. Skipped builds and runs with `publish: false` send nothing.

## Telegram setup

1. Create a dedicated bot through [@BotFather](https://t.me/BotFather) using
   `/newbot` and save its token.
2. Add the bot to the channel's **linked discussion group** and allow it to send
   text messages. Channel administrator access is unnecessary for posting group
   comments. Keep the existing nightly announcement and its comments enabled.
3. Obtain the discussion group ID and the message ID of the automatically
   forwarded nightly announcement **inside that group**. The channel post ID is
   a different ID and must not be used.
4. Add the following settings in GitHub under **Settings → Environments →
   android-release**:

| Type     | Name                                   | Value                                                                   |
| -------- | -------------------------------------- | ----------------------------------------------------------------------- |
| Secret   | `TELEGRAM_NIGHTLY_BOT_TOKEN`           | Token issued by BotFather                                               |
| Variable | `TELEGRAM_NIGHTLY_CHAT_ID`             | Numeric discussion supergroup ID, including its `-100` prefix           |
| Variable | `TELEGRAM_NIGHTLY_REPLY_TO_MESSAGE_ID` | Positive message ID of the forwarded nightly announcement in that group |

Repository Actions secrets and variables with these names also work. Environment
settings take precedence over repository settings of the same type. The two IDs
can also be stored as secrets with the same names; a nonempty variable takes
precedence over the corresponding secret. The existing
`DISCORD_NIGHTLY_WEBHOOK_URL` secret continues to control Discord delivery.

### Finding the two IDs

For a new dedicated bot without a webhook or another update consumer:

1. Open the existing nightly post's comments and send
   `/nightly_setup@YourBotUsername` as a direct reply to the announcement. An
   explicitly addressed command is delivered even with bot privacy mode enabled.
2. In a local terminal, set `TELEGRAM_NIGHTLY_BOT_TOKEN` to the bot token, then read
   the incoming update:

   ```sh
   curl --fail --silent --show-error \
     "https://api.telegram.org/bot${TELEGRAM_NIGHTLY_BOT_TOKEN}/getUpdates" |
     jq '.result[] | select((.message.text? // "") | startswith("/nightly_setup@")) |
       {chat_id: .message.chat.id,
        reply_to_message_id: .message.reply_to_message.message_id,
        is_announcement: .message.reply_to_message.is_automatic_forward}'
   ```

3. Use `chat_id` and `reply_to_message_id` from the matching update, verifying
   `is_announcement` is `true`. Do not use the setup command's own message ID.
   The setup command can then be deleted. No bot server needs to remain running.

Telegram retains updates for at most 24 hours. If using a bot with an existing
webhook or polling service, obtain these fields from that service's received
update instead of competing with its update consumer.

See the Telegram documentation for [channel comments](https://core.telegram.org/api/discussion),
[incoming updates](https://core.telegram.org/bots/api#getupdates) and
[bot privacy](https://core.telegram.org/bots/faq#what-messages-will-my-bot-get).

## Activation and delivery

Merge the workflow change into `master`, the default branch used by scheduled
Actions. Its checkout still builds `develop`; synchronizing the two branches is
unnecessary for activation. Announcement preparation stays inline in the workflow
so it does not depend on helper scripts being present in the checked-out branch.

With all three Telegram settings absent, Telegram delivery is skipped. Partial
configuration, invalid numeric IDs, or rejected delivery fails the notification
step. A missing reply target causes an error instead of an unrelated group
message. Discord and Telegram delivery are attempted independently once the
shared announcement is prepared; a Discord failure does not prevent Telegram.

The next published nightly verifies delivery through the normal workflow. A
manual run with `publish: true` also sends announcements; `force: true` additionally
rebuilds even if no functional changes exist. The `android-release` environment's
branch restrictions still apply to manual runs.

Neither destination retries its POST automatically because a lost response can
mean the message was already delivered. Manually rerunning a publishing workflow
can post another announcement.
