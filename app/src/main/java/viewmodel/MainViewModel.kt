package com.graphene.antivirus.viewmodel

import android.app.Application
import android.content.Intent
import android.net.VpnService
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.graphene.antivirus.data.*
import com.graphene.antivirus.service.GrapheneService
import com.graphene.antivirus.service.GrapheneVpnService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ScanUiState {
    data object Idle : ScanUiState
    data class InProgress(val step: String, val progress: Float) : ScanUiState
    data class Done(val response: ScanResponse) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val consent = ConsentStore(app)
    private val repo = ScanRepository(ApiClient.create(), app)

    private val _scanState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanState: StateFlow<ScanUiState> = _scanState.asStateFlow()

    private val _consented = MutableStateFlow<Boolean?>(null)
    val consented: StateFlow<Boolean?> = _consented.asStateFlow()

    private val _lastScan = MutableStateFlow<Long?>(null)
    val lastScan: StateFlow<Long?> = _lastScan.asStateFlow()

    /** VPN-разрешение запрашивается через Activity; здесь храним результат. */
    private val _vpnConsentIntent = MutableStateFlow<Intent?>(null)
    val vpnConsentIntent: StateFlow<Intent?> = _vpnConsentIntent.asStateFlow()

    init {
        viewModelScope.launch { consent.consentFlow.collect { _consented.value = it } }
    }

    fun grantConsent() = viewModelScope.launch {
        consent.setConsent(true)
        startCoreService()
        prepareVpn()
    }

    fun startCoreService() {
        val ctx = getApplication<Application>()
        ContextCompat.startForegroundService(ctx, Intent(ctx, GrapheneService::class.java))
    }

    fun prepareVpn() {
        val ctx = getApplication<Application>()
        val intent = VpnService.prepare(ctx)
        if (intent == null) {
            startVpnNow()
        } else {
            _vpnConsentIntent.value = intent
        }
    }

    fun onVpnConsentResult(granted: Boolean) {
        _vpnConsentIntent.value = null
        if (granted) startVpnNow()
    }

    private fun startVpnNow() {
        val ctx = getApplication<Application>()
        ContextCompat.startForegroundService(ctx, Intent(ctx, GrapheneVpnService::class.java))
    }

    fun runScan() {
        if (_scanState.value is ScanUiState.InProgress) return
        viewModelScope.launch {
            _scanState.value = ScanUiState.InProgress("Инициализация…", 0f)
            val deviceId = consent.getOrCreateDeviceId()
            val result = repo.performScan(deviceId) { step, progress ->
                _scanState.value = ScanUiState.InProgress(step, progress)
            }
            result.onSuccess {
                _lastScan.value = System.currentTimeMillis()
                _scanState.value = ScanUiState.Done(it)
            }.onFailure {
                _scanState.value = ScanUiState.Error(it.message ?: "Ошибка")
            }
        }
    }

    fun resetScan() { _scanState.value = ScanUiState.Idle }
}