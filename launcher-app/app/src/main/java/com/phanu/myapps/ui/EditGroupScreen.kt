package com.phanu.myapps.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phanu.myapps.data.AppGroup
import com.phanu.myapps.data.AppInfo
import com.phanu.myapps.data.getAppIcon
import com.phanu.myapps.data.getInstalledApps

@Composable
fun EditGroupScreen(
    group: AppGroup,
    onBack: () -> Unit,
    onSave: (AppGroup) -> Unit
) {
    val context = LocalContext.current
    val allApps = remember { context.getInstalledApps() }

    var groupName by remember { mutableStateOf(group.name) }
    var selected by remember { mutableStateOf(group.packageNames.toSet()) }
    var search by remember { mutableStateOf("") }
    var showSetPin by remember { mutableStateOf(false) }
    var currentPin by remember { mutableStateOf(group.pin) }

    val filtered = remember(search, allApps) {
        if (search.isBlank()) allApps
        else allApps.filter { it.name.contains(search, ignoreCase = true) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "‹  ยกเลิก",
                        color = GoldDim,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.clickable { onBack() }
                    )
                    Text(
                        text = "บันทึก",
                        color = Gold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.clickable {
                            onSave(group.copy(name = groupName.ifBlank { group.name }, packageNames = selected.toList(), pin = currentPin))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Group name field
                BasicTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    textStyle = TextStyle(
                        color = GoldLight,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    cursorBrush = SolidColor(Gold),
                    singleLine = true,
                    decorationBox = { inner ->
                        Box {
                            if (groupName.isEmpty()) {
                                Text("ชื่อกลุ่ม...", color = GoldFaint, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                            inner()
                        }
                    }
                )

                Text(
                    text = "${selected.size} แอปที่เลือก",
                    color = GoldFaint,
                    fontSize = 9.sp,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // PIN Lock toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                        .clickable { showSetPin = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (currentPin != null) "🔒" else "🔓", fontSize = 16.sp)
                        Column {
                            Text(
                                text = if (currentPin != null) "PIN Lock เปิดอยู่" else "PIN Lock",
                                color = if (currentPin != null) GoldLight else Color(0xFF888880),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (currentPin != null) "กดเพื่อถอด PIN" else "กดเพื่อตั้งรหัส 4 หลัก",
                                color = GoldFaint,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Text(text = "›", color = Color(0xFF2A2210), fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDark)
                    .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                BasicTextField(
                    value = search,
                    onValueChange = { search = it },
                    textStyle = TextStyle(color = Color(0xFFAA9970), fontSize = 13.sp),
                    cursorBrush = SolidColor(Gold),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🔍", fontSize = 13.sp)
                            Box(modifier = Modifier.weight(1f)) {
                                if (search.isEmpty()) Text("ค้นหาแอป...", color = GoldFaint, fontSize = 13.sp)
                                inner()
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                items(filtered) { app ->
                    AppSelectRow(
                        app = app,
                        isSelected = app.packageName in selected,
                        onToggle = {
                            selected = if (app.packageName in selected)
                                selected - app.packageName
                            else
                                selected + app.packageName
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }

        // PIN setup overlay
        if (showSetPin) {
            SetPinDialog(
                currentPin = currentPin,
                onConfirm = { newPin ->
                    currentPin = newPin
                    showSetPin = false
                },
                onDismiss = { showSetPin = false }
            )
        }
    }
}

@Composable
private fun AppSelectRow(
    app: AppInfo,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val context = LocalContext.current
    val icon by produceState<ImageBitmap?>(initialValue = null, app.packageName) {
        value = context.getAppIcon(app.packageName)?.asImageBitmap()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF131008) else SurfaceDark)
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF3A2A10) else BorderDark,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF111008)),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Image(
                        bitmap = icon!!,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Text(
                text = app.name,
                color = if (isSelected) Color(0xFFDDD0A0) else Color(0xFFAAAAAA),
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )

            // Checkbox
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) Gold else Color(0xFF1A1508))
                    .border(1.5.dp, if (isSelected) Gold else Color(0xFF2A2010), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Text("✓", color = BgBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
