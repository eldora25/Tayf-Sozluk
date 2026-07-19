
package com.example.kelimehatirlatici.ui

import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun GifImage(
    @DrawableRes gifRes: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        AndroidView(
            factory = { context ->
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    val drawable = context.getDrawable(gifRes)
                    if (drawable is AnimatedImageDrawable) {
                        setImageDrawable(drawable)
                        drawable.start()
                    } else {
                        setImageResource(gifRes)
                    }
                }
            },
            modifier = modifier
        )
    } else {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(gifRes)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    }
}
