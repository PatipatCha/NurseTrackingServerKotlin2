package com.nursetracking.nursetrackingserver2.model.dashboard

data class RoomList(
    val ordinal:Int?,
    val room_title:String?,
    val device_id: String? = null,
    val nurse_list : ArrayList<NurseList>? = null
)