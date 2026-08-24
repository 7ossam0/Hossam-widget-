package com.example.data.ai

data class WisdomTopic(
    val title: String,
    val description: String,
    val category: String,
    val quranAyah: String,
    val surahAndAyah: String,
    val tafsirSummary: String,
    val hadithCitation: String,
    val hadithSource: String,
    val practicalSpiritualTip: String
)

object IslamicWisdomAssistant {

    val topics: List<WisdomTopic> = listOf(
        WisdomTopic(
            title = "التعامل مع القلق والضيق النفسي",
            description = "كيف يوجه القرآن المسلم عند اضطراب النفس وتكاثر الهموم؟",
            category = "السكينة والطمأنينة",
            quranAyah = "ٱلَّذِينَ ءَامَنُوا۟ وَتَطْمَئِنُّ قُلُوبُهُم بِذِكْرِ ٱللَّهِ ۗ أَلَا بِذِكْرِ ٱللَّهِ تَطْمَئِنُّ ٱلْقُلُوبُ",
            surahAndAyah = "سورة الرعد - الآية 28",
            tafsirSummary = "تفسير السعدي: أي يزول قلقها واضطرابها وتحضرها البهجة والسرور بذكر الله ومحبته ومعرفته.",
            hadithCitation = "«ما أصاب أحداً قط هم ولا حزن فقال: اللهم إني عبدك ابن عبدك ابن أمتك، ناصيتي بيدك، ماض في حكمك، عدل في قضاؤك... إلا أذهب الله همه وأبدله مكانه فرجاً».",
            hadithSource = "مسند الإمام أحمد وصححه الألباني",
            practicalSpiritualTip = "ابدأ بورد استغفار 100 مرة مع صلاة ركعتين خاشعتين في خلوة تامة."
        ),
        WisdomTopic(
            title = "الصبر عند الشدائد وتأخر الفرج",
            description = "البصيرة القرآنية في معنى الصبر الجميل وجزاء الصابرين.",
            category = "الصبر واليقين",
            quranAyah = "فَإِنَّ مَعَ ٱلْعُسْرِ يُسْرًا ۝ إِنَّ مَعَ ٱلْعُسْرِ يُسْرًا",
            surahAndAyah = "سورة الشرح - الآيتان 5 و 6",
            tafsirSummary = "التفسير الميسر: إن مع الضيق والشدة سهولة ورخاء، ولن يغلب عسر يسرين.",
            hadithCitation = "«واعلم أن النصر مع الصبر، وأن الفرج مع الكرب، وأن مع العسر يسراً».",
            hadithSource = "سنن الترمذي - حديث صحيح",
            practicalSpiritualTip = "اربط همتك بالدعاء بعد كل أذان مباشرة؛ فوقت الأذان من مواطن الإجابة المحققة."
        ),
        WisdomTopic(
            title = "جلب الرزق والبركة في السعي",
            description = "المفاتيح الشرعية لزيادة الرزق والطمأنينة في الكسب الحلال.",
            category = "الرزق والعمل",
            quranAyah = "وَمَن يَتَّقِ ٱللَّهَ يَجْعَل لَّهُۥ مَخْرَجًۭا ۝ وَيَرْزُقْهُ مِنْ حَيْثُ لَا يَحْتَسِبُ",
            surahAndAyah = "سورة الطلاق - الآيتان 2 و 3",
            tafsirSummary = "تفسير ابن كثير: من اتقى الله فيما أمره به وترك ما نهاه عنه، جعل له من كل ضيق فرجاً ورزقه من جهة لا تخطر بباله.",
            hadithCitation = "«من أحب أن يبسط له في رزقه وينسأ له في أثره فليصل رحمه».",
            hadithSource = "صحيح البخاري ومسلم",
            practicalSpiritualTip = "احرص على أذكار الصباح في وقتها، وصلة رحم باتصال أو رسالة تسهم في مضاعفة البركة."
        ),
        WisdomTopic(
            title = "التوبة وتجديد الإيمان بعد الذنب",
            description = "سعة رحمة الله وقبول التائبين دون يأس أو قنوط.",
            category = "المغفرة والرجوع",
            quranAyah = "قُلْ يَـٰعِبَادِىَ ٱلَّذِينَ أَسْرَفُوا۟ عَلَىٰٓ أَنفُسِهِمْ لَا تَقْنَطُوا۟ مِن رَّحْمَةِ ٱللَّهِ ۚ إِنَّ ٱللَّهَ يَغْفِرُ ٱلذُّنُوبَ جَمِيعًا",
            surahAndAyah = "سورة الزمر - الآية 53",
            tafsirSummary = "التفسير الميسر: دعوة عامة لكل العصاة والمذنبين بالإنابة والتوبة، وبيان أن الله يغفر جميع الذنوب لمن تاب منها.",
            hadithCitation = "«التائب من الذنب كمن لا ذنب له».",
            hadithSource = "سنن ابن ماجه وحسنه الألباني",
            practicalSpiritualTip = "توضأ وضوءك للصلاة وصل ركعتي توبة، وأتبع السيئة الحسنة تمحها."
        ),
        WisdomTopic(
            title = "بر الوالدين وحسن المعاملة",
            description = "مكانة الوالدين وحقهما في القرآن والسنة المطهرة.",
            category = "الأخلاق والمعاملات",
            quranAyah = "وَقَضَىٰ رَبُّكَ أَلَّا تَعْبُدُوٓا۟ إِلَّآ إِيَّاهُ وَبِٱلْوَٰلِدَيْنِ إِحْسَـٰنًا",
            surahAndAyah = "سورة الإسراء - الآية 23",
            tafsirSummary = "تفسير الطبري: أمر الله تعالى بإفراده بالعبادة تلوه مباشرة بالأمر بالبر والإحسان بالوالدين وخفض الجناح لهما.",
            hadithCitation = "«رِضَا الرَّبِّ في رِضَا الوَالِدَيْنِ، وسَخَطُ الرَّبِّ في سَخَطِهِمَا».",
            hadithSource = "سنن الترمذي وصححه الحاكم",
            practicalSpiritualTip = "خص والديك بدعوة بعد كل فريضة: {رَّبِّ ٱرْحَمْهُمَا كَمَا رَبَّيَانِى صَغِيرًا}."
        ),
        WisdomTopic(
            title = "أوقات استجابة الدعاء وآدابه",
            description = "كيف تحري الأوقات الفاضلة التي وعد الله فيها بإجابة السائلين؟",
            category = "الدعاء والمناجاة",
            quranAyah = "وَإِذَا سَأَلَكَ عِبَادِى عَنِّى فَإِنِّى قَرِيبٌ ۖ أُجِيبُ دَعْوَةَ ٱلدَّاعِ إِذَا دَعَانِ",
            surahAndAyah = "سورة البقرة - الآية 186",
            tafsirSummary = "تفسير السعدي: قرب الله من عباده وسمعه لندائهم، وإجابته الصادقة لدعوة الداعي بلا واسطة.",
            hadithCitation = "«ينزل ربنا تبارك وتعالى كل ليلة إلى السماء الدنيا حين يبقى ثلث الليل الآخر فيقول: من يدعوني فأستجيب له...».",
            hadithSource = "صحيح البخاري ومسلم",
            practicalSpiritualTip = "استغل الدقائق الأخيرة قبل أذان الفجر، وما بين الأذان والإقامة للدعاء بيقين تام."
        )
    )

    fun searchWisdom(query: String): List<WisdomTopic> {
        val q = query.trim()
        if (q.isEmpty()) return topics
        return topics.filter {
            it.title.contains(q, ignoreCase = true) ||
            it.description.contains(q, ignoreCase = true) ||
            it.category.contains(q, ignoreCase = true) ||
            it.quranAyah.contains(q, ignoreCase = true) ||
            it.tafsirSummary.contains(q, ignoreCase = true) ||
            it.hadithCitation.contains(q, ignoreCase = true)
        }
    }
}
