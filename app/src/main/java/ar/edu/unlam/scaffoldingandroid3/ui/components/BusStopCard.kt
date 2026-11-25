package ar.edu.unlam.scaffoldingandroid3.ui.components

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import ar.edu.unlam.scaffoldingandroid3.R
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.ui.helper.BusStopCardHelper
import ar.edu.unlam.scaffoldingandroid3.ui.theme.FavoriteRed
import kotlinx.coroutines.launch

const val MAX_INITIAL_LINES = 4
val HELPER = BusStopCardHelper()

@Composable
fun BusStopCard(
    stop: BusStop,
    distanceMeters: Double,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
) {
    var expanded by rememberSaveable(stop.stopId) {
        mutableStateOf(false)
    }
    var showShareMenu by rememberSaveable(stop.stopId) {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val totalLines = stop.busLines.size
    val hasOverflow = totalLines > MAX_INITIAL_LINES + 1
    val displayedLines = HELPER.getLinesToDisplay(stop, expanded, hasOverflow)
    val hiddenCount = totalLines - displayedLines.size
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        border =
            BorderStroke(
                width = if (selected) 2.dp else 1.dp,
                color =
                    if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = if (selected) 5.dp else 1.dp,
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 14.dp,
                    ).animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (selected) {
                Text(
                    text = stringResource(R.string.bus_stop_card_selected),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stop.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )

                if (onFavoriteClick != null) {
                    IconButton(
                        onClick = onFavoriteClick,
                    ) {
                        Icon(
                            imageVector =
                                if (isFavorite) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Outlined.FavoriteBorder
                                },
                            contentDescription =
                                if (isFavorite) {
                                    stringResource(
                                        R.string.bus_stop_card_quit_favorites,
                                    )
                                } else {
                                    stringResource(R.string.bus_stop_card_add_favorites)
                                },
                            tint =
                                if (isFavorite) {
                                    FavoriteRed
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                        )
                    }
                }
            }

            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Row(
                    modifier =
                        Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 7.dp,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )

                    Text(
                        text = HELPER.formatDistance(distanceMeters),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }

            HorizontalDivider(
                color =
                    MaterialTheme.colorScheme.outlineVariant.copy(
                        alpha = 0.5f,
                    ),
            )

            Text(
                text =
                    when (totalLines) {
                        0 -> stringResource(R.string.bus_stop_card_no_lines)
                        1 -> stringResource(R.string.bus_stop_card_one_line)
                        else -> "$totalLines ${stringResource(R.string.bus_stop_card_lines)}"
                    },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (totalLines > 0) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    displayedLines.forEach { line ->
                        BusLineChip(line = line)
                    }
                    if (hasOverflow) {
                        BusStopOverflowChip(
                            expanded = expanded,
                            onToggle = { expanded = !expanded },
                            label =
                                if (expanded) {
                                    stringResource(
                                        R.string.bus_stop_card_view_less,
                                    )
                                } else {
                                    stringResource(R.string.bus_stop_card_more_count, hiddenCount)
                                },
                            icon = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            iconDescription =
                                if (expanded) {
                                    stringResource(
                                        R.string.bus_stop_card_view_less_lines,
                                    )
                                } else {
                                    stringResource(R.string.bus_stop_card_view_more_lines)
                                },
                            modifier = modifier,
                        )
                    }
                }
            }

            if (selected) {
                HorizontalDivider(
                    color =
                        MaterialTheme.colorScheme.outlineVariant.copy(
                            alpha = 0.5f,
                        ),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Box {
                        OutlinedButton(
                            onClick = {
                                showShareMenu = true
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )

                            Spacer(
                                modifier = Modifier.width(6.dp),
                            )

                            Text(stringResource(R.string.bus_stop_card_share))
                        }

                        DropdownMenu(
                            expanded = showShareMenu,
                            onDismissRequest = {
                                showShareMenu = false
                            },
                        ) {
                            val shareMessage = HELPER.buildShareMessage(stop)

                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(R.string.bus_stop_card_whatsapp))
                                },
                                onClick = {
                                    showShareMenu = false

                                    val intent =
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, shareMessage.message)
                                            setPackage("com.whatsapp")
                                        }

                                    try {
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        try {
                                            val waIntent =
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    "https://wa.me/?text=${Uri.encode(shareMessage.message)}".toUri(),
                                                )

                                            context.startActivity(waIntent)
                                        } catch (_: Exception) {
                                            val chooser =
                                                Intent.createChooser(
                                                    Intent(
                                                        Intent.ACTION_SEND,
                                                    ).apply {
                                                        type = "text/plain"
                                                        putExtra(
                                                            Intent.EXTRA_TEXT,
                                                            shareMessage.message,
                                                        )
                                                    },
                                                    "Compartir parada",
                                                )

                                            context.startActivity(chooser)
                                        }
                                    }
                                },
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(R.string.bus_stop_card_copy_link))
                                },
                                onClick = {
                                    showShareMenu = false

                                    coroutineScope.launch {
                                        clipboard.setClipEntry(
                                            ClipEntry(
                                                ClipData.newPlainText(
                                                    "Enlace de la parada",
                                                    shareMessage.mapsUrl,
                                                ),
                                            ),
                                        )

                                        Toast
                                            .makeText(
                                                context,
                                                R.string.bus_stop_card_link_copied,
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                    }
                                },
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(R.string.bus_stop_card_other_apps))
                                },
                                onClick = {
                                    showShareMenu = false

                                    val sendIntent =
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, shareMessage.message)
                                        }

                                    val chooser =
                                        Intent.createChooser(
                                            sendIntent,
                                            "Compartir parada",
                                        )

                                    context.startActivity(chooser)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
