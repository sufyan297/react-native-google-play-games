import type {TurboModule} from 'react-native';
import {TurboModuleRegistry} from 'react-native';

export type GooglePlayGamesPlayer = Readonly<{
  id: string;
  displayName: string;
  title: string | null;
  iconImageUrl: string | null;
  hiResImageUrl: string | null;
}>;

export type GooglePlayGamesLeaderboardScore = Readonly<{
  leaderboardId: string;
  rawScore: number;
  formattedScore: string;
  rank: string | null;
  tag: string | null;
}>;

export interface Spec extends TurboModule {
  isAuthenticated(): Promise<boolean>;
  signIn(): Promise<GooglePlayGamesPlayer>;
  signOut(): Promise<void>;
  getPlayer(): Promise<GooglePlayGamesPlayer | null>;
  unlockAchievement(achievementId: string): Promise<void>;
  incrementAchievement(achievementId: string, steps?: number): Promise<void>;
  showAchievements(): Promise<void>;
  submitScore(leaderboardId: string, score: number): Promise<void>;
  showLeaderboard(leaderboardId: string): Promise<void>;
  showAllLeaderboards(): Promise<void>;
  loadCurrentPlayerScore(
    leaderboardId: string,
  ): Promise<GooglePlayGamesLeaderboardScore | null>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('GooglePlayGames');
