package com.nursetracking.nursetrackingserver2.model

data class Androidbox (
        val device_id: String,
        val datetime: String,
        val message: String? = null
)