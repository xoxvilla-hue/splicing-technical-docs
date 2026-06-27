package com.example.calendarwidget

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants[Manifest.permission.READ_CALENDAR] == true) {
            MidnightScheduler.schedule(this)
            NotificationHelper.createChannel(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.POST_NOTIFICATIONS
            )
        )

        val showTomorrow = intent.getBooleanExtra("show_tomorrow", false)

        setContent {
            CalendarAppTheme {
                CalendarScreen(showTomorrow = showTomorrow)
            }
        }
    }
}

@Composable
fun CalendarAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Color(0xFF050D1A),
            surface = Color(0xFF0D1E35),
            primary = Color(0xFF4FC3F7)
        ),
        content = content
    )
}

@Composable
fun CalendarScreen(showTomorrow: Boolean) {
    val context = LocalContext.current
    val repo = remember { CalendarRepository(context) }

    var events by remember { mutableStateOf<List<CalendarEvent>>(emptyList()) }
    var isTomorrow by remember { mutableStateOf(showTomorrow) }
    var headerVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isTomorrow) {
        events = emptyList()
        headerVisible = false
        delay(80)
        events = if (isTomorrow) repo.getTomorrowEvents() else repo.getTodayEvents()
        headerVisible = true
    }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF050D1A), Color(0xFF0A1628))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            AnimatedHeader(visible = headerVisible, isTomorrow = isTomorrow)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DayTab(
                    label = "วันนี้",
                    selected = !isTomorrow,
                    onClick = { isTomorrow = false },
                    modifier = Modifier.weight(1f)
                )
                DayTab(
                    label = "พรุ่งนี้",
                    selected = isTomorrow,
                    onClick = { isTomorrow = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (events.isEmpty() && headerVisible) {
                EmptyState()
            } else {
                EventList(events = events)
            }
        }

        Text(
            text = "By Phanu",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 16.dp),
            color = Color(0xFF1A3050),
            fontSize = 10.sp,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun AnimatedHeader(visible: Boolean, isTomorrow: Boolean) {
    val dateLabel = remember(isTomorrow) {
        val fmt = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("th"))
        val cal = java.util.Calendar.getInstance()
        if (isTomorrow) cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        fmt.format(cal.time)
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -30 }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                text = if (isTomorrow) "นัดหมายพรุ่งนี้ 🌙" else "นัดหมายวันนี้ ☀️",
                color = Color(0xFF4FC3F7),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateLabel,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun DayTab(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF4FC3F7) else Color(0xFF0D1E35),
        animationSpec = tween(250),
        label = "tabColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF050D1A) else Color(0xFF4A6A8A),
        animationSpec = tween(250),
        label = "tabTextColor"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        tonalElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun EventList(events: List<CalendarEvent>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(events) { index, event ->
            AnimatedEventCard(event = event, index = index)
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun AnimatedEventCard(event: CalendarEvent, index: Int) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(event.id) {
        delay(index * 80L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(350)) + slideInHorizontally(tween(350, easing = EaseOutCubic)) { -60 }
    ) {
        EventCard(event = event)
    }
}

@Composable
fun EventCard(event: CalendarEvent) {
    val eventColor = Color(event.calendarColor)
    val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0D1E35))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(eventColor)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${timeFmt.format(Date(event.startTime))} – ${timeFmt.format(Date(event.endTime))}",
                    color = Color(0xFF4FC3F7),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                event.location?.let { loc ->
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "📍 $loc",
                        color = Color(0xFF4A6A8A),
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(eventColor)
                    .alpha(0.7f)
            )
        }
    }
}

@Composable
fun EmptyState() {
    var scale by remember { mutableStateOf(0.8f) }
    val animScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "emptyScale"
    )

    LaunchedEffect(Unit) { scale = 1f }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(animScale)
        ) {
            Text(text = "✨", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "ไม่มีนัดหมาย",
                color = Color(0xFF4A6A8A),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "สบายใจได้เลย!",
                color = Color(0xFF2A4060),
                fontSize = 14.sp
            )
        }
    }
}
