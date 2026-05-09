# Jirani User Manual

## Overview
Jirani is an offline-first PeaceTech Android app for mediation, agreement records, anonymous safety reporting, and local network coordination.

The app is designed to avoid mandatory personal identity. Do not enter phone numbers, email addresses, government IDs, or precise private locations into mediation, agreement, or safety report fields.

## Main Navigation
Use the bottom navigation bar to move between the four main areas:

- Mediation: chat-style de-escalation guidance and tone checking.
- Vault: local agreement records and sync status.
- Safety: anonymous threat reporting with fuzzy location guidance.
- Network: local peer scan status and sync queue information.

## Top Bar
The top bar provides quick security and coordination actions:

- Calculator icon: opens the calculator decoy immediately.
- Settings icon: opens security preferences, including the discreet calculator return code.
- Theme icon: switches between light and dark mode.
- Outgoing data icon: opens Network for sharing/sync status.
- Incoming data icon: opens Vault for local records.
- Mesh status icon: opens Network and indicates peer status.

## Calculator Decoy
The calculator decoy is a privacy feature. It hides Jirani behind a normal-looking calculator screen.

When the decoy is opened, Jirani clears the app navigation back stack so pressing Android Back does not reveal sensitive screens.

To leave the decoy and return to Jirani, enter your discreet code into the calculator.

Default code:

```text
2468=
```

The decoy does not show a visible exit button.

## Changing The Discreet Code
To set your own calculator return code:

1. Open Jirani.
2. Tap the Settings icon in the top bar.
3. Enter a new code in the New code field.
4. Enter the same code in the Confirm code field.
5. Tap Save Discreet Code.

Code rules:

- Use 3-8 digits followed by `=`.
- Example: `1357=`
- Avoid obvious codes like `1234=`.
- Remember your code before using the decoy.

After saving, use the new code in the calculator decoy to return to Jirani.

## Security Notes
The current prototype stores the discreet code in local app state during the app session. It is not logged or sent to a server.

For production use, the discreet code should be stored with encrypted local storage and protected by the same offline-first privacy model as other sensitive Jirani settings.

## Safe Use Guidance
- Use the calculator decoy when you need to quickly hide sensitive mediation, reporting, or agreement information.
- Do not share your discreet code with untrusted people.
- Do not use personally identifying information in reports or agreement drafts.
- Use general locations and approximate time windows for safety reports.
- Verify safety information through trusted local channels before broad sharing.
