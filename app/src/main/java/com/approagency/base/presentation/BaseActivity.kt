package com.approagency.base.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.approagency.base.config.ApproConfig
import com.approagency.base.local.preference.PreferencesHelper
import com.approagency.base.model.ui.ApproSnackBarVisuals
import com.approagency.base.model.ui.SnackBarType
import com.approagency.base.model.ui.UiText
import com.approagency.base.session.SessionManager
import com.approagency.base.theme.ApproTheme
import com.approagency.base.theme.ThemeManager
import com.approagency.base.utils.DeepLinkManager
import com.approagency.base.utils.OtpAutoFillBus
import com.approagency.base.utils.OtpAutofillController
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.compose.koinInject
import java.util.Locale

abstract class BaseActivity : ComponentActivity(), OtpAutofillController {
    lateinit var snackBarHostState: SnackbarHostState

    protected lateinit var composeScope: CoroutineScope

    val drawerState = DrawerState(DrawerValue.Closed)

    val approViewModel: ApproViewModel by viewModel()

    private val otpAutoFillBus: OtpAutoFillBus by inject()
    private var otpSmsReceiver: BroadcastReceiver? = null
    private val smsConsentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val message = result.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                val code = Regex("\\b\\d{5}\\b").find(message ?: "")?.value
                code?.let { otpAutoFillBus.submit(it) }
            }
        }

    protected var showSubscriptionBottomSheet by mutableStateOf(false)

    val sessionManager: SessionManager by inject()
    val themeManager: ThemeManager by inject()
    val config: ApproConfig by inject()
    val deepLinkManager: DeepLinkManager by inject()

    var language: String = config.defaultLocale.language

    companion object {
        private val INTENT_FLAG_KEY = "IS_INTENT_HANDLED"
    }

    @Composable
    abstract fun CreateView()

    override fun attachBaseContext(newBase: Context) {
        language = PreferencesHelper.read(
            PreferencesHelper.Keys.LANGUAGE,
            config.defaultLocale.language
        )

        super.attachBaseContext(
            updateLocale(
                newBase,
                Locale.forLanguageTag(language)
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        snackBarHostState = SnackbarHostState()
        enableEdgeToEdge()
        setContent {
            composeScope = rememberCoroutineScope()
            val insetsController =
                WindowCompat.getInsetsController(window, window.decorView)
            val isDarkMode = koinInject<ThemeManager>().isDarkMode()
            insetsController.isAppearanceLightStatusBars = !isDarkMode
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.Transparent.toArgb()


            ApproTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    CreateView()
                }
            }
        }

        deepLinkManager.handle(intent)
    }

    private fun updateLocale(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)
        deepLinkManager.handle(intent)
    }

    fun showSnackBar(
        message: String,
        type: SnackBarType = SnackBarType.SIMPLE,
        actionLabel: String? = null,
        onActionClick: (() -> Unit)? = null
    ) {
        if (::snackBarHostState.isInitialized) {
            CoroutineScope(Dispatchers.Main).launch {
                snackBarHostState.showSnackbar(
                    ApproSnackBarVisuals(
                        message = message,
                        type = type,
                        actionLabel = actionLabel,
                        withDismissAction = true,
                        duration = SnackbarDuration.Short,
                        onActionClick = onActionClick
                    )
                )
            }
        }
    }

    fun closeDrawer() {
        composeScope.launch {
            drawerState.close()
        }
    }

    fun openDrawer() {
        composeScope.launch {
            drawerState.open()
        }
    }

    fun showSubscriptionDialog() {
        composeScope.launch {
            drawerState.close()
        }
        showSubscriptionBottomSheet = true
    }

    fun hideSubscriptionDialog() {
        showSubscriptionBottomSheet = false
    }

    fun showSnackBar(
        message: UiText,
        type: SnackBarType = SnackBarType.SIMPLE,
        actionLabel: String? = null,
        onActionClick: (() -> Unit)? = null
    ) {
        showSnackBar(
            message = message.asString(this),
            type = type,
            actionLabel = actionLabel,
            onActionClick = onActionClick
        )
    }

    override fun startOtpAutofill() {
        SmsRetriever.getClient(this).startSmsUserConsent(null)
        if (otpSmsReceiver != null) return
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, received: Intent?) {
                if (received?.action != SmsRetriever.SMS_RETRIEVED_ACTION) return
                val extras = received.extras ?: return
                val status = extras.get(SmsRetriever.EXTRA_STATUS) as? Status ?: return
                if (status.statusCode != CommonStatusCodes.SUCCESS) return
                val consentIntent: Intent? =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT, Intent::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT)
                    }
                consentIntent?.let { runCatching { smsConsentLauncher.launch(it) } }
            }
        }
        otpSmsReceiver = receiver
        ContextCompat.registerReceiver(
            this,
            receiver,
            IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION),
            SmsRetriever.SEND_PERMISSION,
            null,
            ContextCompat.RECEIVER_EXPORTED,
        )
    }

    override fun stopOtpAutofill() {
        otpSmsReceiver?.let { runCatching { unregisterReceiver(it) } }
        otpSmsReceiver = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopOtpAutofill()
    }
}