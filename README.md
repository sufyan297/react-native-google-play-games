# react-native-google-play-games

Android-only React Native TurboModule for Google Play Games Services.

This package focuses on Google Play Games single sign-on and achievements for React Native `0.80+` with the New Architecture only.

## Included in v0.2.0

- `isAuthenticated()`
- `signIn()`
- `signOut()`
- `getPlayer()`
- `unlockAchievement()`
- `incrementAchievement()`
- `showAchievements()`

Note: Google Play Games Services v2 on Android no longer exposes a native sign-out API. In this library, `signOut()` is kept in the JS surface for API stability, but currently rejects with `E_SIGN_OUT_UNSUPPORTED`.

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
unlockAchievement(achievementId: string): Promise<void>;
incrementAchievement(achievementId: string, steps?: number): Promise<void>;
showAchievements(): Promise<void>;
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
- If you want Play Games to work on an Android Emulator, add your app's debug keystore SHA-1 to the Android credential in Google Play Console / Google Play Games Services.
- The Google account used on the device must be a tester for the Play Games project when required.
- The app must be installed in a way that matches your Play Games configuration.

Without that setup, the module code can be correct and sign-in will still fail.

### Required Android `APP_ID` setup

This package also requires your app's Google Play Games `APP_ID` to be added to your Android app configuration. Without this, sign-in may fail even when the rest of the Play Games setup looks correct.

Add this to your app's `AndroidManifest.xml` inside the `<application>` tag:

```xml
<meta-data
  android:name="com.google.android.gms.games.APP_ID"
  android:value="@string/app_id" />
```

Then define the value in `android/app/src/main/res/values/strings.xml`:

```xml
<string name="app_id">4xxxxxxxxxxx</string>
```

You can find this `APP_ID` after connecting your game to a Google Cloud project:

`Google Play Console -> Select your game -> Grow Users -> Play Games Services -> Setup and management -> Configuration`

### Emulator and debug keystore note

If you're testing on an emulator or a debug build, make sure the SHA-1 from your debug keystore is added to the Android credential used by Google Play Games Services. In practice, this is often the missing step when Play Games works in production configuration but not in local development.

The Android credential can be found in Google Play Console under your Play Games Services configuration. If the package name matches but the SHA-1 does not, authentication will still fail.

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

## Achievements usage

```ts
import GooglePlayGames from 'react-native-google-play-games';

export async function unlockFirstWin() {
  await GooglePlayGames.unlockAchievement('CgkIxxxxxxxxEAIQAQ');
}

export async function addProgress() {
  await GooglePlayGames.incrementAchievement('CgkIxxxxxxxxEAIQAg', 1);
}

export async function openAchievementsScreen() {
  await GooglePlayGames.showAchievements();
}
```

## Achievements setup requirements

- Define your achievements in Google Play Console before calling these APIs.
- Use the Play Games achievement IDs from your Console configuration.
- `incrementAchievement()` should only be used with incremental achievements.
- `showAchievements()` opens the native Google Play Games achievements UI.

## Example: `PlayGamesContext.tsx`

If you want to keep Play Games auth state available across your app, you can wrap the module in a React context like this:

```tsx
import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react';
import {Platform} from 'react-native';
import GooglePlayGames, {
  type GooglePlayGamesPlayer,
} from 'react-native-google-play-games';

type PlayGamesContextType = {
  isSupported: boolean;
  isInitializing: boolean;
  isAuthenticated: boolean;
  player: GooglePlayGamesPlayer | null;
  error: string | null;
  refreshSession: () => Promise<void>;
  signIn: () => Promise<GooglePlayGamesPlayer | null>;
  signOut: () => Promise<void>;
};

const PlayGamesContext = createContext<PlayGamesContextType | undefined>(
  undefined,
);

export const PlayGamesProvider: React.FC<{children: React.ReactNode}> = ({
  children,
}) => {
  const isSupported = Platform.OS === 'android';
  const [isInitializing, setIsInitializing] = useState(isSupported);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [player, setPlayer] = useState<GooglePlayGamesPlayer | null>(null);
  const [error, setError] = useState<string | null>(null);

  const refreshSession = useCallback(async () => {
    if (!isSupported) {
      setIsInitializing(false);
      setIsAuthenticated(false);
      setPlayer(null);
      setError(null);
      return;
    }

    try {
      setError(null);
      const authenticated = await GooglePlayGames.isAuthenticated();
      setIsAuthenticated(authenticated);

      if (!authenticated) {
        setPlayer(null);
        return;
      }

      const currentPlayer = await GooglePlayGames.getPlayer();
      setPlayer(currentPlayer);
    } catch (sessionError) {
      console.log('Failed to refresh Google Play Games session:', sessionError);
      setIsAuthenticated(false);
      setPlayer(null);
      setError(
        sessionError instanceof Error
          ? sessionError.message
          : 'Failed to refresh Play Games session.',
      );
    } finally {
      setIsInitializing(false);
    }
  }, [isSupported]);

  useEffect(() => {
    refreshSession();
  }, [refreshSession]);

  const signIn = useCallback(async () => {
    if (!isSupported) {
      return null;
    }

    try {
      setError(null);
      const signedInPlayer = await GooglePlayGames.signIn();
      setPlayer(signedInPlayer);
      setIsAuthenticated(true);
      return signedInPlayer;
    } catch (signInError) {
      console.log('Google Play Games sign-in failed:', signInError);
      setIsAuthenticated(false);
      setPlayer(null);
      setError(
        signInError instanceof Error
          ? signInError.message
          : 'Google Play Games sign-in failed.',
      );
      return null;
    }
  }, [isSupported]);

  const signOut = useCallback(async () => {
    if (!isSupported) {
      return;
    }

    try {
      setError(null);
      await GooglePlayGames.signOut();
      setIsAuthenticated(false);
      setPlayer(null);
    } catch (signOutError) {
      console.log('Google Play Games sign-out failed:', signOutError);
      setError(
        signOutError instanceof Error
          ? signOutError.message
          : 'Google Play Games sign-out failed.',
      );
    }
  }, [isSupported]);

  const value = useMemo(
    () => ({
      isSupported,
      isInitializing,
      isAuthenticated,
      player,
      error,
      refreshSession,
      signIn,
      signOut,
    }),
    [
      error,
      isAuthenticated,
      isInitializing,
      isSupported,
      player,
      refreshSession,
      signIn,
      signOut,
    ],
  );

  return (
    <PlayGamesContext.Provider value={value}>
      {children}
    </PlayGamesContext.Provider>
  );
};

export const usePlayGames = () => {
  const context = useContext(PlayGamesContext);

  if (!context) {
    throw new Error('usePlayGames must be used within a PlayGamesProvider');
  }

  return context;
};
```

Wrap your app with the provider:

```tsx
<PlayGamesProvider>
  <YourApp />
</PlayGamesProvider>
```

## Status

Planned next for this package:

- Achievements APIs
