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
package com.menupan.translate.apps.common;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.menupan.translate.apps.common.CameraImageGraphic;
import com.menupan.translate.apps.common.FrameMetadata;
import com.menupan.translate.apps.common.GraphicOverlay;
import com.menupan.translate.apps.java.VisionProcessorBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


/**
 * Processor for the text recognition demo.
 */
public class TextTranslateProcessor {

    private final static String URL = "https://www.googleapis.com/language/translate/v2?key=";
    private final static String KEY = "";
    private final static String TARGET = "&target=ko";
    private final static String SOURCE = "&source=cn";
    private final static String QUERY = "&q=";

    String twText= "Original String";
    String koText = "";

    TextTranslateProcessor(String twText) {
        this.twText = twText;
    }
        public String textTranslate(String twText) {
            StringBuffer result = new StringBuffer();
            try {
                String encodedText = URLEncoder.encode(twText, "UTF-8");


                String html = URL + KEY + SOURCE + TARGET + QUERY + encodedText;
                URL url = new URL(html);


                URLConnection comm = url.openConnection();
//                            comm.setRequestProperty("User-Agent",
//                                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");


                BufferedReader bin = new BufferedReader(new InputStreamReader(comm.getInputStream()));

                StringBuffer sf = new StringBuffer();
                String line;
                while ((line = bin.readLine()) != null) {
                    sf.append(line);
                }
                // result는 Json 데이터

                JSONArray jarray = new JSONObject(sf.toString()).getJSONArray("translations");
                for (int ii = 0; ii < jarray.length(); ii++) {
                    HashMap map = new HashMap<>();
                    JSONObject jObject = jarray.getJSONObject(ii);
                    koText = jObject.optString("translatedText");
                }


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return koText;
        }

}
