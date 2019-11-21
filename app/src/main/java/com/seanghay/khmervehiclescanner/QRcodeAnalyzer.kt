package com.seanghay.khmervehiclescanner

import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject

class QRcodeAnalyzer : ImageAnalysis.Analyzer {

    private val reader: MultiFormatReader = MultiFormatReader()

    private val resultObservable = PublishSubject.create<String>()

    init {
        val map = mapOf<DecodeHintType, Collection<BarcodeFormat>>(
            Pair(DecodeHintType.POSSIBLE_FORMATS, arrayListOf(BarcodeFormat.QR_CODE))
        )
        reader.setHints(map)
    }

    fun getResultFlowable(): Flowable<String> =
        resultObservable.toFlowable(BackpressureStrategy.LATEST)

    override fun analyze(image: ImageProxy, rotationDegrees: Int) {
        if (ImageFormat.YUV_420_888 != image.format) {
            Log.e("BarcodeAnalyzer", "expect YUV_420_888, now = ${image.format}")
            return

        }
        val buffer = image.planes[0].buffer
        val data = ByteArray(buffer.remaining())
        val height = image.height
        val width = image.width
        buffer.get(data)
        val source = PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false)
        val bitmap = BinaryBitmap(HybridBinarizer(source))

        try {
            val result = reader.decode(bitmap)
            resultObservable.onNext(result.text)
        } catch (e: Exception) {
            resultObservable.onNext("")
        }
    }
}
