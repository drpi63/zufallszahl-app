package de.drpi.zufallszahl

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import de.drpi.zufallszahl.databinding.ActivityMainBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var ui: ActivityMainBinding

    private val history = mutableListOf<Long>()
    private val prefs by lazy { getSharedPreferences(PREFS, Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)

        restoreState()
        renderHistory()

        ui.genBtn.setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            hideKeyboard()
            generate()
        }

        ui.clearBtn.setOnClickListener {
            history.clear()
            persistHistory()
            renderHistory()
        }

        ui.maxInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                generate()
                true
            } else {
                false
            }
        }
    }

    override fun onPause() {
        super.onPause()
        prefs.edit()
            .putString(KEY_MIN, ui.minInput.text.toString())
            .putString(KEY_MAX, ui.maxInput.text.toString())
            .apply()
    }

    // --- Kernlogik -------------------------------------------------------

    private fun generate() {
        val min = ui.minInput.text.toString().trim().toLongOrNull()
        val max = ui.maxInput.text.toString().trim().toLongOrNull()

        if (min == null || max == null) {
            showError(getString(R.string.err_invalid))
            return
        }
        if (min >= max) {
            showError(getString(R.string.err_order))
            return
        }
        showError(null)

        val number = Random.nextLong(min, max + 1)

        animateResult(number, min, max)

        history.add(0, number)
        while (history.size > MAX_HISTORY) history.removeAt(history.lastIndex)
        persistHistory()
        renderHistory()
    }

    private fun animateResult(number: Long, min: Long, max: Long) {
        ui.result.animate()
            .alpha(0f)
            .scaleX(0.92f)
            .scaleY(0.92f)
            .setDuration(110)
            .withEndAction {
                ui.result.text = number.toString()
                ui.resultRange.text = getString(R.string.range_format, min, max)
                ui.result.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(130)
                    .start()
            }
            .start()
    }

    private fun showError(message: String?) {
        ui.errorMsg.text = message ?: ""
    }

    // --- Verlauf ---------------------------------------------------------

    private fun renderHistory() {
        ui.historyRow.removeAllViews()

        if (history.isEmpty()) {
            ui.historyEmpty.visibility = View.VISIBLE
            ui.historyScroll.visibility = View.GONE
            return
        }

        ui.historyEmpty.visibility = View.GONE
        ui.historyScroll.visibility = View.VISIBLE

        history.forEachIndexed { index, value ->
            ui.historyRow.addView(buildChip(value, newest = index == 0))
        }
        ui.historyScroll.post { ui.historyScroll.scrollTo(0, 0) }
    }

    private fun buildChip(value: Long, newest: Boolean): TextView {
        val chip = TextView(this)
        chip.text = value.toString()
        chip.textSize = 13f
        chip.typeface = android.graphics.Typeface.MONOSPACE
        chip.gravity = Gravity.CENTER
        chip.setBackgroundResource(
            if (newest) R.drawable.bg_history_chip_newest else R.drawable.bg_history_chip
        )
        chip.setTextColor(
            ContextCompat.getColor(this, if (newest) R.color.badge_fg else R.color.muted)
        )
        chip.setPadding(dp(13), dp(6), dp(13), dp(6))

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.marginEnd = dp(8)
        chip.layoutParams = params

        return chip
    }

    // --- Persistenz ------------------------------------------------------

    private fun restoreState() {
        ui.minInput.setText(prefs.getString(KEY_MIN, "1"))
        ui.maxInput.setText(prefs.getString(KEY_MAX, "100"))

        val stored = prefs.getString(KEY_HISTORY, "").orEmpty()
        if (stored.isNotBlank()) {
            stored.split(",").mapNotNullTo(history) { it.trim().toLongOrNull() }
        }
    }

    private fun persistHistory() {
        prefs.edit()
            .putString(KEY_HISTORY, history.joinToString(","))
            .apply()
    }

    // --- Hilfsfunktionen -------------------------------------------------

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val focused = currentFocus ?: ui.root
        imm.hideSoftInputFromWindow(focused.windowToken, 0)
        focused.clearFocus()
    }

    private companion object {
        const val PREFS = "zufallszahl"
        const val KEY_HISTORY = "history"
        const val KEY_MIN = "min"
        const val KEY_MAX = "max"
        const val MAX_HISTORY = 12
    }
}
