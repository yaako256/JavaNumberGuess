# その他追記

phase4でapk出力が完了し、環境構築終了かと思ったが、クラッシュする。
次のことをやらなきゃいけないらしい。


## app/src/main/res/values/shemes.xml
ここで、テーマ設定をしなければいけない
```xml
<resources>

    <style name="Theme.JavaNumberGuess"
        parent="Theme.AppCompat.Light.NoActionBar" />

</resources>
```

## app/src/main/AndroidManifest.xml
テーマの反映をする
<application>に追記をする
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">


    <application
        android:label="JavaNumberGuess"
        android:allowBackup="true"
         android:theme="@style/Theme.JavaNumberGuess">

        <activity
            android:name=".MainActivity"
            android:exported="true">

            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>

        </activity>

    </application>

</manifest>
```

これでクラッシュせず、無のUIが表示されたアプリができた。