
package ar.edu.unlam.scaffoldingandroid3.ui.components

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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusLine
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.ui.theme.FavoriteRed
import java.util.Locale
import kotlin.math.roundToInt

private const val MAX_INITIAL_LINES = 4

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
    val clipboardManager = LocalClipboardManager.current

    val totalLines = stop.busLines.size
    val hasOverflow = totalLines > MAX_INITIAL_LINES + 1

    val displayedLines =
        if (expanded || !hasOverflow) {
            stop.busLines
        } else {
            stop.busLines.take(MAX_INITIAL_LINES)
        }

    val hiddenCount = totalLines - displayedLines.size

    val mapsUrl =
        "https://maps.google.com/?q=" +
            "${stop.locationData.latitude.value}," +
            "${stop.locationData.longitude.value}"

    val linesText =
        if (stop.busLines.isEmpty()) {
            "Sin líneas informadas"
        } else {
            stop.busLines.joinToString(", ") { line ->
                buildString {
                    append(line.number)

                    if (line.branch.isNotBlank()) {
                        append(" ${line.branch}")
                    }

                    if (line.destination.isNotBlank()) {
                        append(" → ${line.destination}")
                    }
                }
            }
        }

    val shareMessage =
        "Parada: ${stop.name}\n" +
            "Líneas: $linesText\n" +
            "Ubicación: $mapsUrl"

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
                    text = "PARADA SELECCIONADA",
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
                                    "Quitar de favoritos"
                                } else {
                                    "Agregar a favoritos"
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
                        text = formatDistance(distanceMeters),
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
                        0 -> "Sin líneas informadas"
                        1 -> "1 línea disponible"
                        else -> "$totalLines líneas disponibles"
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
                        if (!expanded) {
                            AssistChip(
                                onClick = {
                                    expanded = true
                                },
                                label = {
                                    Text("+$hiddenCount más")
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowDown,
                                        contentDescription = "Ver más líneas",
                                        modifier = Modifier.size(16.dp),
                                    )
                                },
                                colors =
                                    AssistChipDefaults.assistChipColors(
                                        containerColor =
                                            MaterialTheme.colorScheme.secondaryContainer,
                                        labelColor =
                                            MaterialTheme.colorScheme.onSecondaryContainer,
                                    ),
                            )
                        } else {
                            AssistChip(
                                onClick = {
                                    expanded = false
                                },
                                label = {
                                    Text("Ver menos")
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowUp,
                                        contentDescription = "Ver menos líneas",
                                        modifier = Modifier.size(16.dp),
                                    )
                                },
                                colors =
                                    AssistChipDefaults.assistChipColors(
                                        containerColor =
                                            MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor =
                                            MaterialTheme.colorScheme.onSurfaceVariant,
                                    ),
                            )
                        }
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

                            Text("Compartir")
                        }

                        DropdownMenu(
                            expanded = showShareMenu,
                            onDismissRequest = {
                                showShareMenu = false
                            },
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text("Enviar por WhatsApp")
                                },
                                onClick = {
                                    showShareMenu = false

                                    val intent =
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                shareMessage,
                                            )
                                            setPackage("com.whatsapp")
                                        }

                                    try {
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        try {
                                            val waIntent =
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse(
                                                        "https://wa.me/?text=" +
                                                            Uri.encode(shareMessage),
                                                    ),
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
                                                            shareMessage,
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
                                    Text("Copiar enlace")
                                },
                                onClick = {
                                    showShareMenu = false

                                    clipboardManager.setText(
                                        AnnotatedString(mapsUrl),
                                    )

                                    Toast
                                        .makeText(
                                            context,
                                            "Enlace copiado al portapapeles",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                },
                            )

                            DropdownMenuItem(
                                text = {
                                    Text("Otras apps")
                                },
                                onClick = {
                                    showShareMenu = false

                                    val sendIntent =
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                shareMessage,
                                            )
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

@Composable
private fun BusLineChip(
    line: BusLine,
    modifier: Modifier = Modifier,
) {
    val chipLabel =
        buildString {
            append(line.number)

            if (line.branch.isNotBlank()) {
                append(" ${line.branch}")
            } else if (
                line.code.isNotBlank() &&
                line.code != line.number.toString()
            ) {
                val nonDigits =
                    line.code
                        .filter {
                            !it.isDigit()
                        }.trim()

                if (nonDigits.isNotBlank()) {
                    append(" $nonDigits")
                }
            }

            if (line.destination.isNotBlank()) {
                append(" → ${line.destination}")
            }
        }

    SuggestionChip(
        onClick = {},
        label = {
            Text(
                text = chipLabel,
                style = MaterialTheme.typography.bodySmall,
            )
        },
        colors =
            SuggestionChipDefaults.suggestionChipColors(
                containerColor =
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                labelColor =
                    MaterialTheme.colorScheme.onSurface,
            ),
        modifier = modifier,
    )
}

private fun formatDistance(meters: Double): String =
    if (meters < 1_000) {
        "${meters.roundToInt()} m"
    } else {
        String.format(
            Locale.getDefault(),
            "%.1f km",
            meters / 1_000,
        )
    }
