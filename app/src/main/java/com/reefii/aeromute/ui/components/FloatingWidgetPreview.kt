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

@Composable
fun FloatingWidgetPreview(
    settings: AeroMuteSettings,
    isMutedPreview: Boolean,
    onMuteTogglePreview: () -> Unit
) {
    val alpha = settings.transparency
    val sizeDp = settings.widgetScale.sizeDp.dp
    val themePrimary = Color(settings.themePreset.primaryColorHex)

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
                text = "PRATINJAU WIDGET MELAYANG",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sesuaikan transparansi, ukuran & tema di bawah untuk melihat tampilan live",
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

                // Floating Widget Preview Badge
                Surface(
                    modifier = Modifier
                        .size(sizeDp)
                        .shadow(12.dp, CircleShape)
                        .clip(CircleShape)
                        .clickable { onMuteTogglePreview() }
                        .border(2.dp, Color.White.copy(alpha = alpha * 0.7f), CircleShape),
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
                text = if (isMutedPreview) "Status: MUTED (Ketuk badge untuk tes suara)" else "Status: SUARA AKTIF (Ketuk badge untuk tes mute)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isMutedPreview) Color(0xFFF43F5E) else MaterialTheme.colorScheme.primary
            )
        }
    }
}
