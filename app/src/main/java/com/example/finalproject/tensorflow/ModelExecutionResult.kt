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
import android.graphics.Bitmap

@SuppressWarnings("GoodTime")
data class ModelExecutionResult(
  val styledImage: Bitmap,
  val preProcessTime: Long = 0L,
  val stylePredictTime: Long = 0L,
  val styleTransferTime: Long = 0L,
  val postProcessTime: Long = 0L,
  val totalExecutionTime: Long = 0L,
  val executionLog: String = "",
  val errorMessage: String = ""
)
