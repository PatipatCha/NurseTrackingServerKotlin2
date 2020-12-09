package com.nursetracking.nursetrackingserver2.model.setting.layout

data class LayoutSetting (
    var version:String,
    val layout_type:Int,
    val layoutX:Int,
    val layoutY:Int
)