import type {TurboModule} from 'react-native';
import {TurboModuleRegistry} from 'react-native';

export type GooglePlayGamesPlayer = Readonly<{
  id: string;
  displayName: string;
  title: string | null;
  iconImageUrl: string | null;
  hiResImageUrl: string | null;
}>;

export interface Spec extends TurboModule {
  isAuthenticated(): Promise<boolean>;
  signIn(): Promise<GooglePlayGamesPlayer>;
  signOut(): Promise<void>;
  getPlayer(): Promise<GooglePlayGamesPlayer | null>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('GooglePlayGames');
