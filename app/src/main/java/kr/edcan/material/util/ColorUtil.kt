package kr.edcan.material.util

/**
 * Created by LNTCS on 2016-03-28.
 */
object ColorUtil {
    fun isLight(s: String): Int {
        try {
            if (Integer.parseInt(s.substring(0, 2), 16) + Integer.parseInt(s.substring(2, 4), 16) + Integer.parseInt(s.substring(4, 6), 16) >= 510)
                return 1
            else
                return 0
        } catch (e: StringIndexOutOfBoundsException) {
            return -1
        }
    }

    fun hexToRGB(s: String): String{
        return "R${Integer.parseInt(s.substring(0, 2), 16)} G${Integer.parseInt(s.substring(2, 4), 16)} B${Integer.parseInt(s.substring(4, 6), 16)}"
    }
}
