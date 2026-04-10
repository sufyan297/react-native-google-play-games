package com.reactnativegoogleplaygames

import android.app.Activity
import android.content.Intent
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.UiThreadUtil
import com.facebook.react.module.annotations.ReactModule
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.Player

@ReactModule(name = GooglePlayGamesModule.NAME)
class GooglePlayGamesModule(reactContext: ReactApplicationContext) :
  NativeGooglePlayGamesSpec(reactContext) {

  override fun getName(): String = NAME

  override fun isAuthenticated(promise: Promise) {
    withActivity(promise) { activity ->
      PlayGames
        .getGamesSignInClient(activity)
        .isAuthenticated()
        .addOnSuccessListener { authResult ->
          promise.resolve(authResult.isAuthenticated)
        }
        .addOnFailureListener { error ->
          rejectPromise(promise, "E_IS_AUTHENTICATED_FAILED", error)
        }
    }
  }

  override fun signIn(promise: Promise) {
    withActivity(promise) { activity ->
      UiThreadUtil.runOnUiThread {
        PlayGames
          .getGamesSignInClient(activity)
          .signIn()
          .addOnSuccessListener { authResult ->
            if (!authResult.isAuthenticated) {
              promise.reject(
                "E_SIGN_IN_CANCELLED",
                "Google Play Games sign-in was cancelled or not completed.",
              )
              return@addOnSuccessListener
            }

            resolveCurrentPlayer(activity, promise)
          }
          .addOnFailureListener { error ->
            rejectPromise(promise, "E_SIGN_IN_FAILED", error)
          }
      }
    }
  }

  override fun signOut(promise: Promise) {
    promise.reject(
      "E_SIGN_OUT_UNSUPPORTED",
      "Google Play Games Services v2 does not expose a sign-out API on Android.",
    )
  }

  override fun getPlayer(promise: Promise) {
    withActivity(promise) { activity ->
      PlayGames
        .getGamesSignInClient(activity)
        .isAuthenticated()
        .addOnSuccessListener { authResult ->
          if (!authResult.isAuthenticated) {
            promise.resolve(null)
            return@addOnSuccessListener
          }

          resolveCurrentPlayer(activity, promise)
        }
        .addOnFailureListener { error ->
          rejectPromise(promise, "E_GET_PLAYER_FAILED", error)
        }
    }
  }

  override fun unlockAchievement(achievementId: String, promise: Promise) {
    if (achievementId.isBlank()) {
      promise.reject(
        "E_INVALID_ACHIEVEMENT_ID",
        "achievementId must be a non-empty string.",
      )
      return
    }

    withActivity(promise) { activity ->
      PlayGames
        .getAchievementsClient(activity)
        .unlock(achievementId)
        .addOnSuccessListener {
          promise.resolve(null)
        }
        .addOnFailureListener { error ->
          rejectPromise(promise, "E_UNLOCK_ACHIEVEMENT_FAILED", error)
        }
    }
  }

  override fun incrementAchievement(achievementId: String, steps: Double, promise: Promise) {
    if (achievementId.isBlank()) {
      promise.reject(
        "E_INVALID_ACHIEVEMENT_ID",
        "achievementId must be a non-empty string.",
      )
      return
    }

    val stepCount = steps.toInt()
    if (stepCount <= 0) {
      promise.reject(
        "E_INVALID_ACHIEVEMENT_STEPS",
        "steps must be greater than 0.",
      )
      return
    }

    withActivity(promise) { activity ->
      PlayGames
        .getAchievementsClient(activity)
        .increment(achievementId, stepCount)
      promise.resolve(null)
    }
  }

  override fun showAchievements(promise: Promise) {
    withActivity(promise) { activity ->
      UiThreadUtil.runOnUiThread {
        PlayGames
          .getAchievementsClient(activity)
          .achievementsIntent
          .addOnSuccessListener { intent: Intent ->
            activity.startActivityForResult(intent, RC_ACHIEVEMENTS_UI)
            promise.resolve(null)
          }
          .addOnFailureListener { error ->
            rejectPromise(promise, "E_SHOW_ACHIEVEMENTS_FAILED", error)
          }
      }
    }
  }

  private fun resolveCurrentPlayer(activity: Activity, promise: Promise) {
    PlayGames
      .getPlayersClient(activity)
      .currentPlayer
      .addOnSuccessListener { player ->
        promise.resolve(playerToWritableMap(player))
      }
      .addOnFailureListener { error ->
        rejectPromise(promise, "E_GET_PLAYER_FAILED", error)
      }
  }

  private fun withActivity(promise: Promise, block: (Activity) -> Unit) {
    val activity = currentActivity
    if (activity == null) {
      promise.reject(
        "E_NO_ACTIVITY",
        "Google Play Games requires a foreground Android Activity.",
      )
      return
    }

    block(activity)
  }

  private fun playerToWritableMap(player: Player) =
    Arguments.createMap().apply {
      putString("id", player.playerId)
      putString("displayName", player.displayName)
      putString("title", player.title)
      putString("iconImageUrl", player.iconImageUri?.toString())
      putString("hiResImageUrl", player.hiResImageUri?.toString())
    }

  private fun rejectPromise(promise: Promise, code: String, error: Exception) {
    promise.reject(code, error.message, error)
  }

  companion object {
    const val NAME = "GooglePlayGames"
    private const val RC_ACHIEVEMENTS_UI = 9003
  }
}
