import os
import re

LAYOUT_DIR = r"d:\4thYEAR\EAD\EAD_Assignment\EAD_Project\mobile\SmartSolarMobile\app\src\main\res\layout"
JAVA_DIR = r"d:\4thYEAR\EAD\EAD_Assignment\EAD_Project\mobile\SmartSolarMobile\app\src\main\java"

button_pattern = re.compile(r'<com\.google\.android\.material\.button\.MaterialButton\s+android:id="@+id/(button\w*Back)"\s+style="@style/Widget\.Material3\.Button\.TextButton"\s+android:layout_width="wrap_content"\s+android:layout_height="wrap_content"\s+android:text="[^"]+"\s+android:textAllCaps="false"\s+android:textColor="[^"]+"\s*/>')

for root, _, files in os.walk(LAYOUT_DIR):
    for filename in files:
        if filename.endswith(".xml"):
            filepath = os.path.join(root, filename)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            new_content = button_pattern.sub(
                lambda m: '<com.google.android.material.appbar.MaterialToolbar\n'
                          f'                    android:id="@+id/{m.group(1)}"\n'
                          '                    android:layout_width="match_parent"\n'
                          '                    android:layout_height="wrap_content"\n'
                          '                    android:layout_marginStart="-12dp"\n'
                          '                    app:navigationIcon="?attr/homeAsUpIndicator"\n'
                          '                    app:navigationIconTint="@android:color/white"\n'
                          '                    android:background="@android:color/transparent" />',
                content
            )
            
            if new_content != content:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(new_content)
                print(f"Updated {filename}")

# Fix Java code
java_pattern = re.compile(r'findViewById\(\s*R\.id\.(button\w*Back)\s*\)\s*\.setOnClickListener\(\s*v\s*->\s*finish\(\)\s*\);')
java_pattern2 = re.compile(r'findViewById\(\s*R\.id\.(button\w*Back)\s*\)\s*\.setOnClickListener\(\s*this::[a-zA-Z_]+\s*\);')

for root, _, files in os.walk(JAVA_DIR):
    for filename in files:
        if filename.endswith(".java"):
            filepath = os.path.join(root, filename)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            new_content = java_pattern.sub(
                lambda m: f'((com.google.android.material.appbar.MaterialToolbar) findViewById(R.id.{m.group(1)})).setNavigationOnClickListener(v -> finish());',
                content
            )
            
            if new_content != content:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(new_content)
                print(f"Updated Java {filename}")
