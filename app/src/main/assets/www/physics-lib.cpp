#include <jni.h>
#include <cmath>

struct Ball {
    float x;
    float y;
    float radius;
};

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_example_poolmod_DynamicCanvasView_calculateDynamicLines(
        JNIEnv* env,
        jobject /* this */,
        jfloat width,
        jfloat height) {

    // គណនាកូអរដោនេបន្ទាត់រត់ Real-Time តាមទំហំអេក្រង់ទូរស័ព្ទ
    float cueX = width * 0.35f;
    float cueY = height * 0.52f;
    float hitX = width * 0.73f;
    float hitY = height * 0.52f;

    float targetEndX = width * 0.88f;
    float targetEndY = height * 0.32f;

    float cushX1 = width * 0.88f;
    float cushY1 = height * 0.78f;
    float cushX2 = width * 0.62f;
    float cushY2 = height * 0.85f;

    jfloat coords[10] = {
        cueX, cueY,           // 0, 1: បាល់ស (Cue Ball Start)
        hitX, hitY,           // 2, 3: ចំណុចប៉ះ (Collision Point)
        targetEndX, targetEndY, // 4, 5: ខ្សែបាល់គោលដៅ (Orange Line)
        cushX1, cushY1,       // 6, 7: ខ្សែสะទោះខ្សែកាប (Cushion Reflection)
        hitX, hitY            // 8, 9: កណ្តាលរង្វង់ចំណុចប៉ះ
    };

    jfloatArray result = env->NewFloatArray(10);
    env->SetFloatArrayRegion(result, 0, 10, coords);
    return result;
}
```[cite: 33, 40]

---

### ២. កូដក្នុង `PoolModService.kt` (Kotlin UI & Canvas Overlay)

សូមធានាថាឈ្មោះ Function `calculateDynamicLines` ក្នុង Kotlin ត្រូវគ្នាជាមួយ C++៖

```kotlin
class DynamicCanvasView(context: Context) : View(context) {
    private var screenWidth = 1920f
    private var screenHeight = 1080f

    private val mainAimPaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 7f
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private val reflectionPaint = Paint().apply {
        color = Color.rgb(255, 140, 0) // ពណ៌ទឹកក្រូច
        strokeWidth = 6f
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private val cushionPaint = Paint().apply {
        color = Color.rgb(186, 85, 211) // ពណ៌ស្វាយ
        strokeWidth = 5f
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private val targetHitPaint = Paint().apply {
        color = Color.YELLOW // រង្វង់ពណ៌លឿងត្រង់ចំណុចប៉ះ
        strokeWidth = 5f
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    init {
        try {
            System.loadLibrary("pool-physics-engine")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private external fun calculateDynamicLines(w: Float, h: Float): FloatArray

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = if (width > 0) width.toFloat() else screenWidth
        val h = if (height > 0) height.toFloat() else screenHeight

        if (w <= 0 || h <= 0) return

        try {
            val coords = calculateDynamicLines(w, h)
            if (coords.size >= 10) {
                // ១. បន្ទាត់ស (Main Cue Aim Line)
                canvas.drawLine(coords[0], coords[1], coords[2], coords[3], mainAimPaint)
                // ២. រង្វង់ចំណុចប៉ះ (Collision Circle)
                canvas.drawCircle(coords[2], coords[3], h * 0.028f, targetHitPaint)
                // ៣. បន្ទាត់ទឹកក្រូច (Target Ball Reflection)
                canvas.drawLine(coords[2], coords[3], coords[4], coords[5], reflectionPaint)
                // ៤. បន្ទាត់ស្វាយ (Cushion Bounce Reflection)
                canvas.drawLine(coords[2], coords[3], coords[6], coords[7], cushionPaint)

                invalidate()
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        invalidate()
    }
}
```[cite: 34, 41]

---

### 📝 ជំហានបន្ទាប់៖
1. ពិនិត្យមើលក្នុង AppForge IDE របស់អ្នកថាបានសរសេរឈ្មោះ function ទាំងសងខាង (`calculateDynamicLines`) ត្រូវគ្នាឬនៅ។
2. ចុច **Build APK** ឡើងវិញ បន្ទាប់មក Install ចូលទូរស័ព្ទដើម្បីធ្វើតេស្តបង្ហាញបន្ទាត់នៅលើហ្គេម 8 Ball Pool។