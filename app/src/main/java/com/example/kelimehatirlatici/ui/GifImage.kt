package com.example.kelimehatirlatici.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/**
 * GIF animasyonu görüntüleme bileşeni.
 * Coil kütüphanesi olmadan çalışır.
 * 
 * Not: Statik bir resim gösterir. Animasyonlu GIF için
 * Coil kütüphanesi eklenmesi gerekir.
 */
@Composable
fun GifImage(
    modifier: Modifier = Modifier
) {
    // study_gif.gif varsa onu göster, yoksa placeholder
    val resId = try {
        com.example.kelimehatirlatici.R.raw.study_gif
    } catch (e: Exception) {
        null
    }
    
    if (resId != null) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = "Çalışma animasyonu",
            modifier = modifier.size(80.dp)
        )
    }
    // resim yoksa hiçbir şey gösterme
}
