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
package com.menupan.translate.apps.java;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.annotation.KeepName;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateRemoteModel;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.menupan.translate.apps.R;
import com.menupan.translate.apps.common.GraphicOverlay;
import com.menupan.translate.apps.common.VisionImageProcessor;
import com.menupan.translate.apps.common.preference.SettingsActivity;
import com.menupan.translate.apps.java.cloudtextrecognition.CloudTextEnRecognitionProcessor;
import com.menupan.translate.apps.java.cloudtextrecognition.CloudTextRecognitionProcessor;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Activity demonstrating different image detector features with a still image from camera. */
@KeepName
public final class StillImageActivity extends AppCompatActivity {

  private static final String TAG = "StillImageActivity";

  private static final String CLOUD_LABEL_DETECTION = "Cloud Label";
  private static final String CLOUD_LANDMARK_DETECTION = "Landmark";
  private static final String CLOUD_TEXT_DETECTION = "한글";
  private static final String CLOUD_DOCUMENT_TEXT_DETECTION = "영문";

  private static final String SIZE_PREVIEW = "w:max"; // Available on-screen width.
  private static final String SIZE_1024_768 = "w:1024"; // ~1024*768 in a normal ratio
  private static final String SIZE_640_480 = "w:640"; // ~640*480 in a normal ratio

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

  private static final String TEXT_SIZE_55 = "55";
  private static final String TEXT_SIZE_60 = "60";
  private static final String TEXT_SIZE_65 = "65";
  private static final String TEXT_SIZE_70 = "70";
  private static final String TEXT_SIZE_75 = "75";
  private static final String TEXT_SIZE_80 = "80";

  private static final String KEY_IMAGE_URI = "com.menupan.translate.apps.KEY_IMAGE_URI";
  private static final String KEY_IMAGE_MAX_WIDTH = "com.menupan.translate.apps.KEY_IMAGE_MAX_WIDTH";
  private static final String KEY_IMAGE_MAX_HEIGHT = "com.menupan.translate.apps.KEY_IMAGE_MAX_HEIGHT";
  private static final String KEY_SELECTED_SIZE = "com.menupan.translate.apps.KEY_SELECTED_SIZE";
  private static final String KEY_SELECTED_TEXT_COLOR = "com.menupan.translate.apps.KEY_SELECTED_TEXT_COLOR";
  private static final String KEY_SELECTED_TEXT_SIZE = "com.menupan.translate.apps.KEY_SELECTED_TEXT_SIZE";

  private static final int REQUEST_IMAGE_CAPTURE = 1001;
  private static final int REQUEST_CHOOSE_IMAGE = 1002;

  private ImageView preview;
  private GraphicOverlay graphicOverlay;
  private String selectedMode = CLOUD_TEXT_DETECTION;
  private String selectedSize = SIZE_PREVIEW;
  private String selectedTextColor = TEXT_WHITE;
  private String selectedTextSize = TEXT_SIZE_55;

  private static final int PERMISSION_REQUESTS = 1;

  boolean isLandScape;

  private Uri imageUri;
  // Max width (portrait mode)
  private Integer imageMaxWidth;
  // Max height (portrait mode)
  private Integer imageMaxHeight;
  private VisionImageProcessor imageProcessor;

  private AdView mAdView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_still_image);

    if (!allPermissionsGranted()) {
      getRuntimePermissions();
    }

    FirebaseTranslatorOptions options1 = new FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(FirebaseTranslateLanguage.ZH)
            .setTargetLanguage(FirebaseTranslateLanguage.EN)
            .build();
    final FirebaseTranslator taiwanEnglishTranslator =
            FirebaseNaturalLanguage.getInstance().getTranslator(options1);

    FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
            //        .requireWifi()
            .build();
    taiwanEnglishTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener(
                    new OnSuccessListener<Void>() {
                      @Override
                      public void onSuccess(Void v) {
                        // Model downloaded successfully. Okay to start translating.
                        // (Set a flag, unhide the translation UI, etc.)
                      }
                    })
            .addOnFailureListener(
                    new OnFailureListener() {
                      @Override
                      public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "OnFailureListener  OnFailureListener  OnFailureListener  OnFailureListener");
                        // Model couldn’t be downloaded or other internal error.
                        new AlertDialog.Builder(StillImageActivity.this) // TestActivity 부분에는 현재 Activity의 이름 입력.
                                .setMessage("중국어 메뉴판 번역 앱을 사용하시려면 최초 설치 시 언어팩을 내려받아야 사용이 가능합니다." +
                                        " 앱을 재실행하시면 언어팩이 자동 내려받기 됩니다.")     // 제목 부분 (직접 작성)
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {      // 버튼1 (직접 작성)
                                  public void onClick(DialogInterface dialog, int which){
                                    Toast.makeText(getApplicationContext(), "확인 누름", Toast.LENGTH_SHORT).show(); // 실행할 코드
                                  }
                                })
