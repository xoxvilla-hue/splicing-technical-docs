package com.phanu.myapps.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import com.phanu.myapps.data.launchApp
import kotlinx.coroutines.delay

@Composable
fun GroupDetailScreen(
    group: AppGroup,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
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
                        text = "‹  กลับ",
                        color = GoldDim,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.clickable { onBack() }
                    )
                    Text(
                        text = "แก้ไข",
                        color = GoldDim,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.clickable { onEdit() }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = group.name,
                    color = GoldLight,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )
                Text(
                    text = "${group.packageNames.size} APPLICATIONS",
                    color = GoldFaint,
                    fontSize = 9.sp,
                    letterSpacing = 2.sp
                )
            }

            // Gold divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .padding(horizontal = 20.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color(0xFF4A3820), Color.Transparent)
                        )
                    )
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (group.packageNames.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ยังไม่มีแอปในกลุ่มนี้", color = GoldFaint, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "กด \"แก้ไข\" เพื่อเพิ่มแอป",
                            color = Color(0xFF1A1408),
                            fontSize = 12.sp,
                            modifier = Modifier.clickable { onEdit() }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(group.packageNames) { index, pkg ->
                        AppRow(packageName = pkg, index = index)
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }

        // Signature
        Text(
            text = "By Phanu",
            color = GoldFaint,
            fontSize = 13.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 1.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 14.dp)
        )
    }
}

@Composable
private fun AppRow(packageName: String, index: Int) {
    val context = LocalContext.current
    val pm = context.packageManager

    val appName = remember(packageName) {
        try { pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString() }
        catch (e: Exception) { packageName }
    }
    val icon by produceState<ImageBitmap?>(initialValue = null, packageName) {
        value = context.getAppIcon(packageName)?.asImageBitmap()
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(packageName) { delay(index * 60L); visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(280)) + slideInHorizontally(tween(280)) { -40 }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceDark)
                .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
                .clickable { context.launchApp(packageName) }
        ) {
            // Left gold accent
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(1.5.dp)
                    .height(24.dp)
                    .background(Gold.copy(alpha = 0.4f))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Color(0xFF111008))
                        .border(0.5.dp, BorderDark, RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (icon != null) {
                        Image(
                            bitmap = icon!!,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Text(
                    text = appName,
                    color = Color(0xFFCCCCCC),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                Text(text = "›", color = Color(0xFF2A2210), fontSize = 18.sp)
            }
        }
    }
}
