package pro.cashkeeper.inspektor.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pro.cashkeeper.inspektor.ui.overriding.editoverride.EditOverrideScreen
import pro.cashkeeper.inspektor.ui.overriding.overrideslist.OverridesListScreen
import pro.cashkeeper.inspektor.ui.theme.InspektorTheme
import pro.cashkeeper.inspektor.ui.transactiondetails.TransactionDetailsScreen
import pro.cashkeeper.inspektor.ui.transactionlist.TransactionListScreen

@Composable
internal fun App() = InspektorTheme {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "transactions",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("transactions") {
            TransactionListScreen(
                openTransaction = { id ->
                    navController.navigate("transaction/${id}")
                },
                openOverridesScreen = {
                    navController.navigate("overrides")
                },
                openAddOverrideScreen = { id ->
                    navController.navigate("add-override?transaction=${id}")
                }
            )
        }
        composable("transaction/{id}") { backStackEntry ->
            val id = backStackEntry.savedStateHandle.get<String>("id")?.toLongOrNull()
            if (id != null) {
                TransactionDetailsScreen(
                    id,
                    onBack = {
                        navController.popBackStack()
                    },
                    openAddOverrideScreen = {
                        navController.navigate("add-override?transactionId=${id}")
                    },
                )
            }
        }

        composable("overrides") {
            OverridesListScreen(
                openEditOverrideScreen = {
                    navController.navigate(
                        if (it == null) "add-override" else "edit-override/${it}"
                    )
                },
                onBack = {
                    navController.popBackStack()
                },
            )
        }

        composable("edit-override/{id}") { backStackEntry ->
            val id = backStackEntry.savedStateHandle.get<String>("id")?.toLongOrNull()
            EditOverrideScreen(
                overrideId = id ?: 0,
                onBack = {
                    navController.popBackStack()
                },
            )
        }

        composable("add-override?transaction={transactionId}",
            arguments = listOf(navArgument("transactionId") {
                type = NavType.StringType; defaultValue = null; nullable = true
            })
        ) { backStackEntry ->
            val transactionId = backStackEntry.savedStateHandle.get<String>("transactionId")?.toLongOrNull()
            EditOverrideScreen(
                overrideId = 0,
                onBack = {
                    navController.popBackStack()
                },
                transactionId = transactionId
            )
        }
    }
}