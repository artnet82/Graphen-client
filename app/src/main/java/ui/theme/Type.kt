package com.graphene.antivirus.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val GrapheneTypography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.Black, fontSize = 48.sp, letterSpacing = (-1.5).sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 30.sp, letterSpacing = (-0.5).sp),
    headlineMedium= TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, letterSpacing = (-0.3).sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.Medium, fontSize = 15.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 19.sp),
    labelLarge    = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, letterSpacing = 0.5.sp),
    labelSmall    = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 1.2.sp)
)