package com.example.data.sample

import com.example.data.model.CategoryEntity
import com.example.data.model.ContentItemEntity

object SampleData {
    val initialCategories = listOf(
        CategoryEntity(id = 1, name = "أذكار", sortOrder = 1, colorHex = "#10B981"),
        CategoryEntity(id = 2, name = "أدعية", sortOrder = 2, colorHex = "#3B82F6"),
        CategoryEntity(id = 3, name = "قرآن كريم", sortOrder = 3, colorHex = "#8B5CF6"),
        CategoryEntity(id = 4, name = "أحاديث نبوية", sortOrder = 4, colorHex = "#EC4899"),
        CategoryEntity(id = 5, name = "اقتباسات وحكم", sortOrder = 5, colorHex = "#F59E0B"),
        CategoryEntity(id = 6, name = "ملاحظاتي الشخصية", sortOrder = 6, colorHex = "#64748B")
    )

    val initialContentItems = listOf(
        // أذكار
        ContentItemEntity(
            id = 1,
            title = "دعاء الصباح والمساء",
            body = "اللَّهُمَّ بِكَ أَصْبَحْنَا، وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ النُّشُورُ.",
            categoryId = 1,
            sortOrder = 1,
            isFavorite = true
        ),
        ContentItemEntity(
            id = 2,
            title = "آية الكرسي - حفظ وحماية",
            body = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ.",
            categoryId = 1,
            sortOrder = 2,
            isFavorite = true
        ),
        ContentItemEntity(
            id = 3,
            title = "سيد الاستغفار (نص طويل للودجت)",
            body = "اللَّهُمَّ أَنْتَ رَبِّي لاَ إِلَهَ إِلاَّ أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لاَ يَغْفِرُ الذُّنُوبَ إِلاَّ أَنْتَ.\n\nمن قالها موقناً بها حين يُمسي فمات من ليلته دخل الجنة، ومن قالها موقناً بها حين يُصبح فمات من يومه دخل الجنة.",
            categoryId = 1,
            sortOrder = 3,
            isFavorite = false
        ),

        // أدعية
        ContentItemEntity(
            id = 4,
            title = "دعاء التيسير وراحة البال",
            body = "اللَّهُمَّ لا سَهْلَ إِلاَّ مَا جَعَلْتَهُ سَهْلاً، وَأَنْتَ تَجْعَلُ الْحَزْنَ إِذَا شِئْتَ سَهْلاً. اللهم اشرح لي صدري ويسر لي أمري واحلل عقدة من لساني يفقهوا قولي.",
            categoryId = 2,
            sortOrder = 1,
            isFavorite = true
        ),
        ContentItemEntity(
            id = 5,
            title = "دعاء طلب الرزق والبركة",
            body = "اللَّهُمَّ اكْفِنِي بِحَلالِكَ عَنْ حَرَامِكَ، وَأَغْنِنِي بِفَضْلِكَ عَمَّنْ سِوَاكَ. اللهم صبّ علينا الخير صباً صباً ولا تجعل عيشنا كداً كداً.",
            categoryId = 2,
            sortOrder = 2,
            isFavorite = false
        ),

        // قرآن كريم
        ContentItemEntity(
            id = 6,
            title = "سورة الشرح",
            body = "أَلَمْ نَشْرَحْ لَكَ صَدْرَكَ (1) وَوَضَعْنَا عَنكَ وِزْرَكَ (2) الَّذِي أَنقَضَ ظَهْرَكَ (3) وَرَفَعْنَا لَكَ ذِكْرَكَ (4) فَإِنَّ مَعَ الْعُسْرِ يُسْرًا (5) إِنَّ مَعَ الْعُسْرِ يُسْرًا (6) فَإِذَا فَرَغْتَ فَانصَبْ (7) وَإِلَىٰ رَبِّكَ فَارْغَب (8)",
            categoryId = 3,
            sortOrder = 1,
            isFavorite = true
        ),
        ContentItemEntity(
            id = 7,
            title = "الطمأنينة والذكر",
            body = "الَّذِينَ آمَنُوا وَتَطْمَئِنُّ قُلُوبُهُم بِذِكْرِ اللَّهِ ۗ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ ﴿سورة الرعد 28﴾",
            categoryId = 3,
            sortOrder = 2,
            isFavorite = false
        ),

        // أحاديث نبوية
        ContentItemEntity(
            id = 8,
            title = "حديث النية والعمل",
            body = "عن أمير المؤمنين عمر بن الخطاب رضي الله عنه قال: سمعت رسول الله ﷺ يقول: «إنما الأعمال بالنيات، وإنما لكل امرئ ما نوى...»",
            categoryId = 4,
            sortOrder = 1,
            isFavorite = false
        ),
        ContentItemEntity(
            id = 9,
            title = "التيسير والبشرى",
            body = "قال رسول الله ﷺ: «يسروا ولا تعسروا، وبشروا ولا تنفروا». متفق عليه.",
            categoryId = 4,
            sortOrder = 2,
            isFavorite = true
        ),

        // اقتباسات وحكم
        ContentItemEntity(
            id = 10,
            title = "حكمة اليوم عن الصبر والتفاؤل",
            body = "لا تخشَ التأخير في أمورك، فكل تأخيرة في طياتها خيرة من الله تعالى، ولعل أجمل أيام حياتك لم تأتِ بعد.",
            categoryId = 5,
            sortOrder = 1,
            isFavorite = true
        ),
        ContentItemEntity(
            id = 11,
            title = "الإنتاجية والاستمرار",
            body = "النجاح هو حصيلة محاولات صغيرة نكررها كل يوم بثبات وإصرار.",
            categoryId = 5,
            sortOrder = 2,
            isFavorite = false
        ),

        // ملاحظاتي الشخصية
        ContentItemEntity(
            id = 12,
            title = "قائمة الأهداف الأسبوعية",
            body = "• القراءة اليومية لمدة 20 دقيقة.\n• ممارسة الرياضة صباحاً.\n• مراجعة أذكار الصباح والمساء.\n• إنجاز مهام مشروع الودجت بنجاح.",
            categoryId = 6,
            sortOrder = 1,
            isFavorite = false
        )
    )

