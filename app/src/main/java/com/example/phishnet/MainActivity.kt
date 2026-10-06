package com.example.phishnet

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.app.Activity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create SwipeRefreshLayout as the root view
        swipeRefreshLayout = SwipeRefreshLayout(this)
        setContentView(swipeRefreshLayout)
        
        // Create WebView
        webView = WebView(this)
        swipeRefreshLayout.addView(webView)

        // Configure WebView settings
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true
        webSettings.setSupportZoom(true)

        // Spoof user agent to impersonate Chrome
        val chromeUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
        webSettings.userAgentString = chromeUserAgent
        
        // Enable Chrome-based kernel
        WebView.setWebContentsDebuggingEnabled(false)

        // Configure WebViewClient to handle URL navigation
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                swipeRefreshLayout.isRefreshing = false
            }
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()
                
                // Redirect HTTP to HTTPS for phish.net domain
                if (url.startsWith("http://phish.net")) {
                    val httpsUrl = url.replace("http://", "https://")
                    view?.loadUrl(httpsUrl)
                    return true
                }
                
                // Allow these domains to load within the WebView
                val allowedDomains = listOf(
                    "phish.net",
                    "mbird.org",
                    "shopify.com",
                    "myshopify.com"
                )
                
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    val isInApp = allowedDomains.any { domain -> url.contains(domain) }
                    
                    if (isInApp) {
                        // Load allowed links (phish.net, store, Shopify checkout) in-app
                        return false
                    } else {
                        // Open truly external links in browser
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                        return true
                    }
                }
                
                // Load other links within the WebView
                return false
            }
            
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                url: String?
            ): Boolean {
                // Legacy method for older Android versions
                val urlString = url ?: return false
                
                // Redirect HTTP to HTTPS for phish.net domain
                if (urlString.startsWith("http://phish.net")) {
                    val httpsUrl = urlString.replace("http://", "https://")
                    view?.loadUrl(httpsUrl)
                    return true
                }
                
                // Allow these domains to load within the WebView
                val allowedDomains = listOf(
                    "phish.net",
                    "mbird.org",
                    "shopify.com",
                    "myshopify.com"
                )
                
                if (urlString.startsWith("http://") || urlString.startsWith("https://")) {
                    val isInApp = allowedDomains.any { domain -> urlString.contains(domain) }
                    
                    if (isInApp) {
                        // Load allowed links (phish.net, store, Shopify checkout) in-app
                        return false
                    } else {
                        // Open truly external links in browser
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
                        startActivity(intent)
                        return true
                    }
                }
                
                // Load other links within the WebView
                return false
            }
        }

        // Configure SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            webView.reload()
        }
        swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

        // Load phish.net
        webView.loadUrl("https://phish.net")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack()
            }
            // Always consume the back button to prevent app from closing
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        // Do nothing - prevent app from closing
    }
}