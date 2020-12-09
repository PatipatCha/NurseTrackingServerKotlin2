package com.nursetracking.nursetrackingserver2.function

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.nursetracking.nursetrackingserver2.model.Androidbox
import com.nursetracking.nursetrackingserver2.model.client.Brand
import com.nursetracking.nursetrackingserver2.model.client.SendPublish
import com.nursetracking.nursetrackingserver2.model.client.iTAG
import com.nursetracking.nursetrackingserver2.model.client.iTAGList
import com.nursetracking.nursetrackingserver2.model.dashboard.DashboardPublish
import com.nursetracking.nursetrackingserver2.model.dashboard.Layout
import com.nursetracking.nursetrackingserver2.model.dashboard.NurseList
import com.nursetracking.nursetrackingserver2.model.dashboard.RoomList
import com.nursetracking.nursetrackingserver2.model.setting.layout.LayoutSetting
import com.nursetracking.nursetrackingserver2.services.mqtt.MQTTHelper
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ServerFunction {

    private var gson = GsonBuilder().setPrettyPrinting().create()

    fun CheckiTAGVersion(iTAGListArraySetting: ArrayList<iTAGList>?, brandJson: String): ArrayList<iTAGList> {

        //Server brand.json
        val jsonBrandSetting = gson.fromJson(brandJson, Brand::class.java)
        val BrandListSetting = jsonBrandSetting.brand

        val iTAGListSettingModel : ArrayList<iTAGList> = ArrayList()

        iTAGListArraySetting!!.forEach {
            val iTAGName = it.brand_name.toString()
            var mac_address = it.mac_address.toString()
            var distance :Int? = null
            BrandListSetting?.forEach {
                val BrandName = it.brand_name.toString()
                if(iTAGName==BrandName){
                    distance = it.distance
                }
            }
            iTAGListSettingModel.add(
                iTAGList(mac_address, iTAGName, null, distance)
            )
        }
        return iTAGListSettingModel
    }

    fun CheckAndroidbox(jsonMessage: JsonObject, mqttHelper: MQTTHelper, array: ArrayList<String>) {

        //Get Androidbox Device ID Client Message
        val JsonAndroid = jsonMessage["androidbox"]
        var AndroidboxDeviceIdClient = gson.fromJson(JsonAndroid, Androidbox::class.java).device_id

        //Get Androidbox Device ID Server Setting
        val jsonAndriod = array[0]
        val AndroidboxDeviceIdServerArray = JSONObject(jsonAndriod).getJSONArray("device")
        for(AA in 0..AndroidboxDeviceIdServerArray.length() ){

            //Check Androidbox Device ID
            val AndroidboxDeviceIdServer = AndroidboxDeviceIdServerArray.getJSONObject(AA).get("device_id").toString()
            if (AndroidboxDeviceIdServer == AndroidboxDeviceIdClient ) {

                //Server itag_list.json
                val jsoniTAGSetting = gson.fromJson(array[2], iTAG::class.java)
                val iTAGListVersionServer = jsoniTAGSetting.version
                val iTAGListArraySetting = jsoniTAGSetting.itag_list

                //Client
                val JsoniTAGClient = gson.fromJson(jsonMessage["itag"], iTAG::class.java)
                val iTAGListVersionClient = JsoniTAGClient.version
                val iTAGListArrayClient = JsoniTAGClient.itag_list

                //Check iTAG List Version Message
                if (iTAGListVersionServer != iTAGListVersionClient) {
                    val iTAGListSettingModel = CheckiTAGVersion(iTAGListArraySetting,array[1])
                    val message = "Please update version."
                    val datetime: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                        Date()
                    )
                    val and = Androidbox(AndroidboxDeviceIdClient, datetime, message)
                    val itag = iTAG(iTAGListVersionServer, iTAGListSettingModel)
                    val SendPublishModel = SendPublish(and, itag)
                    val sendmqtt = gson.toJson(SendPublishModel)
                    mqttHelper.publishJson("server", sendmqtt.toString())
                }else if(iTAGListVersionServer == iTAGListVersionClient) {
                    var JsonRoomList = array[3]
                    val iTAGListArrayClientSize = iTAGListArrayClient?.size
                    if( iTAGListArrayClientSize!! > 0 ){
                        var JsonLayout = array[4]
                        SendDashboard(mqttHelper,JsonRoomList,AndroidboxDeviceIdClient,iTAGListArrayClient,iTAGListArraySetting,JsonLayout)
                    }
                }
            }
        }
    }

    fun saveiTAG(
        AndroidboxDeviceIdClientRoom: String?,
        AndroidboxDeviceIdClient: String,
        iTAGListArrayClient: ArrayList<iTAGList>?,
        iTAGListArraySetting: ArrayList<iTAGList>?,
        nurselist: ArrayList<NurseList>?,
        roomlist: ArrayList<RoomList>?,
        room_title: String?,
        room_ordinal: Int?
    ) {
        if( AndroidboxDeviceIdClientRoom == AndroidboxDeviceIdClient ) {
            iTAGListArrayClient?.forEach {
                val mac_addressClient = it.mac_address
                var titleOne:String = ""
                var title = ""

                iTAGListArraySetting!!.forEach {
                    val mac_addressSetting = it.mac_address
                    if( mac_addressClient == mac_addressSetting ){
                        title = it.title.toString()
                        titleOne = titleSub(title)
                    }
                }
                nurselist?.add( NurseList(null, titleOne) )
            }
        }
        roomlist?.add( RoomList(room_ordinal,room_title,null,nurselist) )
    }

    fun titleSub(title: String): String {
        var titleOne:String = ""
        if( title.contains(" ") == true ){
            val lastname = title.substringAfterLast(" ")
            val firstnameOne = title.substring(0,1)
            val lastnameOne = lastname.substring(0,1)
            titleOne = firstnameOne + lastnameOne
        }else{
            titleOne = title.substring(0,1)
        }
        return titleOne
    }

    fun SendDashboard(
        mqttHelper: MQTTHelper,
        JsonRoomList: String,
        AndroidboxDeviceIdClient: String,
        iTAGListArrayClient: ArrayList<iTAGList>?,
        iTAGListArraySetting: ArrayList<iTAGList>?,
        JsonLayout: String
    ) {
        //GLOBAL
        var roomlist :ArrayList<RoomList>? = ArrayList()
        val roomlist_setting = gson.fromJson(JsonRoomList, Layout::class.java).room_list
        val layoutType_setting = gson.fromJson(JsonLayout, LayoutSetting::class.java).layout_type
        val roomX_setting = gson.fromJson(JsonLayout, LayoutSetting::class.java).layoutX
        val roomY_setting = gson.fromJson(JsonLayout, LayoutSetting::class.java).layoutY
        val roomlist_setting_size = roomlist_setting?.size ?:0

        //X
        var roomlistX :ArrayList<RoomList>? = ArrayList()
        roomlist_setting?.forEach {
            var room_titleX = it.room_title
            var room_ordinalX = it.ordinal
            var nurselistX: ArrayList<NurseList>? = ArrayList()
            val AndroidboxDeviceIdClientRoom = it.device_id
            saveiTAG(
                AndroidboxDeviceIdClientRoom, AndroidboxDeviceIdClient,
                iTAGListArrayClient, iTAGListArraySetting,
                nurselistX ,roomlistX,room_titleX,room_ordinalX
            )
        }
        roomlistX?.sortBy { it.ordinal }
        for(bX in 0..roomY_setting-1) {
            roomlistX?.removeAt(0)
            roomlistX?.sortBy { it.ordinal }
        }

        //Y
        var roomlistY :ArrayList<RoomList>? = ArrayList()
        for(aY in 0..roomY_setting-1) {
            var room_titleY = roomlist_setting!![aY].room_title
            var room_ordinalY = roomlist_setting!![aY].ordinal
            var nurselistY: ArrayList<NurseList>? = ArrayList()
            val AndroidboxDeviceIdClientRoom = roomlist_setting!![aY].device_id
            saveiTAG(
                AndroidboxDeviceIdClientRoom,AndroidboxDeviceIdClient,
                iTAGListArrayClient,iTAGListArraySetting,
                nurselistY,roomlistY,room_titleY,room_ordinalY
            )
        }
        roomlistY?.sortBy { it.ordinal }

        //Send
        val layoutX = Layout(roomlistX)
        val layoutY = Layout(roomlistY)
        val datetime: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
            Date()
        )
        val androidbox = Androidbox("Dashboard",datetime,"Send room list.")
        val SendPublish = DashboardPublish(androidbox, layoutX,layoutY)
        val sendmqtt = gson.toJson(SendPublish)
        mqttHelper.publishJson("dashboard",sendmqtt.toString())
    }

}