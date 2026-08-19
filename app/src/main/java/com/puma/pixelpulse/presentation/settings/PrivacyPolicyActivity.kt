package com.puma.pixelpulse.presentation.settings

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class PrivacyPolicyActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Política de Privacidad") },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            ) { padding ->
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            webViewClient = WebViewClient()
                            settings.javaScriptEnabled = false
                            loadDataWithBaseURL(
                                null,
                                PRIVACY_POLICY_HTML,
                                "text/html",
                                "UTF-8",
                                null
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
        }
    }

    companion object {
        private const val PRIVACY_POLICY_HTML = """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body { font-family: sans-serif; padding: 16px; line-height: 1.6; color: #333; }
        h1 { font-size: 1.4em; }
        h2 { font-size: 1.2em; margin-top: 1.5em; }
        p { margin: 0.5em 0; }
    </style>
</head>
<body>
    <h1>Política de Privacidad - PixelPulse</h1>
    <p><em>Última actualización: Agosto 2026</em></p>

    <h2>1. Información que recopilamos</h2>
    <p>PixelPulse accede a los videos almacenados en tu dispositivo únicamente cuando tú lo seleccionas explícitamente para configurarlos como fondo de pantalla. No recopilamos, almacenamos ni transmitimos datos personales a servidores externos.</p>

    <h2>2. Permisos</h2>
    <p><strong>READ_MEDIA_VIDEO:</strong> Necesario para acceder a los videos que selecciones para usar como fondo de pantalla.</p>
    <p><strong>SET_WALLPAPER:</strong> Necesario para configurar el fondo de pantalla del dispositivo.</p>

    <h2>3. Almacenamiento</h2>
    <p>Los datos de configuración de tu fondo de pantalla se almacenan únicamente en tu dispositivo. No se envía ninguna información a servidores externos.</p>

    <h2>4. Servicios de terceros</h2>
    <p>PixelPulse puede utilizar Firebase Crashlytics para recopilar informes de errores anónimos y agregados con el fin de mejorar la estabilidad de la aplicación. Estos datos no incluyen información personal identificable.</p>

    <h2>5. Cambios en esta política</h2>
    <p>Podemos actualizar esta política de privacidad periodicamente. Cualquier cambio se reflejará en esta página.</p>

    <h2>6. Contacto</h2>
    <p>Si tienes preguntas sobre esta política de privacidad, contáctanos a través de la Play Store.</p>
</body>
</html>
"""
    }
}
