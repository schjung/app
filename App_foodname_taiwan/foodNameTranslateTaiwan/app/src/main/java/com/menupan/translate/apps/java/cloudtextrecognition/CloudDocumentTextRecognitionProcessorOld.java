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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.document.FirebaseVisionCloudDocumentRecognizerOptions;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.menupan.translate.apps.common.FrameMetadata;
import com.menupan.translate.apps.common.GraphicOverlay;
import com.menupan.translate.apps.java.VisionProcessorBase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Processor for the cloud document text detector demo.
 */
public class CloudDocumentTextRecognitionProcessorOld
        extends VisionProcessorBase<FirebaseVisionDocumentText> {

    private static final String TAG = "CloudDocTextRecProc";

    private final FirebaseVisionDocumentTextRecognizer detector;
   // https://translation.googleapis.com/language/translate/v2/?q=%E5%8F%A4%E4%BB%A3%E4%B9%A6%E9%9D%A2%E6%B1%89%E8%AF%AD%E7%A7%B0%E4%B8%BA%E6%96%87%E8%A8%80%E6%96%87%EF%BC%8C%E7%8E%B0%E4%BB%A3%E4%B9%A6%E9%9D%A2%E6%B1%89%E8%AF%AD%E4%B8%80%E8%88%AC%E6%8C%87%E4%BD%BF%E7%94%A8%E7%8E%B0%E4%BB%A3%E6%A0%87%E5%87%86%E6%B1%89%E8%AF%AD%E8%AF%AD%E6%B3%95%E3%80%81%E8%AF%8D%E6%B1%87%E7%9A%84%E4%B8%AD%E6%96%87%E9%80%9A%E8%A1%8C%E6%96%87%E4%BD%93%EF%BC%88%E5%8F%88%E7%A7%B0%E7%99%BD%E8%AF%9D%E6%96%87%EF%BC%89%E3%80%82&source=zh&target=en&key=YOUR_API_KEY_HERE
    private final static String URL = "https://translation.googleapis.com/language/translate/v2/?key=";
    private final static String KEY = "AIzaSyAG4LN3ZwxHluvoexBR-Nu4u3pN2Seo-ts";
    private final static String TARGET = "&target=ko";
    private final static String SOURCE = "&source=zn";
    private final static String QUERY = "&q=";

    String twText= "Original String";
    String koText = "";

    public CloudDocumentTextRecognitionProcessorOld() {
        super();
        FirebaseVisionCloudDocumentRecognizerOptions options = new FirebaseVisionCloudDocumentRecognizerOptions.Builder().setLanguageHints(Arrays.asList("en","ko","cn", "tw","hi")).build();
        detector = FirebaseVision.getInstance().getCloudDocumentTextRecognizer(options);
    }

    @Override
    protected Task<FirebaseVisionDocumentText> detectInImage(FirebaseVisionImage image) {
        return detector.processImage(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull FirebaseVisionDocumentText text,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        Log.d(TAG, "detected text is: " + text.getText());
        List<FirebaseVisionDocumentText.Block> blocks = text.getBlocks();
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionDocumentText.Paragraph> paragraphs = blocks.get(i).getParagraphs();
            for (int j = 0; j < paragraphs.size(); j++) {
                List<FirebaseVisionDocumentText.Word> words = paragraphs.get(j).getWords();
                for (int l = 0; l < words.size(); l++) {
                    List<FirebaseVisionDocumentText.Symbol> symbols = words.get(l).getSymbols();
                    for (int m = 0; m < symbols.size(); m++) {
                        StringBuffer result = new StringBuffer();
                        try{
                            twText = symbols.get(m).getText();
                            // String encodedText = URLEncoder.encode(cnText,"UTF-8");
                            java.net.URL url = new URL(URL + KEY + SOURCE + TARGET + QUERY + twText);

                            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                            InputStream stream;
                            if(conn.getResponseCode() == 200){
                                stream = conn.getInputStream();
                            }else{
                                stream = conn.getErrorStream();
                            }

                            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                            String line = "";

                            while ((line = reader.readLine()) != null){
                                result.append(line);
                            }
                            // result는 Json 데이터
                            JsonParser parser = new JsonParser();

                            JsonElement element = parser.parse(result.toString());

                            if(element.isJsonObject()){
                                JsonObject object = element.getAsJsonObject();
                                if(object.get("error") == null){
                                    // Json data를 파싱하여 translations 히위 데이터 삽입
                                    koText = object.get("data").getAsJsonObject().
                                            get("translations").getAsJsonArray().
                                            get(0).getAsJsonObject().
                                            get("translatedText").getAsString();
                                }
                            }

                            if(conn.getResponseCode() != 200){
                                Log.e("GoogleTranslatorTask", result.toString());
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        CloudDocumentTextGraphic cloudDocumentTextGraphic = new CloudDocumentTextGraphic(graphicOverlay, symbols.get(m), koText);
                        graphicOverlay.add(cloudDocumentTextGraphic);
                    }
                }
            }
        }
        graphicOverlay.postInvalidate();
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.w(TAG, "Cloud Document Text detection failed." + e);
    }
}
