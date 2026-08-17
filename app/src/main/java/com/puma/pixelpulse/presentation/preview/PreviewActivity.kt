package com.puma.pixelpulse.presentation.preview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PreviewActivity : ComponentActivity() {

    private val applyLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val wallpaperId = intent.getLongExtra(EXTRA_WALLPAPER_ID, -1L)
        if (wallpaperId == -1L) {
            finish()
            return
        }

        setContent {
            PreviewScreen(
                wallpaperId = wallpaperId,
                onBack = { finish() },
                onApply = { intent ->
                    applyLauncher.launch(intent)
                }
            )
        }
    }

    companion object {
        const val EXTRA_WALLPAPER_ID = "wallpaper_id"

        fun createIntent(context: Context, wallpaperId: Long): Intent {
            return Intent(context, PreviewActivity::class.java).apply {
                putExtra(EXTRA_WALLPAPER_ID, wallpaperId)
            }
        }
    }
}
