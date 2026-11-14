package com.support.tech.newsaibuddy.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.support.tech.newsaibuddy.ui.theme.CardBorder

/**
 * Adaptive Layout composable that automatically renders a List-Detail two-pane side-by-side layout
 * when screen width is >= 600dp (tablets, foldables unfolded, or landscape orientation),
 * while rendering single-pane list UI on compact mobile devices (< 600dp).
 */
@Composable
fun <T> AdaptiveTwoPaneLayout(
    listPane: @Composable (selectedItem: T?, onSelect: (T) -> Unit) -> Unit,
    detailPane: @Composable (item: T, onClose: () -> Unit) -> Unit,
    selectedItem: T?,
    onSelectItem: (T?) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isExpandedWidth = configuration.screenWidthDp >= 600

    if (isExpandedWidth) {
        Row(modifier = modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(0.42f)
                    .fillMaxHeight()
            ) {
                listPane(selectedItem) { item ->
                    onSelectItem(item)
                }
            }

            VerticalDivider(color = CardBorder, thickness = 1.dp)

            Box(
                modifier = Modifier
                    .weight(0.58f)
                    .fillMaxHeight()
            ) {
                if (selectedItem != null) {
                    detailPane(selectedItem) {
                        onSelectItem(null)
                    }
                }
            }
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            listPane(selectedItem) { item ->
                onSelectItem(item)
            }
        }
    }
}
