package com.phanu.myapps.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun PinEntryScreen(
    groupName: String,
    correctPin: String,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var entered by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val shakeAnim = remember { Animatable(0f) }

    LaunchedEffect(entered) {
        if (entered.length == 4) {
            if (entered == correctPin) {
                onSuccess()
            } else {
                isError = true
                shakeAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = keyframes {
                        durationMillis = 400
                        -18f at 50
                        18f at 100
                        -14f at 150
                        14f at 200
                        -8f at 250
                        8f at 300
                        0f at 400
                    }
                )
                delay(100)
                entered = ""
                isError = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
            .statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {

        // Back
        Text(
            text = "‹  กลับ",
            color = GoldDim,
            fontSize = 13.sp,
            letterSpacing = 1.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(20.dp)
                .clickable { onBack() }
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {

            // Lock icon + group name
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🔒", fontSize = 32.sp)
                Text(
                    text = groupName,
                    color = GoldLight,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "ใส่รหัส 4 หลัก",
                    color = GoldFaint,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }

            // 4 PIN dots
            Row(
                modifier = Modifier.offset(x = shakeAnim.value.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val filled = index < entered.length
                    Box(
                        modifier = Modifier
                            .size(if (filled) 16.dp else 14.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isError -> Color(0xFFE05050)
                                    filled -> Gold
                                    else -> Color.Transparent
                                }
                            )
                            .border(
                                width = 1.5.dp,
                                color = when {
                                    isError -> Color(0xFFE05050)
                                    filled -> Gold
                                    else -> Color(0xFF3A3015)
                                },
                                shape = CircleShape
                            )
                    )
                }
            }

            // Number pad
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "⌫")
                ).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { key ->
                            PinKey(
                                label = key,
                                onClick = {
                                    when (key) {
                                        "" -> {}
                                        "⌫" -> { if (entered.isNotEmpty()) entered = entered.dropLast(1) }
                                        else -> { if (entered.length < 4) entered += key }
                                    }
                                },
                                isBackspace = key == "⌫",
                                isEmpty = key == ""
                            )
                        }
                    }
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
private fun PinKey(label: String, onClick: () -> Unit, isBackspace: Boolean, isEmpty: Boolean) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "pinKey"
    )

    Box(
        modifier = Modifier
            .size(76.dp)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(
                when {
                    isEmpty -> Color.Transparent
                    isBackspace -> Color(0xFF0D0C09)
                    else -> Color(0xFF0D0C09)
                }
            )
            .then(
                if (!isEmpty) Modifier.border(1.dp, BorderDark, RoundedCornerShape(20.dp)) else Modifier
            )
            .clickable(enabled = !isEmpty) {
                pressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        if (!isEmpty) {
            Text(
                text = label,
                color = if (isBackspace) GoldDim else Color.White,
                fontSize = if (isBackspace) 20.sp else 24.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    LaunchedEffect(pressed) {
        if (pressed) { delay(80); pressed = false }
    }
}

// ---- Set PIN dialog (used in EditGroupScreen) ----

@Composable
fun SetPinDialog(
    currentPin: String?,
    onConfirm: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(if (currentPin != null) "confirm_remove" else "enter") }
    var firstPin by remember { mutableStateOf("") }
    var secondPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var entered by remember { mutableStateOf("") }

    val shakeAnim = remember { Animatable(0f) }

    LaunchedEffect(entered, step) {
        if (entered.length == 4) {
            when (step) {
                "enter" -> { firstPin = entered; entered = ""; step = "confirm" }
                "confirm" -> {
                    if (entered == firstPin) {
                        onConfirm(entered)
                    } else {
                        isError = true
                        shakeAnim.animateTo(0f, keyframes {
                            durationMillis = 350
                            -15f at 50; 15f at 100; -10f at 150; 10f at 200; 0f at 350
                        })
                        delay(100)
                        entered = ""
                        isError = false
                        step = "enter"
                        firstPin = ""
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack.copy(alpha = 0.97f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "ยกเลิก",
            color = GoldDim,
            fontSize = 13.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
                .statusBarsPadding()
                .clickable { onDismiss() }
        )

        if (step == "confirm_remove") {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text("ต้องการถอด PIN ออกไหม?", color = GoldLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1A0808))
                            .border(1.dp, Color(0xFF3A1010), RoundedCornerShape(14.dp))
                            .clickable { onConfirm(null) }
                            .padding(horizontal = 28.dp, vertical = 14.dp)
                    ) {
                        Text("ถอด PIN", color = Color(0xFFE05050), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0D0C09))
                            .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
                            .clickable { onDismiss() }
                            .padding(horizontal = 28.dp, vertical = 14.dp)
                    ) {
                        Text("ยกเลิก", color = GoldDim, fontSize = 14.sp)
                    }
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🔒", fontSize = 28.sp)
                    Text(
                        text = if (step == "enter") "ตั้ง PIN ใหม่" else "ยืนยัน PIN อีกครั้ง",
                        color = GoldLight, fontSize = 20.sp, fontWeight = FontWeight.Bold
                    )
                    if (isError) Text("PIN ไม่ตรงกัน ลองใหม่", color = Color(0xFFE05050), fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.offset(x = shakeAnim.value.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    repeat(4) { i ->
                        val filled = i < entered.length
                        Box(
                            modifier = Modifier
                                .size(if (filled) 16.dp else 14.dp)
                                .clip(CircleShape)
                                .background(if (filled) Gold else Color.Transparent)
                                .border(1.5.dp, if (filled) Gold else Color(0xFF3A3015), CircleShape)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(listOf("1","2","3"), listOf("4","5","6"), listOf("7","8","9"), listOf("","0","⌫")).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { key ->
                                PinKey(
                                    label = key,
                                    onClick = {
                                        when (key) {
                                            "" -> {}
                                            "⌫" -> { if (entered.isNotEmpty()) entered = entered.dropLast(1) }
                                            else -> { if (entered.length < 4) entered += key }
                                        }
                                    },
                                    isBackspace = key == "⌫",
                                    isEmpty = key == ""
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
