package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PrayerTaskEntity
import com.example.data.prayer.DailyPrayerSchedule
import com.example.viewmodel.MainViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpiritualTasksScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val tasks by viewModel.prayerTasks.collectAsStateWithLifecycle()
    val schedule by viewModel.prayerSchedule.collectAsStateWithLifecycle()
    val habits by viewModel.todayHabits.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Prayer Tasks, 1 = Habits Tracker
    var showAddTaskDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("المهام والجدول الروحي", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(
                            text = if (selectedTab == 0) "مهامك اليومية المرتبطة بمواقيت الصلاة" else "متتبع العادات والصلوات اليومية",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (selectedTab == 0) {
                        IconButton(
                            onClick = { showAddTaskDialog = true },
                            modifier = Modifier.testTag("add_prayer_task_btn")
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = "إضافة مهمة", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { showAddTaskDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("مهمة مربوطة بصلاة") },
                    modifier = Modifier.testTag("fab_add_prayer_task")
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tabs Row
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("مهام الصلاة (${tasks.size})", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Outlined.Schedule, contentDescription = null) },
                    modifier = Modifier.testTag("tab_prayer_tasks")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("العادات الروحية", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Outlined.CheckCircleOutline, contentDescription = null) },
                    modifier = Modifier.testTag("tab_habits")
                )
            }

            AnimatedContent(
                targetState = selectedTab,
                label = "SpiritualTasksTabAnim"
            ) { tab ->
                when (tab) {
                    0 -> PrayerTasksView(
                        tasks = tasks,
                        schedule = schedule,
                        onToggleTask = { task, done -> viewModel.togglePrayerTask(task.id, done) },
                        onDeleteTask = { task -> viewModel.deletePrayerTask(task.id) }
                    )
                    1 -> HabitsTrackerView(
                        habits = habits,
                        onUpdateHabit = { updater -> viewModel.updateTodayHabit(updater) }
                    )
                }
            }
        }
    }

    if (showAddTaskDialog) {
        AddPrayerTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, prayerKey, offset, category ->
                viewModel.addPrayerTask(title, prayerKey, offset, category)
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
private fun PrayerTasksView(
    tasks: List<PrayerTaskEntity>,
    schedule: DailyPrayerSchedule,
    onToggleTask: (PrayerTaskEntity, Boolean) -> Unit,
    onDeleteTask: (PrayerTaskEntity) -> Unit
) {
    if (tasks.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.EventNote,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "لا توجد مهام مجدولة بعد",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "اربط مواعيد عملك ووردك القرآني بأوقات الصلوات لتبقى جدولك متناغماً مع الفطرة",
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tasks, key = { it.id }) { task ->
                val resolvedTime = resolvePrayerTaskTime(task, schedule)
                val prayerArabic = getPrayerArabicName(task.prayerKey)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_item_${task.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (task.isCompleted)
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (task.isCompleted) 0.dp else 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { onToggleTask(task, it) },
                            modifier = Modifier.testTag("task_checkbox_${task.id}")
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                color = if (task.isCompleted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        text = "$prayerArabic (${formatOffset(task.offsetMinutes)})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "⏰ $resolvedTime",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconButton(onClick = { onDeleteTask(task) }) {
                            Icon(
                                Icons.Outlined.DeleteOutline,
                                contentDescription = "حذف المهمة",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitsTrackerView(
    habits: com.example.data.model.SpiritualHabitEntity?,
    onUpdateHabit: ((com.example.data.model.SpiritualHabitEntity) -> com.example.data.model.SpiritualHabitEntity) -> Unit
) {
    val h = habits ?: com.example.data.model.SpiritualHabitEntity(dateString = "")

    val completedCount = listOf(
        h.fajrDone, h.dhuhrDone, h.asrDone, h.maghribDone, h.ishaDone,
        h.quranDone, h.morningAthkarDone, h.eveningAthkarDone, h.sunnahDone
    ).count { it }

    val progressRatio = completedCount / 9f

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Progress Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("إنجاز العبادات اليومية 🌿", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text("أنجزت $completedCount من 9 عادات روحية", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = "${(progressRatio * 100).toInt()}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        // Section 1: الصلوات المفروضة
        item {
            Text("الصلوات المفروضة", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    HabitCheckRow("صلاة الفجر في وقتها", h.fajrDone) { onUpdateHabit { it.copy(fajrDone = !it.fajrDone) } }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    HabitCheckRow("صلاة الظهر", h.dhuhrDone) { onUpdateHabit { it.copy(dhuhrDone = !it.dhuhrDone) } }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    HabitCheckRow("صلاة العصر", h.asrDone) { onUpdateHabit { it.copy(asrDone = !it.asrDone) } }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    HabitCheckRow("صلاة المغرب", h.maghribDone) { onUpdateHabit { it.copy(maghribDone = !it.maghribDone) } }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    HabitCheckRow("صلاة العشاء والوتر", h.ishaDone) { onUpdateHabit { it.copy(ishaDone = !it.ishaDone) } }
                }
            }
        }

        // Section 2: الأوراد والأذكار
        item {
            Text("الورد القرآني والأذكار", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    HabitCheckRow("قراءة الورد القرآني اليومي 📖", h.quranDone) { onUpdateHabit { it.copy(quranDone = !it.quranDone) } }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    HabitCheckRow("أذكار الصباح ☀️", h.morningAthkarDone) { onUpdateHabit { it.copy(morningAthkarDone = !it.morningAthkarDone) } }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    HabitCheckRow("أذكار المساء 🌙", h.eveningAthkarDone) { onUpdateHabit { it.copy(eveningAthkarDone = !it.eveningAthkarDone) } }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    HabitCheckRow("السنن الرواتب وصلاة الضحى 🕌", h.sunnahDone) { onUpdateHabit { it.copy(sunnahDone = !it.sunnahDone) } }
                }
            }
        }
    }
}

@Composable
private fun HabitCheckRow(
    title: String,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onToggle() }
        )
    }
}

@Composable
private fun AddPrayerTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, prayerKey: String, offsetMinutes: Int, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var prayerKey by remember { mutableStateOf("FAJR") }
    var offsetMinutes by remember { mutableIntStateOf(20) }
    var category by remember { mutableStateOf("عمل") }

    val prayerOptions = listOf(
        "FAJR" to "صلاة الفجر",
        "DHUHR" to "صلاة الظهر",
        "ASR" to "صلاة العصر",
        "MAGHRIB" to "صلاة المغرب",
        "ISHA" to "صلاة العشاء"
    )

    val offsetOptions = listOf(
        -30 to "قبلها بـ 30 دقيقة",
        -15 to "قبلها بـ 15 دقيقة",
        0 to "مع وقت الأذان مباشرة",
        15 to "بعدها بـ 15 دقيقة",
        20 to "بعدها بـ 20 دقيقة",
        30 to "بعدها بـ 30 دقيقة",
        45 to "بعدها بـ 45 دقيقة",
        60 to "بعدها بساعة"
    )

    val categoryOptions = listOf("عمل", "عبادة", "قرآن", "شخصي", "صحة")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة مهمة مربوطة بصلاة", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان المهمة") },
                    placeholder = { Text("مثال: اجتماع فريق العمل أو ورد القرآن") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_task_title_input"),
                    singleLine = true
                )

                Text("اختر الصلاة المرتبطة:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    prayerOptions.forEach { (key, label) ->
                        FilterChip(
                            selected = prayerKey == key,
                            onClick = { prayerKey = key },
                            label = { Text(label.replace("صلاة ", ""), fontSize = 11.sp) },
                            modifier = Modifier.testTag("prayer_chip_$key")
                        )
                    }
                }

                Text("موعد التذكير بالنسبة للصلاة:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(15 to "+15 د", 20 to "+20 د", 30 to "+30 د", -15 to "-15 د").forEach { (offset, label) ->
                        FilterChip(
                            selected = offsetMinutes == offset,
                            onClick = { offsetMinutes = offset },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Text("التصنيف:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categoryOptions.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, prayerKey, offsetMinutes, category)
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_task_btn")
            ) {
                Text("حفظ المهمة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

private fun resolvePrayerTaskTime(task: PrayerTaskEntity, schedule: DailyPrayerSchedule): String {
    val prayer = when (task.prayerKey) {
        "FAJR" -> schedule.fajr
        "DHUHR" -> schedule.dhuhr
        "ASR" -> schedule.asr
        "MAGHRIB" -> schedule.maghrib
        "ISHA" -> schedule.isha
        else -> schedule.fajr
    }

    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, prayer.hour)
    cal.set(Calendar.MINUTE, prayer.minute)
    cal.set(Calendar.SECOND, 0)
    cal.add(Calendar.MINUTE, task.offsetMinutes)

    val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale("ar"))
    return sdf.format(cal.time)
}

private fun getPrayerArabicName(key: String): String = when (key) {
    "FAJR" -> "صلاة الفجر"
    "DHUHR" -> "صلاة الظهر"
    "ASR" -> "صلاة العصر"
    "MAGHRIB" -> "صلاة المغرب"
    "ISHA" -> "صلاة العشاء"
    else -> "الصلاة"
}

private fun formatOffset(offset: Int): String {
    return when {
        offset == 0 -> "مع الأذان"
        offset > 0 -> "بعدها بـ $offset د"
        else -> "قبلها بـ ${-offset} د"
    }
}
