@file:Suppress("AssignedValueIsNeverRead")

package com.omarltxy5.devicescope

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.omarltxy5.devicescope.ui.theme.DeviceScopeTheme
import com.scottyab.rootbeer.RootBeer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 1. Get the theme mode from DataStore and collect it as state.
            //    It will automatically update when the value in DataStore changes.
            val themeMode by ThemePreferences.getTheme(this)
                .collectAsState(initial = ThemeMode.SYSTEM)

            // 2. Use the collected themeMode to determine if dark theme should be on.
            val useDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            DeviceScopeTheme(
                darkTheme = useDarkTheme, dynamicColor = true // You can make this a setting later!
            ) {
                AppScreen()
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AppScreen() {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    val snappySpec = tween<Float>(durationMillis = 200, easing = FastOutSlowInEasing)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    Triple(0, stringResource(R.string.device), Icons.Filled.Menu),
                    Triple(1, stringResource(R.string.utilities), Icons.Filled.Build),
                    Triple(2, stringResource(R.string.settings), Icons.Filled.Settings),
                    Triple(3, stringResource(R.string.info), Icons.Filled.Info)
                )
                items.forEach { (index, label, icon) ->
                    NavigationBarItem(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index, animationSpec = snappySpec)
                            }
                        },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) })
                }
            }
        }) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top,
                beyondViewportPageCount = 1
            ) { page ->
                when (page) {
                    0 -> HomeScreen()
                    1 -> UtilityScreen()
                    2 -> SettingsScreen()
                    3 -> InfoScreen()
                }
            }
        }
    }
}

