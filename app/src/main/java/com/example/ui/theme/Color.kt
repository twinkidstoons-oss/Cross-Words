package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Primary brand & cheerful light theme palette (Spec Section 13)
val AppBackground = Color(0xFFF0F0F3)
val SurfaceWhite = Color(0xFFFFFFFF)
val SurfaceSoft = Color(0xFFF8F9FC)
val SurfaceTileIdle = Color(0xFFF4F6FA)

val PrimaryBlue = Color(0xFF2F80ED)
val PrimaryBlueDark = Color(0xFF1A5BBF)
val PrimaryBlueSoft = Color(0xFFE3EFFD)

val OrangeAccent = Color(0xFFFF8C1A)
val OrangeSoft = Color(0xFFFFF1E0)

val PinkAccent = Color(0xFFF83F8F)
val PinkSoft = Color(0xFFFFE6F1)

val MainText = Color(0xFF1A1A1A)
val SecondaryText = Color(0xFF5A6475)
val FoundWordText = Color(0xFFC0C0C8)
val OutlineSubtle = Color(0xFFE2E6EE)

val WarningRed = Color(0xFFEF4444)
val WarningRedSoft = Color(0xFFFEE2E2)
val SuccessGreen = Color(0xFF16A34A)
val SuccessGreenSoft = Color(0xFFDCFCE7)
val HintYellow = Color(0xFFFACC15)
val HintYellowBorder = Color(0xFFEAB308)

// 18 distinct accessible highlight colors for completed words (Spec Section 12)
val WordHighlightColors: List<Color> = listOf(
    Color(0xFFEF4444), // 1. Red
    Color(0xFF3B82F6), // 2. Blue
    Color(0xFF22C55E), // 3. Green
    Color(0xFFEAB308), // 4. Yellow
    Color(0xFFEC4899), // 5. Pink
    Color(0xFFF97316), // 6. Orange
    Color(0xFFA855F7), // 7. Purple
    Color(0xFF14B8A6), // 8. Teal
    Color(0xFFF43F5E), // 9. Rose
    Color(0xFF06B6D4), // 10. Cyan
    Color(0xFF84CC16), // 11. Lime
    Color(0xFF8B5CF6), // 12. Violet
    Color(0xFFF59E0B), // 13. Amber
    Color(0xFF10B981), // 14. Emerald
    Color(0xFFD946EF), // 15. Fuchsia
    Color(0xFF0EA5E9), // 16. Sky
    Color(0xFF4ADE80), // 17. Light Green
    Color(0xFFFB923C), // 18. Light Orange
)
