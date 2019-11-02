package music.symphony.com.materialmusicv2.utils.fileutils;

import android.content.Context;
import android.content.UriPermission;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileStatics {
    public static DocumentFile getDocumentFileIfAllowedToWrite(File file, Context con) {
        List<UriPermission> permissionUris = con.getContentResolver().getPersistedUriPermissions();
        for (UriPermission permissionUri : permissionUris) {
            Uri treeUri = permissionUri.getUri();
            DocumentFile rootDocFile = DocumentFile.fromTreeUri(con, treeUri);
            String rootDocFilePath = FileUtils.getFullPathFromTreeUri(treeUri, con);
            if (rootDocFilePath != null) {
                rootDocFilePath = rootDocFilePath.replaceAll("sdcard0", "sdcard1");
            }
            if (rootDocFilePath != null && file.getAbsolutePath().startsWith(rootDocFilePath)) {
                ArrayList<String> pathInRootDocParts = new ArrayList<>();
                while (!rootDocFilePath.equals(file.getAbsolutePath())) {
                    pathInRootDocParts.add(file.getName());
                    file = file.getParentFile();
                }
                DocumentFile docFile = null;
                if (pathInRootDocParts.size() == 0) {
                    docFile = DocumentFile.fromTreeUri(con, rootDocFile.getUri());
                } else {
                    for (int i = pathInRootDocParts.size() - 1; i >= 0; i--) {
                        if (docFile == null) {
                            docFile = rootDocFile.findFile(pathInRootDocParts.get(i));
                        } else {
                            docFile = docFile.findFile(pathInRootDocParts.get(i));
                        }
                    }
                }
                if (docFile != null) {
                    return docFile;
                } else {
                    return null;
                }

            }
        }
        return null;
    }
}
