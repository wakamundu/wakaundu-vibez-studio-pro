
package com.wakaunduvibez.studio;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebViewAssetLoader;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private PermissionRequest pendingRequest;

    private final int MIC_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        WebViewAssetLoader assetLoader =
            new WebViewAssetLoader.Builder()
                .addPathHandler(
                    "/assets/",
                    new WebViewAssetLoader.AssetsPathHandler(this)
                )
                .build();

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public android.webkit.WebResourceResponse
            shouldInterceptRequest(
                WebView view,
                WebResourceRequest request
            ) {
                return assetLoader.shouldInterceptRequest(
                    request.getUrl()
                );
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(
                PermissionRequest request
            ) {
                runOnUiThread(() -> {
                    boolean audioRequested = false;

                    for (String resource : request.getResources()) {
                        if (resource.equals(
                            PermissionRequest.RESOURCE_AUDIO_CAPTURE
                        )) {
                            audioRequested = true;
                        }
                    }

                    if (!audioRequested) {
                        request.deny();
                        return;
                    }

                    if (checkSelfPermission(
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED) {
                        request.grant(request.getResources());
                    } else {
                        pendingRequest = request;
                        requestPermissions(
                            new String[]{
                                Manifest.permission.RECORD_AUDIO
                            },
                            MIC_PERMISSION_CODE
                        );
                    }
                });
            }
        });

        webView.loadUrl(
            "https://appassets.androidplatform.net/assets/index.html"
        );
    }

    @Override
    public void onRequestPermissionsResult(
        int requestCode,
        String[] permissions,
        int[] grantResults
    ) {
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        );

        if (requestCode == MIC_PERMISSION_CODE &&
            pendingRequest != null) {

            if (grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pendingRequest.grant(
                    pendingRequest.getResources()
                );
            } else {
                pendingRequest.deny();
            }

            pendingRequest = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
