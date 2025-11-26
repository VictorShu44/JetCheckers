package com.shu.conversation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shu.design.models.ConversationDetail

@Composable
fun ConversationListScreen(
    onConversationClicked: (ConversationDetail) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(10) { index ->
            val conversationId = index + 1
            val conversationDetail = ConversationDetail(conversationId)
            val backgroundColor = conversationDetail.color
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { onConversationClicked(conversationDetail) }),
                headlineContent = {
                    Text(
                        text = "Conversation $conversationId",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = backgroundColor // Set container color directly
                )
            )
        }
    }
}