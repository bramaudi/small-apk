package id.my.bramaudi.qrman;

import android.content.Context;
import android.os.Environment;
import android.util.Base64;
import android.webkit.JavascriptInterface;

import java.io.File;
import java.io.FileOutputStream;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;

class JavaScriptInterface {

    private final Context context;
    private String fileName;

    public JavaScriptInterface(Context context) {
        this.context = context;
    }

    @JavascriptInterface
    public void setFileName(String text) {
        this.fileName = text;
    }

    @JavascriptInterface
    public void saveDataUrlAsFile(String dataUrl) throws IOException {
        String fileName = this.fileName;
        byte[] fileAsBytes = Base64.decode(dataUrl.replaceFirst("^data:image/jpeg;base64,", ""), 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.Downloads.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.Downloads.DATE_ADDED, System.currentTimeMillis());
            contentValues.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/QRMan");

            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
            OutputStream outputStream = resolver.openOutputStream(uri);
            outputStream.write(fileAsBytes);
            outputStream.close();

        } else {
            final File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file, false);
            fileOutputStream.write(fileAsBytes);
            fileOutputStream.flush();
        }
        Toast.makeText(context, "Download complete!", Toast.LENGTH_SHORT).show();
    }
}