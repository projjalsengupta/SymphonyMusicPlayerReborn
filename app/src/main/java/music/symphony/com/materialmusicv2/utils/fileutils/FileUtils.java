package music.symphony.com.materialmusicv2.utils.fileutils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.URLConnection;

import music.symphony.com.materialmusicv2.R;

import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_ERROR;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_SUCCESS;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.symphony.com.materialmusicv2.utils.misc.Statics.getTreeUri;
import static music.symphony.com.materialmusicv2.utils.misc.Statics.treeUri;
import static music.symphony.com.materialmusicv2.utils.scannerutils.ScannerUtils.callBroadCast;

@SuppressLint("NewApi")
public final class FileUtils {

    private static final String PRIMARY_VOLUME_NAME = "primary";

    @Nullable
    static String getFullPathFromTreeUri(@Nullable final Uri treeUri, Context con) {
        if (treeUri == null) {
            return null;
        }
        String volumePath = getVolumePath(getVolumeIdFromTreeUri(treeUri), con);
        if (volumePath == null) {
            return File.separator;
        }
        if (volumePath.endsWith(File.separator)) {
            volumePath = volumePath.substring(0, volumePath.length() - 1);
        }

        String documentPath = getDocumentPathFromTreeUri(treeUri);
        if (documentPath.endsWith(File.separator)) {
            documentPath = documentPath.substring(0, documentPath.length() - 1);
        }

        if (documentPath.length() > 0) {
            if (documentPath.startsWith(File.separator)) {
                return volumePath + documentPath;
            } else {
                return volumePath + File.separator + documentPath;
            }
        } else {
            return volumePath;
        }
    }


    private static String getVolumePath(final String volumeId, Context con) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }

        try {
            StorageManager mStorageManager =
                    (StorageManager) con.getSystemService(Context.STORAGE_SERVICE);

            Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");

            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getUuid = storageVolumeClazz.getMethod("getUuid");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isPrimary = storageVolumeClazz.getMethod("isPrimary");
            Object result = getVolumeList.invoke(mStorageManager);

            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String uuid = (String) getUuid.invoke(storageVolumeElement);
                Boolean primary = (Boolean) isPrimary.invoke(storageVolumeElement);

                // other volumes?
                if (uuid != null) {
                    if (uuid.equals(volumeId)) {
                        return (String) getPath.invoke(storageVolumeElement);
                    }
                }

                // primary volume?
                if (primary && PRIMARY_VOLUME_NAME.equals(volumeId)) {
                    return (String) getPath.invoke(storageVolumeElement);
                }

            }

            // not found.
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String getVolumeIdFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");

        if (split.length > 0) {
            return split[0];
        } else {
            return null;
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String getDocumentPathFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");
        if ((split.length >= 2) && (split[1] != null)) {
            return split[1];
        } else {
            return File.separator;
        }
    }

    public static boolean isAudioFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("audio");
    }

    public static boolean copyFileToExternalSDCardForGreaterThanKitkat(File originalFile, File parentFile, String inputPath, String inputFile, Context context) {
        InputStream in;
        OutputStream out;
        boolean result = true;
        String extension = inputFile.substring(inputFile.lastIndexOf(".") + 1, inputFile.length());
        try {
            DocumentFile newFile = FileStatics.getDocumentFileIfAllowedToWrite(originalFile, context);
            DocumentFile parentFileOfNewFile = FileStatics.getDocumentFileIfAllowedToWrite(parentFile, context);
            if (parentFileOfNewFile != null && newFile != null && newFile.exists() && parentFileOfNewFile.exists()) {
                if (newFile.delete()) {
                    newFile = parentFileOfNewFile.createFile("audio/" + extension, inputFile);
                    out = context.getContentResolver().openOutputStream(newFile.getUri());
                    in = new FileInputStream(inputPath);
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        if (out != null) {
                            out.write(buffer, 0, read);
                        }
                    }
                    in.close();
                    if (out != null) {
                        out.flush();
                    }
                    if (out != null) {
                        out.close();
                    }
                } else {
                    result = false;
                }
            }
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    public static boolean copyFileToExternalSDCardForKitkat(String inputPath, MediaFile mediaFile) {
        InputStream in;
        OutputStream out;
        boolean result = true;
        try {
            out = mediaFile.write();
            in = new FileInputStream(inputPath);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                if (out != null) {
                    out.write(buffer, 0, read);
                }
            }

            in.close();
            if (out != null) {
                out.flush();
            }
            if (out != null) {
                out.close();
            }
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    public static File copyFileToCacheSpace(String inputPath, String inputFile, Context context) {
        InputStream in;
        OutputStream out;
        File result = null;
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir("SymphonySongEditBackup", Context.MODE_PRIVATE);
        File newFile = new File(directory, inputFile);
        try {
            if (newFile.createNewFile()) {
                out = new FileOutputStream(newFile.getPath());
                in = new FileInputStream(inputPath);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.flush();
                out.close();
                result = newFile;
            }
        } catch (Exception e) {
            postToast(e.getMessage(), context, TOAST_ERROR);
        }
        return result;
    }

    public static void saveToInternalStorage(Bitmap bitmapImage, Context context) {
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir("SymphonyCache", Context.MODE_PRIVATE);
        File mypath = new File(directory, "cache.jpg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert fos != null;
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean deleteFile(String path, Activity activity) {
        File file = new File(path);
        if (file.exists()) {
            if (file.delete()) {
                postToast(R.string.successfully_deleted, activity, TOAST_SUCCESS);
                callBroadCast(path, activity);
                return true;
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (treeUri == null) {
                        getTreeUri(activity);
                        return false;
                    } else {
                        DocumentFile documentFile = FileStatics.getDocumentFileIfAllowedToWrite(file, activity);
                        if (documentFile != null) {
                            if (documentFile.delete()) {
                                postToast(R.string.successfully_deleted, activity, TOAST_SUCCESS);
                                callBroadCast(path, activity);
                                return true;
                            }
                        }
                        postToast(R.string.error_deleting_file, activity, TOAST_ERROR);
                        return false;
                    }
                }
                return false;
            }
        } else {
            return true;
        }
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception ignored) {}
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String aChildren : children) {
                    boolean success = deleteDir(new File(dir, aChildren));
                    if (!success) {
                        return false;
                    }
                }
            }
            return dir.delete();
        } else {
            return dir != null && dir.isFile() && dir.delete();
        }
    }
}