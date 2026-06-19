package dev.datarun.datarun_mobile

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val authChannelName = "dev.datarun.mobile/auth_handoff"
    private var pendingAuthResult: MethodChannel.Result? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            authChannelName
        ).setMethodCallHandler { call, result ->
            when (call.method) {
                "authorize" -> {
                    val authorizationUrl = call.argument<String>("authorizationUrl")
                    if (authorizationUrl.isNullOrBlank()) {
                        result.error(
                            "invalid_request",
                            "Missing authorization URL",
                            null
                        )
                    } else {
                        openAuthorizationUrl(authorizationUrl, result)
                    }
                }
                else -> result.notImplemented()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAuthRedirect(intent)
    }

    private fun openAuthorizationUrl(
        authorizationUrl: String,
        result: MethodChannel.Result
    ) {
        if (pendingAuthResult != null) {
            result.error("auth_in_progress", "Sign in is already active", null)
            return
        }

        pendingAuthResult = result
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authorizationUrl))
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        try {
            startActivity(intent)
        } catch (exception: ActivityNotFoundException) {
            pendingAuthResult = null
            result.error(
                "no_external_agent",
                "No system browser is available for sign in",
                null
            )
        }
    }

    private fun handleAuthRedirect(intent: Intent) {
        val callbackUri = intent.data ?: return
        val result = pendingAuthResult ?: return
        pendingAuthResult = null
        result.success(callbackUri.toString())
    }
}
