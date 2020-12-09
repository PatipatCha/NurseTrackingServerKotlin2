package com.nursetracking.nursetrackingserver2.model.client

import com.nursetracking.nursetrackingserver2.model.Androidbox


data class SendPublish(
    val androidbox: Androidbox,
    val itag: iTAG
)