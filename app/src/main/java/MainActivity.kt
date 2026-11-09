package com.graphene.antivirus

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.graphene.antivirus.data.ConsentStore
import com.graphene.antivirus.ui.screens.*
import com.graphene.antivirus.ui.theme.GrapheneBlack
import com.graphene.antivirus.ui.theme.GrapheneTheme
import com.graphene.antivirus.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    private val notifLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    private val vpnLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        vm.onVpnConsentResult(res.resultCode == RESULT_OK)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= 33) notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

        setContent {
            GrapheneTheme { Root(vm, ConsentStore(applicationContext), vpnLauncher::launch) }
        }
    }
}

@Composable
private fun Root(
    vm: MainViewModel,
    store: ConsentStore,
    launchVpn: (android.content.Intent) -> Unit
) {
    val consented by vm.consented.collectAsState()
    val vpnIntent by vm.vpnConsentIntent.collectAsState()
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()

    LaunchedEffect(vpnIntent) { vpnIntent?.let(launchVpn) }

    Box(Modifier.fillMaxSize().background(GrapheneBlack)) {
        when (consented) {
            null -> Unit
            false -> ConsentScreen(onAccept = {
                scope.launch {
                    store.setConsent(true)
                    vm.grantConsent()
                    nav.navigate("sync_setup")
                }
            })
            true -> NavHost(nav, startDestination = "dashboard") {
                composable("dashboard") {
                    DashboardScreen(
                        vm = vm,
                        onStartScan = { nav.navigate("scan") },
                        onOpenPermissions = { nav.navigate("perms") },
                        onOpenSettings = { nav.navigate("settings") },
                        onOpenSyncSetup = { nav.navigate("sync_setup") },
                        onOpenSync = { nav.navigate("sync") }
                    )
                }
                composable("scan") {
                    ScanScreen(vm, onBack = { nav.popBackStack() })
                    LaunchedEffect(Unit) { vm.runScan() }
                }
                composable("perms") { PermissionsScreen(onBack = { nav.popBackStack() }) }
                composable("settings") { SettingsScreen(onBack = { nav.popBackStack() }) }
                composable("sync_setup") { SyncSetupScreen(onBack = { nav.popBackStack() }) }
                composable("sync") { SyncScreen(onBack = { nav.popBackStack() }) }
            }
        }
    }
}