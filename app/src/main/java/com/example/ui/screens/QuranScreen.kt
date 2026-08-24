package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.quran.Ayah
import com.example.data.quran.Surah
import com.example.data.quran.SurahType
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

import com.example.ui.theme.QuranHafsFontFamily

/**
 * أنماط القراءة للمصحف الشريف: نهاري (Classic Cream) / ليلي (Night Slate) / سيبيا (Warm Parchment)
 */
enum class MushafTheme(
    val label: String,
    val icon: ImageVector,
    val pageBackground: Color,
    val cardBackground: Color,
    val textColor: Color,
    val surahHeaderBg: Color,
    val surahHeaderText: Color,
    val ayahNumberColor: Color,
    val frameBorderColor: Color,
    val highlightBg: Color,
    val highlightText: Color
) {
    DAY(
        label = "نهاري",
        icon = Icons.Outlined.LightMode,
        pageBackground = Color(0xFFFDFBF7),
        cardBackground = Color(0xFFF6EFE2),
        textColor = Color(0xFF1C1917),
        surahHeaderBg = Color(0xFFEFE5D0),
        surahHeaderText = Color(0xFF78350F),
        ayahNumberColor = Color(0xFFB45309),
        frameBorderColor = Color(0xFFDCC8A0),
        highlightBg = Color(0xFFFEF3C7),
        highlightText = Color(0xFF78350F)
    ),
    NIGHT(
        label = "ليلي",
        icon = Icons.Outlined.DarkMode,
        pageBackground = Color(0xFF0F172A),
        cardBackground = Color(0xFF1E293B),
        textColor = Color(0xFFF1F5F9),
        surahHeaderBg = Color(0xFF1E293B),
        surahHeaderText = Color(0xFF38BDF8),
        ayahNumberColor = Color(0xFF34D399),
        frameBorderColor = Color(0xFF334155),
        highlightBg = Color(0xFF1E3A8A),
        highlightText = Color(0xFF67E8F9)
    ),
    SEPIA(
        label = "سيبيا",
        icon = Icons.Outlined.WbIncandescent,
        pageBackground = Color(0xFFF4ECD8),
        cardBackground = Color(0xFFEADFCA),
        textColor = Color(0xFF2C2216),
        surahHeaderBg = Color(0xFFDFD1B5),
        surahHeaderText = Color(0xFF854D0E),
        ayahNumberColor = Color(0xFF92400E),
        frameBorderColor = Color(0xFFD3BE96),
        highlightBg = Color(0xFFDFCEAA),
        highlightText = Color(0xFF451A03)
    )
}

/**
 * تحويل الأرقام إلى الأرقام العربية المشرقية (١، ٢، ٣...)
 */
