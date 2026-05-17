package pro.cashkeeper.inspektor.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

internal val baseline = Typography()

internal val InspektorTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontSize = 60.sp, lineHeight = 68.sp),
    displayMedium = baseline.displayMedium.copy(fontSize = 48.sp, lineHeight = 56.sp),
    displaySmall = baseline.displaySmall.copy(fontSize = 38.sp, lineHeight = 46.sp),
    headlineLarge = baseline.headlineLarge.copy(fontSize = 34.sp, lineHeight = 42.sp),
    headlineMedium = baseline.headlineMedium.copy(fontSize = 30.sp, lineHeight = 38.sp),
    headlineSmall = baseline.headlineSmall.copy(fontSize = 26.sp, lineHeight = 34.sp),
    titleLarge = baseline.titleLarge.copy(fontSize = 24.sp, lineHeight = 30.sp),
    titleMedium = baseline.titleMedium.copy(fontSize = 18.sp, lineHeight = 26.sp),
    titleSmall = baseline.titleSmall.copy(fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = baseline.bodyLarge.copy(fontSize = 18.sp, lineHeight = 26.sp),
    bodyMedium = baseline.bodyMedium.copy(fontSize = 16.sp, lineHeight = 24.sp),
    bodySmall = baseline.bodySmall.copy(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = baseline.labelLarge.copy(fontSize = 16.sp, lineHeight = 22.sp),
    labelMedium = baseline.labelMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
    labelSmall = baseline.labelSmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
)
