package com.musicfree.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

fun <T : ViewModel> simpleViewModelFactory(
    initializer: () -> T,
): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
            return initializer() as VM
        }
    }
}
