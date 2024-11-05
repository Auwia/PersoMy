# Mantieni i nomi delle classi che estendono Application
-keep class * extends android.app.Application

# Mantieni i nomi delle Activity, Fragment e Service
-keep class * extends android.app.Activity
-keep class * extends android.app.Fragment
-keep class * extends android.app.Service

# Mantieni i nomi delle classi e metodi annotati con @Keep
-keep @androidx.annotation.Keep class *
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Mantieni i nomi delle classi, metodi e variabili pubbliche in pacchetti specifici (ad es. Retrofit, Gson)
-keep class com.app.persomy.** { *; }
