# react-native-google-play-games

Android-only React Native TurboModule for Google Play Games Services.

This v1 package focuses on Google Play Games single sign-on for React Native `0.80+` with the New Architecture only.

## Included in v1

- `isAuthenticated()`
- `signIn()`
- `signOut()`
- `getPlayer()`

## API

```ts
export type GooglePlayGamesPlayer = {
  id: string;
  displayName: string;
  title: string | null;
  iconImageUrl: string | null;
  hiResImageUrl: string | null;
};

isAuthenticated(): Promise<boolean>;
signIn(): Promise<GooglePlayGamesPlayer>;
signOut(): Promise<void>;
getPlayer(): Promise<GooglePlayGamesPlayer | null>;
```

## Installation

```bash
npm install react-native-google-play-games
```

This library is Android-only and expects:

- React Native `0.80+`
- New Architecture enabled
- An Android app that is already configured for Google Play Games Services

## Native Android notes

The library auto-initializes `PlayGamesSdk` through an Android `ContentProvider`, so consuming apps do not need to manually call `PlayGamesSdk.initialize(...)`.

The Android namespace used by this library is:

```text
com.reactnativegoogleplaygames
```

The TurboModule is registered internally as:

```text
GooglePlayGames
```

## Google Play Games setup requirements

For sign-in to succeed, the consuming Android app still needs proper Play Games configuration in Google Play Console and Google Cloud:

- Your game must be created in Play Console with Play Games Services enabled.
- The Android package name and SHA-1 certificate fingerprint must match the linked Android credential.
- The Google account used on the device must be a tester for the Play Games project when required.
- The app must be installed in a way that matches your Play Games configuration.

Without that setup, the module code can be correct and sign-in will still fail.

## Usage

```ts
import GooglePlayGames from 'react-native-google-play-games';

export async function ensurePlayGamesPlayer() {
  const alreadyAuthenticated = await GooglePlayGames.isAuthenticated();

  if (!alreadyAuthenticated) {
    return GooglePlayGames.signIn();
  }

  const player = await GooglePlayGames.getPlayer();

  if (!player) {
    throw new Error('Player was not available after authentication.');
  }

  return player;
}
```

## Status

Planned next for this package:

- Achievements APIs
