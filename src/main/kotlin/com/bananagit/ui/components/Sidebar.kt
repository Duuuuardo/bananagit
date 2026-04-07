package com.bananagit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bananagit.i18n.LocalStrings
import com.bananagit.viewmodel.Tab

@Composable
fun Sidebar(
    currentTab: Tab,
    onTabSelected: (Tab) -> Unit,
    stagedCount: Int,
    unstagedCount: Int
) {
    val s = LocalStrings.current
    Surface(
        modifier = Modifier.width(200.dp).fillMaxHeight(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            Spacer(Modifier.height(8.dp))

            SidebarItem(
                icon = Icons.Default.Description,
                label = s.tabStatus,
                badge = (stagedCount + unstagedCount).let { if (it > 0) "$it" else null },
                isSelected = currentTab == Tab.STATUS,
                onClick = { onTabSelected(Tab.STATUS) }
            )
            SidebarItem(
                icon = Icons.Default.History,
                label = s.tabHistory,
                badge = null,
                isSelected = currentTab == Tab.HISTORY,
                onClick = { onTabSelected(Tab.HISTORY) }
            )
            SidebarItem(
                icon = Icons.Default.AccountTree,
                label = s.tabBranches,
                badge = null,
                isSelected = currentTab == Tab.BRANCHES,
                onClick = { onTabSelected(Tab.BRANCHES) }
            )
            SidebarItem(
                icon = Icons.Default.Difference,
                label = s.tabDiff,
                badge = null,
                isSelected = currentTab == Tab.DIFF,
                onClick = { onTabSelected(Tab.DIFF) }
            )
        }
    }
}

@Composable
private fun SidebarItem(
    icon: ImageVector,
    label: String,
    badge: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected)
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    else
        MaterialTheme.colorScheme.surfaceVariant

    val fg = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = fg, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = fg,
            modifier = Modifier.weight(1f)
        )
        if (badge != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ) {
                Text(
                    text = badge,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
