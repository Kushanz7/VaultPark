package com.kushan.vaultpark.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kushan.vaultpark.ui.theme.DarkSurface
import com.kushan.vaultpark.ui.theme.PrimaryPurple
import com.kushan.vaultpark.ui.theme.TextLight
import com.kushan.vaultpark.ui.theme.TextSecondaryDark

/**
 * Profile field component for displaying/editing user information
 */
@Composable
fun ProfileField(
    icon: ImageVector,
    label: String,
    value: String,
    isEditing: Boolean = false,
    onValueChange: (String) -> Unit = {},
    readOnly: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = PrimaryPurple,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = TextSecondaryDark,
                    fontWeight = FontWeight.Medium
                )

                if (isEditing && !readOnly) {
                    TextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedIndicatorColor = PrimaryPurple,
                            unfocusedIndicatorColor = TextSecondaryDark
                        ),
                        textStyle = androidx.compose.material3.LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            color = TextLight
                        ),
                        singleLine = true
                    )
                } else {
                    Text(
                        text = value,
                        fontSize = 16.sp,
                        color = TextLight,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Setting switch component for preferences
 */
@Composable
fun SettingSwitch(
    icon: ImageVector,
    label: String,
    description: String = "",
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) PrimaryPurple else TextSecondaryDark,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    color = TextLight,
                    fontWeight = FontWeight.Medium
                )

                if (description.isNotEmpty()) {
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = TextSecondaryDark,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PrimaryPurple,
                checkedTrackColor = PrimaryPurple.copy(alpha = 0.3f),
                uncheckedThumbColor = TextSecondaryDark,
                uncheckedTrackColor = TextSecondaryDark.copy(alpha = 0.2f)
            )
        )
    }
}

/**
 * Profile stat card for displaying statistics
 */
@Composable
fun ProfileStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(DarkSurface, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = PrimaryPurple,
            modifier = Modifier.size(32.dp)
        )

        Text(
            text = value,
            fontSize = 20.sp,
            color = PrimaryPurple,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondaryDark,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * Profile picture component with upload overlay
 */
@Composable
fun ProfilePictureWithUpload(
    imageUrl: String?,
    initials: String,
    isUploading: Boolean = false,
    onUploadClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(PrimaryPurple.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrEmpty()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = initials,
                fontSize = 40.sp,
                color = PrimaryPurple,
                fontWeight = FontWeight.Bold
            )
        }

        if (isUploading) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = PrimaryPurple,
                    strokeWidth = 3.dp
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .background(PrimaryPurple, CircleShape)
                    .clickable { onUploadClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add photo",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Divider with horizontal padding
 */
@Composable
fun ProfileDivider(
    modifier: Modifier = Modifier
) {
    Divider(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = TextSecondaryDark.copy(alpha = 0.2f),
        thickness = 1.dp
    )
}
