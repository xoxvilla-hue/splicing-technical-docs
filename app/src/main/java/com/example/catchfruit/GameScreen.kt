package com.example.catchfruit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlin.random.Random

// แนวเกม: ลากตะกร้าซ้าย-ขวารับผลไม้ที่ตกลงมา เก็บคะแนน และหลบระเบิด
// Catch falling fruit with the basket, score points, and dodge bombs.

private val FRUITS = listOf("🍎", "🍌", "🍇", "🍓", "🍊", "🍉", "🍑", "🍒", "🥝", "🍍")
private const val BOMB = "💣"

private enum class Phase { START, PLAYING, GAME_OVER }

/** สิ่งที่ตกลงมา: ผลไม้หรือระเบิด */
private data class FallingItem(
    val id: Long,
    val xFraction: Float, // 0f..1f ตำแหน่งแนวนอนเทียบกับความกว้างจอ
    val y: Float,         // ตำแหน่งแนวตั้ง (px) วัดจากด้านบน
    val emoji: String,
    val isBomb: Boolean,
    val speed: Float,     // px ต่อวินาที
)

@Composable
fun GameScreen() {
    val density = LocalDensity.current

    // ขนาดพื้นที่เล่น (px)
    var widthPx by remember { mutableFloatStateOf(0f) }
    var heightPx by remember { mutableFloatStateOf(0f) }

    var phase by remember { mutableStateOf(Phase.START) }
    var items by remember { mutableStateOf(listOf<FallingItem>()) }
    var basketCenterX by remember { mutableFloatStateOf(0f) }
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var best by rememberSaveable { mutableIntStateOf(0) }

    // ขนาดต่าง ๆ เป็น px
    val itemSizePx = with(density) { 46.dp.toPx() }
    val basketWidthPx = with(density) { 84.dp.toPx() }
    val basketSizeSp = 64.sp

    fun startGame() {
        score = 0
        lives = 3
        items = emptyList()
        basketCenterX = widthPx / 2f
        phase = Phase.PLAYING
    }

    // ลูปเกม: ทำงานเฉพาะตอนกำลังเล่น
    LaunchedEffect(phase) {
        if (phase != Phase.PLAYING) return@LaunchedEffect

        var lastFrame = withFrameNanos { it }
        var spawnTimer = 0f
        var nextId = 0L

        while (phase == Phase.PLAYING) {
            val now = withFrameNanos { it }
            val dt = ((now - lastFrame) / 1_000_000_000.0).toFloat().coerceAtMost(0.05f)
            lastFrame = now

            if (widthPx <= 0f || heightPx <= 0f) continue

            // ความยากเพิ่มขึ้นตามคะแนน
            val difficulty = 1f + score / 30f
            val spawnInterval = (1.1f / difficulty).coerceAtLeast(0.45f)

            // เกิดของใหม่
            spawnTimer += dt
            if (spawnTimer >= spawnInterval) {
                spawnTimer = 0f
                val isBomb = Random.nextFloat() < 0.18f
                val baseSpeed = with(density) { 220.dp.toPx() }
                items = items + FallingItem(
                    id = nextId++,
                    xFraction = Random.nextFloat() * 0.86f + 0.07f,
                    y = -itemSizePx,
                    emoji = if (isBomb) BOMB else FRUITS.random(),
                    isBomb = isBomb,
                    speed = baseSpeed * (0.85f + Random.nextFloat() * 0.4f) * difficulty,
                )
            }

            // เส้นรับของตะกร้า (ใกล้ด้านล่างจอ)
            val catchLine = heightPx - itemSizePx * 1.6f
            val basketHalf = basketWidthPx / 2f

            val survivors = ArrayList<FallingItem>(items.size)
            var gainedScore = 0
            var lostLife = 0

            for (item in items) {
                val newY = item.y + item.speed * dt
                val itemCenterX = item.xFraction * widthPx

                val reachedBasket = newY >= catchLine
                val withinBasket = kotlin.math.abs(itemCenterX - basketCenterX) <= basketHalf

                when {
                    reachedBasket && withinBasket -> {
                        // จับได้
                        if (item.isBomb) lostLife++ else gainedScore++
                    }
                    newY > heightPx -> {
                        // ตกพ้นจอไปแล้ว (พลาด) — ไม่หักคะแนน เพื่อให้เด็กเล่นสบาย
                    }
                    else -> survivors.add(item.copy(y = newY))
                }
            }

            if (gainedScore > 0) score += gainedScore
            if (lostLife > 0) lives -= lostLife
            items = survivors

            if (lives <= 0) {
                if (score > best) best = score
                phase = Phase.GAME_OVER
            }
        }
    }

    // พื้นหลังท้องฟ้าไล่สี
    val skyBrush = Brush.verticalGradient(
        listOf(Color(0xFF81D4FA), Color(0xFFE1F5FE), Color(0xFFC8E6C9))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(skyBrush)
            .onSizeChanged { size ->
                widthPx = size.width.toFloat()
                heightPx = size.height.toFloat()
                if (basketCenterX == 0f) basketCenterX = widthPx / 2f
            }
            .pointerInput(phase) {
                if (phase == Phase.PLAYING) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            event.changes.firstOrNull()?.let { change ->
                                basketCenterX = change.position.x
                                    .coerceIn(basketWidthPx / 2f, widthPx - basketWidthPx / 2f)
                            }
                        }
                    }
                }
            }
    ) {
        // ของที่กำลังตก
        for (item in items) {
            val xPx = (item.xFraction * widthPx - itemSizePx / 2f).roundToInt()
            val yPx = (item.y - itemSizePx / 2f).roundToInt()
            Text(
                text = item.emoji,
                fontSize = 40.sp,
                modifier = Modifier.offset { IntOffset(xPx, yPx) }
            )
        }

        // ตะกร้า
        if (phase == Phase.PLAYING) {
            val basketPxSize = with(density) { basketSizeSp.toPx() }
            val bx = (basketCenterX - basketPxSize / 2f).roundToInt()
            val by = (heightPx - basketPxSize * 1.4f).roundToInt()
            Text(
                text = "🧺",
                fontSize = basketSizeSp,
                modifier = Modifier.offset { IntOffset(bx, by) }
            )
        }

        // แถบคะแนนและหัวใจ
        if (phase == Phase.PLAYING) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "คะแนน: $score",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
                Text(text = "❤️".repeat(lives), fontSize = 22.sp)
            }
        }

        if (phase == Phase.START) {
            Overlay(
                title = "🍎 จับผลไม้ 🧺",
                lines = listOf(
                    "ลากนิ้วซ้าย-ขวาเพื่อขยับตะกร้า",
                    "รับผลไม้ให้ได้คะแนน 🍓🍌🍇",
                    "ระวังระเบิด! 💣 อย่ารับนะ",
                    "มี 3 หัวใจ ❤️❤️❤️"
                ),
                buttonText = "เริ่มเล่น ▶",
                onClick = { startGame() }
            )
        }

        if (phase == Phase.GAME_OVER) {
            Overlay(
                title = "จบเกม!",
                lines = listOf(
                    "คะแนนของหนู: $score 🎉",
                    "คะแนนสูงสุด: $best ⭐"
                ),
                buttonText = "เล่นอีกครั้ง 🔄",
                onClick = { startGame() }
            )
        }
    }
}

@Composable
private fun Overlay(
    title: String,
    lines: List<String>,
    buttonText: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x88000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp)
                .background(Color(0xFFFFFDE7), RoundedCornerShape(24.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            lines.forEach { line ->
                Text(
                    text = line,
                    fontSize = 18.sp,
                    color = Color(0xFF424242),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Button(onClick = onClick) {
                Text(text = buttonText, fontSize = 20.sp)
            }
        }
    }
}
