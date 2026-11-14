package com.example.mobilefinance.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobilefinance.CircularIconButton
import com.yourapp.ui.theme.*

@Composable
fun AddTransactionScreen(
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = ScreenBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top Bar with Back Button and Title
            AddTransactionTopBar(
                onBackClick = onBackClick
            )
        }
    }
}

@Composable
fun AddTransactionTopBar(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularIconButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Back",
            onClick = onBackClick
        )

        Text(
            text = "Add Transaction",
            color = Ink,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.size(44.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddTransactionScreenPreview() {
    AddTransactionScreen()
}
