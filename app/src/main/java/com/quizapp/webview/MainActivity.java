package com.quizapp.webview;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends Activity {

    private WebView webView;
    private ImageView splashLogo;
    private View noInternetLayout;
    private SwipeRefreshLayout swipeRefresh;
    private android.widget.ProgressBar splashProgress;
    private TextView welcomeText;
    private StarFieldView starField;
    private android.os.Handler progressHandler = new android.os.Handler();
    private int progressValue = 0;
    private static final String BASE_HOST = "quizapgame.netlify.app";
    private long lastBackPressTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        webView = findViewById(R.id.webview);
        splashLogo = findViewById(R.id.splashLogo);
        noInternetLayout = findViewById(R.id.noInternetLayout);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        splashProgress = findViewById(R.id.splashProgress);
        welcomeText = findViewById(R.id.welcomeText);
        starField = findViewById(R.id.starField);

        webView.setBackgroundColor(0x00000000);
        webView.setVerticalScrollBarEnabled(false);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri uri = Uri.parse(url);
                String host = uri.getHost();
                if (host != null && host.contains(BASE_HOST)) {
                    return false;
                }
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (Exception e) {
                    // no app to handle it, ignore
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                new android.os.Handler().postDelayed(() -> {
                    splashLogo.setVisibility(View.GONE);
                    splashProgress.setVisibility(View.GONE);
                    welcomeText.setVisibility(View.GONE);
                    progressHandler.removeCallbacksAndMessages(null);
                    if (starField != null) starField.stopStars();
                    starField.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                    noInternetLayout.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                }, 3000);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                splashLogo.setVisibility(View.GONE);
                splashProgress.setVisibility(View.GONE);
                welcomeText.setVisibility(View.GONE);
                if (starField != null) starField.stopStars();
                starField.setVisibility(View.GONE);
                webView.setVisibility(View.GONE);
                noInternetLayout.setVisibility(View.VISIBLE);
                swipeRefresh.setRefreshing(false);
            }
        });

        swipeRefresh.setOnRefreshListener(() -> {
            if (isConnected()) {
                webView.reload();
            } else {
                swipeRefresh.setRefreshing(false);
                noInternetLayout.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
            }
        });

        Button retryButton = findViewById(R.id.retryButton);
        retryButton.setOnClickListener(v -> {
            if (isConnected()) {
                noInternetLayout.setVisibility(View.GONE);
                splashLogo.setVisibility(View.VISIBLE);
                webView.reload();
            }
        });

        loadPage();
    }

    private void loadPage() {
        if (isConnected()) {
            webView.setVisibility(View.GONE);
            splashLogo.setVisibility(View.VISIBLE);
            splashProgress.setVisibility(View.VISIBLE);
            welcomeText.setVisibility(View.VISIBLE);
            splashProgress.setProgress(0);
            progressValue = 0;

            // صوت عند فتح التطبيق
            try {
                MediaPlayer mp = MediaPlayer.create(this, R.raw.startup_sound);
                if (mp != null) {
                    mp.start();
                    mp.setOnCompletionListener(MediaPlayer::release);
                }
            } catch (Exception e) {
                // no sound file yet
            }

            // Fade in للوغو
            android.view.animation.Animation fadeIn = android.view.animation.AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in);
            splashLogo.startAnimation(fadeIn);

            // Fade in لرسالة الترحيب بعد ثانية
            new android.os.Handler().postDelayed(() -> {
                welcomeText.animate().alpha(1f).setDuration(800).start();
            }, 1000);

            // Progress bar تعمر فـ 3 ثواني
            progressHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (progressValue < 100) {
                        progressValue += 1;
                        splashProgress.setProgress(progressValue);
                        progressHandler.postDelayed(this, 30);
                    }
                }
            });

            webView.loadUrl("https://quizapgame.netlify.app/");
        } else {
            splashLogo.setVisibility(View.GONE);
            splashProgress.setVisibility(View.GONE);
            welcomeText.setVisibility(View.GONE);
            noInternetLayout.setVisibility(View.VISIBLE);
        }
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            }
            if (System.currentTimeMillis() - lastBackPressTime < 2000) {
                finish();
                return true;
            }
            lastBackPressTime = System.currentTimeMillis();
            Toast.makeText(this, "اضغط رجوع مرة أخرى للخروج", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
