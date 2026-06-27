package org.jellyfin.androidtv.ui.playback.overlay

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.AbstractComposeView
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Fullscreen overlay that shows a single bitmap (a trickplay frame) scaled to fit while scrubbing.
 * When a bitmap is set the overlay is fully opaque (black behind the frame) so the playing/paused
 * video underneath can't bleed through; when null it renders nothing and doesn't intercept input.
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

		Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
			Image(
				bitmap = current.asImageBitmap(),
				contentDescription = null,
				contentScale = ContentScale.Fit,
				modifier = Modifier.fillMaxSize(),
			)
		}
	}
}
