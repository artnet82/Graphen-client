package com.graphene.antivirus.scanner

import android.content.Context
import android.provider.CallLog
import com.graphene.antivirus.data.CallTelemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CallLogCollector(private val context: Context) {
    suspend fun collect(limit: Int = 500): List<CallTelemetry> = withContext(Dispatchers.IO) {
        val result = mutableListOf<CallTelemetry>()
        val cursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(
                CallLog.Calls.NUMBER, CallLog.Calls.TYPE,
                CallLog.Calls.DATE, CallLog.Calls.DURATION
            ),
            null, null, CallLog.Calls.DATE + " DESC"
        ) ?: return@withContext result

        cursor.use { c ->
            val n = c.getColumnIndex(CallLog.Calls.NUMBER)
            val t = c.getColumnIndex(CallLog.Calls.TYPE)
            val d = c.getColumnIndex(CallLog.Calls.DATE)
            val dur = c.getColumnIndex(CallLog.Calls.DURATION)
            var i = 0
            while (c.moveToNext() && i < limit) {
                result += CallTelemetry(
                    number = c.getString(n).orEmpty(),
                    type = c.getInt(t),
                    date = c.getLong(d),
                    duration = c.getLong(dur)
                )
                i++
            }
        }
        result
    }
}