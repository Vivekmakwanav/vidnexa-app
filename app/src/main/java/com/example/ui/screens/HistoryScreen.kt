package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.model.DownloadHistory
import com.example.ui.theme.*
import com.example.ui.viewmodel.VidnexaViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: VidnexaViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val historyList by viewModel.downloadHistoryList.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("date_desc") } // date_desc, date_asc, size_desc, title_asc
    var showSortMenu by remember { mutableStateOf(false) }

    // Filter and Sort history items
    val filteredHistory = remember(historyList, searchQuery, sortBy) {
        historyList
            .filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.platform.contains(searchQuery, ignoreCase = true)
            }
            .sortedWith { a, b ->
                when (sortBy) {
                    "date_desc" -> b.timestamp.compareTo(a.timestamp)
                    "date_asc" -> a.timestamp.compareTo(b.timestamp)
                    "size_desc" -> b.fileSize.compareTo(a.fileSize)
                    "title_asc" -> a.title.lowercase().compareTo(b.title.lowercase())
                    else -> b.timestamp.compareTo(a.timestamp)
                }
            }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- Header Title ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Download Log",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                if (filteredHistory.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            viewModel.clearAllHistory()
                            Toast.makeText(context, "Log cleared successfully", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear All History",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Search Bar & Sort Pill ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search title or platform...") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, "Search") },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("history_search_input")
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort history",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Newest First") },
                            onClick = { sortBy = "date_desc"; showSortMenu = false },
                            leadingIcon = { Icon(Icons.Default.ArrowDownward, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Oldest First") },
                            onClick = { sortBy = "date_asc"; showSortMenu = false },
                            leadingIcon = { Icon(Icons.Default.ArrowUpward, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Largest Size") },
                            onClick = { sortBy = "size_desc"; showSortMenu = false },
                            leadingIcon = { Icon(Icons.Default.SdCard, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Alphabetical A-Z") },
                            onClick = { sortBy = "title_asc"; showSortMenu = false },
                            leadingIcon = { Icon(Icons.Default.SortByAlpha, null) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- History Items Container ---
            if (filteredHistory.isEmpty()) {
                // Empty state view
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "No items",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No match found" else "History empty",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Try refining your query search terms." else "Your resolved downloads will be listed here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Box(modifier = Modifier.weight(1f)) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        filteredHistory.forEach { historyItem ->
                            HistoryItemCard(
                                item = historyItem,
                                viewModel = viewModel,
                                context = context
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    item: DownloadHistory,
    viewModel: VidnexaViewModel,
    context: Context
) {
    val platformDetails = getPlatformDetails(item.platform)
    val formatter = remember { SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()) }
    val formattedDate = remember(item.timestamp) { formatter.format(Date(item.timestamp)) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Compact Thumbnail
                if (!item.thumbnailUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = item.thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(platformDetails.first.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = platformDetails.second,
                            contentDescription = null,
                            tint = platformDetails.first,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.platform} • ${item.formatSelected}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$formattedDate • ${formatBytes(item.fileSize)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                // Delete Button
                IconButton(
                    onClick = {
                        // Deletes file if exists & cleans Room entry
                        item.downloadPath?.let { path ->
                            val file = File(path)
                            if (file.exists()) file.delete()
                        }
                        viewModel.deleteHistoryItem(item.id)
                        Toast.makeText(context, "Removed from log", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Completed / In-progress Action Panel
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Badge Indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (item.status) {
                                "COMPLETED" -> MintGreen.copy(alpha = 0.12f)
                                "FAILED" -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                                else -> AmberWarn.copy(alpha = 0.12f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.status,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = when (item.status) {
                            "COMPLETED" -> MintGreen
                            "FAILED" -> MaterialTheme.colorScheme.error
                            else -> AmberWarn
                        }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (item.status == "COMPLETED") {
                        // Share
                        OutlinedButton(
                            onClick = { shareFile(context, item.downloadPath, item.title) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Share, "Share", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }

                        // Open Play File
                        Button(
                            onClick = { openFile(context, item.downloadPath) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, "Open", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    } else if (item.status == "FAILED") {
                        // Retry Action button
                        Button(
                            onClick = {
                                viewModel.resumeDownload(item)
                                Toast.makeText(context, "Retrying download...", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberWarn),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Refresh, "Retry", tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        // Active downloading action hints
                        Text(
                            text = "Download in progress...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

// --- Native Intent Helper Methods ---

private fun openFile(context: Context, path: String?) {
    if (path.isNullOrEmpty()) {
        Toast.makeText(context, "File path is empty", Toast.LENGTH_SHORT).show()
        return
    }
    val file = File(path)
    if (!file.exists()) {
        Toast.makeText(context, "File does not exist locally anymore.", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val authority = "${context.packageName}.fileprovider"
        val uri: Uri = FileProvider.getUriForFile(context, authority, file)
        
        val mimeType = if (path.endsWith("mp3")) "audio/*" else "video/*"
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Open File via"))
    } catch (e: Exception) {
        Toast.makeText(context, "Error opening file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

private fun shareFile(context: Context, path: String?, title: String) {
    if (path.isNullOrEmpty()) return
    val file = File(path)
    if (!file.exists()) {
        Toast.makeText(context, "File does not exist.", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val authority = "${context.packageName}.fileprovider"
        val uri: Uri = FileProvider.getUriForFile(context, authority, file)
        
        val mimeType = if (path.endsWith("mp3")) "audio/*" else "video/*"
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Media File"))
    } catch (e: Exception) {
        Toast.makeText(context, "Error sharing file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}