//                                      .setNegativeButton("취소", new DialogInterface.OnClickListener() {     // 버튼2 (직접 작성)
//                                          public void onClick(DialogInterface dialog, int which){
//                                              Toast.makeText(getApplicationContext(), "취소 누름", Toast.LENGTH_SHORT).show(); // 실행할 코드
//                                          }
//                                      })
                                .show();
                      }
                    });

    FirebaseTranslatorOptions options2 = new FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(FirebaseTranslateLanguage.EN)
            .setTargetLanguage(FirebaseTranslateLanguage.KO)
            .build();
    final FirebaseTranslator englishKoreaTranslator =
            FirebaseNaturalLanguage.getInstance().getTranslator(options2);
    FirebaseModelDownloadConditions conditions2 = new FirebaseModelDownloadConditions.Builder()
            //        .requireWifi()
            .build();
    englishKoreaTranslator.downloadModelIfNeeded(conditions2)
            .addOnSuccessListener(
                    new OnSuccessListener<Void>() {
                      @Override
                      public void onSuccess(Void v) {
                        // Model downloaded successfully. Okay to start translating.
                        // (Set a flag, unhide the translation UI, etc.)
                      }
                    })
            .addOnFailureListener(
                    new OnFailureListener() {
                      @Override
                      public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "OnFailureListener  OnFailureListener  OnFailureListener  OnFailureListener");
                        // Model couldn’t be downloaded or other internal error.
                        new AlertDialog.Builder(StillImageActivity.this) // TestActivity 부분에는 현재 Activity의 이름 입력.
                                .setMessage("중국어 메뉴판 번역 앱을 사용하시려면 최초 설치 시 언어팩을 내려받아야 사용이 가능합니다." +
                                        " 재실행하시면 언어팩이 자동 내려받기 됩니다.")    // 제목 부분 (직접 작성)
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {      // 버튼1 (직접 작성)
                                  public void onClick(DialogInterface dialog, int which){
                                    Toast.makeText(getApplicationContext(), "확인 누름", Toast.LENGTH_SHORT).show(); // 실행할 코드
                                  }
                                })
