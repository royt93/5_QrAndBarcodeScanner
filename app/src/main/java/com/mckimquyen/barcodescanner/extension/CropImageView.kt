package com.mckimquyen.barcodescanner.extension

import android.graphics.Bitmap
import com.isseiaoki.simplecropview.CropImageView
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

/**
 * Extension functions to bridge SimpleCropView's RxJava1 to RxJava3
 */

fun com.isseiaoki.simplecropview.LoadRequest.executeAsCompletable3(): Completable {
    return Completable.create { emitter ->
        val rxCompletable = this.executeAsCompletable()
        rxCompletable.subscribe(
            { emitter.onComplete() },
            { error -> emitter.onError(error) }
        )
    }
}

fun CropImageView.cropAsSingle3(): Single<Bitmap> {
    return Single.create { emitter ->
        val rxSingle = this.cropAsSingle()
        rxSingle.subscribe(
            { bitmap -> emitter.onSuccess(bitmap) },
            { error -> emitter.onError(error) }
        )
    }
}
