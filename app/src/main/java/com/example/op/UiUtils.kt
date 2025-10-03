package com.example.op

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Zdieľaná šablóna pre obrazovky, ktorá poskytuje konzistentný vzhľad
 * s hornou lištou (TopAppBar).
 *
 * @param header Obsah pre hornú lištu (zvyčajne TopAppBar).
 * @param content Hlavný obsah obrazovky.
 */
@Composable
fun ScaffoldTemplate(
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = header
    ) { innerPadding ->
        content(innerPadding)
    }
}