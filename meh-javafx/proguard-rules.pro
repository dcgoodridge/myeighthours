-injars       build/libs/meh.jar 
-outjars      build/libs/meh_proguard.jar 
-libraryjars  <java.home>/lib/rt.jar 
-libraryjars "<java.home>/lib/javaws.jar"
-libraryjars  <java.home>/lib/ext/jfxrt.jar
-printmapping build/libs/meh_proguard.map 


#Proguard no sincroniza las referencias en los FXML con los JAVA..
-keepclasseswithmembers class * { @javafx.fxml.FXML <fields>; }
-keepclasseswithmembers class * { @javafx.fxml.FXML <methods>; }

#https://stackoverflow.com/questions/29307262/proguard-breaks-javafx-application
#-classobfuscationdictionary obfuscationClassNames.txt

-keep public class myeighthours.Main { 
      public static void main(java.lang.String[]); 
}


# algunas de stackoverflow
-dontusemixedcaseclassnames
-dontshrink
-dontoptimize
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
#-flattenpackagehierarchy
-repackageclasses 'p'
-allowaccessmodification
-adaptresourcefilecontents **.fxml,**.properties,META-INF/MANIFEST.MF,images/*.jar,publicCerts.store,production.version
-keepattributes javafx.fxml.FXML,Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,!LocalVariableTable,!LocalVariableTypeTable,*Annotation*,Synthetic,EnclosingMethod
#-keepattributes !LocalVariableTable,!LocalVariableTypeTable
-keepclassmembers class * {
    @javafx.fxml.FXML *;
}
-keepclassmembernames public class com.javafx.main.Main {
    public static void main(java.lang.String[]);
}
-keepclasseswithmembers public class com.javafx.main.Main, com.product.main.EntryFX, net.license.LicenseEntryPoint {
    public *; public static *;
}