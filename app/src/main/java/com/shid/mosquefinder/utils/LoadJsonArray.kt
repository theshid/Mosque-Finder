package com.shid.mosquefinder.utils

import android.content.res.Resources
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

 fun loadJsonArray(resources:Resources,fileId:Int,jsonName:String): JSONArray? {
    val builder = StringBuilder()
    val `in` =
        resources.openRawResource(fileId)
    val reader =
        BufferedReader(InputStreamReader(`in`))
    var line: String?
    try {
        while (reader.readLine().also { line = it } != null) {
            builder.append(line)
        }
        val json = JSONObject(builder.toString())
        return json.getJSONArray(jsonName)
    } catch (exception: IOException) {
        exception.printStackTrace()
    } catch (exception: JSONException) {
        exception.printStackTrace()
    }
    return null
}