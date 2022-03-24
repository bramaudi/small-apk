package id.my.bramaudi.qrman;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity {

    public static final int PICKFILE_RESULT_CODE = 1;
    private final int PERMISSION_REQUEST_CODE = 11;

    private WebView browser;
    private ValueCallback<Uri[]> pickedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        browser = findViewById(R.id.webview);
        browser.setWebViewClient(getMyWebViewClient());
        browser.setWebChromeClient(getMyWebChromeClient());
        browserSettings();
        browser.addJavascriptInterface(new JavaScriptInterface(this), "Android");
        browser.loadUrl("https://qrman.vercel.app");
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void browserSettings() {
        browser.getSettings().setAppCacheEnabled(true);
        browser.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.getSettings().setDomStorageEnabled(true);
        browser.getSettings().setAllowFileAccess(true);
        browser.getSettings().setMediaPlaybackRequiresUserGesture(false);
        browser.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        browser.getSettings().setSupportMultipleWindows(true);
        browser.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            if (hasStoragePermission()
                    || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                    || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                saveFile(url);
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        });
    }

    private boolean hasStoragePermission() {
        String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private void saveFile(String url) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show();
            return;
        }
        browser.loadUrl("javascript: Android.saveDataUrlAsFile('" + url + "');");
    }

    private WebViewClient getMyWebViewClient() {
        return new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                String script = "const download = document.querySelector('a');" +
                        "const input = download.parentElement.parentElement.querySelector('input');" +
                        "input.onchange = () => Android.setFileName(download.getAttribute('download') || 'unknown');";
                view.evaluateJavascript(script, null);
            }
        };
    }

    private WebChromeClient getMyWebChromeClient() {
        return new WebChromeClient() {

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                WebView.HitTestResult result = view.getHitTestResult();
                String data = result.getExtra();
                Context context = view.getContext();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data));
                context.startActivity(browserIntent);
                return false;
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                result.confirm();
                return true;
            }

            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                pickedFile = filePathCallback;

                Intent pickIntent = new Intent();
                pickIntent.setType("image/*");
                pickIntent.setAction(Intent.ACTION_GET_CONTENT);
                // we will handle the returned data in onActivityResult
                startActivityForResult(Intent.createChooser(pickIntent, "Select Picture"), PICKFILE_RESULT_CODE);
                return true;
            }

            @Override
            @TargetApi(Build.VERSION_CODES.M)
            public void onPermissionRequest(PermissionRequest request) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    request.grant(request.getResources());
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
                }
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                browser.reload();
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
                if (permissions[0].equals(Manifest.permission.CAMERA)) {
                    Toast.makeText(this, "You can tap \"Camera\" button again.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "You can try download again.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            pickedFile.onReceiveValue(null);
            pickedFile = null;
        }
        if (resultCode == RESULT_OK) {
            if (pickedFile == null) return;
            Uri result = data.getData();
            pickedFile.onReceiveValue(new Uri[]{result});
            pickedFile = null;
        }
    }
}