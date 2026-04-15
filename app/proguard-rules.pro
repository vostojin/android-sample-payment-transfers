# Add project specific ProGuard rules here.

# Keep domain models
-keep class com.fintech.payment.domain.model.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# Hilt
-keepclasseswithmembernames class * {
    @dagger.hilt.* <methods>;
}
