package com.support.tech.newsaibuddy.ui.components

//noinspection SuspiciousImport
import android.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.support.tech.newsaibuddy.data.entity.Article
import com.support.tech.newsaibuddy.ui.theme.appColor40

@Composable
fun NewsCardView(articles: List<Article>) {
    LazyColumn(
        userScrollEnabled = true,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        items(articles.size) {
            var article = articles[it]
            var expanded by remember {
                mutableStateOf(article.expanded)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White, shape = RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .clickable {
                        article.expanded = !expanded
                        expanded = article.expanded
                    }
            ) {
                Column {
                    Text(
                        text = it.toString(),
                        style = TextStyle(fontSize = 24.sp)
                    )
                    Text(text = article.description.toString())
                    AnimatedVisibility(expanded) {
                        Column {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(article.urlToImage)
                                    .crossfade(true)
                                    .build(),
                                placeholder = painterResource(R.drawable.ic_menu_view),
                                contentDescription = "",
                                error = painterResource(id = R.drawable.stat_notify_error),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.clip(RectangleShape),
                            )
                            Text(text = article.publishedAt.toString())
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun AppLoader() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        CircularProgressIndicator(
            color = appColor40,
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
        )
    }
}

@Composable
fun EmptyStateComponent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(text = "No news available")
    }
}
