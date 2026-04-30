package com.musicfree.android.data.remote

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

internal fun JsonObject.string(key: String): String? = this[key]?.jsonPrimitive?.contentOrNull

internal fun JsonObject.int(key: String): Int? = this[key]?.jsonPrimitive?.intOrNull

internal fun JsonObject.long(key: String): Long? = this[key]?.jsonPrimitive?.longOrNull

internal fun JsonObject.bool(key: String): Boolean? = this[key]?.jsonPrimitive?.booleanOrNull

internal fun JsonObject.obj(key: String): JsonObject? = this[key] as? JsonObject

internal fun JsonObject.array(key: String): JsonArray? = this[key] as? JsonArray

internal val JsonElement.jsonPrimitive: JsonPrimitive
    get() = this as JsonPrimitive
