package com.example.energyhub.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.energyhub.model.ErrorType

@Composable
fun ErrorLog(errors: List<ErrorType>,
             onClose: () -> Unit,
             modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onClose,
            ) {
                Image(imageVector = Icons.Default.Close, contentDescription = "Close button")
            }
        }
        LazyColumn {
            items(errors) {
                ErrorRecord(it, modifier.padding(8.dp))
            }
        }
    }
}

@Composable
fun ErrorRecord(
    error: ErrorType,
    modifier: Modifier = Modifier
){
    Box(modifier) {
        Column {
            Text(error.type ?: "", style = MaterialTheme.typography.bodyLarge)
            Text(error.msg ?: "", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
internal fun ShowError(snackbarHostState: SnackbarHostState,
                      error: ErrorType,
                      onClick:()->Unit){
    val msg = "${error.type}\n${error.msg}"
    LaunchedEffect("Error Snackbar") {
        val result = snackbarHostState
            .showSnackbar(
                message = msg,
                actionLabel = "More...",
                withDismissAction = true,
                // Defaults to SnackbarDuration.Short
                duration = SnackbarDuration.Short
            )
        when (result) {
            SnackbarResult.ActionPerformed -> {
                onClick()
            }

            SnackbarResult.Dismissed -> {
                /* Handle snackbar dismissed */
            }
        }
    }
}

@Composable
fun ErrorHost(
    showErrorLog: Boolean,
    errors: List<ErrorType>,
    onCloseLog: () -> Unit,
    onShowMore: () -> Unit,
    afterShow: () -> Unit,
    snackbarHostState: SnackbarHostState,
    content: @Composable BoxScope.() -> Unit
) {
    val errorLog: MutableList<ErrorType> = remember {
        mutableListOf()
    }

    if (showErrorLog) {
        ErrorLog(errorLog, onCloseLog)
    } else {
        errors.forEach {
            ShowError(snackbarHostState, it, onShowMore)
        }
        errorLog.addAll(errors)
        afterShow()
        Box {
            content()
        }

    }
}