fun Int.toArabicDigits(): String {
    val arabicNumerals = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return this.toString().map { char ->
        if (char in '0'..'9') arabicNumerals[char - '0'] else char
    }.joinToString("")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val surahs = viewModel.quranSurahs
    val selectedSurahNumber by viewModel.selectedSurahNumber.collectAsStateWithLifecycle()
    val ayahs by viewModel.currentSurahAyahs.collectAsStateWithLifecycle()
    val bookmarks by viewModel.quranBookmarks.collectAsStateWithLifecycle()
    val selectedAyahForTafsir by viewModel.selectedAyahForTafsir.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Surahs List, 1 = Mushaf Continuous Reader, 2 = Bookmarks
    var isPlayingAudio by remember { mutableStateOf(false) }
    var playingAyahIndex by remember { mutableIntStateOf(0) }

    // تخصيص القراءة للمصحف: الوضع (نهاري / ليلي / سيبيا) وحجم الخط
    var mushafTheme by remember { mutableStateOf(MushafTheme.DAY) }
    var fontSizeSp by remember { mutableFloatStateOf(24f) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var clickedAyahForHighlight by remember { mutableStateOf<Ayah?>(null) }

    val currentSurah = surahs.firstOrNull { it.number == selectedSurahNumber } ?: surahs[0]

    val filteredSurahs = remember(searchQuery, surahs) {
        if (searchQuery.isBlank()) surahs
        else surahs.filter {
            it.nameArabic.contains(searchQuery) ||
            it.nameEnglish.contains(searchQuery, ignoreCase = true) ||
            it.number.toString().contains(searchQuery)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (selectedTab == 1) "سورة ${currentSurah.nameArabic}" else "المصحف الشريف",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        )
                        if (selectedTab == 1) {
                            Text(
                                text = "${currentSurah.type.labelArabic} • ${currentSurah.versesCount} آية • الجزء ${currentSurah.juzNumber} • ص ${currentSurah.pageNumber}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (selectedTab == 1) {
                        IconButton(
                            onClick = { selectedTab = 0 },
                            modifier = Modifier.testTag("back_to_surah_list")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "فهرس السور")
                        }
                    }
                },
                actions = {
                    if (selectedTab == 1) {
                        // زر التبديل السريع بين الوضع النهاري والليلي والسيبيا
                        IconButton(
                            onClick = {
                                mushafTheme = when (mushafTheme) {
                                    MushafTheme.DAY -> MushafTheme.NIGHT
                                    MushafTheme.NIGHT -> MushafTheme.SEPIA
                                    MushafTheme.SEPIA -> MushafTheme.DAY
                                }
                            },
                            modifier = Modifier.testTag("toggle_mushaf_theme_btn")
                        ) {
                            Icon(
                                imageVector = mushafTheme.icon,
                                contentDescription = "تغيير الوضع (نهاري/ليلي)",
                                tint = when (mushafTheme) {
                                    MushafTheme.DAY -> Color(0xFFF59E0B)
                                    MushafTheme.NIGHT -> Color(0xFF60A5FA)
                                    MushafTheme.SEPIA -> Color(0xFFD97706)
                                }
                            )
                        }

                        // زر إعدادات الخط والتخصيص
                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier.testTag("mushaf_text_settings_btn")
                        ) {
                            Icon(Icons.Outlined.FormatSize, contentDescription = "حجم الخط")
                        }

                        // زر الاستماع للتلاوة
                        IconButton(
                            onClick = {
                                isPlayingAudio = !isPlayingAudio
                                if (isPlayingAudio) playingAyahIndex = 0
                            },
                            modifier = Modifier.testTag("play_quran_audio_btn")
                        ) {
                            Icon(
                                if (isPlayingAudio) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                                contentDescription = "استماع للتلاوة",
                                tint = if (isPlayingAudio) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (selectedTab == 1) mushafTheme.pageBackground else MaterialTheme.colorScheme.surface,
                    titleContentColor = if (selectedTab == 1) mushafTheme.textColor else MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = if (selectedTab == 1) mushafTheme.textColor else MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = if (selectedTab == 1) mushafTheme.textColor else MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(if (selectedTab == 1) mushafTheme.pageBackground else MaterialTheme.colorScheme.surface)
        ) {
            // شريط التبويب العلوي
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = if (selectedTab == 1) mushafTheme.cardBackground else MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("فهرس السور (${surahs.size})", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Outlined.FormatListNumbered, contentDescription = null) },
                    modifier = Modifier.testTag("tab_surah_list")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(currentSurah.nameArabic, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Outlined.MenuBook, contentDescription = null) },
                    modifier = Modifier.testTag("tab_reader")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("الفواصل (${bookmarks.size})", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Outlined.Bookmark, contentDescription = null) },
                    modifier = Modifier.testTag("tab_bookmarks")
                )
            }

            AnimatedContent(
                targetState = selectedTab,
                label = "QuranTabContent"
            ) { tab ->
                when (tab) {
                    0 -> SurahsListView(
                        surahs = filteredSurahs,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onSurahSelected = { surah ->
                            viewModel.selectSurah(surah.number)
                            selectedTab = 1
                        }
                    )
                    1 -> ContinuousMushafReaderView(
                        surah = currentSurah,
                        ayahs = ayahs,
                        surahsList = surahs,
                        theme = mushafTheme,
                        fontSizeSp = fontSizeSp,
                        highlightedAyah = clickedAyahForHighlight,
                        onAyahClick = { ayah ->
                            clickedAyahForHighlight = ayah
                            viewModel.showTafsirForAyah(ayah)
                        },
                        onSelectSurah = { surahNum ->
                            viewModel.selectSurah(surahNum)
                        },
                        onBookmarkAyah = { ayah ->
                            viewModel.addBookmark(
                                surahNumber = ayah.surahNumber,
                                surahName = ayah.surahName,
                                ayahNumber = ayah.ayahNumber,
                                note = "فاصلة قراءة في الآية ${ayah.ayahNumber}"
                            )
                        }
                    )
                    2 -> BookmarksListView(
                        bookmarks = bookmarks,
                        onBookmarkClick = { b ->
                            viewModel.selectSurah(b.surahNumber)
                            selectedTab = 1
                        },
                        onDeleteBookmark = { b ->
                            viewModel.deleteBookmark(b.id)
                        }
                    )
                }
            }
        }
    }

    // نافذة إعدادات الخط والوضع النهاري/الليلي
    if (showSettingsDialog) {
        MushafSettingsBottomSheet(
            currentTheme = mushafTheme,
            currentFontSize = fontSizeSp,
            onThemeChange = { mushafTheme = it },
            onFontSizeChange = { fontSizeSp = it },
            onDismiss = { showSettingsDialog = false }
        )
    }

    // نافذة التفسير الميسر عند الضغط على أي آية
    selectedAyahForTafsir?.let { ayah ->
        TafsirDetailModal(
            ayah = ayah,
            theme = mushafTheme,
            onDismiss = {
                viewModel.showTafsirForAyah(null)
            },
            onBookmark = {
                viewModel.addBookmark(
                    surahNumber = ayah.surahNumber,
                    surahName = ayah.surahName,
                    ayahNumber = ayah.ayahNumber,
                    note = "فاصلة مرجعية مع التفسير"
                )
            }
        )
    }
}