//                                      .setNegativeButton("취소", new DialogInterface.OnClickListener() {     // 버튼2 (직접 작성)
//                                          public void onClick(DialogInterface dialog, int which){
//                                              Toast.makeText(getApplicationContext(), "취소 누름", Toast.LENGTH_SHORT).show(); // 실행할 코드
//                                          }
//                                      })
                                .show();
                      }
                    });

    Button getImageButton = findViewById(R.id.getImageButton);
    getImageButton.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View view) {
            // Menu for selecting either: a) take new photo b) select from existing
            PopupMenu popup = new PopupMenu(StillImageActivity.this, view);
            popup.setOnMenuItemClickListener(
                new OnMenuItemClickListener() {
                  @Override
                  public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                      case R.id.select_images_from_local:
                        startChooseImageIntentForResult();
                        return true;
                      case R.id.take_photo_using_camera:
                        startCameraIntentForResult();
                        return true;
                      default:
                        return false;
                    }
                  }
                });

            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.camera_button_menu, popup.getMenu());
            popup.show();
          }
        });
    preview = findViewById(R.id.previewPane);
    if (preview == null) {
      Log.d(TAG, "Preview is null");
    }
    graphicOverlay = findViewById(R.id.previewOverlay);
    if (graphicOverlay == null) {
      Log.d(TAG, "graphicOverlay is null");
    }

    populateFeatureSelector();
    // populateSizeSelector();
    populateTextColorSelector();
    populateTextSizeSelector();
    createImageProcessor(selectedTextColor, selectedTextSize);

    isLandScape =
        (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

    if (savedInstanceState != null) {
      imageUri = savedInstanceState.getParcelable(KEY_IMAGE_URI);
      imageMaxWidth = savedInstanceState.getInt(KEY_IMAGE_MAX_WIDTH);
      imageMaxHeight = savedInstanceState.getInt(KEY_IMAGE_MAX_HEIGHT);
      selectedSize = savedInstanceState.getString(KEY_SELECTED_SIZE);
      selectedTextColor = savedInstanceState.getString(KEY_SELECTED_TEXT_COLOR);
      selectedTextSize = savedInstanceState.getString(KEY_SELECTED_TEXT_SIZE);
      if (imageUri != null) {
        tryReloadAndDetectInImage();
      }
    }

    MobileAds.initialize(this, new OnInitializationCompleteListener() {
      @Override
      public void onInitializationComplete(InitializationStatus initializationStatus) {
      }
    });

    mAdView = findViewById(R.id.adView);
    AdRequest adRequest = new AdRequest.Builder().build();
    mAdView.loadAd(adRequest);

  }

  @Override
  public void onResume() {
    super.onResume();
    Log.d(TAG, "onResume");
    createImageProcessor(selectedTextColor, selectedTextSize);
    tryReloadAndDetectInImage();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.still_image_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.settings) {
      Intent intent = new Intent(this, SettingsActivity.class);
      intent.putExtra(SettingsActivity.EXTRA_LAUNCH_SOURCE, SettingsActivity.LaunchSource.STILL_IMAGE);
      startActivity(intent);
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private void populateFeatureSelector() {
    Spinner featureSpinner = findViewById(R.id.featureSelector);
    List<String> options = new ArrayList<>();
//    options.add(CLOUD_LABEL_DETECTION);
//    options.add(CLOUD_LANDMARK_DETECTION);
    options.add(CLOUD_TEXT_DETECTION);
    options.add(CLOUD_DOCUMENT_TEXT_DETECTION);
    // Creating adapter for featureSpinner
    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_style, options);
    // Drop down layout style - list view with radio button
    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // attaching data adapter to spinner
    featureSpinner.setAdapter(dataAdapter);
    featureSpinner.setOnItemSelectedListener(
        new OnItemSelectedListener() {

          @Override
          public void onItemSelected(
                  AdapterView<?> parentView, View selectedItemView, int pos, long id) {
            selectedMode = parentView.getItemAtPosition(pos).toString();
            createImageProcessor(selectedTextColor, selectedTextSize);
            tryReloadAndDetectInImage();
          }

          @Override
          public void onNothingSelected(AdapterView<?> arg0) {}
        });
  }

  private void populateTextColorSelector() {
    Spinner featureSpinner = findViewById(R.id.textColorSelector);
    List<String> options = new ArrayList<>();
    options.add(TEXT_WHITE);
    options.add(TEXT_BLACK);
    options.add(TEXT_RED);
    options.add(TEXT_BLUE);
    options.add(TEXT_DKGRAY);
    options.add(TEXT_MAGENTA);
    options.add(TEXT_GREEN);
    options.add(TEXT_LTGRAY);
    options.add(TEXT_CYAN);
    options.add(TEXT_YELLOW);

    // Creating adapter for featureSpinner
    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_style, options);
    // Drop down layout style - list view with radio button
    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // attaching data adapter to spinner
    featureSpinner.setAdapter(dataAdapter);
    featureSpinner.setOnItemSelectedListener(
            new OnItemSelectedListener() {

              @Override
              public void onItemSelected(
                      AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                selectedTextColor = parentView.getItemAtPosition(pos).toString();
                createImageProcessor(selectedTextColor,selectedTextSize);
                tryReloadAndDetectInImage();
              }

              @Override
              public void onNothingSelected(AdapterView<?> arg0) {}
            });
  }

  private void populateTextSizeSelector() {
    Spinner featureSpinner = findViewById(R.id.textSizeSelector);
    List<String> options = new ArrayList<>();
    options.add(TEXT_SIZE_55);
    options.add(TEXT_SIZE_60);
    options.add(TEXT_SIZE_65);
    options.add(TEXT_SIZE_70);
    options.add(TEXT_SIZE_75);
    options.add(TEXT_SIZE_80);

    // Creating adapter for featureSpinner
    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_style, options);
    // Drop down layout style - list view with radio button
    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // attaching data adapter to spinner
    featureSpinner.setAdapter(dataAdapter);
    featureSpinner.setOnItemSelectedListener(
            new OnItemSelectedListener() {

              @Override
              public void onItemSelected(
                      AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                selectedTextSize = parentView.getItemAtPosition(pos).toString();
                createImageProcessor(selectedTextColor,selectedTextSize);
                tryReloadAndDetectInImage();
              }

              @Override
              public void onNothingSelected(AdapterView<?> arg0) {}
            });
  }

