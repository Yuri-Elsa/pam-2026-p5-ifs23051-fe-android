package org.delcom.pam_p5_ifs23051.helper

import androidx.navigation.NavHostController

object RouteHelper {
    fun to(
        navController: NavHostController,
        destination: String,
        removeBackStack: Boolean = false,
        popUpTo: String? = null,
    ) {
        if (removeBackStack) {
            navController.navigate(destination) {
                if(popUpTo == null){
                    popUpTo(0) { inclusive = true } // hapus semua stack
                }else{
                    popUpTo(popUpTo) { inclusive = true }
                }
                launchSingleTop = true
            }
        } else {
            navController.navigate(destination) {
                launchSingleTop = true
            }
        }
    }

    fun back(
        navController: NavHostController,
    ) {
        navController.popBackStack()
    }
}