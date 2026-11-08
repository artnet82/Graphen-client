package com.graphene.antivirus.scanner

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.graphene.antivirus.data.SyncFolder
import com.graphene.antivirus.data.SyncedFileMeta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class FolderScanner(private val context: Context) {

    private val maxFileSize = 50L * 1024 * 1024     // 50 MB
    private val maxFiles    = 3000
    private val maxDepth    = 15

    suspend fun scanAll(folders: List<SyncFolder>): List<SyncedFileMeta> =
        withContext(Dispatchers.IO) {
            val out = mutableListOf<SyncedFileMeta>()
            for (folder in folders) {
                try {
                    scanFolder(folder.uri, out)
                } catch (_: Exception) { }
                if (out.size >= maxFiles) break
            }
            out
        }

    private fun scanFolder(folderUriStr: String, out: MutableList<SyncedFileMeta>) {
        val treeUri = Uri.parse(folderUriStr)
        val rootDocId = try {
            DocumentsContract.getTreeDocumentId(treeUri)
        } catch (_: Exception) { return }
        walk(treeUri, rootDocId, "", out, 0)
    }

    private fun walk(
        treeUri: Uri,
        docId: String,
        parentPath: String,
        out: MutableList<SyncedFileMeta>,
        depth: Int
    ) {
        if (depth > maxDepth || out.size >= maxFiles) return

        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId)
        val cursor = try {
            context.contentResolver.query(
                childrenUri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_MIME_TYPE,
                    DocumentsContract.Document.COLUMN_SIZE,
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED
                ),
                null, null, null
            )
        } catch (_: Exception) { null }

        cursor?.use { c ->
            while (c.moveToNext() && out.size < maxFiles) {
                val childDocId = c.getString(0) ?: continue
                val name       = c.getString(1) ?: continue
                val mime       = c.getString(2) ?: ""
                val size       = c.getLong(3)
                val modified   = c.getLong(4)
                val childPath  = if (parentPath.isEmpty()) name else "$parentPath/$name"

                if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                    walk(treeUri, childDocId, childPath, out, depth + 1)
                } else {
                    if (size <= 0 || size > maxFileSize) continue
                    val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, childDocId)
                    val sha = computeSha256(docUri)
                    out.add(
                        SyncedFileMeta(
                            folderUri = treeUri.toString(),
                            documentUri = docUri.toString(),
                            relativePath = childPath,
                            name = name,
                            size = size,
                            modified = modified,
                            mime = mime,
                            sha256 = sha
                        )
                    )
                }
            }
        }
    }

    private fun computeSha256(uri: Uri): String? = try {
        val digest = MessageDigest.getInstance("SHA-256")
        context.contentResolver.openInputStream(uri)?.use { input ->
            val buf = ByteArray(16 * 1024)
            var n: Int
            while (input.read(buf).also { n = it } > 0) digest.update(buf, 0, n)
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    } catch (_: Exception) { null }
}