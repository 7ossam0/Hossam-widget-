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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.quran.Ayah
import com.example.data.quran.Surah
import com.example.data.quran.SurahType
import com.example.viewmodel.MainViewModel

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
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Surahs List, 1 = Reader View, 2 = Bookmarks
    var isPlayingAudio by remember { mutableStateOf(false) }
    var playingAyahIndex by remember { mutableIntStateOf(0) }

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
                            fontSize = 20.sp
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع للسور")
                        }
                    }
                },
                actions = {
                    if (selectedTab == 1) {
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
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Selector (السور، القراءة، الفواصل)
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
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
                    1 -> QuranReaderView(
                        surah = currentSurah,
                        ayahs = ayahs,
                        isPlaying = isPlayingAudio,
                        playingAyahIndex = playingAyahIndex,
                        onAyahClick = { ayah ->
                            viewModel.showTafsirForAyah(ayah)
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

    // Tafsir Bottom Sheet Modal
    selectedAyahForTafsir?.let { ayah ->
        TafsirDetailModal(
            ayah = ayah,
            onDismiss = { viewModel.showTafsirForAyah(null) },
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
                        // Surah Number Badge
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = surah.number.toString(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 15.sp
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

@Composable
private fun QuranReaderView(
    surah: Surah,
    ayahs: List<Ayah>,
    isPlaying: Boolean,
    playingAyahIndex: Int,
    onAyahClick: (Ayah) -> Unit,
    onBookmarkAyah: (Ayah) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Surah Header Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "سورة ${surah.nameArabic}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${surah.type.labelArabic} • ${surah.versesCount} آيات • الجزء ${surah.juzNumber}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )

                    if (surah.number != 9 && surah.number != 1) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Ayahs List
        items(ayahs, key = { "${it.surahNumber}_${it.ayahNumber}" }) { ayah ->
            val isHighlighted = isPlaying && ayah.ayahNumber == (playingAyahIndex + 1)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAyahClick(ayah) }
                    .testTag("ayah_item_${ayah.ayahNumber}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isHighlighted)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    else
                        MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isHighlighted) 4.dp else 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Ayah Badge
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ayah.ayahNumber.toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row {
                            IconButton(onClick = { onBookmarkAyah(ayah) }) {
                                Icon(
                                    Icons.Outlined.BookmarkBorder,
                                    contentDescription = "حفظ فاصلة",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { onAyahClick(ayah) }) {
                                Icon(
                                    Icons.Outlined.MenuBook,
                                    contentDescription = "التفسير",
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Uthmani Text
                    Text(
                        text = ayah.textUthmani,
                        fontSize = 22.sp,
                        lineHeight = 40.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "اضغط على الآية لعرض التفسير الميسر وأسباب النزول 💡",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

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
                    text = "انقر على رمز الفاصلة بجانب أي آية أثناء القراءة لحفظ موضعك",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TafsirDetailModal(
    ayah: Ayah,
    onDismiss: () -> Unit,
    onBookmark: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "تفسير سورة ${ayah.surahName} - آية ${ayah.ayahNumber}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "التفسير الميسر المعتمد (مركز تفسير)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ayah Text Box
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = ayah.textUthmani,
                    fontSize = 20.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tafsir text
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

            // Asbab Al-Nuzul if present
            ayah.asbabNuzul?.let { asbab ->
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "سبب النزول / المناسبة:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFFD97706)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = asbab,
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        clipboard.setText(AnnotatedString("${ayah.textUthmani}\n[سورة ${ayah.surahName}: ${ayah.ayahNumber}]\nالتفسير: ${ayah.tafsirMuyassar}"))
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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
