package org.jellyfin.androidtv.ui.playback.overlay

import android.content.Context
import android.graphics.Bitmap
import coil3.ImageLoader
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.maxBitmapSize
import coil3.request.transformations
import coil3.size.Dimension
import coil3.size.Size
import coil3.toBitmap
import org.jellyfin.androidtv.util.coil.SubsetTransformation
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.trickplayApi
import org.jellyfin.sdk.api.client.util.AuthorizationHeaderBuilder
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.MediaSourceInfo
import org.jellyfin.sdk.model.serializer.toUUIDOrNull
import java.util.concurrent.atomic.AtomicInteger

/**
 * Loads a single trickplay frame (a cropped tile) for an arbitrary playback position, for the
 * fullscreen scrub preview. Same fetch as [CustomSeekProvider], but driven by a position in ms and
 * with a latest-wins guard so only the most recent request updates the preview while scrubbing.
 */
class TrickplayScrubLoader(
	private val context: Context,
	private val imageLoader: ImageLoader,
	private val api: ApiClient,
) {
	private val requestNumber = AtomicInteger(0)

	fun interface BitmapCallback {
		fun onBitmap(bitmap: Bitmap)
	}

	fun hasTrickplay(item: BaseItemDto?, mediaSource: MediaSourceInfo?): Boolean {
		if (item == null || mediaSource == null) return false
		return item.trickplay?.get(mediaSource.id)?.values?.firstOrNull() != null
	}

	fun load(item: BaseItemDto, mediaSource: MediaSourceInfo, timeMs: Long, callback: BitmapCallback) {
		val mediaSourceId = mediaSource.id?.toUUIDOrNull() ?: return
		val trickPlayInfo = item.trickplay?.get(mediaSource.id)?.values?.firstOrNull() ?: return

		val tile = timeMs.floorDiv(trickPlayInfo.interval).toInt()
		val tilesPerSheet = trickPlayInfo.tileWidth * trickPlayInfo.tileHeight
		val sheetIndex = tile / tilesPerSheet
		val tileOffset = tile % tilesPerSheet
		val offsetX = (tileOffset % trickPlayInfo.tileWidth) * trickPlayInfo.width
		val offsetY = (tileOffset / trickPlayInfo.tileWidth) * trickPlayInfo.height

		val url = api.trickplayApi.getTrickplayTileImageUrl(
			itemId = item.id,
			width = trickPlayInfo.width,
			index = sheetIndex,
			mediaSourceId = mediaSourceId,
		)

		val headers = NetworkHeaders.Builder().apply {
			set(
				key = "Authorization",
				value = AuthorizationHeaderBuilder.buildHeader(
					api.clientInfo.name,
					api.clientInfo.version,
					api.deviceInfo.id,
					api.deviceInfo.name,
					api.accessToken,
				),
			)
		}.build()

		val request = requestNumber.incrementAndGet()

		imageLoader.enqueue(ImageRequest.Builder(context).apply {
			data(url)
			size(Size.ORIGINAL)
			maxBitmapSize(Size(Dimension.Undefined, Dimension.Undefined))
			httpHeaders(headers)
			transformations(SubsetTransformation(offsetX, offsetY, trickPlayInfo.width, trickPlayInfo.height))
			target(
				onSuccess = { image ->
					// Only the most recent scrub request updates the preview.
					if (request == requestNumber.get()) callback.onBitmap(image.toBitmap())
				},
			)
		}.build())
	}
}
