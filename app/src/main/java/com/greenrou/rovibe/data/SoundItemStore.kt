package com.greenrou.rovibe.data

import android.content.Context
import org.json.JSONObject
import java.time.LocalDateTime

class SoundItemStore(context: Context) {

    private val dir = context.filesDir.resolve("sounds").also { it.mkdirs() }

    fun loadAll(): List<SoundItem> =
        dir.listFiles()
            ?.filter { it.extension == "json" }
            ?.mapNotNull { file ->
                runCatching {
                    val obj = JSONObject(file.readText())
                    SoundItem(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        createdAt = LocalDateTime.parse(obj.getString("createdAt")),
                        content = obj.getString("content"),
                    )
                }.getOrNull()
            }
            ?.sortedByDescending { it.createdAt }
            ?: emptyList()

    fun save(item: SoundItem) {
        val obj = JSONObject()
        obj.put("id", item.id)
        obj.put("name", item.name)
        obj.put("createdAt", item.createdAt.toString())
        obj.put("content", item.content)
        dir.resolve("${item.id}.json").writeText(obj.toString())
    }

    fun delete(id: String) {
        dir.resolve("$id.json").delete()
    }
}
