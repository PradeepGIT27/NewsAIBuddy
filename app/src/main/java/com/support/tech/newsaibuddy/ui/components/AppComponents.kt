package com.support.tech.newsaibuddy.ui.components

//noinspection SuspiciousImport
import android.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.support.tech.newsaibuddy.data.entity.Article
import com.support.tech.newsaibuddy.ui.theme.appColor40
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun NewsCardView(articles: List<Article>, navController: NavController) {
    LazyColumn(
        userScrollEnabled = true,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        items(articles.size) {
            val article = articles[it]
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
                        text = article.title.toString(),
                        style = TextStyle(
                            fontSize = 18.sp,
                            color = Color.Black,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = article.description.toString())
                    Spacer(modifier = Modifier.padding(4.dp))
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
                                modifier = Modifier
                                    .clip(RectangleShape)
                                    .wrapContentWidth(),
                            )
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(text = article.content.toString())
                            Spacer(modifier = Modifier.padding(4.dp))
                            Row {
                                Text(
                                    text = "Reference Link : ",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Light
                                    )
                                )
                                BasicText(
                                    text = androidx.compose.ui.text.AnnotatedString(
                                        article.source!!.name.toString()
                                    ),
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        color = Color.Blue,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Light
                                    ),
                                    modifier = Modifier.clickable {
//                                        uriHandler.openUri(article.url.toString()) -> To Open Url in default browser
                                        val encodedUrl = URLEncoder.encode(
                                            article.url.toString(),
                                            StandardCharsets.UTF_8.toString()
                                        )
                                        navController.navigate("referenceScreen/${encodedUrl}")
                                    }
                                )
                            }

                            //Display Author name
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(
                                text = "Author : ${article.author}",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Light
                                )
                            )
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
