package com.example.hotel_app.presentation.ui.utils

import android.content.Context
import android.widget.ImageView
import coil.ImageLoader
import coil.load
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.hotel_app.R

/**
 * Утилита для загрузки изображений с правильными настройками Coil.
 * 
 * ## Dispatchers:
 * - Coil автоматически использует Dispatchers.IO для загрузки изображений
 * - Обновление UI происходит на Dispatchers.Main
 * 
 * ## Примеры использования:
 * ```kotlin
 * // Загрузка из сети
 * ImageLoadingUtils.loadImage(imageView, imageUrl)
 * 
 * // Загрузка с кастомными настройками
 * ImageLoadingUtils.loadImage(
 *     imageView = imageView,
 *     data = imageUrl,
 *     placeholder = R.drawable.placeholder,
 *     error = R.drawable.error
 * )
 * ```
 */
object ImageLoadingUtils {

    /**
     * Загрузка изображения из сети с настройками по умолчанию.
     * Coil автоматически использует Dispatchers.IO для загрузки.
     *
     * @param imageView ImageView для отображения
     * @param imageUrl URL изображения
     * @param placeholder Ресурс для отображения во время загрузки
     * @param error Ресурс для отображения при ошибке
     */
    fun loadImage(
        imageView: ImageView,
        imageUrl: String?,
        placeholder: Int = R.drawable.ic_launcher_background,
        error: Int = R.drawable.ic_launcher_background
    ) {
        imageView.load(imageUrl) {
            // ✅ Coil автоматически использует правильный Dispatcher
            // Но можно явно указать при необходимости:
            // dispatcher(Dispatchers.IO)
            
            crossfade(true)
            placeholder(placeholder)
            error(error)
            
            // Кэширование
            memoryCacheKey(imageUrl)
            diskCacheKey(imageUrl)
        }
    }

    /**
     * Загрузка изображения с callback для обработки ошибок.
     *
     * @param imageView ImageView для отображения
     * @param data Данные для загрузки (URL, File, Uri, resource ID)
     * @param placeholder Ресурс для отображения во время загрузки
     * @param error Ресурс для отображения при ошибке
     * @param onError Callback при ошибке загрузки
     * @param onSuccess Callback при успешной загрузке
     */
    fun loadImage(
        imageView: ImageView,
        data: Any?,
        placeholder: Int = R.drawable.ic_launcher_background,
        error: Int = R.drawable.ic_launcher_background,
        onError: ((Throwable) -> Unit)? = null,
        onSuccess: (() -> Unit)? = null
    ) {
        imageView.load(data) {
            crossfade(true)
            placeholder(placeholder)
            error(error)
            
            listener(
                onError = { request: ImageRequest, result: ErrorResult ->
                    onError?.invoke(result.throwable)
                    result.throwable.printStackTrace()
                },
                onSuccess = { request: ImageRequest, result: SuccessResult ->
                    onSuccess?.invoke()
                }
            )
        }
    }

    /**
     * Загрузка изображения с трансформациями.
     *
     * @param imageView ImageView для отображения
     * @param data Данные для загрузки
     * @param transformations Трансформации (crop, circle, blur)
     * @param placeholder Ресурс для отображения во время загрузки
     * @param error Ресурс для отображения при ошибке
     */
    fun loadImageWithTransform(
        imageView: ImageView,
        data: Any?,
        transformations: List<coil.transform.Transformation> = emptyList(),
        placeholder: Int = R.drawable.ic_launcher_background,
        error: Int = R.drawable.ic_launcher_background
    ) {
        imageView.load(data) {
            crossfade(true)
            placeholder(placeholder)
            error(error)
            transformations(transformations)
        }
    }

    /**
     * Предзагрузка изображения в кэш.
     * Полезно для предварительной загрузки изображений.
     *
     * @param context Context
     * @param imageUrl URL изображения для предзагрузки
     */
    fun preloadImage(context: Context, imageUrl: String) {
        val imageLoader = ImageLoader.Builder(context).build()
        imageLoader.enqueue(
            ImageRequest.Builder(context)
                .data(imageUrl)
                .build()
        )
    }

    /**
     * Очистка кэша изображений.
     * Выполняется асинхронно.
     *
     * @param context Context
     */
    fun clearCache(context: Context) {
        val imageLoader = ImageLoader.Builder(context).build()
        imageLoader.memoryCache?.clear()
        imageLoader.diskCache?.clear()
    }
}
