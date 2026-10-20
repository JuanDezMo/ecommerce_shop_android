# Reglas de R8 para release. Cada regla dice qué protege; si una desaparece, hay que volver a ejecutar el APK de release.

# kotlinx.serialization lee los DTO por reflexión: sin estas reglas R8 renombra o quita los serializadores
# y las respuestas del backend dejan de leerse (SerializationException en tiempo de ejecución)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class co.com.learningswift.shop.**$$serializer { *; }
-keepclassmembers class co.com.learningswift.shop.** {
    *** Companion;
}
-keepclasseswithmembers class co.com.learningswift.shop.** {
    kotlinx.serialization.KSerializer serializer(...);
}
