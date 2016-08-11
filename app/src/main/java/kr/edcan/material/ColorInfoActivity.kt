package kr.edcan.material

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_color_info.*
import kr.edcan.material.model.ColorData
import kr.edcan.material.util.ColorUtil

/**
 * Created by LNTCS on 2016-04-15.
 */
class ColorInfoActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_color_info)

        var data = ColorData()
        data.id = intent.getIntExtra("id", 0)
        data.colorSetId = intent.getIntExtra("colorSetId",0)
        data.name = intent.getStringExtra("name")
        data.color = intent.getStringExtra("color").toUpperCase()
        data.memo = intent.getStringExtra("memo")
        data.colorRes = Color.parseColor(intent.getStringExtra("color"))

        layPreview.setBackgroundColor(data.colorRes)
        edtColorName.setText(data.name)
        edtColorName.setSelection(data.name.length)
        edtHexColor.setText(data.color.substring(1))
        tvRGBColor.text = ColorUtil.hexToRGB(data.color.substring(1))
        edtMemo.setText(data.memo)
        colorSet(data.color)
        navBack.setOnClickListener { onBackPressed() }
        navDel.setOnClickListener {
            val realm = Realm.getInstance(this)
            val cData = realm.where(ColorData::class.java).equalTo("id", data.id).findFirst()
            realm.beginTransaction()
            cData.removeFromRealm()
            realm.commitTransaction()
            finish()
        }

        edtHexColor.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().length == 6) {
                    layPreview.setBackgroundColor(Color.parseColor("#" + s.toString()))
                    tvRGBColor.text = ColorUtil.hexToRGB(s.toString())
                    colorSet("#" + s.toString())
                } else if (s.toString().length == 3) {
                    val s1 = s.toString()
                    val color = "#" + s1.substring(0, 1) + s1.substring(0, 1) + s1.substring(1, 2) + s1.substring(1, 2) + s1.substring(2, 3) + s1.substring(2, 3)
                    layPreview.setBackgroundColor(Color.parseColor(color))
                    tvRGBColor.text = ColorUtil.hexToRGB(color.substring(1))
                    colorSet(color)
                }
            }
        })

        tvRGBColor.setOnClickListener {
            copy(tvRGBColor.text.toString())
        }
        rgbCopy.setOnClickListener {
            copy(tvRGBColor.text.toString())
        }
        hexCopy.setOnClickListener {
            copy("#"+edtHexColor.text.toString())
        }

        saveFab.setOnClickListener {
            if (edtColorName.text.toString().trim { it <= ' ' }.length < 1) {
                Toast.makeText(this, "이름은 공백일 수 없습니다.", Toast.LENGTH_SHORT).show()
            } else if (edtHexColor.text.toString().trim { it <= ' ' }.length == 6 || edtHexColor.text.toString().trim { it <= ' ' }.length == 3) {
                val color: String
                if (edtHexColor.text.toString().length == 3) {
                    val s1 = edtHexColor.text.toString()
                    color = "#" + s1.substring(0, 1) + s1.substring(0, 1) + s1.substring(1, 2) + s1.substring(1, 2) + s1.substring(2, 3) + s1.substring(2, 3)
                } else {
                    color = "#" + edtHexColor.text.toString()
                }
                val realm = Realm.getInstance(this)
                val cData = realm.where(ColorData::class.java).equalTo("id", data.id).findFirst()
                realm.beginTransaction()
                cData.name = edtColorName.text.toString()
                cData.color = color.toUpperCase()
                cData.colorRes = Color.parseColor(color)
                cData.memo = edtMemo.text.toString()
                realm.copyToRealmOrUpdate(cData)
                realm.commitTransaction()
                Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "올바르지 않은 색상값입니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun copy(text: String) {
        var clipboard : ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        var clip : ClipData =ClipData.newPlainText("Copied Text", text)
        clipboard.primaryClip = clip
        Toast.makeText(this, "클립보드에 복사되었습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun colorSet(color: String?) {
        if(ColorUtil.isLight(color?.substring(1)!!) == 0){
            edtColorName.setTextColor(Color.WHITE)
            navBack.setImageResource(R.drawable.ic_back)
            navDel.setImageResource(R.drawable.ic_delete)
        } else {
            edtColorName.setTextColor(Color.parseColor("#242424"))
            navBack.setImageResource(R.drawable.ic_back_black)
            navDel.setImageResource(R.drawable.ic_delete_black)
        }
    }
}