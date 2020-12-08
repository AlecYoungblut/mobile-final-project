/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.finalproject.tensorflow

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.RectF
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class ImageUtils {
  companion object {
    private fun decodeExifOrientation(orientation: Int): Matrix {
      val matrix = Matrix()

      // Apply transformation corresponding to declared EXIF orientation
      when (orientation) {
        ExifInterface.ORIENTATION_NORMAL, ExifInterface.ORIENTATION_UNDEFINED -> Unit
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90F)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180F)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270F)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1F, 1F)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1F, -1F)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
          matrix.postScale(-1F, 1F)
          matrix.postRotate(270F)
        }
        ExifInterface.ORIENTATION_TRANSVERSE -> {
          matrix.postScale(-1F, 1F)
          matrix.postRotate(90F)
        }

        else -> throw IllegalArgumentException("Invalid orientation: $orientation")
      }

      return matrix
    }

    fun decodeBitmap(file: File): Bitmap {
      val exif = ExifInterface(file.absolutePath)
      val transformation =
        decodeExifOrientation(
          exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90
          )
        )

      val bitmap = BitmapFactory.decodeFile(file.absolutePath)
      return Bitmap.createBitmap(
        BitmapFactory.decodeFile(file.absolutePath),
        0, 0, bitmap.width, bitmap.height, transformation, true
      )
    }

    fun scaleBitmapAndKeepRatio(
      targetBmp: Bitmap,
      reqHeightInPixels: Int,
      reqWidthInPixels: Int
    ): Bitmap {
      if (targetBmp.height == reqHeightInPixels && targetBmp.width == reqWidthInPixels) {
        return targetBmp
      }
      val matrix = Matrix()
      matrix.setRectToRect(
        RectF(
          0f, 0f,
          targetBmp.width.toFloat(),
          targetBmp.width.toFloat()
        ),
        RectF(
          0f, 0f,
          reqWidthInPixels.toFloat(),
          reqHeightInPixels.toFloat()
        ),
        Matrix.ScaleToFit.FILL
      )
      return Bitmap.createBitmap(
        targetBmp, 0, 0,
        targetBmp.width,
        targetBmp.width, matrix, true
      )
    }

    fun bitmapToByteBuffer(
      bitmapIn: Bitmap,
      width: Int,
      height: Int,
      mean: Float = 0.0f,
      std: Float = 255.0f
    ): ByteBuffer {
      val bitmap = scaleBitmapAndKeepRatio(bitmapIn, width, height)
      val inputImage = ByteBuffer.allocateDirect(1 * width * height * 3 * 4)
      inputImage.order(ByteOrder.nativeOrder())
      inputImage.rewind()

      val intValues = IntArray(width * height)
      bitmap.getPixels(intValues, 0, width, 0, 0, width, height)
      var pixel = 0
      for (y in 0 until height) {
        for (x in 0 until width) {
          val value = intValues[pixel++]

          inputImage.putFloat(((value shr 16 and 0xFF) - mean) / std)
          inputImage.putFloat(((value shr 8 and 0xFF) - mean) / std)
          inputImage.putFloat(((value and 0xFF) - mean) / std)
        }
      }

      inputImage.rewind()
      return inputImage
    }

    fun createEmptyBitmap(imageWidth: Int, imageHeigth: Int, color: Int = 0): Bitmap {
      val ret = Bitmap.createBitmap(imageWidth, imageHeigth, Bitmap.Config.RGB_565)
      if (color != 0) {
        ret.eraseColor(color)
      }
      return ret
    }

    fun loadBitmapFromResources(context: Context, path: String): Bitmap {
      val inputStream = context.assets.open(path)
      return BitmapFactory.decodeStream(inputStream)
    }

    fun convertArrayToBitmap(
      imageArray: Array<Array<Array<FloatArray>>>,
      imageWidth: Int,
      imageHeight: Int
    ): Bitmap {
      val conf = Bitmap.Config.ARGB_8888
      val styledImage = Bitmap.createBitmap(imageWidth, imageHeight, conf)

      for (x in imageArray[0].indices) {
        for (y in imageArray[0][0].indices) {
          val color = Color.rgb(
            ((imageArray[0][x][y][0] * 255).toInt()),
            ((imageArray[0][x][y][1] * 255).toInt()),
            (imageArray[0][x][y][2] * 255).toInt()
          )

          styledImage.setPixel(y, x, color)
        }
      }
      return styledImage
    }
  }
}
