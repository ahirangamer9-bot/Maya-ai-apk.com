package com.thehunterai.maya

import com.example.api.MayaApi as CoreMayaApi

object MayaApi {
    suspend fun sendMessage(message: String): String {
        return CoreMayaApi.sendMessage(message)
    }
}
