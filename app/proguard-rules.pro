-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# Kotlin
#https://stackoverflow.com/questions/33547643/how-to-use-kotlin-with-proguard
#https://medium.com/@AthorNZ/kotlin-metadata-jackson-and-proguard-f64f51e5ed32
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Android X
-dontwarn androidx.**
-dontwarn com.google.android.material.**
-keep interface androidx.* { *; }
-keep class androidx.** { *; }
-keep class com.google.android.material.** { *; }

# RxJava, RxAndroid (https://gist.github.com/kosiara/487868792fbd3214f9c9)
-keep class rx.schedulers.Schedulers {
    public static <methods>;
}
-keep class rx.schedulers.ImmediateScheduler {
    public <methods>;
}
-keep class rx.schedulers.TestScheduler {
    public <methods>;
}
-keep class rx.schedulers.Schedulers {
    public static ** test();
}
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    long producerNode;
    long consumerNode;
}
-dontwarn sun.misc.Unsafe
-dontwarn org.reactivestreams.FlowAdapters
-dontwarn org.reactivestreams.**
-dontwarn java.util.concurrent.flow.**
-dontwarn java.util.concurrent.**

### Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }
-keep class com.google.gson.mckimquyen.android.model.** { *; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# LeakCanary
-keep class org.eclipse.mat.** { *; }
-keep class com.squareup.leakcanary.** { *; }

# Room
-dontwarn androidx.room.paging.**
-keep class * extends androidx.room.RoomDatabase

# VCard Parser
-dontwarn ezvcard.**
-keep,includedescriptorclasses class ezvcard.** { *; }

# Suppress warnings
-dontwarn com.sun.org.apache.xml.internal.utils.PrefixResolver
-dontwarn java.beans.BeanInfo
-dontwarn java.beans.IndexedPropertyDescriptor
-dontwarn java.beans.IntrospectionException
-dontwarn java.beans.Introspector
-dontwarn java.beans.MethodDescriptor
-dontwarn java.beans.PropertyDescriptor
-dontwarn java.rmi.Remote
-dontwarn java.rmi.RemoteException
-dontwarn java.rmi.server.RemoteObject
-dontwarn java.rmi.server.UnicastRemoteObject
-dontwarn javax.swing.tree.TreeNode
-dontwarn org.apache.xml.utils.PrefixResolver
-dontwarn org.jaxen.BaseXPath
-dontwarn org.jaxen.FunctionContext
-dontwarn org.jaxen.JaxenException
-dontwarn org.jaxen.NamespaceContext
-dontwarn org.jaxen.Navigator
-dontwarn org.jaxen.VariableContext
-dontwarn org.jaxen.XPathFunctionContext
-dontwarn org.jaxen.dom.DocumentNavigator
-dontwarn org.python.core.Py
-dontwarn org.python.core.PyDictionary
-dontwarn org.python.core.PyException
-dontwarn org.python.core.PyFloat
-dontwarn org.python.core.PyInteger
-dontwarn org.python.core.PyLong
-dontwarn org.python.core.PyNone
-dontwarn org.python.core.PyObject
-dontwarn org.python.core.PySequence
-dontwarn org.python.core.PyString
-dontwarn org.python.core.PyStringMap
-dontwarn org.python.core.PySystemState
-dontwarn org.zeroturnaround.javarebel.ClassEventListener
-dontwarn org.zeroturnaround.javarebel.Reloader
-dontwarn org.zeroturnaround.javarebel.ReloaderFactory

# Keep classes to prevent removal
-keep class com.sun.org.apache.xml.internal.utils.PrefixResolver { *; }
-keep class java.beans.BeanInfo { *; }
-keep class java.beans.IndexedPropertyDescriptor { *; }
-keep class java.beans.IntrospectionException { *; }
-keep class java.beans.Introspector { *; }
-keep class java.beans.MethodDescriptor { *; }
-keep class java.beans.PropertyDescriptor { *; }
-keep class java.rmi.Remote { *; }
-keep class java.rmi.RemoteException { *; }
-keep class java.rmi.server.RemoteObject { *; }
-keep class java.rmi.server.UnicastRemoteObject { *; }
-keep class javax.swing.tree.TreeNode { *; }
-keep class org.apache.xml.utils.PrefixResolver { *; }
-keep class org.jaxen.BaseXPath { *; }
-keep class org.jaxen.FunctionContext { *; }
-keep class org.jaxen.JaxenException { *; }
-keep class org.jaxen.NamespaceContext { *; }
-keep class org.jaxen.Navigator { *; }
-keep class org.jaxen.VariableContext { *; }
-keep class org.jaxen.XPathFunctionContext { *; }
-keep class org.jaxen.dom.DocumentNavigator { *; }
-keep class org.python.core.Py { *; }
-keep class org.python.core.PyDictionary { *; }
-keep class org.python.core.PyException { *; }
-keep class org.python.core.PyFloat { *; }
-keep class org.python.core.PyInteger { *; }
-keep class org.python.core.PyLong { *; }
-keep class org.python.core.PyNone { *; }
-keep class org.python.core.PyObject { *; }
-keep class org.python.core.PySequence { *; }
-keep class org.python.core.PyString { *; }
-keep class org.python.core.PyStringMap { *; }
-keep class org.python.core.PySystemState { *; }
-keep class org.zeroturnaround.javarebel.ClassEventListener { *; }
-keep class org.zeroturnaround.javarebel.Reloader { *; }
-keep class org.zeroturnaround.javarebel.ReloaderFactory { *; }
