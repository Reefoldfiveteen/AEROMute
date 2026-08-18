package com.reefii.aeromute.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.VolumeDown
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reefii.aeromute.data.AeroMuteSettings
import com.reefii.aeromute.data.AppStrings

@Composable
fun FloatingWidgetPreview(
    settings: AeroMuteSettings,
    isMutedPreview: Boolean,
    onMuteTogglePreview: () -> Unit
) {
    val alpha = settings.transparency
    val sizeDp = settings.widgetScale.sizeDp.dp
    val themePrimary = Color(settings.themePreset.primaryColorHex)
    val strings = AppStrings.get(settings.appLanguage)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = strings.previewHeader,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = strings.previewSub,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Simulated Canvas with grid lines representing phone screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Background grid decorative lines
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.White.copy(alpha = 0.05f))
                        )
                    }
                }

                val badgeShape: androidx.compose.ui.graphics.Shape = when (settings.widgetShape) {
                    com.reefii.aeromute.data.WidgetShape.CIRCLE -> CircleShape
                    com.reefii.aeromute.data.WidgetShape.SQUIRCLE -> RoundedCornerShape(16.dp)
                    com.reefii.aeromute.data.WidgetShape.PILL -> RoundedCornerShape(24.dp)
                    com.reefii.aeromute.data.WidgetShape.TEARDROP -> RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 4.dp, bottomEnd = 28.dp)
                }

                // Floating Widget Preview Badge
                Surface(
                    modifier = Modifier
                        .size(sizeDp)
                        .shadow(12.dp, badgeShape)
                        .clip(badgeShape)
                        .clickable { onMuteTogglePreview() }
                        .border(2.dp, Color.White.copy(alpha = alpha * 0.7f), badgeShape),
                    color = if (isMutedPreview) Color(0xFFF43F5E).copy(alpha = alpha) else themePrimary.copy(alpha = alpha),
                    contentColor = Color.White
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        if (isMutedPreview) {
                            Icon(
                                imageVector = Icons.Rounded.VolumeOff,
                                contentDescription = "Muted Preview",
                                modifier = Modifier.size((sizeDp.value * 0.5f).dp),
                                tint = Color.White
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.VolumeUp,
                                    contentDescription = "Active Volume Preview",
                                    modifier = Modifier.size((sizeDp.value * 0.44f).dp),
                                    tint = Color.White
                                )
                                Text(
                                    text = "75%",
                                    fontSize = (sizeDp.value * 0.22f).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isMutedPreview) strings.previewStatusMuted else strings.previewStatusActive,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isMutedPreview) Color(0xFFF43F5E) else MaterialTheme.colorScheme.primary
            )
        }
    }
}