/**
 * واجهة المصحف الشريف المتتابع (الآيات متتابعة مثل صفحات المصحف الشريف تماماً)
 */
@Composable
private fun ContinuousMushafReaderView(
    surah: Surah,
    ayahs: List<Ayah>,
    surahsList: List<Surah>,
    theme: MushafTheme,
    fontSizeSp: Float,
    highlightedAyah: Ayah?,
    onAyahClick: (Ayah) -> Unit,
    onSelectSurah: (Int) -> Unit,
    onBookmarkAyah: (Ayah) -> Unit
) {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showJumpDialog by remember { mutableStateOf(false) }

    val previousSurah = surahsList.firstOrNull { it.number == surah.number - 1 }
    val nextSurah = surahsList.firstOrNull { it.number == surah.number + 1 }

    // تجميع آيات السورة حسب صفحات المصحف الشريف الرسمية
    val pagesGroup = remember(ayahs) {
        if (ayahs.isEmpty()) {
            emptyMap()
        } else {
            ayahs.groupBy { it.pageNumber }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(theme.pageBackground),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // شريط الأدوات السريع أعلى القارئ
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = { showJumpDialog = true },
                        label = { Text("انتقال سريع لآية / صفحة", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Navigation,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = theme.ayahNumberColor
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = theme.cardBackground,
                            labelColor = theme.textColor
                        ),
                        border = BorderStroke(1.dp, theme.frameBorderColor)
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = theme.cardBackground,
                        border = BorderStroke(1.dp, theme.frameBorderColor)
                    ) {
                        Text(
                            text = "${ayahs.size} من ${surah.versesCount} آية",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.ayahNumberColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // برواز السورة الكلاسيكي وتفاصيلها الفلكية
            item {
                SurahDecorativeHeader(surah = surah, theme = theme)
                Spacer(modifier = Modifier.height(10.dp))
            }

            // البسملة الشريفة (لكل السور عدا التوبة والفاتحة حيث الفاتحة آيتها الأولى البسملة)
            if (surah.number != 9 && surah.number != 1) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                            fontSize = (fontSizeSp * 0.95f).sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.surahHeaderText,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // صفحات المصحف الشريف المتتابعة
            if (pagesGroup.isNotEmpty()) {
                pagesGroup.forEach { (pageNumber, pageAyahs) ->
                    item(key = "mushaf_page_$pageNumber") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .border(BorderStroke(1.5.dp, theme.frameBorderColor), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = theme.cardBackground),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                // ترويسة صفحة المصحف (الجزء والسورة)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "الجزء ${pageAyahs.firstOrNull()?.juzNumber ?: surah.juzNumber}",
                                        fontSize = 11.sp,
                                        color = theme.surahHeaderText,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "سورة ${surah.nameArabic}",
                                        fontSize = 11.sp,
                                        color = theme.surahHeaderText,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                HorizontalDivider(
                                    color = theme.frameBorderColor.copy(alpha = 0.5f),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )

                                // النص القرآني المتتابع للصفحة
                                InteractiveMushafText(
                                    ayahs = pageAyahs,
                                    theme = theme,
                                    fontSizeSp = fontSizeSp,
                                    highlightedAyahNumber = highlightedAyah?.ayahNumber,
                                    onAyahClick = onAyahClick
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // رقم الصفحة القرآني المذهّب
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "— ${pageNumber.toArabicDigits()} —",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.ayahNumberColor
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // حالة التحميل أو العرض الاحتياطي
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.5.dp, theme.frameBorderColor), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = theme.cardBackground)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            InteractiveMushafText(
                                ayahs = ayahs,
                                theme = theme,
                                fontSizeSp = fontSizeSp,
                                highlightedAyahNumber = highlightedAyah?.ayahNumber,
                                onAyahClick = onAyahClick
                            )
                        }
                    }
                }
            }

            // أزرار التنقل السريع بين السور (السابقة / التالية)
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (previousSurah != null) {
                        OutlinedButton(
                            onClick = { onSelectSurah(previousSurah.number) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = theme.textColor
                            ),
                            border = BorderStroke(1.dp, theme.frameBorderColor),
                            modifier = Modifier.testTag("btn_prev_surah")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("سورة ${previousSurah.nameArabic}", fontSize = 13.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (nextSurah != null) {
                        Button(
                            onClick = { onSelectSurah(nextSurah.number) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = theme.surahHeaderBg,
                                contentColor = theme.surahHeaderText
                            ),
                            modifier = Modifier.testTag("btn_next_surah")
                        ) {
                            Text("سورة ${nextSurah.nameArabic}", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // نافذة الانتقال السريع
    if (showJumpDialog) {
        MushafJumpDialog(
            surah = surah,
            surahsList = surahsList,
            onDismiss = { showJumpDialog = false },
            onJumpToAyah = { targetAyahNum ->
                showJumpDialog = false
                val targetPage = ayahs.firstOrNull { it.ayahNumber == targetAyahNum }?.pageNumber
                val pageIndex = if (targetPage != null) {
                    pagesGroup.keys.indexOf(targetPage).coerceAtLeast(0) + 2 // +2 for header items
                } else {
                    0
                }
                coroutineScope.launch {
                    listState.animateScrollToItem(pageIndex)
                }
            },
            onJumpToSurah = { targetSurahNum ->
                showJumpDialog = false
                onSelectSurah(targetSurahNum)
            }
        )
    }
}

/**
 * نافذة الانتقال السريع إلى أي آية أو سورة
 */
@Composable
private fun MushafJumpDialog(
    surah: Surah,
    surahsList: List<Surah>,
    onDismiss: () -> Unit,
    onJumpToAyah: (Int) -> Unit,
    onJumpToSurah: (Int) -> Unit
) {
    var ayahInput by remember { mutableStateOf("") }
    var selectedSurahForJump by remember { mutableIntStateOf(surah.number) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Navigation, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("الانتقال السريع في المصحف", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "سورة ${surah.nameArabic} (١ - ${surah.versesCount} آية):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = ayahInput,
                    onValueChange = { ayahInput = it.filter { char -> char.isDigit() } },
                    label = { Text("رقم الآية (١ - ${surah.versesCount})") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "أو اختر سورة أخرى:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                var expandedSurahMenu by remember { mutableStateOf(false) }
                val currentJumpSurah = surahsList.firstOrNull { it.number == selectedSurahForJump } ?: surah

                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedSurahMenu = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${currentJumpSurah.number}. سورة ${currentJumpSurah.nameArabic}", fontWeight = FontWeight.Bold)
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                    }
                }

                DropdownMenu(
                    expanded = expandedSurahMenu,
                    onDismissRequest = { expandedSurahMenu = false },
                    modifier = Modifier.heightIn(max = 260.dp)
                ) {
                    surahsList.forEach { s ->
                        DropdownMenuItem(
                            text = { Text("${s.number}. ${s.nameArabic} (${s.versesCount} آية)") },
                            onClick = {
                                selectedSurahForJump = s.number
                                expandedSurahMenu = false
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedSurahForJump != surah.number) {
                        onJumpToSurah(selectedSurahForJump)
                    } else {
                        val num = ayahInput.toIntOrNull()
                        if (num != null && num in 1..surah.versesCount) {
                            onJumpToAyah(num)
                        } else {
                            onDismiss()
                        }
                    }
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("انتقال")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

/**
 * النص القرآني المتتابع مع دعم النقر المباشر على أي آية وتظليلها
 */
@Composable
private fun InteractiveMushafText(
    ayahs: List<Ayah>,
    theme: MushafTheme,
    fontSizeSp: Float,
    highlightedAyahNumber: Int?,
    onAyahClick: (Ayah) -> Unit
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    // بناء النص القرآني المتتابع الكامل مع رموز نهاية الآيات
    val annotatedString = remember(ayahs, theme, fontSizeSp, highlightedAyahNumber) {
        buildAnnotatedString {
            ayahs.forEach { ayah ->
                val isHighlighted = highlightedAyahNumber == ayah.ayahNumber

                // تحديد نطاق الآية للتعرف على النقر
                pushStringAnnotation(tag = "AYAH_CLICK", annotation = ayah.ayahNumber.toString())

                // نص الآية الكريمة
                withStyle(
                    SpanStyle(
                        color = if (isHighlighted) theme.highlightText else theme.textColor,
                        background = if (isHighlighted) theme.highlightBg else Color.Transparent,
                        fontSize = fontSizeSp.sp,
                        fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium
                    )
                ) {
                    append(ayah.textUthmani)
                }

                append(" ")

                // رمز وشارة رقم الآية المشرقية القرآني
                withStyle(
                    SpanStyle(
                        color = if (isHighlighted) theme.highlightText else theme.ayahNumberColor,
                        background = if (isHighlighted) theme.highlightBg else Color.Transparent,
                        fontSize = (fontSizeSp * 0.82f).sp,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append("﴿${ayah.ayahNumber.toArabicDigits()}﴾")
                }

                pop()
                append("   ")
            }
        }
    }

    Text(
        text = annotatedString,
        fontFamily = QuranHafsFontFamily,
        lineHeight = (fontSizeSp * 1.95f).sp,
        textAlign = TextAlign.Justify,
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(annotatedString) {
                detectTapGestures { pos ->
                    textLayoutResult?.let { layoutResult ->
                        val offset = layoutResult.getOffsetForPosition(pos)
                        annotatedString.getStringAnnotations(tag = "AYAH_CLICK", start = offset, end = offset)
                            .firstOrNull()?.let { annotation ->
                                val clickedAyahNum = annotation.item.toIntOrNull()
                                val clickedAyah = ayahs.firstOrNull { it.ayahNumber == clickedAyahNum }
                                if (clickedAyah != null) {
                                    onAyahClick(clickedAyah)
                                }
                            }
                    }
                }
            }
            .testTag("continuous_mushaf_text_flow"),
        onTextLayout = { textLayoutResult = it }
    )
}

/**
 * برواز السورة المزخرف في أعلى الصفحة
 */
@Composable
private fun SurahDecorativeHeader(
    surah: Surah,
    theme: MushafTheme
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.5.dp, theme.frameBorderColor), RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surahHeaderBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // زخرفة أعلى العنوان
            Text(
                text = "❖ ❖ ❖",
                fontSize = 12.sp,
                color = theme.surahHeaderText.copy(alpha = 0.6f),
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            // اسم السورة
            Text(
                text = "سُورَةُ ${surah.nameArabic}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = theme.surahHeaderText
            )

            Spacer(modifier = Modifier.height(6.dp))

            // تفاصيل السورة (النزول، عدد الآيات، الجزء، الصفحة)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = theme.cardBackground
                ) {
                    Text(
                        text = surah.type.labelArabic,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.surahHeaderText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Text("•", color = theme.surahHeaderText)

                Text(
                    text = "${surah.versesCount} آيات",
                    fontSize = 12.sp,
                    color = theme.surahHeaderText
                )

                Text("•", color = theme.surahHeaderText)

                Text(
                    text = "الجزء ${surah.juzNumber}",
                    fontSize = 12.sp,
                    color = theme.surahHeaderText
                )

                Text("•", color = theme.surahHeaderText)

                Text(
                    text = "ص ${surah.pageNumber}",
                    fontSize = 12.sp,
                    color = theme.surahHeaderText
                )
            }
        }
    }
}

/**
 * فهرس السور الشريفة مع البحث السريع
 */
@Composable
private fun SurahsListView(
    surahs: List<Surah>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSurahSelected: (Surah) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("surah_search_input"),
            placeholder = { Text("ابحث عن سورة أو رقمها...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "مسح")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(surahs, key = { it.number }) { surah ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSurahSelected(surah) }
                        .testTag("surah_item_${surah.number}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // شارة رقم السورة
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = surah.number.toArabicDigits(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "سورة ${surah.nameArabic}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${surah.nameEnglish} • ${surah.type.labelArabic} • ${surah.versesCount} آيات",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "ص ${surah.pageNumber}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "جزء ${surah.juzNumber}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * نافذة الفواصل المحفوظة
 */
@Composable
private fun BookmarksListView(
    bookmarks: List<com.example.data.model.QuranBookmarkEntity>,
    onBookmarkClick: (com.example.data.model.QuranBookmarkEntity) -> Unit,
    onDeleteBookmark: (com.example.data.model.QuranBookmarkEntity) -> Unit
) {
    if (bookmarks.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "لا توجد فواصل محفوظة بعد",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "انقر على أي آية أثناء القراءة لحفظ فاصلة قراءة مرجعية",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(bookmarks, key = { it.id }) { b ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBookmarkClick(b) },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Bookmark,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "سورة ${b.surahName} - الآية ${b.ayahNumber}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            if (b.note.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = b.note,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { onDeleteBookmark(b) }) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "حذف",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * نافذة التفسير الميسر المنبثقة عند الضغط على أي آية
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TafsirDetailModal(
    ayah: Ayah,
    theme: MushafTheme,
    onDismiss: () -> Unit,
    onBookmark: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // شريط العنوان
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "تفسير سورة ${ayah.surahName} - الآية ${ayah.ayahNumber.toArabicDigits()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "التفسير الميسر المعتمد (مجمع الملك فهد / مركز تفسير)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // صندوق نص الآية الكريمة بالرسم العثماني
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${ayah.textUthmani} ﴿${ayah.ayahNumber.toArabicDigits()}﴾",
                    fontFamily = QuranHafsFontFamily,
                    fontSize = 22.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Right,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // نص التفسير الميسر
            Text(
                text = "المعنى والتفسير الميسر:",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = ayah.tafsirMuyassar,
                fontSize = 15.sp,
                lineHeight = 26.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // سبب النزول إن وُجد
            ayah.asbabNuzul?.let { asbab ->
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF3C7).copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "مناسبة الآية / سبب النزول 📜:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFB45309)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = asbab,
                            fontSize = 13.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF78350F)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // أزرار الإجراءات السريعة (نسخ / حفظ فاصلة)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        clipboard.setText(AnnotatedString("${ayah.textUthmani} ﴿${ayah.ayahNumber}﴾\n[سورة ${ayah.surahName}: ${ayah.ayahNumber}]\nالتفسير: ${ayah.tafsirMuyassar}"))
                        copied = true
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(if (copied) Icons.Default.Check else Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (copied) "تم النسخ" else "نسخ الآية")
                }

                Button(
                    onClick = {
                        onBookmark()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("حفظ فاصلة")
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

/**
 * نافذة تخصيص القراءة (الوضع النهاري/الليلي وحجم الخط)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MushafSettingsBottomSheet(
    currentTheme: MushafTheme,
    currentFontSize: Float,
    onThemeChange: (MushafTheme) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "إعدادات المصحف والقراءة 📖",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // اختيار نمط القراءة (نهاري / ليلي / سيبيا)
            Text(
                text = "وضع القراءة والسمة:",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MushafTheme.values().forEach { theme ->
                    val isSelected = currentTheme == theme
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onThemeChange(theme) },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.4f)
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = theme.cardBackground
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = theme.icon,
                                contentDescription = theme.label,
                                tint = theme.textColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = theme.label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = theme.textColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // حجم الخط القرآني
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "حجم الخط القرآني:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${currentFontSize.toInt()} sp",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("A-", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = currentFontSize,
                    onValueChange = onFontSizeChange,
                    valueRange = 18f..38f,
                    steps = 9,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                )
                Text("A+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
