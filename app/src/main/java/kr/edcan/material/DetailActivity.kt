package kr.edcan.material

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import co.mobiwise.materialintro.shape.Focus
import co.mobiwise.materialintro.shape.FocusGravity
import co.mobiwise.materialintro.view.MaterialIntroView
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_color_detail.*
import kr.edcan.material.model.ColorData
import kr.edcan.material.util.ColorUtil
import java.util.*

/**
 * Created by LNTCS on 2016-04-14.
 */
class DetailActivity : AppCompatActivity() {

    internal var mContext: Context? = null
    internal var cList = ArrayList<ColorData>()
    internal var mwidth: Int = 0
    internal var mheight:Int = 0
    internal var colorListAdapter: ColorListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_color_detail)
        setSupportActionBar(toolbar)
        mContext = this
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        collapsingToolbarLayout.title = intent.getStringExtra("title")
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandingTitleStyle)
        if (ColorUtil.isLight(intent.getStringExtra("color").substring(1)) == 1) {
            collapsingToolbarLayout.setExpandedTitleColor(ContextCompat.getColor(mContext, R.color.text_grey))
        } else if (ColorUtil.isLight(intent.getStringExtra("color").substring(1)) == 0) {
            collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE)
        }
        toolbarBackground.setBackgroundColor(Color.parseColor(intent.getStringExtra("color")))
        initData()
        val display = windowManager.defaultDisplay

        mwidth = display.width
        mheight = display.height

        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL
        detailRecycler.layoutManager = llm
        colorListAdapter = ColorListAdapter(cList)
        detailRecycler.adapter = colorListAdapter
        MaterialIntroView.Builder(this)
                .enableDotAnimation(true)
                .enableIcon(false)
                .setFocusGravity(FocusGravity.CENTER)
                .setFocusType(Focus.MINIMUM)
                .setDelayMillis(500)
                .enableFadeAnimation(true)
                .performClick(false)
                .setInfoText("+ 버튼을 누르면 새로운 색을 추가할 수 있습니다.")
                .setTarget(detailFab).setTextColor(Color.WHITE)
                .setInfoTextSize(15).setUsageId("add_Color")
                .show()
        detailFab.setOnClickListener { addColor(detailFab) }
    }
    private fun initData() {
        val realm = Realm.getInstance(mContext)
        for (cData in realm.where(ColorData::class.java).equalTo("colorSetId", intent.getIntExtra("id", 0)).findAll()) {
            cList.add(cData)
        }
    }

    inner class ColorListAdapter(val list:ArrayList<ColorData>): RecyclerView.Adapter<ColorHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, i:Int):ColorHolder {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.content_detail_list, viewGroup, false)
            return ColorHolder(view)
        }
        internal var dialog: Dialog? = null
        override fun onBindViewHolder(viewHolder:ColorHolder, position:Int) {
            val data = list[position]
            viewHolder.title.text = data.name
            viewHolder.sub.text = data.color
            viewHolder.thumb.setCardBackgroundColor(data.colorRes)
            viewHolder.lay.setOnClickListener{
                var i = Intent(mContext, ColorInfoActivity::class.java)
                i.putExtra("name", data.name)
                i.putExtra("color", data.color)
                i.putExtra("id", data.id)
                i.putExtra("colorSetId", data.colorSetId)
                i.putExtra("memo", data.memo)
                startActivity(i)
            }
        }
        override fun getItemCount():Int {
            return list.size
        }
    }

    class ColorHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView
        var sub: TextView
        var thumb: CardView
        var lay: LinearLayout
        init {
            title = itemView.findViewById(R.id.content_detail_title) as TextView
            lay = itemView.findViewById(R.id.content_detail_lay) as LinearLayout
            sub = itemView.findViewById(R.id.content_detail_sub) as TextView
            thumb = itemView.findViewById(R.id.content_detail_thumb) as CardView
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    internal var dialog: Dialog? = null
    fun addColor(v: View) {
        val builder = AlertDialog.Builder(mContext)
        val view = LayoutInflater.from(mContext).inflate(R.layout.dialog_add_colorset, null, false)
        val bg = view.findViewById(R.id.dialog_bg) as RelativeLayout
        val name = view.findViewById(R.id.dialog_name) as EditText
        val code = view.findViewById(R.id.dialog_code) as EditText
        view.findViewById(R.id.dialog_create).setOnClickListener {
            if (name.text.toString().trim { it <= ' ' }.length < 1) {
                Toast.makeText(mContext, "이름은 공백일 수 없습니다.", Toast.LENGTH_SHORT).show()
            } else if (code.text.toString().trim { it <= ' ' }.length == 6 || code.text.toString().trim { it <= ' ' }.length == 3) {
                val color: String
                if (code.text.toString().length == 3) {
                    val s1 = code.text.toString()
                    color = "#" + s1.substring(0, 1) + s1.substring(0, 1) + s1.substring(1, 2) + s1.substring(1, 2) + s1.substring(2, 3) + s1.substring(2, 3)
                } else {
                    color = "#" + code.text.toString()
                }
                val realm = Realm.getInstance(mContext)
                realm.beginTransaction()
                val cData = ColorData()
                cData.id = realm.where(ColorData::class.java).findAll().size
                cData.name = name.text.toString()
                cData.color = color.toUpperCase()
                cData.colorRes = Color.parseColor(color)
                cData.colorSetId = intent.getIntExtra("id", 0)
                realm.copyToRealm(cData)
                cList.add(cData)
                realm.commitTransaction()
                colorListAdapter?.notifyDataSetChanged()
                detailRecycler.smoothScrollBy(0, detailRecycler.height)
                appBarLayout.setExpanded(false, true)
                dialog?.dismiss()
            } else {
                Toast.makeText(mContext, "올바르지 않은 색상값입니다.", Toast.LENGTH_SHORT).show()
            }
        }
        view.findViewById(R.id.dialog_cancel).setOnClickListener { dialog?.dismiss() }
        name.setSelection(name.text.length)
        code.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                if (s.toString().length == 6) {
                    bg.setBackgroundColor(Color.parseColor("#" + s.toString()))
                    if (ColorUtil.isLight(s.toString()) == 1) {
                        name.setTextColor(ContextCompat.getColor(mContext, R.color.text_grey))
                        code.setTextColor(ContextCompat.getColor(mContext, R.color.text_grey))
                    } else if (ColorUtil.isLight(s.toString()) == 0) {
                        name.setTextColor(Color.WHITE)
                        code.setTextColor(Color.WHITE)
                    }
                } else if (s.toString().length == 3) {
                    val s1 = s.toString()
                    val color = "#" + s1.substring(0, 1) + s1.substring(0, 1) + s1.substring(1, 2) + s1.substring(1, 2) + s1.substring(2, 3) + s1.substring(2, 3)
                    bg.setBackgroundColor(Color.parseColor(color))
                    if (ColorUtil.isLight(color.substring(1)) == 1) {
                        name.setTextColor(ContextCompat.getColor(mContext, R.color.text_grey))
                        code.setTextColor(ContextCompat.getColor(mContext, R.color.text_grey))
                    } else if (ColorUtil.isLight(color.substring(1)) == 0) {
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

    override fun onResume() {
        super.onResume()
        if(colorListAdapter != null){
            cList.clear()
            initData()
            (colorListAdapter as ColorListAdapter).notifyDataSetChanged()
        }
    }
}
