package com.sowrobrana.oshudhreminder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sowrobrana.oshudhreminder.alarms.NotificationHelper
import com.sowrobrana.oshudhreminder.ui.MedicineViewModel
import com.sowrobrana.oshudhreminder.ui.MedicineViewModelFactory
import com.sowrobrana.oshudhreminder.ui.screens.AddMedicineScreen
import com.sowrobrana.oshudhreminder.ui.screens.HistoryScreen
import com.sowrobrana.oshudhreminder.ui.screens.HomeScreen
import com.sowrobrana.oshudhreminder.ui.screens.MedicinesScreen
import com.sowrobrana.oshudhreminder.ui.theme.OshudhReminderTheme

class MainActivity : ComponentActivity() {

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createChannel(this)
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            OshudhReminderTheme {
                OshudhApp(
                    onRequestExactAlarm = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val intent = Intent(
                                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                Uri.parse("package:$packageName"),
                            )
                            startActivity(intent)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun OshudhApp(onRequestExactAlarm: () -> Unit) {
    val navController = rememberNavController()
    val vm: MedicineViewModel = viewModel(
        factory = MedicineViewModelFactory(
            androidx.compose.ui.platform.LocalContext.current.applicationContext
                as android.app.Application,
        ),
    )
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val tabs = listOf(
        Triple("home", "আজকের ডোজ", Icons.Filled.Today),
        Triple("medicines", "ওষুধ", Icons.Filled.Medication),
        Triple("history", "ইতিহাস", Icons.Filled.History),
    )

    Scaffold(
        bottomBar = {
            if (currentRoute != "add") {
                NavigationBar {
                    tabs.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            selected = currentRoute == route,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentRoute == "home" || currentRoute == "medicines") {
                FloatingActionButton(onClick = { navController.navigate("add") }) {
                    Icon(Icons.Filled.Add, contentDescription = "ওষুধ যোগ করুন")
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding),
        ) {
            val slideIn: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300),
                ) + fadeIn(animationSpec = tween(300))
            }
            val slideOut: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300),
                ) + fadeOut(animationSpec = tween(300))
            }
            val popIn: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300),
                ) + fadeIn(animationSpec = tween(300))
            }
            val popOut: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300),
                ) + fadeOut(animationSpec = tween(300))
            }
            composable(
                "home",
                enterTransition = slideIn,
                exitTransition = slideOut,
                popEnterTransition = popIn,
                popExitTransition = popOut,
            ) {
                HomeScreen(vm = vm, onRequestExactAlarm = onRequestExactAlarm)
            }
            composable(
                "medicines",
                enterTransition = slideIn,
                exitTransition = slideOut,
                popEnterTransition = popIn,
                popExitTransition = popOut,
            ) {
                MedicinesScreen(vm = vm)
            }
            composable(
                "history",
                enterTransition = slideIn,
                exitTransition = slideOut,
                popEnterTransition = popIn,
                popExitTransition = popOut,
            ) {
                HistoryScreen(vm = vm)
            }
            composable(
                "add",
                enterTransition = slideIn,
                exitTransition = slideOut,
                popEnterTransition = popIn,
                popExitTransition = popOut,
            ) {
                AddMedicineScreen(
                    vm = vm,
                    onDone = { navController.popBackStack() },
                )
            }
        }
    }
}
