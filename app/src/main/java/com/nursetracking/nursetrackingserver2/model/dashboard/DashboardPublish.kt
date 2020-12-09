package com.nursetracking.nursetrackingserver2.model.dashboard

import com.nursetracking.nursetrackingserver2.model.Androidbox


data class DashboardPublish(
    val androidbox: Androidbox?,
    val layoutX: Layout,
    val layoutY: Layout
)