    val initialTasbeehItems = listOf(
        com.example.data.model.TasbeehEntity(
            id = 1,
            title = "سُبْحَانَ اللَّهِ",
            subtitle = "تنزيه الله تعالى عن كل نقص",
            targetCount = 33,
            colorHex = "#00E5FF",
            isFavorite = true,
            orderIndex = 1
        ),
        com.example.data.model.TasbeehEntity(
            id = 2,
            title = "الْحَمْدُ لِلَّهِ",
            subtitle = "الثناء والشكر لله على نعمه",
            targetCount = 33,
            colorHex = "#10B981",
            isFavorite = false,
            orderIndex = 2
        ),
        com.example.data.model.TasbeehEntity(
            id = 3,
            title = "لَا إِلَهَ إِلَّا اللَّهُ",
            subtitle = "كلمة التوحيد الخالصة",
            targetCount = 33,
            colorHex = "#F59E0B",
            isFavorite = false,
            orderIndex = 3
        ),
        com.example.data.model.TasbeehEntity(
            id = 4,
            title = "اللَّهُ أَكْبَرُ",
            subtitle = "تعظيم وإجلال رب العالمين",
            targetCount = 33,
            colorHex = "#8B5CF6",
            isFavorite = false,
            orderIndex = 4
        ),
        com.example.data.model.TasbeehEntity(
            id = 5,
            title = "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
            subtitle = "طلب المغفرة والتوبة ومحو الذنوب",
            targetCount = 100,
            colorHex = "#EC4899",
            isFavorite = false,
            orderIndex = 5
        ),
        com.example.data.model.TasbeehEntity(
            id = 6,
            title = "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ",
            subtitle = "بركة ورحمة وتفريج للكرب",
            targetCount = 100,
            colorHex = "#3B82F6",
            isFavorite = false,
            orderIndex = 6
        ),
        com.example.data.model.TasbeehEntity(
            id = 7,
            title = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            subtitle = "كنز من كنوز الجنة",
            targetCount = 33,
            colorHex = "#14B8A6",
            isFavorite = false,
            orderIndex = 7
        ),
        com.example.data.model.TasbeehEntity(
            id = 8,
            title = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ ، سُبْحَانَ اللَّهِ الْعَظِيمِ",
            subtitle = "خفيفتان على اللسان ثقيلتان في الميزان",
            targetCount = 100,
            colorHex = "#F43F5E",
            isFavorite = false,
            orderIndex = 8
        )
    )
}
