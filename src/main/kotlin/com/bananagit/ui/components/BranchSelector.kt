package com.bananagit.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bananagit.git.BranchInfo
import com.bananagit.i18n.LocalStrings

@Composable
fun BranchSelector(
    currentBranch: String,
    branches: List<BranchInfo>,
    onCheckoutBranch: (String) -> Unit,
    onNewBranch: () -> Unit
) {
    val s = LocalStrings.current
    var expanded by remember { mutableStateOf(false) }

    Box {
        FilledTonalButton(onClick = { expanded = true }) {
            Icon(Icons.Default.AccountTree, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(currentBranch, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(18.dp))
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            branches.forEach { branch ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (branch.isCurrent) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(4.dp))
                            }
                            Text(branch.name)
                        }
                    },
                    onClick = {
                        expanded = false
                        if (!branch.isCurrent) onCheckoutBranch(branch.name)
                    }
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(s.newBranchEllipsis)
                    }
                },
                onClick = { expanded = false; onNewBranch() }
            )
        }
    }
}
