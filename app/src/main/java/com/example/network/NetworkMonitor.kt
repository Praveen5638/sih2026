package com.example.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class NetworkState {
    ONLINE,
    OFFLINE,
    UNSTABLE,
    SYNCING
}

object NetworkMonitor {

    private val _networkState = MutableStateFlow(NetworkState.ONLINE)
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    private var isSimulatedOffline = false

    fun isOnline(): Boolean {
        return if (isSimulatedOffline) false else _networkState.value == NetworkState.ONLINE || _networkState.value == NetworkState.SYNCING
    }

    fun setSimulatedOffline(offline: Boolean) {
        isSimulatedOffline = offline
        _networkState.value = if (offline) NetworkState.OFFLINE else NetworkState.ONLINE
    }

    fun setSyncing(syncing: Boolean) {
        if (!isSimulatedOffline) {
            _networkState.value = if (syncing) NetworkState.SYNCING else NetworkState.ONLINE
        }
    }

    fun initialize(context: Context) {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (cm == null) {
                _networkState.value = NetworkState.ONLINE
                return
            }

            val builder = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

            val initialConnected = isNetworkConnected(cm)
            _networkState.value = if (initialConnected) NetworkState.ONLINE else NetworkState.OFFLINE

            cm.registerNetworkCallback(builder.build(), object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    if (!isSimulatedOffline) {
                        _networkState.value = NetworkState.ONLINE
                    }
                }

                override fun onLost(network: Network) {
                    if (!isSimulatedOffline) {
                        _networkState.value = NetworkState.OFFLINE
                    }
                }

                override fun onUnavailable() {
                    if (!isSimulatedOffline) {
                        _networkState.value = NetworkState.OFFLINE
                    }
                }
            })
        } catch (e: Exception) {
            // Default to online if permissions or callback registration fails in restricted environments
            _networkState.value = NetworkState.ONLINE
        }
    }

    private fun isNetworkConnected(cm: ConnectivityManager): Boolean {
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
