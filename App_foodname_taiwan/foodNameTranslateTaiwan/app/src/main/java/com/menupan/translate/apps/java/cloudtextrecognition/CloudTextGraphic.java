// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.menupan.translate.apps.java.cloudtextrecognition;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.menupan.translate.apps.common.GraphicOverlay;
import com.menupan.translate.apps.common.GraphicOverlay.Graphic;

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
public class CloudTextGraphic extends Graphic {
  private static final int RECT_COLOR = Color.YELLOW;
  private int TEXT_COLOR = Color.WHITE;
  private float TEXT_SIZE = 54.0f;
  private static final float STROKE_WIDTH = 0.0f;

  private final Paint rectPaint;
  private final Paint textPaint;
  private final FirebaseVisionText.Element element;
  private final GraphicOverlay overlay;
  private String twText = "";

  private static final String TEXT_BLACK = "BLACK";
  private static final String TEXT_WHITE = "WHITE";
  private static final String TEXT_RED = "RED";
  private static final String TEXT_BLUE = "BLUE";
  private static final String TEXT_DKGRAY = "DKGRAY";
  private static final String TEXT_MAGENTA = "MAGENTA";
  private static final String TEXT_GREEN = "GREEN";
  private static final String TEXT_LTGRAY = "LTGRAY";
  //private static final String TEXT_TRANSPARENT = "글자색:BLACK";
  private static final String TEXT_CYAN = "CYAN";
  private static final String TEXT_YELLOW = "YELLOW";
  private int TEXT_CNT = 0;
  CloudTextGraphic(GraphicOverlay overlay, FirebaseVisionText.Element element, String twText,String text_color, String text_size, int l) {
    super(overlay);
    TEXT_CNT = l;
    this.element = element;
    this.overlay = overlay;
    this.twText = twText;
    float f_num = Float.valueOf(text_size);
    TEXT_SIZE = f_num;

    switch (text_color) {

      case TEXT_WHITE:
        TEXT_COLOR = Color.WHITE;
        break;
      case TEXT_BLACK:
        TEXT_COLOR = Color.BLACK;
        break;
      case TEXT_RED:
        TEXT_COLOR = Color.RED;
        break;
      case TEXT_BLUE:
        TEXT_COLOR = Color.BLUE;
        break;
      case TEXT_DKGRAY:
        TEXT_COLOR = Color.DKGRAY;
        break;
      case TEXT_MAGENTA:
        TEXT_COLOR = Color.MAGENTA;
        break;
      case TEXT_GREEN:
        TEXT_COLOR = Color.GREEN;
        break;
      case TEXT_LTGRAY:
        TEXT_COLOR = Color.LTGRAY;
        break;
      case TEXT_CYAN:
        TEXT_COLOR = Color.CYAN;
        break;
      case TEXT_YELLOW:
        TEXT_COLOR = Color.YELLOW;
        break;
      default:
        throw new IllegalStateException("Unknown size");
    }

    rectPaint = new Paint();
    rectPaint.setColor(TEXT_COLOR);
    rectPaint.setAlpha(80);
    rectPaint.setStyle(Paint.Style.STROKE);
    rectPaint.setStrokeWidth(STROKE_WIDTH);

    textPaint = new Paint();
    textPaint.setColor(TEXT_COLOR);
    textPaint.setTextSize(TEXT_SIZE);
  }

  /** Draws the text block annotations for position, size, and raw value on the supplied canvas. */
  @Override
  public void draw(Canvas canvas) {
    if (element == null) {
      throw new IllegalStateException("Attempting to draw a null text.");
    }
        //adminif(TEXT_CNT == 0) canvas.saveLayerAlpha(0, 0, canvas.getWidth(), canvas.getHeight(), 0xAF, Canvas.ALL_SAVE_FLAG);
        Rect rect = element.getBoundingBox();
        // canvas.drawARGB(1, 0, 0, 0);
         canvas.drawRect(rect, rectPaint);
        // canvas.drawText(element.getText(), rect.left, rect.bottom, textPaint);

        canvas.drawText(twText, rect.left, rect.bottom, textPaint);
  }
}
