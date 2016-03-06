package com.sonymobile.lifelog;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.sonymobile.lifelog.auth.GetAuthTokenTask;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by championswimmer on 21/4/15.
 */
public class LoginActivity extends Activity {
    private static final Uri AUTH_BASE_URL = Uri.parse("https://platform.lifelog.sonymobile.com/oauth/2/authorize");

    static Pattern AUTH_CODE_PATTERN = Pattern.compile("(code)" + "(=)" + "(.*)");
    String authentication_code = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final WebView wv = (WebView) findViewById(R.id.webview);
        if (Build.VERSION.SDK_INT >= 19) {
            wv.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        }
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(LifeLog.getCallback_url())) {
                    Matcher m = AUTH_CODE_PATTERN.matcher(url);
                    if (m.find()) {
                        wv.setVisibility(View.GONE);
                        authentication_code = m.group(3);
                        GetAuthTokenTask gat = new GetAuthTokenTask(getApplicationContext());
                        gat.getAuth(authentication_code, new GetAuthTokenTask.OnAuthenticatedListener() {
                            @Override
                            public void onAuthenticated(String auth_token) {
                                LifeLog.auth_token = auth_token;
                                setResult(LifeLog.LOGINACTIVITY_REQUEST_CODE);
                                finish();

                            }
                        });
                    }

                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        if (TextUtils.isEmpty(LifeLog.getScope())) {
            throw new RuntimeException("Scope parameter is empty!");
        }

        Uri uri = AUTH_BASE_URL.buildUpon()
                .appendQueryParameter("client_id", LifeLog.getClient_id())
                .appendQueryParameter("scope", LifeLog.getScope()).build();


        wv.loadUrl(uri.toString());
    }

}
