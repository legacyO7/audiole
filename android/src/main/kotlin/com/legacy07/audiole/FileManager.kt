package com.legacy07.audiole

import android.os.Environment
import android.util.Log
import java.io.File
import java.net.URLConnection

class FileManager {
    fun getFileManager(folderUri:String): HashMap<String, Any> {

        val returnMap: HashMap<String, Any> = HashMap<String, Any>()
        var path: String = folderUri
        if(path=="")
            path=Environment.getExternalStorageDirectory().toString()
        val directory = File(path)
        val dirs = mutableListOf<String>()
        val media = mutableListOf<String>()
        if (directory.exists()) {
            val files: Array<File> = directory.listFiles()

            for (i in files.indices) {
                if (files[i].isDirectory&&!files[i].isHidden)
                    dirs.add(files[i].name)
                else if(isMedia(files[i].path)){
                    media.add(files[i].name)
                }
            }
        } else
            Log.d("Directory", "is empty")
        returnMap["dirs"] = dirs
        returnMap["media"] = media
        returnMap["path"] = path
        return returnMap
    }

    fun isMedia(path: String?): Boolean {
        var mimeType: String? =null
        if(path!=null){
            mimeType = URLConnection.guessContentTypeFromName(path)
        }
        return mimeType != null && mimeType.startsWith("audio")
    }

}