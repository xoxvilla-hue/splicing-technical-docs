package com.phanu.myapps.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phanu.myapps.data.AppGroup
import com.phanu.myapps.data.getAppIcon
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    groups: List<AppGroup>,
    onGroupClick: (AppGroup) -> Unit,
    onAddGroup: () -> Unit,
    onDeleteGroup: (AppGroup) -> Unit
) {
    var headerVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); headerVisible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = headerVisible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -24 }
            ) {
                Column {
                    Text(
                        text = "MY APPS",
                        color = GoldDim,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "กลุ่มของฉัน",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color(0xFF2A2010), Color.Transparent)
                        )
                    )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (groups.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ยังไม่มีกลุ่ม", color = GoldFaint, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("กด + เพื่อสร้างกลุ่มแรก", color = Color(0xFF1E1808), fontSize = 12.sp)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(groups) { index, group ->
                        GroupTile(
                            group = group,
                            index = index,
                            onClick = { onGroupClick(group) },
                            onLongClick = { onDeleteGroup(group) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Add button
            Button(
                onClick = onAddGroup,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0D0C09),
                    contentColor = Gold
                )
            ) {
                Text(
                    text = "+ เพิ่มกลุ่มใหม่",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Signature
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Text(
                    text = "By Phanu",
                    color = GoldFaint,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroupTile(
    group: AppGroup,
    index: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(group.id) { delay(index * 70L); visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { 30 }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
        ) {
            // Gold top line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Gold, Color.Transparent)
                        )
                    )
            )

            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = group.name,
                    color = GoldLight,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )
                Text(
                    text = "${group.packageNames.size} แอป",
                    color = GoldFaint,
                    fontSize = 9.sp,
                    letterSpacing = 0.8.sp
                )

                if (group.packageNames.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        group.packageNames.take(4).forEach { pkg ->
                            AppIconThumb(packageName = pkg)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text("กดค้างเพื่อลบ · กดเพื่อเพิ่มแอป", color = Color(0xFF1A1408), fontSize = 8.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AppIconThumb(packageName: String) {
    val context = LocalContext.current
    val icon by produceState<ImageBitmap?>(initialValue = null, packageName) {
        value = context.getAppIcon(packageName)?.asImageBitmap()
    }

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(Color(0xFF111008))
            .border(0.5.dp, BorderDark, RoundedCornerShape(7.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Image(
                bitmap = icon!!,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
