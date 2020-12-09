package com.nursetracking.nursetrackingserver2.function

import android.content.Context
import java.io.BufferedReader
import java.io.File
import java.nio.charset.Charset

class Assets(contextThis: Context) {

    private val contextThis = contextThis

    fun readAsset(s: String): String = contextThis.assets.open(s).bufferedReader().use(BufferedReader::readText)

    fun ReadConfig(): ArrayList<String> {
        val JsonAndroidbox = readAsset("androidbox.json")
        val JsonBrand = readAsset("brand.json")
        val JsoniTAGList = readAsset("itag_list.json")
        val JsonRoomList = readAsset("room_list.json")
        val JsonLayout = readAsset("layout.json")
        val array : ArrayList<String> = ArrayList()
        array.add(JsonAndroidbox)
        array.add(JsonBrand)
        array.add(JsoniTAGList)
        array.add(JsonRoomList)
        array.add(JsonLayout)
        return array
    }

    fun WriteText(filename:String ,s:String){
        contextThis.openFileOutput(filename, Context.MODE_PRIVATE).use {
            it.write(s.toByteArray())
            it.close()
        }
    }

    fun ReadText(filename:String){
        var ins = File(filename).inputStream()
        var content = ins.readBytes().toString(Charset.defaultCharset())
    }



}