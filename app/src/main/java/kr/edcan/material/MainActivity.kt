package kr.edcan.material

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import co.mobiwise.materialintro.shape.Focus
import co.mobiwise.materialintro.shape.FocusGravity
import co.mobiwise.materialintro.view.MaterialIntroView
import com.orhanobut.logger.Logger
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import kr.edcan.material.model.ColorData
import kr.edcan.material.model.ColorSet
import kr.edcan.material.util.ColorUtil
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.nio.charset.Charset
import java.util.*

class MainActivity : AppCompatActivity() {

    internal var gridAdapter: GridAdapter? = null
    internal var mdTitles = arrayOf("Red", "Pink", "Purple", "Deep_Purple", "Indigo", "Blue", "Light_Blue", "Cyan", "Teal", "Green", "Light_Green", "Lime", "Yellow", "Amber", "Orange", "Deep_Orange", "Brown", "Grey", "Blue_grey")
    internal var cList = ArrayList<ColorSet>()
    internal var mContext: Context? = null
    internal var pref: SharedPreferences? = null
    internal var editor: SharedPreferences.Editor? = null

    internal var dialog: Dialog? = null
    internal var mwidth: Int = 0
    internal var mheight:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mContext = this
        Logger.init("ASDF")

        pref = getSharedPreferences("Material", Context.MODE_PRIVATE)
        editor = pref?.edit()
        if (pref!!.getBoolean("isFirst", true)) {
            setDefaultMaterialColors()
        }
        val display = windowManager.defaultDisplay
        mwidth = display.width
        mheight = display.height

        loadColorSets()
        gridAdapter = GridAdapter(mContext as MainActivity)
        mainGrid.adapter = gridAdapter
        fab.setOnClickListener { fab(fab) }

