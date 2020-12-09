package com.nursetracking.nursetrackingserver2.model.client

data class iTAG(
    val version: String,
    val itag_list: ArrayList<iTAGList>? = null
)