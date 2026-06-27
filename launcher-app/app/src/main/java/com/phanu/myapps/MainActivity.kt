package com.phanu.myapps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.phanu.myapps.data.AppGroup
import com.phanu.myapps.data.GroupRepository
import com.phanu.myapps.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyAppsTheme {
                AppNavigation()
            }
        }
    }
}

sealed class Screen {
    object Home : Screen()
    data class GroupDetail(val groupId: String) : Screen()
    data class EditGroup(val groupId: String) : Screen()
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val repo = remember { GroupRepository(context) }

    var groups by remember { mutableStateOf(repo.getGroups()) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }
    var groupToDelete by remember { mutableStateOf<AppGroup?>(null) }

    BackHandler(enabled = currentScreen != Screen.Home) {
        currentScreen = Screen.Home
    }

    when (val screen = currentScreen) {
        is Screen.Home -> HomeScreen(
            groups = groups,
            onGroupClick = { group ->
                if (group.packageNames.isEmpty()) {
                    currentScreen = Screen.EditGroup(group.id)
                } else {
                    currentScreen = Screen.GroupDetail(group.id)
                }
            },
            onAddGroup = { showAddDialog = true },
            onDeleteGroup = { groupToDelete = it }
        )

        is Screen.GroupDetail -> {
            val group = groups.find { it.id == screen.groupId }
            if (group != null) {
                GroupDetailScreen(
                    group = group,
                    onBack = { currentScreen = Screen.Home },
                    onEdit = { currentScreen = Screen.EditGroup(screen.groupId) }
                )
            }
        }

        is Screen.EditGroup -> {
            val group = groups.find { it.id == screen.groupId }
            if (group != null) {
                EditGroupScreen(
                    group = group,
                    onBack = { currentScreen = Screen.Home },
                    onSave = { updated ->
                        groups = repo.updateGroup(updated)
                        currentScreen = Screen.GroupDetail(updated.id)
                    }
                )
            }
        }
    }

    // Add group dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; newGroupName = "" },
            containerColor = Color(0xFF0D0C09),
            title = {
                Text("ชื่อกลุ่มใหม่", color = GoldLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                OutlinedTextField(
                    value = newGroupName,
                    onValueChange = { newGroupName = it },
                    placeholder = { Text("เช่น ทำงาน, ติดต่อ", color = GoldFaint) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = BorderDark,
                        cursorColor = Gold
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newGroupName.isNotBlank()) {
                        groups = repo.addGroup(newGroupName.trim())
                        newGroupName = ""
                        showAddDialog = false
                    }
                }) {
                    Text("เพิ่ม", color = Gold, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; newGroupName = "" }) {
                    Text("ยกเลิก", color = GoldDim)
                }
            }
        )
    }

    // Delete confirmation dialog
    groupToDelete?.let { group ->
        AlertDialog(
            onDismissRequest = { groupToDelete = null },
            containerColor = Color(0xFF0D0C09),
            title = {
                Text("ลบกลุ่ม \"${group.name}\"?", color = GoldLight, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("กลุ่มนี้จะถูกลบออก แอปในเครื่องไม่ได้รับผลกระทบ", color = GoldDim, fontSize = 13.sp)
            },
            confirmButton = {
                TextButton(onClick = {
                    groups = repo.deleteGroup(group.id)
                    groupToDelete = null
                }) {
                    Text("ลบ", color = Color(0xFFE05050), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { groupToDelete = null }) {
                    Text("ยกเลิก", color = GoldDim)
                }
            }
        )
    }
}
