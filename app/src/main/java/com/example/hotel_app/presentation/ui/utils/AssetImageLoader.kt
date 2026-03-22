package com.example.hotel_app.presentation.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Утилита для загрузки и обработки изображений из assets.
 * Использует Dispatchers.IO для декодирования Bitmap.
 */
object AssetImageLoader {

    /**
     * Загрузка Bitmap из assets на IO Dispatcher.
     *
     * @param context Context
     * @param assetPath Путь к файлу в assets (например, "rooms/room_101.jpg")
     * @return Bitmap или null при ошибке
     */
    suspend fun loadFromAssets(
        context: Context,
        assetPath: String
    ): Bitmap? {
        // ✅ Декодирование выполняется на IO Dispatcher
        return withContext(Dispatchers.IO) {
            try {
                context.assets.open(assetPath).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Загрузка Bitmap с обработкой ошибки.
     *
     * @param context Context
     * @param assetPath Путь к файлу в assets
     * @param onError Callback при ошибке
     * @return Bitmap или null
     */
    suspend fun loadFromAssets(
        context: Context,
        assetPath: String,
        onError: (Exception) -> Unit
    ): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                context.assets.open(assetPath).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            } catch (e: Exception) {
                onError(e)
                null
            }
        }
    }

    /**
     * Загрузка и обработка Bitmap (ресайз, crop, etc).
     *
     * @param context Context
     * @param assetPath Путь к файлу в assets
     * @param processor Функция обработки Bitmap
     * @return Обработанный Bitmap
     */
    suspend fun loadAndProcess(
        context: Context,
        assetPath: String,
        processor: (Bitmap) -> Bitmap
    ): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                context.assets.open(assetPath).use { inputStream ->
                    val original = BitmapFactory.decodeStream(inputStream)
                    original?.let { processor(it) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Оптимизированная загрузка с уменьшением размера.
     * Использует inSampleSize для экономии памяти.
     *
     * @param context Context
     * @param assetPath Путь к файлу в assets
     * @param reqWidth Требуемая ширина
     * @param reqHeight Требуемая высота
     * @return Bitmap с оптимизированным размером
     */
    suspend fun loadResizedImage(
        context: Context,
        assetPath: String,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                context.assets.open(assetPath).use { inputStream ->
                    // Сначала получаем размеры изображения
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeStream(inputStream, null, options)

                    // Вычисляем inSampleSize
                    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

                    // Декодируем с уменьшенным размером
                    inputStream.reset()
                    options.inJustDecodeBounds = false
                    BitmapFactory.decodeStream(inputStream, null, options)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Расчёт inSampleSize для оптимизации памяти.
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height, width) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight &&
                   halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}