//  private void populateSizeSelector() {
//    Spinner sizeSpinner = findViewById(R.id.sizeSelector);
//    List<String> options = new ArrayList<>();
//    options.add(SIZE_PREVIEW);
////    options.add(SIZE_1024_768);
////    options.add(SIZE_640_480);
//
//    // Creating adapter for featureSpinner
//    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_style, options);
//    // Drop down layout style - list view with radio button
//    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//    // attaching data adapter to spinner
//    sizeSpinner.setAdapter(dataAdapter);
//    sizeSpinner.setOnItemSelectedListener(
//        new OnItemSelectedListener() {
//
//          @Override
//          public void onItemSelected(
//                  AdapterView<?> parentView, View selectedItemView, int pos, long id) {
//            selectedSize = parentView.getItemAtPosition(pos).toString();
//            tryReloadAndDetectInImage();
//          }
//
//          @Override
//          public void onNothingSelected(AdapterView<?> arg0) {}
//        });
//  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putParcelable(KEY_IMAGE_URI, imageUri);

    if (imageMaxHeight != null) {
      outState.putInt(KEY_IMAGE_MAX_HEIGHT, imageMaxHeight);
    }
    if (imageMaxWidth != null) {
      outState.putInt(KEY_IMAGE_MAX_WIDTH, imageMaxWidth);
    }
    outState.putString(KEY_SELECTED_SIZE, selectedSize);
  }

  private void startCameraIntentForResult() {
    // Clean up last time's image
    imageUri = null;
    preview.setImageBitmap(null);

    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
      ContentValues values = new ContentValues();
      values.put(MediaStore.Images.Media.TITLE, "New Picture");
      values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
      imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
      takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
      startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }
  }

  private void startChooseImageIntentForResult() {
    Intent intent = new Intent();
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    startActivityForResult(Intent.createChooser(intent, "메뉴판 사진을 선택하세요"), REQUEST_CHOOSE_IMAGE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
      tryReloadAndDetectInImage();
    } else if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == RESULT_OK) {
      // In this case, imageUri is returned by the chooser, save it.
      imageUri = data.getData();
      tryReloadAndDetectInImage();
    }
  }

  private void tryReloadAndDetectInImage() {
    try {
      if (imageUri == null) {
        return;
      }

      // Clear the overlay first
      graphicOverlay.clear();

      Bitmap imageBitmap;
      if (Build.VERSION.SDK_INT < 29) {
        imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
      } else {
        ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageUri);
        imageBitmap = ImageDecoder.decodeBitmap(source);
      }

      // Get the dimensions of the View
      Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();

      int targetWidth = targetedSize.first;
      int maxHeight = targetedSize.second;

      // Determine how much to scale down the image
      float scaleFactor =
              Math.max(
                      (float) imageBitmap.getWidth() / (float) targetWidth,
                      (float) imageBitmap.getHeight() / (float) maxHeight);
      Log.e(TAG, "Error retrieving saved image scaleFactor :: "+scaleFactor);

//      if (imageBitmap.getWidth() > imageBitmap.getHeight()) {
//        imageBitmap = Bitmap.createBitmap(imageBitmap, imageBitmap.getWidth() / 2 - imageBitmap.getHeight() / 2, 0, imageBitmap.getHeight(), imageBitmap.getHeight()); // h < w 일 때, h기준으로 1:1 중앙 크롭
//      } else {
//        if (isLandScape) {
//          imageBitmap = Bitmap.createBitmap(imageBitmap, 0, imageBitmap.getHeight() / 2 - imageBitmap.getWidth() / 2, imageBitmap.getWidth(), imageBitmap.getWidth()); // h > w 일 때, w기준으로 1:1 중앙 크롭
//        } else {
//          imageBitmap = Bitmap.createBitmap(imageBitmap, imageBitmap.getHeight() / 2 - imageBitmap.getWidth() / 2, 0, imageBitmap.getWidth(), imageBitmap.getWidth()); // h > w 일 때, w기준으로 1:1 중앙 크롭
//        }
//      }

      Bitmap resizedBitmap = Bitmap.createScaledBitmap(
              imageBitmap,
              (int) (imageBitmap.getWidth() / scaleFactor),
              (int) (imageBitmap.getHeight() / scaleFactor),
              true);

      if(!isLandScape && imageBitmap.getWidth() > imageBitmap.getHeight()) {
        int rotate = 90;
        // resizedBitmap = rotateImage(resizedBitmap, 90);
        Matrix matrix = new Matrix();

        matrix.postRotate(rotate);

        imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight() , matrix, true);
      }

      resizedBitmap = Bitmap.createScaledBitmap(
              imageBitmap,
              targetWidth,
              maxHeight,
              true);

      preview.setImageBitmap(resizedBitmap);

      imageProcessor.process(resizedBitmap, graphicOverlay);
    } catch (IOException e) {
      Log.e(TAG, "Error retrieving saved image");
    }
  }

  // Returns max image width, always for portrait mode. Caller needs to swap width / height for
  // landscape mode.
  private Integer getImageMaxWidth() {
    if (imageMaxWidth == null) {
      // Calculate the max width in portrait mode. This is done lazily since we need to wait for
      // a UI layout pass to get the right values. So delay it to first time image rendering time.
      if (isLandScape) {
        imageMaxWidth =
            ((View) preview.getParent()).getHeight() - findViewById(R.id.controlPanel).getHeight();
      } else {
        imageMaxWidth = ((View) preview.getParent()).getWidth();
      }
    }

    return imageMaxWidth;
  }

  // Returns max image height, always for portrait mode. Caller needs to swap width / height for
  // landscape mode.
  private Integer getImageMaxHeight() {
    if (imageMaxHeight == null) {
      // Calculate the max width in portrait mode. This is done lazily since we need to wait for
      // a UI layout pass to get the right values. So delay it to first time image rendering time.
      if (isLandScape) {
        imageMaxHeight = ((View) preview.getParent()).getWidth();
      } else {
        imageMaxHeight =
            ((View) preview.getParent()).getHeight() - findViewById(R.id.controlPanel).getHeight();
      }
    }

    return imageMaxHeight;
  }

  // Gets the targeted width / height.
  private Pair<Integer, Integer> getTargetedWidthHeight() {
    int targetWidth;
    int targetHeight;

    switch (selectedSize) {
      case SIZE_PREVIEW:
        int maxWidthForPortraitMode = getImageMaxWidth();
        int maxHeightForPortraitMode = getImageMaxHeight();
        targetWidth = isLandScape ? maxHeightForPortraitMode : maxWidthForPortraitMode;
        targetHeight = isLandScape ? maxWidthForPortraitMode : maxHeightForPortraitMode;
        break;
      case SIZE_640_480:
        targetWidth = isLandScape ? 640 : 480;
        targetHeight = isLandScape ? 480 : 640;
        break;
      case SIZE_1024_768:
        targetWidth = isLandScape ? 1024 : 768;
        targetHeight = isLandScape ? 768 : 1024;
        break;
      default:
        throw new IllegalStateException("Unknown size");
    }

    return new Pair<>(targetWidth, targetHeight);
  }

  private void createImageProcessor(String text_color, String text_size) {
    switch (selectedMode) {
//      case CLOUD_LABEL_DETECTION:
//        imageProcessor = new CloudImageLabelingProcessor();
//        break;
//      case CLOUD_LANDMARK_DETECTION:
//        imageProcessor = new CloudLandmarkRecognitionProcessor();
//        break;
      case CLOUD_TEXT_DETECTION:
        imageProcessor = new CloudTextRecognitionProcessor(text_color, text_size);
        break;
      case CLOUD_DOCUMENT_TEXT_DETECTION:
        imageProcessor = new CloudTextEnRecognitionProcessor(text_color, text_size);
        break;
      default:
        throw new IllegalStateException("Unknown selectedMode: " + selectedMode);
    }
  }
  private String[] getRequiredPermissions() {
    try {
      PackageInfo info =
              this.getPackageManager()
                      .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
      String[] ps = info.requestedPermissions;
      if (ps != null && ps.length > 0) {
        return ps;
      } else {
        return new String[0];
      }
    } catch (Exception e) {
      return new String[0];
    }
  }

  private boolean allPermissionsGranted() {
    for (String permission : getRequiredPermissions()) {
      if (!isPermissionGranted(this, permission)) {
        return false;
      }
    }
    return true;
  }

  private void getRuntimePermissions() {
    List<String> allNeededPermissions = new ArrayList<>();
    for (String permission : getRequiredPermissions()) {
      if (!isPermissionGranted(this, permission)) {
        allNeededPermissions.add(permission);
      }
    }

    if (!allNeededPermissions.isEmpty()) {
      ActivityCompat.requestPermissions(
              this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
    }
  }

  private static boolean isPermissionGranted(Context context, String permission) {
    if (ContextCompat.checkSelfPermission(context, permission)
            == PackageManager.PERMISSION_GRANTED) {
      Log.i(TAG, "Permission granted: " + permission);
      return true;
    }
    Log.i(TAG, "Permission NOT granted: " + permission);
    return false;
  }
}
