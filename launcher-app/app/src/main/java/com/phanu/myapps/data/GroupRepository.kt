package com.phanu.myapps.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class GroupRepository(context: Context) {
    private val prefs = context.getSharedPreferences("app_groups", Context.MODE_PRIVATE)

    fun getGroups(): List<AppGroup> {
        val json = prefs.getString("groups", null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                val pkgs = obj.getJSONArray("packages")
                AppGroup(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    packageNames = (0 until pkgs.length()).map { pkgs.getString(it) }
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveGroups(groups: List<AppGroup>) {
        val array = JSONArray()
        groups.forEach { group ->
            val obj = JSONObject().apply {
                put("id", group.id)
                put("name", group.name)
                put("packages", JSONArray(group.packageNames))
            }
            array.put(obj)
        }
        prefs.edit().putString("groups", array.toString()).apply()
    }

    fun addGroup(name: String): List<AppGroup> {
        val groups = getGroups().toMutableList()
        groups.add(AppGroup(id = UUID.randomUUID().toString(), name = name))
        saveGroups(groups)
        return groups
    }

    fun deleteGroup(id: String): List<AppGroup> {
        val groups = getGroups().filter { it.id != id }
        saveGroups(groups)
        return groups
    }

    fun updateGroup(updated: AppGroup): List<AppGroup> {
        val groups = getGroups().map { if (it.id == updated.id) updated else it }
        saveGroups(groups)
        return groups
    }
}