        MaterialIntroView.Builder(this)
                .enableDotAnimation(true)
                .enableIcon(false)
                .setFocusGravity(FocusGravity.CENTER)
                .setFocusType(Focus.MINIMUM)
                .setDelayMillis(500)
                .enableFadeAnimation(true)
                .performClick(false)
                .setInfoText("+ 버튼을 눌러 나만의 팔레트를 만들어보세요!")
                .setTarget(fab)
                .setTextColor(Color.WHITE)
                .setInfoTextSize(15)
                .setUsageId("add_ColorSet")
                .setListener {
                    MaterialIntroView.Builder(this@MainActivity).
                            enableDotAnimation(true)
                            .enableIcon(false)
                            .setFocusGravity(FocusGravity.CENTER)
                            .setFocusType(Focus.MINIMUM)
                            .setDelayMillis(500)
                            .enableFadeAnimation(true)
                            .performClick(false)
                            .setInfoText("각각의 팔레트를 길게 눌러 수정/삭제 할 수 있습니다.")
                            .setTarget(mainGridFocus)
                            .setTextColor(Color.WHITE)
                            .setInfoTextSize(15)
                            .setUsageId("edit_ColorSet")
                            .show() }
                .show()
    }

    private fun loadColorSets() {
        val realm = Realm.getInstance(this)
        for (set in realm.where(ColorSet::class.java).findAll()) {
            cList.add(set)
        }
    }
    private fun setDefaultMaterialColors() {
        Logger.e("setDefaultMaterialColors")
        val mdColors = resources.getStringArray(R.array.md_colors)
        val jsonArray = loadJSONFromAsset()
        val realm = Realm.getInstance(this)
        realm.beginTransaction()
        for (i in 0..mdTitles.size - 1) {
            val set = ColorSet()
            set.id = i
            set.name = mdTitles[i]
            set.color = mdColors[i].toUpperCase()
            set.colorRes = Color.parseColor(mdColors[i])
            realm.copyToRealm(set)
        }
        if(jsonArray == null) return
        for (i in 0..jsonArray.length().minus(1)) {
            try {
                val tmp = jsonArray.getJSONObject(i)
                val color = ColorData()
                color.id = tmp!!.getInt("id")
                color.colorSetId = tmp.getInt("colorSetId")
                color.color = tmp.getString("color").toUpperCase()
                color.colorRes = Color.parseColor(tmp.getString("color"))
                color.name = tmp.getString("name")
                realm.copyToRealm(color)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
        realm.commitTransaction()
        editor?.putBoolean("isFirst", false)
        editor?.commit()
    }

    fun loadJSONFromAsset(): JSONArray? {
        var json: JSONArray? = null
        try {
            val `is` = assets.open("mdColors.json")
            val size = `is`.available()
            val buffer : ByteArray = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            json = JSONArray(String(buffer, Charset.forName("UTF-8")))
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return json
    }

    internal fun fab(v: View) {
        val builder = AlertDialog.Builder(mContext)
        val view = LayoutInflater.from(mContext).inflate(R.layout.dialog_add_colorset, null, false)
        val bg = view.findViewById(R.id.dialog_bg) as RelativeLayout
        val name = view.findViewById(R.id.dialog_name) as EditText
        val code = view.findViewById(R.id.dialog_code) as EditText
        view.findViewById(R.id.dialog_create).setOnClickListener{
            if (name.text.toString().trim().length < 1) {
                Toast.makeText(mContext, "이름은 공백일 수 없습니다.", Toast.LENGTH_SHORT).show()
            } else if (code.text.toString().trim().length=== 6 || code.text.toString().trim().length === 3) {
                val color: String
                if (code.text.toString().length === 3) {
                    val s1 = code.text.toString()
                    color = "#" + s1.substring(0, 1) + s1.substring(0, 1) + s1.substring(1, 2) + s1.substring(1, 2) + s1.substring(2, 3) + s1.substring(2, 3)
                } else {
                    color = "#" + code.text.toString()
                }
                val realm = Realm.getInstance(mContext)
                realm.beginTransaction()
                val colorSet = ColorSet()
                colorSet.id = realm.where(ColorSet::class.java).findAll().size
                colorSet.name = name.text.toString()
                colorSet.color = color.toUpperCase()
                colorSet.colorRes = Color.parseColor(color)
                realm.copyToRealm(colorSet)
                cList.add(colorSet)
                realm.commitTransaction()
                gridAdapter?.notifyDataSetChanged()
                dialog?.dismiss()
            } else {
                Toast.makeText(mContext, "올바르지 않은 색상값입니다.", Toast.LENGTH_SHORT).show()
            }
        }
        view.findViewById(R.id.dialog_cancel).setOnClickListener{
            dialog?.dismiss()
        }

        name.setSelection(name.text.length)
        code.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                if (s.toString().length === 6) {
                    bg.setBackgroundColor(Color.parseColor("#" + s.toString()))
                    if (ColorUtil.isLight(s.toString()) === 1) {
                        name.setTextColor(ContextCompat.getColor(mContext, R.color.text_grey))
                        code.setTextColor(ContextCompat.getColor(mContext, R.color.text_grey))
                    } else if (ColorUtil.isLight(s.toString()) === 0) {
                        name.setTextColor(Color.WHITE)
                        code.setTextColor(Color.WHITE)
                    }
                } else if (s.toString().length === 3) {
                    val s1 = s.toString()
                    val color = "#" + s1.substring(0, 1) + s1.substring(0, 1) + s1.substring(1, 2) + s1.substring(1, 2) + s1.substring(2, 3) + s1.substring(2, 3)
                    bg.setBackgroundColor(Color.parseColor(color))
                    if (ColorUtil.isLight(color.substring(1)) === 1) {
                        name.setTextColor(ContextCompat.getColor(mContext, R.color.text_grey))
                        code.setTextColor(ContextCompat.getColor(mContext, R.color.text_grey))
                    } else if (ColorUtil.isLight(color.substring(1)) === 0) {
                        name.setTextColor(Color.WHITE)
                        code.setTextColor(Color.WHITE)
                    }
                }
            }
        })
        builder.setView(view)
        dialog = builder.create()
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog?.window?.attributes)
        lp.width = (mwidth / 1.2).toInt()
        val window = dialog?.window
        window?.attributes = lp
        dialog?.show()
    }
    internal inner class GridAdapter(var mContext: Context) : BaseAdapter() {
        var mInflater: LayoutInflater

        init {
            mInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }

        override fun getCount(): Int {
            return cList.size
        }

        override fun getItem(position: Int): Any? {
            return null
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.content_main_grid, parent, false)
            }
            val data = cList[position]
            val contGridTitle = convertView?.findViewById(R.id.contGridTitle) as TextView
            val contGridSubtitle = convertView?.findViewById(R.id.contGridSubtitle) as TextView
            val contGridBg = convertView?.findViewById(R.id.contGridBg) as CardView
            val contGridLay = convertView?.findViewById(R.id.contGridLay) as RelativeLayout
            contGridTitle.text = data.name
            contGridSubtitle.text = data.color
            contGridBg.setCardBackgroundColor(data.colorRes)
            contGridLay.setOnClickListener {
                val i = Intent(mContext, DetailActivity::class.java)
                i.putExtra("title", data.name)
                i.putExtra("id", data.id)
                i.putExtra("color", data.color)
                startActivity(i)
            }
            contGridLay.setOnLongClickListener{
                val builder = AlertDialog.Builder(mContext)
                val view = LayoutInflater.from(mContext).inflate(R.layout.dialog_edit_color, null, false)
                val dialogSave = view.findViewById(R.id.dialogSave) as TextView
                val dialogBg = view.findViewById(R.id.dialogBg) as RelativeLayout
                val dialogName = view.findViewById(R.id.dialogName) as EditText
                val dialogCode = view.findViewById(R.id.dialogCode) as EditText
                val dialogDelete = view.findViewById(R.id.dialogDelete) as TextView
                val dialogCancel = view.findViewById(R.id.dialogCancel) as TextView

                dialogSave.setOnClickListener {
                    if (dialogName.text.toString().trim { it <= ' ' }.length < 1) {
                        Toast.makeText(mContext, "이름은 공백일 수 없습니다.", Toast.LENGTH_SHORT).show()
                    } else if (dialogCode.text.toString().trim { it <= ' ' }.length == 6 || dialogCode.text.toString().trim { it <= ' ' }.length == 3) {
                        val color: String
                        if (dialogCode.text.toString().length == 3) {
                            val s1 = dialogCode.text.toString()
                            color = "#" + s1.substring(0, 1) + s1.substring(0, 1) + s1.substring(1, 2) + s1.substring(1, 2) + s1.substring(2, 3) + s1.substring(2, 3)
                        } else {
                            color = "#" + dialogCode.text.toString()
                        }
                        val realm = Realm.getInstance(mContext)
                        val cData = realm.where(ColorSet::class.java).equalTo("id", data.id).findFirst()
                        realm.beginTransaction()
                        cData.name = dialogName.text.toString()
                        cData.color = color
                        cData.colorRes = Color.parseColor(color)
                        realm.copyToRealmOrUpdate(cData)
                        realm.commitTransaction()
                        gridAdapter?.notifyDataSetChanged()
                        dialog?.dismiss()
                    } else {
                        Toast.makeText(mContext, "올바르지 않은 색상값입니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                dialogDelete.setOnClickListener {
                    val realm = Realm.getInstance(mContext)
                    cList.remove(data)
                    realm.beginTransaction()
                    realm.where(ColorSet::class.java).equalTo("id", data.id).findFirst().removeFromRealm()
                    realm.commitTransaction()
                    gridAdapter?.notifyDataSetChanged()
                    dialog?.dismiss()
                }
                dialogCancel.setOnClickListener { dialog?.dismiss() }
                dialogBg.setBackgroundColor(data.colorRes)
                dialogName.setText(data.name)
                dialogName.setSelection(dialogName.text.length)
                dialogCode.setText(data.color.substring(1))
                dialogCode.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                    }

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    }

                    override fun afterTextChanged(s: Editable) {
                        if (s.toString().length === 6) {
                            dialogBg.setBackgroundColor(Color.parseColor("#" + s.toString()))
                            if (ColorUtil.isLight(s.toString()) === 1) {
                                dialogName.setTextColor(ContextCompat.getColor(mContext, R.color.text_grey))
                                dialogCode.setTextColor(ContextCompat.getColor(mContext, R.color.text_grey))
                            } else if (ColorUtil.isLight(s.toString()) === 0) {
                                dialogName.setTextColor(Color.WHITE)
                                dialogCode.setTextColor(Color.WHITE)
                            }
                        } else if (s.toString().length === 3) {
                            val s1 = s.toString()
                            val color = "#" + s1.substring(0, 1) + s1.substring(0, 1) + s1.substring(1, 2) + s1.substring(1, 2) + s1.substring(2, 3) + s1.substring(2, 3)
                            Logger.d("" + ColorUtil.isLight(color.substring(1)))
                            dialogBg.setBackgroundColor(Color.parseColor(color))
                            if (ColorUtil.isLight(color.substring(1)) === 1) {
                                dialogName.setTextColor(ContextCompat.getColor(mContext, R.color.text_grey))
                                dialogCode.setTextColor(ContextCompat.getColor(mContext, R.color.text_grey))
                            } else if (ColorUtil.isLight(color.substring(1)) === 0) {
                                dialogName.setTextColor(Color.WHITE)
                                dialogCode.setTextColor(Color.WHITE)
                            }
                        }
                    }
                })
                if (ColorUtil.isLight(data.color.substring(1)) === 1) {
                    dialogName.setTextColor(ContextCompat.getColor(mContext, R.color.text_grey))
                    dialogCode.setTextColor(ContextCompat.getColor(mContext, R.color.text_grey))
                }
                builder.setView(view)
                dialog = builder.create()
                val lp = WindowManager.LayoutParams()
                lp.copyFrom(dialog?.window?.attributes)
                lp.width = (mwidth / 1.2).toInt()
                val window = dialog?.window
                window?.attributes = lp
                dialog?.show()
                false
            }
            return convertView!!
        }
    }
}
