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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.transition.Transition;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer;
import com.google.firebase.ml.vision.document.FirebaseVisionCloudDocumentRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.menupan.translate.apps.common.FrameMetadata;
import com.menupan.translate.apps.common.GraphicOverlay;
import com.menupan.translate.apps.common.TextTranslateProcessor;
import com.menupan.translate.apps.java.VisionProcessorBase;
import com.menupan.translate.apps.java.models.ZhTc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Processor for the cloud document text detector demo.
 */
public class CloudDocumentTextRecognitionProcessor
        extends VisionProcessorBase<FirebaseVisionDocumentText> {

    private static final String TAG = "CloudDocTextRecProc";

    private final FirebaseVisionDocumentTextRecognizer detector;

    private GraphicOverlay graphicOverlayNew ;

    private List<FirebaseVisionDocumentText.Block> blockList;

    private HashMap<String, String> map;

    String twText= "Original String";
    String koText = "";

    public CloudDocumentTextRecognitionProcessor() {
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
        // graphicOverlay.clear();
        Log.d(TAG, "detected text is: " + text.getText());
        List<FirebaseVisionDocumentText.Block> blocks = text.getBlocks();
        graphicOverlayNew = graphicOverlay;
        FirebaseTranslatorOptions options1 = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(FirebaseTranslateLanguage.ZH)
                .setTargetLanguage(FirebaseTranslateLanguage.EN)
                .build();
        final FirebaseTranslator taiwanEnglishTranslator =
                FirebaseNaturalLanguage.getInstance().getTranslator(options1);

        FirebaseTranslatorOptions options2 = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(FirebaseTranslateLanguage.EN)
                .setTargetLanguage(FirebaseTranslateLanguage.KO)
                .build();
        final FirebaseTranslator englishKoreaTranslator =
                FirebaseNaturalLanguage.getInstance().getTranslator(options2);

        blockList = blocks;
        map = new HashMap<>();
        final int[] ll = {0};
        final int[] lll = {0};
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionDocumentText.Paragraph> paragraphs = blocks.get(i).getParagraphs();
            for (int j = 0; j < paragraphs.size(); j++) {
                List<FirebaseVisionDocumentText.Word> words = paragraphs.get(j).getWords();
                for (int l = 0; l < words.size(); l++) {
                    List<FirebaseVisionDocumentText.Symbol> symbols = words.get(l).getSymbols();
                    for (int m = 0; m < symbols.size(); m++) {
                        lll[0]++;
//                        CloudDocumentTextGraphic cloudDocumentTextGraphic = new CloudDocumentTextGraphic(graphicOverlay, symbols.get(m), koText);
//                        graphicOverlay.add(cloudDocumentTextGraphic);
                    }
                }
            }
        }

        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionDocumentText.Paragraph> paragraphs = blocks.get(i).getParagraphs();
            for (int j = 0; j < paragraphs.size(); j++) {
                List<FirebaseVisionDocumentText.Word> words = paragraphs.get(j).getWords();
                for (int l = 0; l < words.size(); l++) {
                    List<FirebaseVisionDocumentText.Symbol> symbols = words.get(l).getSymbols();
                    for (int m = 0; m < symbols.size(); m++) {
                        final String final_zh_text = symbols.get(m).getText();
                        final String[] final_en_text = {""};
                        taiwanEnglishTranslator.translate(symbols.get(m).getText())
                                .addOnSuccessListener(
                                        new OnSuccessListener<String>() {
                                            @Override
                                            public void onSuccess(@NonNull String en_text) {
                                                Log.e(TAG,"englishText  englishText  englishText  :: "+en_text);
                                                final_en_text[0] = en_text;
                                                englishKoreaTranslator.translate(en_text)
                                                        .addOnSuccessListener(
                                                                new OnSuccessListener<String>() {
                                                                    @Override
                                                                    public void onSuccess(@NonNull String ko_text) {
                                                                        if(ko_text == null){ko_text = " ";}
                                                                        map.put("zh"+ll[0], final_zh_text);
                                                                        map.put("ko"+ll[0], ko_text);
                                                                        map.put("en"+ll[0], final_en_text[0]);
                                                                        ll[0]++;
                                                                        if(lll[0] == ll[0]){
                                                                            graphic();
                                                                        }
                                                                    }
                                                                });

                                            }
                                        });

//                        CloudDocumentTextGraphic cloudDocumentTextGraphic = new CloudDocumentTextGraphic(graphicOverlay, symbols.get(m), koText);
//                        graphicOverlay.add(cloudDocumentTextGraphic);
                    }
                }
            }
        }
