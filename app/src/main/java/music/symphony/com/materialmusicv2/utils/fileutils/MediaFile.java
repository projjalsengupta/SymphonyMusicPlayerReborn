package music.symphony.com.materialmusicv2.utils.fileutils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class MediaFile {

    private final File file;
    private final ContentResolver contentResolver;
    private final Uri filesUri;
    private final Uri imagesUri;

    public MediaFile(ContentResolver contentResolver, File file) {
        this.file = file;
        this.contentResolver = contentResolver;
        filesUri = MediaStore.Files.getContentUri("external");
        imagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    public boolean delete()
            throws IOException {
        if (!file.exists()) {
            return true;
        }

        boolean directory = file.isDirectory();
        if (directory) {
            String[] files = file.list();
            if (files != null && files.length > 0) {
                return false;
            }
        }

        String where = MediaStore.MediaColumns.DATA + "=?";
        String[] selectionArgs = new String[]{file.getAbsolutePath()};

        contentResolver.delete(filesUri, where, selectionArgs);

        if (file.exists()) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Files.FileColumns.DATA, file.getAbsolutePath());
            contentResolver.insert(imagesUri, values);

            contentResolver.delete(filesUri, where, selectionArgs);
        }

        return !file.exists();
    }

    public File getFile() {
        return file;
    }

    public OutputStream write()
            throws IOException {
        if (file.exists() && file.isDirectory()) {
            throw new IOException("File exists and is a directory.");
        }

        String where = MediaStore.MediaColumns.DATA + "=?";
        String[] selectionArgs = new String[]{file.getAbsolutePath()};
        contentResolver.delete(filesUri, where, selectionArgs);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Files.FileColumns.DATA, file.getAbsolutePath());
        Uri uri = contentResolver.insert(filesUri, values);

        if (uri == null) {
            throw new IOException("Internal error.");
        }

        return contentResolver.openOutputStream(uri);
    }
}