package com.nursetracking.nursetrackingserver2.screen

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.gson.JsonParser
import com.nursetracking.nursetrackingserver2.R
import com.nursetracking.nursetrackingserver2.function.Assets
import com.nursetracking.nursetrackingserver2.function.ServerFunction
import com.nursetracking.nursetrackingserver2.services.mqtt.MQTTHelper
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage

class ServerFragment : Fragment() {

    private var contextThis = requireContext()
    private lateinit var mqttHelper: MQTTHelper

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_server, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //
        val array = Assets(contextThis).ReadConfig()
        startMqtt(mqttHelper,array)
        //
        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    //
    fun startMqtt(mqttHelper: MQTTHelper, array: ArrayList<String>?) {
        mqttHelper.init()
        mqttHelper.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, s: String) {
                Log.d("MQTT Connect",s.toString() )
            }

            override fun connectionLost(throwable: Throwable) {

            }

            @Throws(Exception::class)
            override fun messageArrived(topic: String, mqttMessage: MqttMessage) {

                //CheckVersion
                val JsonMessage = JsonParser().parse(mqttMessage.toString()).getAsJsonObject()
                if (array != null) {
                    ServerFunction().CheckAndroidbox(JsonMessage,mqttHelper,array)
                }

            }

            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {
            }
        })
    }
    //

}