//        graphicOverlay.postInvalidate();
    }

    public void graphic(){
        graphicOverlayNew.clear();
        int ii = 0;
        int lll = 0;
        HashMap<String, String> map2 = new HashMap<>();
        HashMap<String, Integer> map3 = new HashMap<>();

        for (int i = 0; i < blockList.size(); i++) {
            List<FirebaseVisionDocumentText.Paragraph> paragraphs = blockList.get(i).getParagraphs();
            int jj = 0;
            for (int j = 0; j < paragraphs.size(); j++) {
                List<FirebaseVisionDocumentText.Word> words = paragraphs.get(j).getWords();
                int ll = 0;
                for (int l = 0; l < words.size(); l++) {
                    int mm = 0;
                    String textSum = "";
                    List<FirebaseVisionDocumentText.Symbol> symbols = words.get(l).getSymbols();
                    for (int m = 0; m < symbols.size(); m++) {
                        String text = map.get("ko"+lll);
                        if(text == null){
                            text = "";
                        }else if(text.equals("위안")){
                            text = "";
                        }else if(text.equals("ruan")){
                            text = "";
                        }else if(text.matches(".*[0-9].*")){
                            text = "";
                        }
                        String entext = map.get("en"+lll);
                        if(entext == null){entext = "";}
                        String zhtext = map.get("zh"+lll);
                        if(zhtext == null){zhtext = "";}

                        if(i == ii && j == jj){
                            textSum =  textSum + "  "+text;
                            if(l+1 == symbols.size()) {
                                map3.put("int1"+lll,i);
                                map3.put("int2"+lll,j);
                                map3.put("int3"+lll,symbols.size());
                                map2.put("text"+lll,textSum);
                            }else{
                                map3.put("int1"+lll,i);
                                map3.put("int2"+lll,j);
                                map3.put("int3"+lll,0);
                                map2.put("text"+lll,"");
                            }

                        }else{
                            map3.put("int1"+lll,i);
                            map3.put("int2"+lll,j);
                            map3.put("int3"+lll,0);
                            map2.put("text"+lll,"");
                        }

                        Log.e(TAG,"textSum textSum textSum textSumtSum :: "+" ii "+ii+"  jj "+jj+"  ll  "+ll+"  mm  "+mm+"  ::  "+textSum);
                        // writeNewZhTc(zhtext,entext,text,"N","");
                        lll++;
                        mm++;
                    }
                    ll++;
                }
                jj++;
            }
            ii++;
        }

        for (int i = 0; i < blockList.size(); i++) {
            List<FirebaseVisionDocumentText.Paragraph> paragraphs = blockList.get(i).getParagraphs();
            int jj = 0;
            for (int j = 0; j < paragraphs.size(); j++) {
                List<FirebaseVisionDocumentText.Word> words = paragraphs.get(j).getWords();
                int ll = 0;
                for (int l = 0; l < words.size(); l++) {
                    int mm = 0;
                    String textSum = "";
                    List<FirebaseVisionDocumentText.Symbol> symbols = words.get(l).getSymbols();
                    for (int m = 0; m < symbols.size(); m++) {

                        String sumText = "";
                        for(int n = 0; n < map3.size(); n++) {
                            if(map3.get("int1"+n) == i && map3.get("int2"+n) == j && map3.get("int3"+n) > 0) {
                                if(l == 0) {
                                    sumText = map2.get("text" + n);
                                }
                                break;
                            }
                        }
                        CloudDocumentTextGraphic cloudDocumentTextGraphic = new CloudDocumentTextGraphic(graphicOverlayNew, symbols.get(m), sumText);
                        graphicOverlayNew.add(cloudDocumentTextGraphic);

                        lll++;
                        ll++;
                    }
                }
            }
        }

        graphicOverlayNew.postInvalidate();
    }


    private DatabaseReference mDatabase;
    public void writeNewZhTc(String zh, String en, String ko, String upyn, String imgurl) {
        ZhTc zhTc = new ZhTc(zh,en, ko, upyn, imgurl);
        // mDatabase.child("zh_tc").child(zh).getKey();
        mDatabase.child("zh_tc").child(zh).setValue(zhTc);
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.w(TAG, "Cloud Document Text detection failed." + e);
    }
}
