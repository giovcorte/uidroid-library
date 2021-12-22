# uidroid-library
View binding made stateful and customizable for Android applications

To import and use this library in yours projects, add the following statements in your app module gradle:

    implementation 'com.github.giovcorte.uidroid-library:uidroid:1.7'
    annotationProcessor('com.github.giovcorte.uidroid-library:processor:1.7')
    implementation 'com.github.giovcorte.uidroid-library:annotation:1.7'
    implementation 'com.github.giovcorte.uidroid-library:processor:1.7'
    
and this in your project gradle file (under repositories):

    maven { url 'https://jitpack.io' }
    
For now the documentation is provided as detailed comments in the source code, but a more user friendly docu is coming! This library doesn't use any external library, so it's light weight and dependencies free.
