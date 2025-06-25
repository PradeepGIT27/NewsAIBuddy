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

// Composable function to display a list of news articles in a card view
@Composable
fun NewsCardView(articles: List<Article>, navController: NavController) {
    // LazyColumn to efficiently display a scrollable list of items
    LazyColumn(
        userScrollEnabled = true,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        // Center the items horizontally within the LazyColumn

        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        items(articles.size) {
            val article = articles[it]
            var expanded by remember {
                mutableStateOf(article.expanded)
            }
            // Box to create a card-like container for each article
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White, shape = RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .clickable {
                        // Toggle the expanded state of the article when clicked
                        article.expanded = !expanded
                        expanded = article.expanded
                    }
            ) {
                Column {
                    Text(
                        text = article.title.toString(),
                        // Style for the article title
                        style = TextStyle(
                            fontSize = 18.sp,
                            color = Color.Black,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = article.description.toString())
                    Spacer(modifier = Modifier.padding(4.dp))
                    // AnimatedVisibility to show/hide additional details when expanded
                    AnimatedVisibility(expanded) {
                        Column {
                            // AsyncImage to load and display the article image
                            AsyncImage(
                                // Build the image request with the article's image URL
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(article.urlToImage)
                                    .crossfade(true)
                                    .build(),
                                placeholder = painterResource(R.drawable.ic_menu_view),
                                contentDescription = "",
                                error = painterResource(id = R.drawable.stat_notify_error),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    // Clip the image to a rectangular shape
                                    .clip(RectangleShape)
                                    .wrapContentWidth(),
                            )
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(text = article.content.toString())
                            Spacer(modifier = Modifier.padding(4.dp))
                            // Row to display the reference link
                            Row {
                                Text(
                                    // Label for the reference link
                                    text = "Reference Link : ",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Light
                                    )
                                )
                                BasicText(
                                    // Display the source name as a clickable link
                                    text = androidx.compose.ui.text.AnnotatedString(
                                        article.source!!.name.toString()
                                    ),
                                    // Style for the reference link
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        color = Color.Blue,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Light
                                    ),
                                    modifier = Modifier.clickable {
//                                        uriHandler.openUri(article.url.toString()) -> To Open Url in default browser
                                        // Encode the URL before navigating
                                        val encodedUrl = URLEncoder.encode(
                                            article.url.toString(),
                                            StandardCharsets.UTF_8.toString()
                                        )
                                        // Navigate to the reference screen with the encoded URL
                                        navController.navigate("referenceScreen/${encodedUrl}")
                                    }
                                )
                            }

                            //Display Author name
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(
                                text = "Author : ${article.author}",
                                // Style for the author name
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

// Composable function to display a loading indicator
@Composable
fun AppLoader() {
    // Box to center the CircularProgressIndicator
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        // CircularProgressIndicator to show loading progress
        CircularProgressIndicator(
            color = appColor40,
            modifier = Modifier
                // Wrap the content width and height
                .wrapContentWidth()
                .wrapContentHeight(),
        )
    }
}

// Composable function to display an empty state message
@Composable
fun EmptyStateComponent() {
    // Box to center the Text message
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        // Text to display when no news is available
        Text(text = "No news available")
    }
}
