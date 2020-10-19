package com.menupan.translate.apps

import android.content.Intent
import com.menupan.example.internal.BaseEntryChoiceActivity
import com.menupan.example.internal.Choice
import com.menupan.translate.apps.java.*

class EntryChoiceActivity : BaseEntryChoiceActivity() {

    override fun getChoices(): List<Choice> {
        return listOf(
                Choice(
                        "실시간 메뉴판 번역",
                        "실시간으로 메뉴판을 번역합니다.",
                        Intent(this,
                                LivePreviewActivity::class.java)),
                Choice(
                        "저장된 이미지 메뉴판 번역",
                        "저장된 이미지나 쵤영된 이미지로 메뉴판을 번역합니다.",
                        Intent(this,
                                StillImageActivity::class.java))
        )
    }
}
