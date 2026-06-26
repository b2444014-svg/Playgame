package com.simple.webapp

import android.annotation.SuppressLint
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var btnHome: View

    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private lateinit var fileChooser: ActivityResultLauncher<Intent>

    // Untuk minta izin notifikasi (Android 13+)
    private val notifPermLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    // The app opens this local home page (search portal). News sites are
    // listed in assets/sites.js — that's the only file you need to edit.
    private val startUrl: String = "file:///android_asset/home.html"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Switch from the splash theme to the normal app theme
        setTheme(R.style.Theme_SimpleWebApp)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        progressBar = findViewById(R.id.progress)
        swipe = findViewById(R.id.swipe)
        btnHome = findViewById(R.id.btnHome)
        btnHome.visibility = View.GONE   // tombol "Beranda" mengambang dimatikan

        fileChooser = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val data = if (result.resultCode == RESULT_OK) result.data else null
            val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, data)
            filePathCallback?.onReceiveValue(uris)
            filePathCallback = null
        }

        configureWebView()

        if (savedInstanceState == null) {
            webView.loadUrl(startUrl)
        } else {
            webView.restoreState(savedInstanceState)
        }

        swipe.setOnRefreshListener {
            val current = webView.url
            if (current == null || current.startsWith("file:///android_asset/")) {
                webView.loadUrl(startUrl)
            } else {
                webView.reload()
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })

        setupPush()
    }

    /** Siapkan push notification: channel + langganan topik + izin notifikasi. */
    private fun setupPush() {
        // Buat channel notifikasi (wajib Android 8+) supaya notif bisa tampil
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default_channel", "Notifikasi", NotificationManager.IMPORTANCE_HIGH
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }

        // Berlangganan notif untuk semua pengguna (topik "all")
        FirebaseMessaging.getInstance().subscribeToTopic("all")

        // Minta izin notifikasi (wajib Android 13+ agar notif muncul)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            allowFileAccess = true
            mediaPlaybackRequiresUserGesture = false
            cacheMode = WebSettings.LOAD_DEFAULT
            setSupportZoom(false)
            builtInZoomControls = false
            userAgentString = "$userAgentString SimpleWebApp/1.0"
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val scheme = request.url.scheme ?: ""
                // Open non-web links (tel:, mailto:, whatsapp:, intent:, etc.) in their own apps
                if (scheme != "http" && scheme != "https") {
                    return try {
                        startActivity(Intent(Intent.ACTION_VIEW, request.url))
                        true
                    } catch (e: Exception) {
                        true
                    }
                }
                return false // keep all normal web pages inside the app
            }

            override fun onPageStarted(view: WebView, url: String?, favicon: android.graphics.Bitmap?) {
                updateHomeButton(url)
            }

            override fun onPageFinished(view: WebView, url: String?) {
                swipe.isRefreshing = false
                updateHomeButton(url)
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                if (request.isForMainFrame) {
                    view.loadUrl("file:///android_asset/error.html")
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progressBar.progress = newProgress
                progressBar.visibility = if (newProgress in 1..99) View.VISIBLE else View.GONE
            }

            override fun onShowFileChooser(
                webView: WebView,
                callback: ValueCallback<Array<Uri>>,
                params: FileChooserParams
            ): Boolean {
                filePathCallback?.onReceiveValue(null)
                filePathCallback = callback
                return try {
                    fileChooser.launch(params.createIntent())
                    true
                } catch (e: Exception) {
                    filePathCallback = null
                    false
                }
            }
        }

        // Handle file downloads via the system / browser
        webView.setDownloadListener { url, _, _, _, _ ->
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: Exception) {
                // ignore
            }
        }

        // Bridge between the home page and the app
        webView.addJavascriptInterface(WebAppBridge(), "AndroidApp")
    }

    /** Tombol "Beranda" mengambang dinonaktifkan — selalu disembunyikan. */
    private fun updateHomeButton(url: String?) {
        btnHome.visibility = View.GONE
    }

    inner class WebAppBridge {
        @JavascriptInterface
        fun open(url: String) {
            runOnUiThread { webView.loadUrl(url) }
        }

        @JavascriptInterface
        fun home() {
            runOnUiThread { webView.loadUrl(startUrl) }
        }

        @JavascriptInterface
        fun retry() {
            runOnUiThread { webView.loadUrl(startUrl) }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }
}
