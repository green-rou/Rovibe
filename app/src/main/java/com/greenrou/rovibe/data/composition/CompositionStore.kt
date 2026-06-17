package com.greenrou.rovibe.data.composition

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class CompositionStore(context: Context) {

    private val dir = context.filesDir.resolve("compositions").also { it.mkdirs() }

    fun loadAll(): List<Composition> =
        dir.listFiles()
            ?.filter { it.extension == "json" }
            ?.mapNotNull { file ->
                runCatching { parseComposition(JSONObject(file.readText())) }.getOrNull()
            }
            ?.sortedByDescending { it.createdAt }
            ?: emptyList()

    fun save(composition: Composition) {
        dir.resolve("${composition.id}.json").writeText(toJson(composition).toString())
    }

    fun delete(id: String) {
        dir.resolve("$id.json").delete()
    }

    private fun toJson(composition: Composition): JSONObject {
        val obj = JSONObject()
        obj.put("id", composition.id)
        obj.put("name", composition.name)
        obj.put("bpm", composition.bpm)
        obj.put("createdAt", composition.createdAt)

        val tracksArray = JSONArray()
        composition.tracks.forEach { track ->
            val trackObj = JSONObject()
            trackObj.put("id", track.id)
            trackObj.put("name", track.name)

            val patternsArray = JSONArray()
            track.patterns.forEach { block ->
                val blockObj = JSONObject()
                blockObj.put("id", block.id)
                blockObj.put("soundId", block.soundId)
                blockObj.put("startBar", block.startBar.toDouble())
                blockObj.put("durationBars", block.durationBars.toDouble())
                patternsArray.put(blockObj)
            }
            trackObj.put("patterns", patternsArray)
            tracksArray.put(trackObj)
        }
        obj.put("tracks", tracksArray)
        return obj
    }

    private fun parseComposition(obj: JSONObject): Composition {
        val tracksArray = obj.optJSONArray("tracks") ?: JSONArray()
        val tracks = (0 until tracksArray.length()).map { i ->
            val trackObj = tracksArray.getJSONObject(i)
            val patternsArray = trackObj.optJSONArray("patterns") ?: JSONArray()
            val patterns = (0 until patternsArray.length()).map { j ->
                val blockObj = patternsArray.getJSONObject(j)
                PatternBlock(
                    id = blockObj.getString("id"),
                    soundId = blockObj.getString("soundId"),
                    startBar = blockObj.optDouble("startBar", 0.0).toFloat(),
                    durationBars = blockObj.optDouble("durationBars", 1.0).toFloat(),
                )
            }
            CompositionTrack(
                id = trackObj.getString("id"),
                name = trackObj.getString("name"),
                patterns = patterns,
            )
        }
        return Composition(
            id = obj.getString("id"),
            name = obj.getString("name"),
            bpm = obj.optInt("bpm", 120),
            tracks = tracks,
            createdAt = obj.getString("createdAt"),
        )
    }
}
