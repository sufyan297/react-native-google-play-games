import {Platform} from 'react-native';

import NativeGooglePlayGames from './NativeGooglePlayGames';
import type {GooglePlayGamesPlayer} from './NativeGooglePlayGames';

function ensureAndroid(): void {
  if (Platform.OS !== 'android') {
    throw new Error(
      'react-native-google-play-games is available on Android only.',
    );
  }
}

export type {GooglePlayGamesPlayer};

export async function isAuthenticated(): Promise<boolean> {
  ensureAndroid();
  return NativeGooglePlayGames.isAuthenticated();
}

export async function signIn(): Promise<GooglePlayGamesPlayer> {
  ensureAndroid();
  return NativeGooglePlayGames.signIn();
}

export async function signOut(): Promise<void> {
  ensureAndroid();
  return NativeGooglePlayGames.signOut();
}

export async function getPlayer(): Promise<GooglePlayGamesPlayer | null> {
  ensureAndroid();
  return NativeGooglePlayGames.getPlayer();
}

export async function unlockAchievement(achievementId: string): Promise<void> {
  ensureAndroid();
  return NativeGooglePlayGames.unlockAchievement(achievementId);
}

export async function incrementAchievement(
  achievementId: string,
  steps = 1,
): Promise<void> {
  ensureAndroid();
  return NativeGooglePlayGames.incrementAchievement(achievementId, steps);
}

export async function showAchievements(): Promise<void> {
  ensureAndroid();
  return NativeGooglePlayGames.showAchievements();
}

export default {
  isAuthenticated,
  signIn,
  signOut,
  getPlayer,
  unlockAchievement,
  incrementAchievement,
  showAchievements,
};
