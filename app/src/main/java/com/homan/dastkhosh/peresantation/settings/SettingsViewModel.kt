package com.homan.dastkhosh.peresantation.settings


import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homan.dastkhosh.data.manager.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val backupManager: BackupManager
) : ViewModel() {

    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent = _messageEvent.asSharedFlow()

    fun exportDatabase(uri: Uri) {
        viewModelScope.launch {
            backupManager.exportBackup(uri)
                .onSuccess { _messageEvent.emit("بکاپ با موفقیت ذخیره شد!") }
                .onFailure { _messageEvent.emit("خطا در ذخیره بکاپ!") }
        }
    }

    fun importDatabase(uri: Uri) {
        viewModelScope.launch {
            backupManager.importBackup(uri)
                .onSuccess { _messageEvent.emit("اطلاعات با موفقیت بازگردانی شد!") }
                .onFailure { _messageEvent.emit("فایل نامعتبر است یا خطا در بازگردانی!") }
        }
    }
}