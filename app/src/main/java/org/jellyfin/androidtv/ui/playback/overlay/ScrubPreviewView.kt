package org.jellyfin.androidtv.ui.playback.overlay

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.AbstractComposeView
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Fullscreen overlay that shows a single bitmap (a trickplay frame) scaled to fit while scrubbing.
 * When the bitmap is null it renders nothing (fully transparent) and does not intercept input.
 */
class ScrubPreviewView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyle: Int = 0,
) : AbstractComposeView(context, attrs, defStyle) {
	private val bitmapState = MutableStateFlow<Bitmap?>(null)

	fun setBitmap(bitmap: Bitmap?) {
		bitmapState.value = bitmap
	}

	@Composable
	override fun Content() {
		val bitmap by bitmapState.collectAsState()
		val current = bitmap ?: return

		Image(
			bitmap = current.asImageBitmap(),
			contentDescription = null,
			contentScale = ContentScale.Fit,
			modifier = Modifier.fillMaxSize(),
		)
	}
}
