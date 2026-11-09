package com.example.myapp
import kotlin.math.sqrt
import kotlin.math.pow

#include <jni.h>
#include <cmath>
#include <vector>

// Structure សម្រាប់រក្សាទុកកូអរដោនេបាល់ (X, Y)
struct Ball {
    float x;
    float y;
    float radius;
};

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_example_poolmod_DirectLineCanvasView_calculateRealTimePhysics(
        JNIEnv* env,
        jobject /* this */,
        jfloat cueX, jfloat cueY,       // កូអរដោនេបាល់ស
        jfloat targetX, jfloat targetY, // កូអរដោនេបាល់គោលដៅ
        jfloat angleRad) {              // មុំតម្រង់ (Angle in Radians)

    // ១. គណនាចំណុចប៉ះទង្គិចរវាងបាល់ស និងបាល់គោលដៅ (Impact Point)
    float ballRadius = 15.0f; // កាំគ្រាប់បាល់
    float distance = std::sqrt(std::pow(targetX - cueX, 2) + std::pow(targetY - cueY, 2));
    
    // Vector ទិសដៅបាញ់
    float dirX = (targetX - cueX) / distance;
    float dirY = (targetY - cueY) / distance;

    // ចំណុចដែលបាល់សនឹងទៅប៉ះបាល់គោលដៅ
    float hitX = targetX - (dirX * ballRadius * 2.0f);
    float hitY = targetY - (dirY * ballRadius * 2.0f);

    // ២. គណនាមុំสะទោះបាល់គោលដៅ (Target Ball Path Vector)
    float targetPathX = targetX + (dirX * 400.0f);
    float targetPathY = targetY + (dirY * 400.0f);

    // ៣. គណនាមុំ 90 ថ្នាក់ សម្រាប់ចលនាបាល់សបន្ទាប់ពីប៉ះ (Cue Ball Tangent Line)
    float cueTangentX = hitX + (-dirY * 200.0f);
    float cueTangentY = hitY + (dirX * 200.0f);

    jfloat coords[10] = {
        cueX, cueY,           // 0, 1: បាល់ស (Start)
        hitX, hitY,           // 2, 3: ចំណុចប៉ះ (Hit Center)
        targetPathX, targetPathY, // 4, 5: ខ្សែបាល់គោលដៅ (Orange Line)
        cueTangentX, cueTangentY, // 6, 7: ខ្សែបាល់សสะទោះ (Purple Line)
        targetX, targetY      // 8, 9: រង្វង់បាល់គោលដៅ
    };

    jfloatArray result = env->NewFloatArray(10);
    env->SetFloatArrayRegion(result, 0, 10, coords);
    return result;
}