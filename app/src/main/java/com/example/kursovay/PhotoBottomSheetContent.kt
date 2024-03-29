package com.example.kursovay


import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp

@Composable
fun PhotoBottomSheetContent(bitmaps: List<Bitmap>, modifier: Modifier = Modifier) { // отбражение в галерее, список растовых изображений
    if(bitmaps.isEmpty()) {
        Box(modifier = modifier.padding(16.dp), contentAlignment = Alignment.Center) {
            Text("Что б тут чет было, надо сделать фото)")
        }
    } else {
        LazyVerticalStaggeredGrid(columns = StaggeredGridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalItemSpacing = 16.dp, contentPadding = PaddingValues(16.dp), modifier = modifier) { // расположение в сетке фоток
            items(bitmaps) { bitmap ->
                Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.clip(RoundedCornerShape(10.dp)))
            }
        }
    }
}