package id.my.bramaudi.qrman;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

public class MainActivity extends Activity {

    public static final int PICKFILE_RESULT_CODE = 1;

    private WebView browser;
    private ValueCallback<Uri[]> uploadMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        browser = findViewById(R.id.webview);
        browser.setWebChromeClient(getMyWebChromeClient());
        browserSettings();
        browser.loadUrl("https://qrman.vercel.app");
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void browserSettings() {
        browser.getSettings().setAppCacheEnabled(true);
        browser.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.getSettings().setDomStorageEnabled(true);
        browser.getSettings().setAllowFileAccess(true);
        browser.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
    }

    private WebChromeClient getMyWebChromeClient() {
        return new WebChromeClient() {

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                result.confirm();
                return true;
            }

            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                uploadMessage = filePathCallback;

                Intent pickIntent = new Intent();
                pickIntent.setType("image/*");
                pickIntent.setAction(Intent.ACTION_GET_CONTENT);
                // we will handle the returned data in onActivityResult
                startActivityForResult(Intent.createChooser(pickIntent, "Select Picture"), PICKFILE_RESULT_CODE);
                return true;
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            uploadMessage.onReceiveValue(null);
            uploadMessage = null;
        }
        if (resultCode == RESULT_OK) {
            if (uploadMessage == null) return;
            Uri result = data.getData();
            uploadMessage.onReceiveValue(new Uri[]{result});
            uploadMessage = null;
        }
    }
}