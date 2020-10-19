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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.menupan.translate.apps.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Demo app chooser which takes care of runtime permission requesting and allows you to pick from
 * all available testing Activities.
 */
public final class ChooserActivity extends AppCompatActivity
    implements OnRequestPermissionsResultCallback, AdapterView.OnItemClickListener {
  private static final String TAG = "ChooserActivity";
  private static final int PERMISSION_REQUESTS = 1;

  private static final Class<?>[] CLASSES =
      new Class<?>[] {
              StillImageActivity.class,
       //       LivePreviewActivity.class,

      };

  private static final int[] DESCRIPTION_IDS =
      new int[] {
                R.string.desc_still_image_activity,
       //         R.string.desc_camera_source_activity,
      };

  private static final int[] DESCRIPTION_IDS_SUB =
          new int[] {
                  R.string.desc_still_image_activity_sub,
                    //         R.string.desc_camera_source_activity,
          };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");

    setContentView(R.layout.activity_chooser);

    // Set up ListView and Adapter
    ListView listView = findViewById(R.id.testActivityListView);

    MyArrayAdapter adapter = new MyArrayAdapter(this, android.R.layout.simple_list_item_2, CLASSES);
    adapter.setDescriptionIds(DESCRIPTION_IDS);
      adapter.setDescriptionIdsSub(DESCRIPTION_IDS_SUB);

    listView.setAdapter(adapter);
    listView.setOnItemClickListener(this);

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
                              new AlertDialog.Builder(ChooserActivity.this) // TestActivity 부분에는 현재 Activity의 이름 입력.
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
                              new AlertDialog.Builder(ChooserActivity.this) // TestActivity 부분에는 현재 Activity의 이름 입력.
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

    if (!allPermissionsGranted()) {
      getRuntimePermissions();
    }
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Class<?> clicked = CLASSES[position];
    startActivity(new Intent(this, clicked));
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

  private static class MyArrayAdapter extends ArrayAdapter<Class<?>> {

    private final Context context;
    private final Class<?>[] classes;
    private int[] descriptionIds;
    private int[] descriptionIdsSub;

    public MyArrayAdapter(Context context, int resource, Class<?>[] objects) {
      super(context, resource, objects);

      this.context = context;
      classes = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view = convertView;

      if (convertView == null) {
        LayoutInflater inflater =
            (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.simple_list_item_1, null);
      }

      // ((TextView) view.findViewById(android.R.id.text1)).setText("중국어 메뉴판 \n사진 번역 시작");
      // ((TextView) view.findViewById(android.R.id.text1)).setText(descriptionIds[position]);
        ((TextView) view.findViewById(android.R.id.text1)).setText(descriptionIds[position]);
        ((TextView) view.findViewById(android.R.id.text2)).setText(descriptionIdsSub[position]);

      return view;
    }

    public void setDescriptionIds(int[] descriptionIds) {
      this.descriptionIds = descriptionIds;
    }

    public void setDescriptionIdsSub(int[] descriptionIdsSub) {
        this.descriptionIdsSub = descriptionIdsSub;
    }
  }
}