@Composable
fun UtilityScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.utilities),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )


        RootStatusCard()


    }
}
@Composable
fun RootStatusCard() {
    val context = LocalContext.current
    val provider = remember { RootInfoProvider(context) }

    // FIXED: Changed .equals() to .getRootResults()
    val results = remember { provider.getRootResults() }
    val overallRooted = remember { provider.isRooted() }

    SectionCard(title = "Root Integrity") {
        InfoRow(
            label = "Overall Status",
            value = if (overallRooted) "Rooted" else "Not Rooted"
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        results.forEach { (name, detected) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = name, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = if (detected) "FOUND" else "NOT FOUND",
                    color = if (detected) Color.Red else Color.Green,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
@Composable
fun HomeScreen() {
    val deviceCodename = remember { Build.DEVICE }
    val boardManufacturer = remember { Build.MANUFACTURER }
    val buildNumber = remember { Build.ID }
    val fingerprint = remember { Build.FINGERPRINT }
    val deviceManufacturer = remember { Build.BRAND }
    val sdk = remember { Build.VERSION.SDK_INT }
    val release = remember { Build.VERSION.RELEASE }
    val model = remember { Build.MODEL }
    val hardware = remember { Build.HARDWARE }
    val supportedABIs = remember { Build.SUPPORTED_ABIS.joinToString(", ") }
    val baseband = remember { Build.getRadioVersion() ?: "Unknown" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Welcome to DeviceScope!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        SectionCard("Device") {
            InfoRow(stringResource(R.string.brand), deviceManufacturer)
            InfoRow(stringResource(R.string.codename), deviceCodename)
            InfoRow(stringResource(R.string.manufacturer), boardManufacturer)
        }

        SectionCard("System") {
            InfoRow(stringResource(R.string.build_id), buildNumber)
            InfoRow(stringResource(R.string.android_version), release)
            InfoRow(stringResource(R.string.sdk), sdk.toString())
            InfoRow(stringResource(R.string.fingerprint), fingerprint)
        }

        SectionCard("Hardware") {
            InfoRow(stringResource(R.string.model), model)
            InfoRow(stringResource(R.string.hardware), hardware)
            InfoRow(stringResource(R.string.baseband), baseband)
            InfoRow(stringResource(R.string.supported_abis), supportedABIs)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isLink = value.startsWith("http://") || value.startsWith("https://")
    val haptic = LocalHapticFeedback.current

    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable {
                if (isLink) {
                    val intent = Intent(Intent.ACTION_VIEW, value.toUri())
                    context.startActivity(intent)
                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                } else {
                    scope.launch {
                        val clipData = ClipData.newPlainText(label, value)
                        clipboard.setClipEntry(ClipEntry(clipData))
                        Toast.makeText(context, "$label copied!", Toast.LENGTH_SHORT).show()
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    }
                }
            })
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
fun InfoScreen() {
    val context = LocalContext.current
    var showLibraries by remember { mutableStateOf(false) }

    if (showLibraries) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // Back Button
            Row(modifier = Modifier
                .fillMaxWidth()
                .clickable { showLibraries = false }
                .padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.back_to_info), style = MaterialTheme.typography.labelLarge)
            }

            Text(
                text = stringResource(R.string.open_source_libraries),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )

            // The Licenses
            LicenseCard("Jetpack Compose", "Google", "Apache 2.0")
            LicenseCard("Material 3", "Google", "Apache 2.0")
            LicenseCard("DataStore", "Google", "Apache 2.0")
            LicenseCard("RootBeer", "scottyab", "Apache 2.0")
            LicenseCard("Kotlin Coroutines", "JetBrains", "Apache 2.0")

            Spacer(modifier = Modifier.height(24.dp))
        }
    } else {
        val packageInfo = remember(context) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.getPackageInfo(
                        context.packageName,
                        PackageManager.PackageInfoFlags.of(0)
                    )
                } else {
                    @Suppress("DEPRECATION") context.packageManager.getPackageInfo(
                        context.packageName, 0
                    )
                }
            } catch (_: Exception) {
                null
            }
        }

        val versionName = packageInfo?.versionName ?: "v0.1 REL-PRE-ALPHA"
        val versionCode = packageInfo?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) it.longVersionCode.toString()
            else @Suppress("DEPRECATION") it.versionCode.toString()
        } ?: "0"
        val unknownString = stringResource(R.string.unknown)
        val isDebug =
            (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val buildType = if (isDebug) stringResource(R.string.debug) else stringResource(R.string.release)

        val installDate = remember(packageInfo, unknownString) {
            packageInfo?.firstInstallTime?.let {
                val date = Date(it)
                SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                    .format(date)
            } ?: unknownString
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "DeviceScope",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                stringResource(R.string.info_subtitle),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            SectionCard(stringResource(R.string.application)) {
                InfoRow(stringResource(R.string.version), "$versionName ($versionCode)")
                InfoRow(stringResource(R.string.build_type), buildType)
                InfoRow(stringResource(R.string.install_date), installDate)
            }

            SectionCard(stringResource(R.string.system_environment)) {
                InfoRow(stringResource(R.string.target_sdk), "SDK ${context.applicationInfo.targetSdkVersion}")
                InfoRow(stringResource(R.string.device_sdk), "SDK ${Build.VERSION.SDK_INT}")
            }

            SectionCard(stringResource(R.string.about_me)) {
                InfoRow("Github", "https://github.com/omarltxy5/")
                InfoRow("XDA", "https://xdaforums.com/m/omarltxy58.13298208/")
                InfoRow("Telegram", "https://t.me/omarltxy58")
            }


            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { showLibraries = true },
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                        alpha = 0.4f
                    )
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.open_source_libraries),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            stringResource(R.string.view_licenses_and_credits), style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "made with love, © 2026 omarltxy5 <3",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentTheme by ThemePreferences.getTheme(context)
        .collectAsState(initial = ThemeMode.SYSTEM)

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )


        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.app_theme),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = stringResource(R.string.settings_Desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                    Surface(
                        onClick = { expanded = true },
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentTheme.name.lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.padding(start = 4.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded, onDismissRequest = { expanded = false }) {
                        ThemeMode.entries.forEach { mode ->
                            DropdownMenuItem(text = {
                                Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
                            }, onClick = {
                                scope.launch {
                                    ThemePreferences.setTheme(context, mode)
                                }
                                expanded = false
                            }, leadingIcon = {
                                val icon = when (mode) {
                                    ThemeMode.LIGHT -> Icons.Default.LightMode
                                    ThemeMode.DARK -> Icons.Default.DarkMode
                                    ThemeMode.SYSTEM -> Icons.Default.SettingsSuggest
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (currentTheme == mode) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            })
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.padding(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.app_language),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "TODO: Implement. to send translations, open an issue in the GitHub repository under the translation tag.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }


            }
        }
    }
}
@Composable
fun LicenseCard(name: String, author: String, license: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = name, style = MaterialTheme.typography.titleSmall)
            Text(text = "By $author", style = MaterialTheme.typography.bodySmall)
            Text(
                text = license,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
class RootInfoProvider(context: Context) {
    private val rootBeer = RootBeer(context)
    private val pm = context.packageManager

    fun getRootResults(): Map<String, Boolean> {
        return mapOf(
            "SU Binary" to rootBeer.checkForSuBinary(),
            "KernelSU" to detectKSU(),
            "KSU Next" to detectKSUNext(),
            "Magisk/SuperSU" to rootBeer.detectRootManagementApps(),
            "BusyBox" to rootBeer.checkForBusyBoxBinary(),
            "RW System" to rootBeer.checkForRWPaths(),
            "Test Keys" to rootBeer.detectTestKeys()
        )
    }

    private fun detectKSU(): Boolean {
        val managerInstalled = isPkgInstalled("me.weishu.kernelsu") || isPkgInstalled("io.github.tiann.kernelsu")
        // Check for common KSU paths or the dev node
        val ksuNode = java.io.File("/dev/ksu").exists()
        return managerInstalled || ksuNode
    }

    private fun detectKSUNext(): Boolean {
        val nextManager = isPkgInstalled("com.rifsxd.ksunext")
        val susfsNode = java.io.File("/dev/susfs").exists()
        return nextManager || susfsNode
    }

    private fun isPkgInstalled(pkg: String): Boolean {
        return try {
            pm.getPackageInfo(pkg, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun isRooted(): Boolean = rootBeer.isRooted || detectKSU() || detectKSUNext()
}