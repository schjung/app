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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.menupan.translate.apps.common.FrameMetadata;
import com.menupan.translate.apps.common.GraphicOverlay;
import com.menupan.translate.apps.java.VisionProcessorBase;
import com.menupan.translate.apps.java.models.ZhTc;
import com.menupan.translate.apps.java.models.ZhTcPosts;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Processor for the cloud text detector demo.
 */
public class CloudTextSumRecognitionProcessor extends VisionProcessorBase<FirebaseVisionText> {

    private static final String TAG = "CloudTextRecProc";

    private final FirebaseVisionTextRecognizer detector;

    private GraphicOverlay graphicOverlayNew ;
    private HashMap<String, String> map;
    private HashMap<String, String> zhmap;

    // Translate translate;
    private List<FirebaseVisionText.TextBlock> blockList;
    private DatabaseReference mDatabase;
    private ZhTc zhTc;
    public CloudTextSumRecognitionProcessor() {
        super();
        FirebaseVisionCloudTextRecognizerOptions options = new FirebaseVisionCloudTextRecognizerOptions.Builder().setLanguageHints(Arrays.asList("zh")).build();
        detector = FirebaseVision.getInstance().getCloudTextRecognizer(options);
    }

    @Override
    protected Task<FirebaseVisionText> detectInImage(FirebaseVisionImage image) {
        return detector.processImage(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull FirebaseVisionText text,
            @NonNull FrameMetadata frameMetadata,
            @NonNull final GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        graphicOverlayNew = graphicOverlay;
        if (text == null) {
            return; // TODO: investigate why this is needed
        }

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

//        mDatabase.child("zh_tc_list").addListenerForSingleValueEvent(
//             new ValueEventListener() {
//                 @Override
//                 public void onDataChange(DataSnapshot dataSnapshot) {
//
//                     zhTc = dataSnapshot.getValue(ZhTc.class);
//                     Log.e(TAG, "addListenerForSingleValueEvent :: zh  " + zhTc.zh + " ko " + zhTc.ko + "  en " + zhTc.en);
//                 }
//
//                 @Override
//                 public void onCancelled(DatabaseError databaseError) {
//
//                 }
//             });

        List<FirebaseVisionText.TextBlock> blocks = text.getTextBlocks();
        blockList = blocks;
        map = new HashMap<>();
        zhmap = new HashMap<>();
        final int[] ll = {0};
        final int[] lll = {0};
        final int[] mm = {0};
        final String[] oldZhText = {""};
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                final List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                // elementList = elements;
                for (int l = 0; l < elements.size(); l++) {
                    lll[0]++;
                    Pattern pattern = Pattern.compile("^[a-zA-Z0-9`~!@#$%^&*()-=_+\\[\\]{}:;',./<>?\\\\|]*$");
                    Matcher matcher = pattern.matcher(elements.get(l).getText());
                    String zhText = elements.get(l).getText();
                    Boolean check = true;
                    if(matcher.find()){
                        zhText = "none";
                        check = false;
                    }else if(zhText.matches(".*[0-9].*")){
                        zhText = "none";
                        check = false;
                    }else if(zhText.equals("元")){
                        zhText = "none";
                        check = false;
                    }
                    if(check) {
//                        mDatabase.child("zh_tc_list").child(zhText).addListenerForSingleValueEvent(
//                                new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(DataSnapshot dataSnapshot) {
//
//                                        zhTc = dataSnapshot.getValue(ZhTc.class);
//                                        if (zhTc.zh != null && !zhTc.zh.equals(oldZhText[0])) {
//                                        //    Log.e(TAG, "addListenerForSingleValueEvent :: zh  " + zhTc.zh + " ko " + zhTc.ko + "  en " + zhTc.en + "  mm[0]  " + mm[0]);
//
//                                            zhmap.put("zh" + mm[0], zhTc.zh);
//                                            zhmap.put("ko" + mm[0], zhTc.ko);
//                                            zhmap.put("en" + mm[0], zhTc.en);
//                                            oldZhText[0] = zhTc.zh;
//                                            mm[0]++;
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onCancelled(DatabaseError databaseError) {
//
//                                    }
//                                });
                    }

                }
            }
        }

        int ii = 0;
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            int jj = 0;
            for (int j = 0; j < lines.size(); j++) {
                final List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                // elementList = elements;
                String sum_text = "";
                for (int l = 0; l < elements.size(); l++) {
                //    Log.e(TAG,"originalText  originalText  originalText  :: "+elements.get(l).getText());

                    final String final_zh_text = "";
                    final String[] final_en_text = {""};
                    final String[] final_ko_text = {""};

                    if(i == ii && j == jj) {
                        sum_text = sum_text + elements.get(l).getText();
                        // final_zh_text = sum_text;
                        Log.e(TAG,"sum_text  sum_text  sum_text  :: "+sum_text);
                        final String final_sum_text = sum_text;
                        taiwanEnglishTranslator.translate(sum_text)
                                .addOnSuccessListener(
                                        new OnSuccessListener<String>() {
                                            @Override
                                            public void onSuccess(@NonNull String en_text) {
                                                //       Log.e(TAG,"englishText  englishText  englishText  :: "+en_text);
                                                final_en_text[0] = en_text;
                                                englishKoreaTranslator.translate(en_text)
                                                        .addOnSuccessListener(
                                                                new OnSuccessListener<String>() {
                                                                    @Override
                                                                    public void onSuccess(@NonNull String ko_text) {
                                                                        final_ko_text[0] = ko_text;
                                                                        if (ko_text == null) {
                                                                            ko_text = " ";
                                                                        }
                                                                        Log.e(TAG,"ko_text  ko_text  ko_text  :: "+ko_text);
                                                                        map.put("zh" + ll[0], final_sum_text);
                                                                        map.put("ko" + ll[0], ko_text);
                                                                        map.put("en" + ll[0], final_en_text[0]);
                                                                        ll[0]++;
                                                                        if (lll[0] == ll[0]) {
                                                                            graphic();
                                                                        }
                                                                    }
                                                                });

                                            }
                                        });
                    }

                }
                jj++;
            }
            ii++;
        }



//        graphicOverlay.postInvalidate();
    }

    public void graphic(){
        mDatabase = FirebaseDatabase.getInstance().getReference();
        graphicOverlayNew.clear();
        int ii = 0;
        int lll = 0;
        HashMap<String, String> map2 = new HashMap<>();
        HashMap<String, Integer> map3 = new HashMap<>();
        for (int i = 0; i < blockList.size(); i++) {
            List<FirebaseVisionText.Line> lines = blockList.get(i).getLines();
            int jj = 0;
            for (int j = 0; j < lines.size(); j++) {
                final List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                String textSum = "";
                int ll = 0;
                // elementList = elements;
                for (int l = 0; l < elements.size(); l++) {
                    // Log.e(TAG,"map graphic map graphic map graphic :: "+" i "+i+"  j "+j+"  l  "+l+"  ::  "+map.get("ko"+lll));
                    String text = map.get("ko"+lll);

                    Pattern pattern = Pattern.compile("^[a-zA-Z0-9`~!@#$%^&*()-=_+\\[\\]{}:;',./<>?\\\\|]*$");
                    Matcher matcher = pattern.matcher(text);

                    String entext = map.get("en"+lll);
                    if(entext == null){entext = "";}
                    String zhtext = map.get("zh"+lll);
                    if(zhtext == null){zhtext = "";}
                    boolean zhCheck = true;
                    if(text == null){
                        text = "";
                        zhCheck = false;
                    }else if(text.equals("위안")){
                        text = "";
                        zhCheck = false;
//                    }else if(text.equals("ruan")){
//                        text = "";
//                        zhCheck = false;
                    }else if(text.matches(".*[0-9].*")){
                        text = "";
                        zhCheck = false;
                    }else if(matcher.find()){
                        text = "";
                        zhCheck = false;
                    }
//                    Log.e(TAG,"zhtext zhtext zhtext zhtext 1111111111111:: "+zhtext);
//                    Log.e(TAG,"text text text text 111111111111111111111:: "+text);
//                    Log.e(TAG, "zhCheck zhCheck zhCheck zhCheck 11111111:: " + zhCheck);
                    for(int mm = 0; mm < zhmap.size();mm++){
//                        Log.e(TAG,"zhtext zhtext zhtext zhtext 2222222222222222222:: "+zhtext);
                    //    Log.e(TAG,"zhmap zhmap zhmapzhmap 222222222222222222222222:: "+zhmap.get("zh"+mm)+"  mm  "+mm);
//
                       if(zhtext.equals(zhmap.get("zh"+mm))){
                           text = zhmap.get("ko"+mm);
                           zhCheck = false;
                           break;
                       }
//                        Log.e(TAG, "zhCheck zhCheck zhCheck zhCheck 22222222222222:: " + zhCheck);
                    }

//                    for(String zh_text : zhTc) {
//                        Log.e(TAG,"zhtext zhtext zhtext zhtext 33333333333333333:: "+zhtext);
//                    }
//                    Log.e(TAG,"text text text text 3333333333333333333333333:: "+text);
//                    Log.e(TAG, "zhCheck zhCheck zhCheck zhCheck 333333333333:: " + zhCheck);
                    if(i == ii && j == jj){
                        textSum =  textSum + "  "+text;
                        if(l+1 == elements.size()) {
                            map3.put("int1"+lll,i);
                            map3.put("int2"+lll,j);
                            map3.put("int3"+lll,elements.size());
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

                    // Log.e(TAG,"textSum textSum textSum textSumtSum :: "+" ii "+ii+"  jj "+jj+"  ll  "+ll+"  ::  "+textSum);

                        if(zhCheck) {
                                Log.e(TAG, "zhtext zhtext zhtext zhtext 999999999999999999999999999999999999999999999999999999:: " + zhtext);
                                Log.e(TAG, "text text text text 999999999999999999999999999999999999999999999999999999:: " + text);

                                writeNewZhTc(zhtext, entext, text, "N", "");
                        }
                    lll++;
                    ll++;
                }
                jj++;
            }
            ii++;
        }

        for (int i = 0; i < blockList.size(); i++) {
            List<FirebaseVisionText.Line> lines = blockList.get(i).getLines();

            for (int j = 0; j < lines.size(); j++) {
                final List<FirebaseVisionText.Element> elements = lines.get(j).getElements();

                // elementList = elements;
                for (int l = 0; l < elements.size(); l++) {
                    String sumText = "";
                            for(int m = 0; m < map3.size(); m++) {
                                if(map3.get("int1"+m) == i && map3.get("int2"+m) == j && map3.get("int3"+m) > 0) {
                                    if(l == 0) {
                                        sumText = map2.get("text" + m);
                                    }
                                    break;
                                }
                            }
                            CloudTextGraphic cloudTextGraphic = new CloudTextGraphic(graphicOverlayNew, elements.get(l), sumText, "","", l);
                            graphicOverlayNew.add(cloudTextGraphic);

                    lll++;

                }

            }

        }

        graphicOverlayNew.postInvalidate();
    }

//    public void writeNewZhTc(String zh, String en, String ko, String upyn, String imgurl) {
//        ZhTc zhTc = new ZhTc(zh, en, ko, upyn, imgurl);
//        Log.e(TAG,"writeNewZhTc :: "+" zh "+zh+"  en "+en+"  ko  "+ko);
//        // mDatabase.child("zh_tc").child(zh).getKey();
//        mDatabase.child("zh_tc_data").child(zh).setValue(zhTc);
//    }

    public void writeNewZhTc(String zh, String en, String ko, String upyn, String imgurl) {

        // String key = mDatabase.child("zh_tc_list").push().getKey();
        ZhTcPosts zhTc = new ZhTcPosts(zh, en, ko, upyn, imgurl);
        Map<String, Object> zhTcValues = zhTc.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/zh_tc_list/" + zh, zhTcValues);
        // Log.e(TAG,"writeNewZhTc :: "+key+" childUpdates "+childUpdates.get("/zh_tc_list/" + key));
        mDatabase.updateChildren(childUpdates);
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.w(TAG, "Cloud Text detection failed." + e);
    }
}
